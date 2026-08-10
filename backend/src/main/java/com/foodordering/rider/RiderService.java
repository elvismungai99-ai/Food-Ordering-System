package com.foodordering.rider;

import com.foodordering.User.entity.Role;
import com.foodordering.User.entity.User;
import com.foodordering.User.repository.UserRepository;
import com.foodordering.auth.AuthResponse;
import com.foodordering.common.exception.BusinessRuleException;
import com.foodordering.common.exception.ConflictException;
import com.foodordering.common.exception.ResourceNotFoundException;
import com.foodordering.order.Order;
import com.foodordering.order.OrderRepository;
import com.foodordering.order.OrderStatus;
import com.foodordering.restaurant.Restaurant;
import com.foodordering.restaurant.RestaurantRepository;
import com.foodordering.rider.dto.CreateDeliveryRequest;
import com.foodordering.rider.dto.AutoDeliveryRequest;
import com.foodordering.rider.dto.DeliveryRequestDto;
import com.foodordering.rider.dto.RejectDeliveryRequest;
import com.foodordering.rider.dto.RiderAvailabilityRequest;
import com.foodordering.rider.dto.RiderDashboardDto;
import com.foodordering.rider.dto.RiderDto;
import com.foodordering.rider.dto.RiderLocationRequest;
import com.foodordering.rider.dto.RiderRegistrationRequest;
import com.foodordering.security.JwtUtil;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class RiderService {

    private static final BigDecimal BASE_PAYOUT =
            BigDecimal.valueOf(120);
    private static final BigDecimal PAYOUT_PER_KM =
            BigDecimal.valueOf(35);
    private static final BigDecimal REJECTION_PENALTY =
            BigDecimal.valueOf(2);
    private static final long LOCATION_FRESHNESS_MINUTES =
            10;

    private final UserRepository userRepository;
    private final RiderRepository riderRepository;
    private final DeliveryRequestRepository deliveryRequestRepository;
    private final RestaurantRepository restaurantRepository;
    private final OrderRepository orderRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public RiderService(
            UserRepository userRepository,
            RiderRepository riderRepository,
            DeliveryRequestRepository deliveryRequestRepository,
            RestaurantRepository restaurantRepository,
            OrderRepository orderRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil
    ) {
        this.userRepository = userRepository;
        this.riderRepository = riderRepository;
        this.deliveryRequestRepository = deliveryRequestRepository;
        this.restaurantRepository = restaurantRepository;
        this.orderRepository = orderRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public AuthResponse registerRider(
            RiderRegistrationRequest request
    ) {

        String email =
                request.getEmail()
                        .trim()
                        .toLowerCase();

        if (userRepository.existsByEmail(email)
                || riderRepository.existsByEmail(email)) {
            throw new ConflictException(
                    "An account with this email already exists"
            );
        }

        String fullName =
                request.getFullName().trim();

        User user = new User();
        user.setFullName(fullName);
        user.setFirstName(extractFirstName(fullName));
        user.setLastName(extractLastName(fullName));
        user.setEmail(email);
        user.setPasswordHash(
                passwordEncoder.encode(
                        request.getPassword()
                )
        );
        user.setRole(Role.RIDER);
        user.setActive(true);

        User savedUser =
                userRepository.save(user);

        Rider rider = new Rider();
        rider.setUserId(savedUser.getId());
        rider.setFullName(fullName);
        rider.setPhoneNumber(
                normalizePhoneNumber(
                        request.getPhoneNumber()
                )
        );
        rider.setEmail(email);
        rider.setVehicleType(request.getVehicleType());
        rider.setLicencePlate(
                request.getLicencePlate()
                        .trim()
                        .toUpperCase()
        );
        rider.setStatus(RiderStatus.PENDING_APPROVAL);
        rider.setOperationalStatus(
                RiderOperationalStatus.CLOSED
        );
        rider.setOnline(false);

        riderRepository.save(rider);

        String token =
                jwtUtil.generateToken(
                        savedUser.getId(),
                        savedUser.getEmail(),
                        savedUser.getRole()
                );

        return new AuthResponse(
                token,
                savedUser.getId(),
                Role.RIDER,
                extractFirstName(fullName)
        );
    }

    @Transactional(readOnly = true)
    public RiderDashboardDto getDashboard(
            UUID riderUserId
    ) {

        Rider rider =
                getRiderByUserId(riderUserId);

        List<DeliveryRequest> requests =
                deliveryRequestRepository
                        .findByRiderIdOrderByRequestedAtDesc(
                                rider.getId()
                        );

        RiderDashboardDto dashboard =
                new RiderDashboardDto();

        dashboard.setRider(
                new RiderDto(rider)
        );
        dashboard.setDeliveryRequests(
                requests.stream()
                        .map(DeliveryRequestDto::new)
                        .toList()
        );
        dashboard.setTotalEarnings(
                sumPayouts(
                        requests,
                        DeliveryRequestStatus.DELIVERED
                )
        );
        dashboard.setPendingPayout(
                requests.stream()
                        .filter(request ->
                                request.getStatus()
                                != DeliveryRequestStatus.REJECTED
                                && request.getStatus()
                                != DeliveryRequestStatus.CANCELLED
                                && request.getStatus()
                                != DeliveryRequestStatus.DELIVERED
                        )
                        .map(DeliveryRequest::getEstimatedPayout)
                        .filter(amount -> amount != null)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        )
        );
        dashboard.setCompletedDeliveries(
                deliveryRequestRepository
                        .countByRiderIdAndStatus(
                                rider.getId(),
                                DeliveryRequestStatus.DELIVERED
                        )
        );
        dashboard.setRejectedRequests(
                rider.getTotalRejections()
        );

        return dashboard;
    }

    @Transactional
    public RiderDto updateAvailability(
            UUID riderUserId,
            RiderAvailabilityRequest request
    ) {

        Rider rider =
                getRiderByUserId(riderUserId);

        rider.setOperationalStatus(
                request.getOperationalStatus()
        );
        rider.setOnline(
                request.isOnline()
                && request.getOperationalStatus()
                == RiderOperationalStatus.OPEN
                && rider.getStatus()
                == RiderStatus.APPROVED
        );

        return new RiderDto(
                riderRepository.save(rider)
        );
    }

    @Transactional
    public RiderDto updateLocation(
            UUID riderUserId,
            RiderLocationRequest request
    ) {

        Rider rider =
                getRiderByUserId(riderUserId);

        rider.setCurrentLatitude(
                request.getLatitude()
        );
        rider.setCurrentLongitude(
                request.getLongitude()
        );
        rider.setLastLocationUpdatedAt(
                LocalDateTime.now()
        );

        return new RiderDto(
                riderRepository.save(rider)
        );
    }

    @Transactional(readOnly = true)
    public List<RiderDto> getAvailableRiders() {

        return riderRepository
                .findByStatusAndOperationalStatusAndOnline(
                        RiderStatus.APPROVED,
                        RiderOperationalStatus.OPEN,
                        true
                )
                .stream()
                .map(RiderDto::new)
                .toList();
    }

    @Transactional
    public DeliveryRequestDto createDeliveryRequest(
            UUID ownerId,
            CreateDeliveryRequest request
    ) {

        Restaurant restaurant =
                restaurantRepository
                        .findByOwnerId(ownerId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Restaurant not found for this owner"
                                )
                        );

        Order order =
                orderRepository
                        .findWithItemsById(
                                request.getOrderId()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Order not found"
                                )
                        );

        if (!restaurant.getId().equals(order.getRestaurantId())) {
            throw new BusinessRuleException(
                    "You can request riders only for your restaurant orders"
            );
        }

        if (hasActiveDeliveryRequest(order.getId())) {
            throw new ConflictException(
                    "This order already has a delivery request"
            );
        }

        Rider rider =
                riderRepository
                        .findById(request.getRiderId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Rider not found"
                                )
                        );

        if (
                rider.getStatus() != RiderStatus.APPROVED
                || rider.getOperationalStatus()
                != RiderOperationalStatus.OPEN
                || !rider.isOnline()
        ) {
            throw new BusinessRuleException(
                    "This rider is not available right now"
            );
        }

        BigDecimal distanceKm =
                calculateDistanceKm(
                        request.getRestaurantLatitude(),
                        request.getRestaurantLongitude(),
                        order.getDeliveryLatitude(),
                        order.getDeliveryLongitude()
                );

        DeliveryRequest deliveryRequest =
                new DeliveryRequest();

        deliveryRequest.setOrderId(order.getId());
        deliveryRequest.setRiderId(rider.getId());
        deliveryRequest.setRestaurantId(restaurant.getId());
        deliveryRequest.setRestaurantName(restaurant.getName());
        deliveryRequest.setRestaurantAddress(restaurant.getAddress());
        deliveryRequest.setRestaurantLatitude(
                request.getRestaurantLatitude()
        );
        deliveryRequest.setRestaurantLongitude(
                request.getRestaurantLongitude()
        );
        deliveryRequest.setCustomerAddress(
                order.getDeliveryAddress()
        );
        deliveryRequest.setCustomerLatitude(
                order.getDeliveryLatitude()
        );
        deliveryRequest.setCustomerLongitude(
                order.getDeliveryLongitude()
        );
        deliveryRequest.setDistanceKm(distanceKm);
        deliveryRequest.setEstimatedPayout(
                request.getEstimatedPayout() != null
                        ? request.getEstimatedPayout()
                        : estimatePayout(distanceKm)
        );
        deliveryRequest.setStatus(
                DeliveryRequestStatus.REQUESTED
        );

        return new DeliveryRequestDto(
                deliveryRequestRepository.save(
                        deliveryRequest
                )
        );
    }

    @Transactional
    public DeliveryRequestDto createAutomaticDeliveryRequest(
            UUID ownerId,
            AutoDeliveryRequest request
    ) {

        Restaurant restaurant =
                restaurantRepository
                        .findByOwnerId(ownerId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Restaurant not found for this owner"
                                )
                        );

        Order order =
                orderRepository
                        .findWithItemsById(
                                request.getOrderId()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Order not found"
                                )
                        );

        if (!restaurant.getId().equals(order.getRestaurantId())) {
            throw new BusinessRuleException(
                    "You can request riders only for your restaurant orders"
            );
        }

        if (hasActiveDeliveryRequest(order.getId())) {
            throw new ConflictException(
                    "This order already has a delivery request"
            );
        }

        if (
                request.getRestaurantLatitude() == null
                || request.getRestaurantLongitude() == null
        ) {
            throw new BusinessRuleException(
                    "Restaurant latitude and longitude are required for automatic rider matching"
            );
        }

        RiderMatch selectedMatch =
                riderRepository
                        .findByStatusAndOperationalStatusAndOnline(
                                RiderStatus.APPROVED,
                                RiderOperationalStatus.OPEN,
                                true
                        )
                        .stream()
                        .filter(rider ->
                                !hasActiveDeliveryRequestForRider(
                                        rider.getId()
                                )
                        )
                        .map(rider ->
                                buildRiderMatch(
                                        rider,
                                        request.getRestaurantLatitude(),
                                        request.getRestaurantLongitude()
                                )
                        )
                        .min(
                                Comparator
                                        .comparing(
                                                RiderMatch::hasCurrentLocation
                                        )
                                        .reversed()
                                        .thenComparing(
                                                RiderMatch::score
                                        )
                                        .thenComparing(
                                                RiderMatch::distanceKm,
                                                Comparator.nullsLast(
                                                        Comparator.naturalOrder()
                                                )
                                        )
                        )
                        .orElseThrow(() ->
                                new BusinessRuleException(
                                        "No online riders are available for automatic assignment"
                                )
                        );

        DeliveryRequest deliveryRequest =
                buildDeliveryRequest(
                        restaurant,
                        order,
                        selectedMatch.rider(),
                        request.getRestaurantLatitude(),
                        request.getRestaurantLongitude(),
                        selectedMatch.distanceKm(),
                        request.getEstimatedPayout()
                );

        deliveryRequest.setAssignmentScore(
                selectedMatch.score()
        );

        return new DeliveryRequestDto(
                deliveryRequestRepository.save(
                        deliveryRequest
                )
        );
    }

    @Transactional(readOnly = true)
    public List<DeliveryRequestDto> getRestaurantDeliveryRequests(
            UUID ownerId
    ) {

        Restaurant restaurant =
                restaurantRepository
                        .findByOwnerId(ownerId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Restaurant not found for this owner"
                                )
                        );

        return deliveryRequestRepository
                .findByRestaurantIdOrderByRequestedAtDesc(
                        restaurant.getId()
                )
                .stream()
                .map(DeliveryRequestDto::new)
                .toList();
    }

    @Transactional
    public DeliveryRequestDto acceptRequest(
            UUID riderUserId,
            UUID requestId
    ) {

        DeliveryRequest request =
                getOwnedDeliveryRequest(
                        riderUserId,
                        requestId
                );

        requireStatus(
                request,
                DeliveryRequestStatus.REQUESTED
        );

        request.setStatus(
                DeliveryRequestStatus.ACCEPTED
        );
        request.setRespondedAt(
                LocalDateTime.now()
        );

        return new DeliveryRequestDto(
                deliveryRequestRepository.save(request)
        );
    }

    @Transactional
    public DeliveryRequestDto rejectRequest(
            UUID riderUserId,
            UUID requestId,
            RejectDeliveryRequest rejectRequest
    ) {

        Rider rider =
                getRiderByUserId(riderUserId);

        DeliveryRequest request =
                deliveryRequestRepository
                        .findByIdAndRiderId(
                                requestId,
                                rider.getId()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Delivery request not found"
                                )
                        );

        requireStatus(
                request,
                DeliveryRequestStatus.REQUESTED
        );

        request.setStatus(
                DeliveryRequestStatus.REJECTED
        );
        request.setRejectionReason(
                rejectRequest
                        .getReason()
                        .trim()
        );
        request.setRespondedAt(
                LocalDateTime.now()
        );
        rider.incrementRejections();

        riderRepository.save(rider);

        return new DeliveryRequestDto(
                deliveryRequestRepository.save(request)
        );
    }

    @Transactional
    public DeliveryRequestDto markArrivedAtRestaurant(
            UUID riderUserId,
            UUID requestId
    ) {

        DeliveryRequest request =
                getOwnedDeliveryRequest(
                        riderUserId,
                        requestId
                );

        requireStatus(
                request,
                DeliveryRequestStatus.ACCEPTED
        );

        request.setStatus(
                DeliveryRequestStatus.ARRIVED_AT_RESTAURANT
        );
        request.setArrivedAtRestaurantAt(
                LocalDateTime.now()
        );

        return new DeliveryRequestDto(
                deliveryRequestRepository.save(request)
        );
    }

    @Transactional
    public DeliveryRequestDto confirmPickup(
            UUID riderUserId,
            UUID requestId
    ) {

        DeliveryRequest request =
                getOwnedDeliveryRequest(
                        riderUserId,
                        requestId
                );

        requireStatus(
                request,
                DeliveryRequestStatus.ARRIVED_AT_RESTAURANT
        );

        request.setStatus(
                DeliveryRequestStatus.PICKED_UP
        );
        request.setPickedUpAt(
                LocalDateTime.now()
        );

        return new DeliveryRequestDto(
                deliveryRequestRepository.save(request)
        );
    }

    @Transactional
    public DeliveryRequestDto confirmDelivery(
            UUID riderUserId,
            UUID requestId
    ) {

        DeliveryRequest request =
                getOwnedDeliveryRequest(
                        riderUserId,
                        requestId
                );

        requireStatus(
                request,
                DeliveryRequestStatus.PICKED_UP
        );

        request.setStatus(
                DeliveryRequestStatus.DELIVERED
        );
        request.setDeliveredAt(
                LocalDateTime.now()
        );

        Order order =
                orderRepository
                        .findById(request.getOrderId())
                        .orElse(null);

        if (order != null) {
            order.setStatus(OrderStatus.DELIVERED);
            orderRepository.save(order);
        }

        return new DeliveryRequestDto(
                deliveryRequestRepository.save(request)
        );
    }

    private Rider getRiderByUserId(
            UUID riderUserId
    ) {

        return riderRepository
                .findByUserId(riderUserId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Rider profile not found"
                        )
                );
    }

    private DeliveryRequest getOwnedDeliveryRequest(
            UUID riderUserId,
            UUID requestId
    ) {

        Rider rider =
                getRiderByUserId(riderUserId);

        return deliveryRequestRepository
                .findByIdAndRiderId(
                        requestId,
                        rider.getId()
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Delivery request not found"
                        )
                );
    }

    private void requireStatus(
            DeliveryRequest request,
            DeliveryRequestStatus status
    ) {

        if (request.getStatus() != status) {
            throw new BusinessRuleException(
                    "Delivery request must be "
                    + status
                    + " for this action"
            );
        }
    }

    private BigDecimal sumPayouts(
            List<DeliveryRequest> requests,
            DeliveryRequestStatus status
    ) {

        return requests.stream()
                .filter(request ->
                        request.getStatus() == status
                )
                .map(DeliveryRequest::getEstimatedPayout)
                .filter(amount -> amount != null)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );
    }

    private BigDecimal estimatePayout(
            BigDecimal distanceKm
    ) {

        if (distanceKm == null) {
            return BASE_PAYOUT;
        }

        return BASE_PAYOUT.add(
                distanceKm.multiply(PAYOUT_PER_KM)
        );
    }

    private DeliveryRequest buildDeliveryRequest(
            Restaurant restaurant,
            Order order,
            Rider rider,
            BigDecimal restaurantLatitude,
            BigDecimal restaurantLongitude,
            BigDecimal distanceKm,
            BigDecimal requestedPayout
    ) {

        DeliveryRequest deliveryRequest =
                new DeliveryRequest();

        deliveryRequest.setOrderId(order.getId());
        deliveryRequest.setRiderId(rider.getId());
        deliveryRequest.setRestaurantId(restaurant.getId());
        deliveryRequest.setRestaurantName(restaurant.getName());
        deliveryRequest.setRestaurantAddress(restaurant.getAddress());
        deliveryRequest.setRestaurantLatitude(restaurantLatitude);
        deliveryRequest.setRestaurantLongitude(restaurantLongitude);
        deliveryRequest.setCustomerAddress(order.getDeliveryAddress());
        deliveryRequest.setCustomerLatitude(order.getDeliveryLatitude());
        deliveryRequest.setCustomerLongitude(order.getDeliveryLongitude());
        deliveryRequest.setDistanceKm(distanceKm);
        deliveryRequest.setEstimatedPayout(
                requestedPayout != null
                        ? requestedPayout
                        : estimatePayout(distanceKm)
        );
        deliveryRequest.setStatus(
                DeliveryRequestStatus.REQUESTED
        );

        return deliveryRequest;
    }

    private boolean hasCurrentLocation(
            Rider rider
    ) {

        return rider.getCurrentLatitude() != null
                && rider.getCurrentLongitude() != null
                && rider.getLastLocationUpdatedAt() != null
                && rider.getLastLocationUpdatedAt()
                        .isAfter(
                                LocalDateTime
                                        .now()
                                        .minusMinutes(
                                                LOCATION_FRESHNESS_MINUTES
                                        )
                        );
    }

    private boolean hasActiveDeliveryRequest(
            UUID orderId
    ) {

        return deliveryRequestRepository
                .existsByOrderIdAndStatusNotIn(
                        orderId,
                        inactiveDeliveryStatuses()
                );
    }

    private boolean hasActiveDeliveryRequestForRider(
            UUID riderId
    ) {

        return deliveryRequestRepository
                .existsByRiderIdAndStatusNotIn(
                        riderId,
                        inactiveDeliveryStatuses()
                );
    }

    private List<DeliveryRequestStatus> inactiveDeliveryStatuses() {

        return List.of(
                DeliveryRequestStatus.REJECTED,
                DeliveryRequestStatus.CANCELLED,
                DeliveryRequestStatus.DELIVERED
        );
    }

    private RiderMatch buildRiderMatch(
            Rider rider,
            BigDecimal restaurantLatitude,
            BigDecimal restaurantLongitude
    ) {

        BigDecimal distanceKm =
                calculateDistanceKm(
                        rider.getCurrentLatitude(),
                        rider.getCurrentLongitude(),
                        restaurantLatitude,
                        restaurantLongitude
                );

        BigDecimal rejectionPenalty =
                BigDecimal
                        .valueOf(rider.getTotalRejections())
                        .multiply(REJECTION_PENALTY);

        BigDecimal score =
                distanceKm != null
                        ? distanceKm.add(rejectionPenalty)
                        : rejectionPenalty;

        return new RiderMatch(
                rider,
                distanceKm,
                score,
                hasCurrentLocation(rider)
        );
    }

    private BigDecimal calculateDistanceKm(
            BigDecimal fromLatitude,
            BigDecimal fromLongitude,
            BigDecimal toLatitude,
            BigDecimal toLongitude
    ) {

        if (
                fromLatitude == null
                || fromLongitude == null
                || toLatitude == null
                || toLongitude == null
        ) {
            return null;
        }

        double earthRadiusKm = 6371.0;
        double fromLat = Math.toRadians(fromLatitude.doubleValue());
        double toLat = Math.toRadians(toLatitude.doubleValue());
        double latDelta = Math.toRadians(
                toLatitude.doubleValue()
                - fromLatitude.doubleValue()
        );
        double lonDelta = Math.toRadians(
                toLongitude.doubleValue()
                - fromLongitude.doubleValue()
        );

        double a =
                Math.sin(latDelta / 2)
                * Math.sin(latDelta / 2)
                + Math.cos(fromLat)
                * Math.cos(toLat)
                * Math.sin(lonDelta / 2)
                * Math.sin(lonDelta / 2);

        double c =
                2 * Math.atan2(
                        Math.sqrt(a),
                        Math.sqrt(1 - a)
                );

        return BigDecimal
                .valueOf(earthRadiusKm * c)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private String normalizePhoneNumber(
            String phoneNumber
    ) {

        return phoneNumber
                .trim()
                .replaceAll("\\s+", "")
                .replace("-", "");
    }

    private String extractFirstName(
            String fullName
    ) {

        if (fullName == null || fullName.isBlank()) {
            return "";
        }

        return fullName.trim().split("\\s+")[0];
    }

    private String extractLastName(
            String fullName
    ) {

        if (fullName == null || fullName.isBlank()) {
            return "";
        }

        String[] parts = fullName.trim().split("\\s+");

        if (parts.length <= 1) {
            return "";
        }

        return String.join(
                " ",
                java.util.Arrays.copyOfRange(
                        parts,
                        1,
                        parts.length
                )
        );
    }

    private record RiderMatch(
            Rider rider,
            BigDecimal distanceKm,
            BigDecimal score,
            boolean hasCurrentLocation
    ) {
    }
}
