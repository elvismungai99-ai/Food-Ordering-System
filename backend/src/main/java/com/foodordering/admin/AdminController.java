package com.foodordering.admin;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.foodordering.User.entity.User;
import com.foodordering.audit.AuditLog;
import com.foodordering.audit.AuditLogService;
import com.foodordering.order.OrderService;
import com.foodordering.payment.PaymentResult;
import com.foodordering.payment.PaymentService;
import com.foodordering.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.foodordering.User.dto.UserDto;
import com.foodordering.User.entity.Role;
import com.foodordering.User.repository.UserRepository;
import com.foodordering.order.OrderRepository;
import com.foodordering.order.dto.OrderDto;
import com.foodordering.restaurant.RestaurantDto;
import com.foodordering.restaurant.Restaurant;
import com.foodordering.restaurant.RestaurantRepository;
import com.foodordering.restaurant.RestaurantService;
import com.foodordering.restaurant.RestaurantStatus;
import com.foodordering.rider.DeliveryRequestRepository;
import com.foodordering.rider.Rider;
import com.foodordering.rider.RiderOperationalStatus;
import com.foodordering.rider.RiderRepository;
import com.foodordering.rider.RiderStatus;
import com.foodordering.rider.dto.DeliveryRequestDto;
import com.foodordering.rider.dto.RiderDto;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final RestaurantService restaurantService;
    private final RiderRepository riderRepository;
    private final DeliveryRequestRepository deliveryRequestRepository;
    private final OrderRepository orderRepository;
    private final SecurityUtils securityUtils;
    private final AuditLogService auditLogService;
    private final OrderService orderService;
    private final PaymentService paymentService;

    @Autowired
    public AdminController(
            UserRepository userRepository,
            RestaurantRepository restaurantRepository,
            RestaurantService restaurantService,
            RiderRepository riderRepository,
            DeliveryRequestRepository deliveryRequestRepository,
            OrderRepository orderRepository,
            SecurityUtils securityUtils,
            AuditLogService auditLogService,
            @Autowired(required = false) OrderService orderService,
            @Autowired(required = false) PaymentService paymentService
    ) {
        this.userRepository = userRepository;
        this.restaurantRepository = restaurantRepository;
        this.restaurantService = restaurantService;
        this.riderRepository = riderRepository;
        this.deliveryRequestRepository = deliveryRequestRepository;
        this.orderRepository = orderRepository;
        this.securityUtils = securityUtils;
        this.auditLogService = auditLogService;
        this.orderService = orderService;
        this.paymentService = paymentService;
    }

    public AdminController(
            UserRepository userRepository,
            RestaurantRepository restaurantRepository,
            RestaurantService restaurantService,
            RiderRepository riderRepository,
            DeliveryRequestRepository deliveryRequestRepository,
            OrderRepository orderRepository,
            SecurityUtils securityUtils,
            AuditLogService auditLogService
    ) {
        this(userRepository, restaurantRepository, restaurantService, riderRepository, deliveryRequestRepository, orderRepository, securityUtils, auditLogService, null, null);
    }

    @GetMapping("/customers")
    public ResponseEntity<List<UserDto>> getAllCustomers() {
        securityUtils.requireSuperAdmin();
        List<UserDto> customers = userRepository
                .findByRole(Role.CUSTOMER)
                .stream()
                .map(UserDto::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(customers);
    }

    @DeleteMapping("/customers/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable UUID id) {
        securityUtils.requireSuperAdmin();
        userRepository.deleteById(id);
        auditLogService.logAction("CUSTOMER_DELETED", id.toString(), "USER", "Super admin deleted customer account");
        return ResponseEntity.ok().build();
    }

    @GetMapping("/customers/{id}/activities")
    public ResponseEntity<List<OrderDto>> getCustomerActivities(@PathVariable UUID id) {
        securityUtils.requireSuperAdmin();
        return ResponseEntity.ok(
                orderRepository
                        .findByCustomerIdOrderByCreatedAtDesc(id)
                        .stream()
                        .map(OrderDto::new)
                        .toList()
        );
    }

    @GetMapping("/owners")
    public ResponseEntity<List<UserDto>> getAllOwners() {
        securityUtils.requireSuperAdmin();
        List<UserDto> owners = userRepository
                .findByRole(Role.OWNER)
                .stream()
                .map(UserDto::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(owners);
    }

    @GetMapping("/owners/{id}/activities")
    public ResponseEntity<List<OrderDto>> getOwnerActivities(@PathVariable UUID id) {
        securityUtils.requireSuperAdmin();

        Restaurant restaurant = restaurantRepository
                .findByOwnerId(id)
                .orElseThrow(() ->
                        new RuntimeException("Restaurant not found for this owner")
                );

        return ResponseEntity.ok(
                orderRepository
                        .findByRestaurantIdOrderByCreatedAtDesc(
                                restaurant.getId()
                        )
                        .stream()
                        .map(OrderDto::new)
                        .toList()
        );
    }

    @GetMapping("/restaurants")
    public ResponseEntity<List<RestaurantDto>> getAllRestaurants() {
        securityUtils.requireSuperAdmin();
        List<RestaurantDto> restaurants = restaurantRepository
                .findAll()
                .stream()
                .map(RestaurantDto::new)
                .toList();

        return ResponseEntity.ok(restaurants);
    }

    @GetMapping("/restaurants/pending")
    public ResponseEntity<?> getPendingRestaurants() {
        securityUtils.requireSuperAdmin();
        return ResponseEntity.ok(
                restaurantService.getRestaurantsByStatus(
                        RestaurantStatus.PENDING_APPROVAL
                )
        );
    }

    @PatchMapping("/restaurants/{id}/status")
    public ResponseEntity<?> updateRestaurantStatus(
            @PathVariable UUID id,
            @RequestParam RestaurantStatus status
    ) {
        securityUtils.requireSuperAdmin();
        Object updated = restaurantService.updateApprovalStatus(id, status);
        auditLogService.logAction("RESTAURANT_STATUS_CHANGED", id.toString(), "RESTAURANT", "Status updated to " + status);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/restaurants/{id}")
    public ResponseEntity<Void> deleteRestaurant(@PathVariable UUID id) {
        securityUtils.requireSuperAdmin();
        restaurantRepository.deleteById(id);
        auditLogService.logAction("RESTAURANT_DELETED", id.toString(), "RESTAURANT", "Super admin deleted restaurant");
        return ResponseEntity.ok().build();
    }

    @GetMapping("/riders")
    public ResponseEntity<List<RiderDto>> getAllRiders() {
        securityUtils.requireSuperAdmin();
        return ResponseEntity.ok(
                riderRepository
                        .findAll()
                        .stream()
                        .map(RiderDto::new)
                        .toList()
        );
    }

    @PatchMapping("/riders/{id}/status")
    public ResponseEntity<RiderDto> updateRiderStatus(
            @PathVariable UUID id,
            @RequestParam RiderStatus status
    ) {
        securityUtils.requireSuperAdmin();

        Rider rider = riderRepository
                .findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Rider not found")
                );

        rider.setStatus(status);

        if (status != RiderStatus.APPROVED) {
            rider.setOnline(false);
            rider.setOperationalStatus(
                    RiderOperationalStatus.CLOSED
            );
        }

        Rider saved = riderRepository.save(rider);
        auditLogService.logAction("RIDER_STATUS_CHANGED", id.toString(), "RIDER", "Status updated to " + status);
        return ResponseEntity.ok(new RiderDto(saved));
    }

    @GetMapping("/riders/activities")
    public ResponseEntity<List<DeliveryRequestDto>> getAllRiderActivities() {
        securityUtils.requireSuperAdmin();
        return ResponseEntity.ok(
                deliveryRequestRepository
                        .findAllByOrderByRequestedAtDesc()
                        .stream()
                        .map(DeliveryRequestDto::new)
                        .toList()
        );
    }

    @GetMapping("/riders/{id}/activities")
    public ResponseEntity<List<DeliveryRequestDto>> getRiderActivities(@PathVariable UUID id) {
        securityUtils.requireSuperAdmin();
        return ResponseEntity.ok(
                deliveryRequestRepository
                        .findByRiderIdOrderByRequestedAtDesc(id)
                        .stream()
                        .map(DeliveryRequestDto::new)
                        .toList()
        );
    }

    @PostMapping("/orders/{id}/refund")
    public ResponseEntity<OrderDto> refundOrder(
            @PathVariable UUID id,
            @RequestBody(required = false) Map<String, String> body
    ) {
        User admin = securityUtils.requireSuperAdmin();
        String reason = body != null ? body.get("reason") : "Admin refund";
        return ResponseEntity.ok(orderService.refundOrder(id, reason, admin.getId()));
    }

    @PostMapping("/orders/{id}/reconcile-payment")
    public ResponseEntity<PaymentResult> reconcileOrderPayment(
            @PathVariable UUID id
    ) {
        User admin = securityUtils.requireSuperAdmin();
        return ResponseEntity.ok(paymentService.reconcilePayment(id, admin.getId()));
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<List<AuditLog>> getAuditLogs() {
        securityUtils.requireSuperAdmin();
        return ResponseEntity.ok(auditLogService.getRecentLogs());
    }
}
