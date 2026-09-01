package com.foodordering.security;

import com.foodordering.User.entity.User;
import com.foodordering.User.repository.UserRepository;
import com.foodordering.User.entity.SavedAddress;
import com.foodordering.User.repository.SavedAddressRepository;
import com.foodordering.User.UserService;
import com.foodordering.User.dto.SavedAddressRequest;
import com.foodordering.cart.Cart;
import com.foodordering.cart.CartRepository;
import com.foodordering.cart.CartService;
import com.foodordering.common.exception.ForbiddenOperationException;
import com.foodordering.common.exception.ResourceNotFoundException;
import com.foodordering.menu.MenuItem;
import com.foodordering.menu.MenuItemController;
import com.foodordering.menu.MenuItemDto;
import com.foodordering.menu.MenuItemRepository;
import com.foodordering.order.OrderRepository;
import com.foodordering.payment.PricingService;
import com.foodordering.restaurant.Restaurant;
import com.foodordering.restaurant.RestaurantRepository;
import com.foodordering.restaurant.RestaurantService;
import com.foodordering.review.ReviewRepository;
import com.foodordering.review.ReviewService;
import com.foodordering.review.dto.CreateReviewRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerDataProtectionSecurityTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private MenuItemRepository menuItemRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private RestaurantService restaurantService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private SavedAddressRepository savedAddressRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private com.foodordering.auth.RefreshTokenRepository refreshTokenRepository;

    @Mock
    private com.foodordering.rider.DeliveryRequestRepository deliveryRequestRepository;

    @Mock
    private com.foodordering.rider.RiderRepository riderRepository;

    @Mock
    private com.foodordering.payment.PaymentService paymentService;

    private SecurityUtils securityUtils;
    private CartService cartService;
    private UserService userService;
    private ReviewService reviewService;
    private MenuItemController menuItemController;
    private com.foodordering.order.OrderService orderService;
    private com.foodordering.order.OrderTrackingService orderTrackingService;

    private User customerA;
    private User customerB;
    private User ownerA;
    private User ownerB;

    @BeforeEach
    void setUp() {
        securityUtils = new SecurityUtils(userRepository);

        PricingService pricingService = new PricingService(
                BigDecimal.valueOf(150),
                BigDecimal.valueOf(35),
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );
        cartService = new CartService(cartRepository, menuItemRepository, restaurantService, pricingService);
        userService = new UserService(userRepository, savedAddressRepository, passwordEncoder, refreshTokenRepository);
        reviewService = new ReviewService(reviewRepository, orderRepository, userRepository);
        menuItemController = new MenuItemController(menuItemRepository, restaurantRepository, reviewRepository, securityUtils);
        orderService = new com.foodordering.order.OrderService(
                orderRepository,
                cartRepository,
                menuItemRepository,
                restaurantRepository,
                restaurantService,
                paymentService,
                pricingService,
                new com.foodordering.order.OrderStateMachine()
        );
        orderTrackingService = new com.foodordering.order.OrderTrackingService(
                orderRepository,
                deliveryRequestRepository,
                riderRepository
        );

        customerA = new User();
        customerA.setId(UUID.randomUUID());
        customerA.setEmail("customerA@example.com");
        customerA.setRole("CUSTOMER");
        customerA.setActive(true);

        customerB = new User();
        customerB.setId(UUID.randomUUID());
        customerB.setEmail("customerB@example.com");
        customerB.setRole("CUSTOMER");
        customerB.setActive(true);

        ownerA = new User();
        ownerA.setId(UUID.randomUUID());
        ownerA.setEmail("ownerA@example.com");
        ownerA.setRole("OWNER");
        ownerA.setActive(true);

        ownerB = new User();
        ownerB.setId(UUID.randomUUID());
        ownerB.setEmail("ownerB@example.com");
        ownerB.setRole("OWNER");
        ownerB.setActive(true);
    }

    private void authenticateAs(User user) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        user.getEmail(),
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
                )
        );
        lenient().when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    }

    @Test
    void testCustomerCannotModifyAnotherCustomersCartItem() {
        authenticateAs(customerB);

        UUID cartItemId = UUID.randomUUID();
        Cart cartB = new Cart();
        cartB.setId(UUID.randomUUID());
        cartB.setCustomerId(customerB.getId());

        when(cartRepository.findWithItemsByCustomerId(customerB.getId())).thenReturn(Optional.of(cartB));

        // Attempting to remove an item that belongs to Customer A's cart
        assertThrows(ResourceNotFoundException.class, () ->
                cartService.removeItem(customerB.getId(), cartItemId)
        );
    }

    @Test
    void testCustomerCannotModifyAnotherCustomersSavedAddress() {
        authenticateAs(customerB);

        UUID addressId = UUID.randomUUID();
        // Database lookup with findByIdAndUserId ensures address belonging to customer A is not returned for customer B
        when(savedAddressRepository.findByIdAndUserId(addressId, customerB.getId())).thenReturn(Optional.empty());

        SavedAddressRequest updateRequest = new SavedAddressRequest();
        updateRequest.setLabel("Home");
        updateRequest.setAddress("456 New Road");

        assertThrows(ResourceNotFoundException.class, () ->
                userService.updateSavedAddress(customerB.getId(), addressId, updateRequest)
        );

        assertThrows(ResourceNotFoundException.class, () ->
                userService.deleteSavedAddress(customerB.getId(), addressId)
        );
    }

    @Test
    void testCustomerCannotReviewAnotherCustomersOrder() {
        authenticateAs(customerB);

        UUID orderId = UUID.randomUUID();
        when(orderRepository.findByIdAndCustomerId(orderId, customerB.getId())).thenReturn(Optional.empty());

        CreateReviewRequest reviewRequest = new CreateReviewRequest();
        reviewRequest.setOrderId(orderId);
        reviewRequest.setRating(5);
        reviewRequest.setComment("Great food");

        assertThrows(ResourceNotFoundException.class, () ->
                reviewService.createReview(customerB.getId(), reviewRequest)
        );
    }

    @Test
    void testRestaurantOwnerCannotModifyAnotherRestaurantsMenuItem() {
        authenticateAs(ownerB);

        Restaurant restaurantA = new Restaurant();
        restaurantA.setId(UUID.randomUUID());
        restaurantA.setOwnerId(ownerA.getId()); // Owned by Owner A!

        MenuItem menuItemA = new MenuItem();
        menuItemA.setId(UUID.randomUUID());
        menuItemA.setName("Owner A's Burger");
        menuItemA.setPrice(BigDecimal.valueOf(500));
        menuItemA.setRestaurant(restaurantA);

        when(menuItemRepository.findById(menuItemA.getId())).thenReturn(Optional.of(menuItemA));

        MenuItemDto updateDto = new MenuItemDto();
        updateDto.setName("Hacked Item Name");
        updateDto.setPrice(BigDecimal.valueOf(1));

        // Owner B attempts to update Owner A's menu item
        ForbiddenOperationException exception = assertThrows(
                ForbiddenOperationException.class,
                () -> menuItemController.updateMenuItem(menuItemA.getId(), updateDto)
        );

        assertTrue(exception.getMessage().contains("manage this restaurant"));
    }

    @Test
    void testRestaurantOwnerCannotDeleteAnotherRestaurantsMenuItem() {
        authenticateAs(ownerB);

        Restaurant restaurantA = new Restaurant();
        restaurantA.setId(UUID.randomUUID());
        restaurantA.setOwnerId(ownerA.getId()); // Owned by Owner A!

        MenuItem menuItemA = new MenuItem();
        menuItemA.setId(UUID.randomUUID());
        menuItemA.setName("Owner A's Pizza");
        menuItemA.setRestaurant(restaurantA);

        when(menuItemRepository.findById(menuItemA.getId())).thenReturn(Optional.of(menuItemA));

        ForbiddenOperationException exception = assertThrows(
                ForbiddenOperationException.class,
                () -> menuItemController.deleteMenuItem(menuItemA.getId())
        );

        assertTrue(exception.getMessage().contains("manage this restaurant"));
        verify(menuItemRepository, never()).delete(any());
    }

    @Test
    void testSecurityUtilsLoadsLiveRoleFromDatabase() {
        // User's JWT claimed role is CUSTOMER, but database user has been promoted to SUPER_ADMIN
        User liveUser = new User();
        liveUser.setId(UUID.randomUUID());
        liveUser.setEmail("promoted@example.com");
        liveUser.setRole("SUPER_ADMIN");
        liveUser.setActive(true);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "promoted@example.com",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")) // Stale JWT authority
                )
        );

        when(userRepository.findByEmail("promoted@example.com")).thenReturn(Optional.of(liveUser));

        // SecurityUtils fetches fresh role directly from database
        assertEquals("SUPER_ADMIN", securityUtils.getCurrentUserRole());
        assertTrue(securityUtils.isSuperAdmin());
    }

    @Test
    void testCustomerCannotAccessAnotherCustomersOrder() {
        UUID orderOfCustomerAId = UUID.randomUUID();
        // Customer B tries to view Customer A's order
        when(orderRepository.findByIdAndCustomerId(orderOfCustomerAId, customerB.getId()))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                orderService.getCustomerOrder(customerB.getId(), orderOfCustomerAId)
        );
    }

    @Test
    void testCustomerCannotCancelAnotherCustomersOrder() {
        UUID orderOfCustomerAId = UUID.randomUUID();
        // Customer B tries to cancel Customer A's order
        when(orderRepository.findByIdAndCustomerId(orderOfCustomerAId, customerB.getId()))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                orderService.cancelCustomerOrder(customerB.getId(), orderOfCustomerAId, "Fraudulent cancel attempt")
        );
    }

    @Test
    void testRestaurantOwnerCannotViewAnotherRestaurantsOrders() {
        Restaurant restaurantA = new Restaurant();
        restaurantA.setId(UUID.randomUUID());
        restaurantA.setOwnerId(ownerA.getId()); // Owned by Owner A

        when(restaurantRepository.existsById(restaurantA.getId())).thenReturn(true);
        when(restaurantRepository.findById(restaurantA.getId())).thenReturn(Optional.of(restaurantA));

        // Owner B attempts to view orders for Restaurant A without superadmin privileges
        com.foodordering.common.exception.BusinessRuleException exception = assertThrows(
                com.foodordering.common.exception.BusinessRuleException.class,
                () -> orderService.getRestaurantOrders(ownerB.getId(), restaurantA.getId(), false)
        );

        assertTrue(exception.getMessage().contains("You can access only orders for your own restaurant"));
    }

    @Test
    void testRestaurantOwnerCannotUpdateStatusOfAnotherRestaurantsOrder() {
        Restaurant restaurantA = new Restaurant();
        restaurantA.setId(UUID.randomUUID());
        restaurantA.setOwnerId(ownerA.getId()); // Owned by Owner A

        com.foodordering.order.Order orderA = new com.foodordering.order.Order();
        orderA.setId(UUID.randomUUID());
        orderA.setRestaurantId(restaurantA.getId());
        orderA.setStatus(com.foodordering.order.OrderStatus.PENDING);

        when(orderRepository.findById(orderA.getId())).thenReturn(Optional.of(orderA));
        when(restaurantRepository.findById(restaurantA.getId())).thenReturn(Optional.of(restaurantA));

        // Owner B attempts to confirm or progress an order belonging to Restaurant A
        com.foodordering.common.exception.BusinessRuleException exception = assertThrows(
                com.foodordering.common.exception.BusinessRuleException.class,
                () -> orderService.updateOrderStatus(ownerB.getId(), orderA.getId(), com.foodordering.order.OrderStatus.CONFIRMED, false)
        );

        assertTrue(exception.getMessage().contains("You can access only orders for your own restaurant"));
    }

    @Test
    void testOrderTracking_MasksRiderLocationWhenDelivered() {
        UUID orderId = UUID.randomUUID();
        com.foodordering.order.Order order = new com.foodordering.order.Order();
        order.setId(orderId);
        order.setCustomerId(customerA.getId());
        order.setStatus(com.foodordering.order.OrderStatus.DELIVERED); // Order already completed!

        com.foodordering.rider.Rider rider = new com.foodordering.rider.Rider();
        rider.setId(UUID.randomUUID());
        rider.setFullName("John Rider");
        rider.setPhoneNumber("0712345678");
        rider.setCurrentLatitude(BigDecimal.valueOf(-1.286389));
        rider.setCurrentLongitude(BigDecimal.valueOf(36.817223));
        rider.setVehicleType(com.foodordering.rider.VehicleType.MOTORCYCLE);
        rider.setLicencePlate("KMCE 123X");

        com.foodordering.rider.DeliveryRequest request = new com.foodordering.rider.DeliveryRequest();
        request.setOrderId(orderId);
        request.setRiderId(rider.getId());
        request.setStatus(com.foodordering.rider.DeliveryRequestStatus.DELIVERED);

        when(orderRepository.findByIdAndCustomerId(orderId, customerA.getId())).thenReturn(Optional.of(order));
        when(deliveryRequestRepository.findByOrderId(orderId)).thenReturn(Optional.of(request));
        when(riderRepository.findById(rider.getId())).thenReturn(Optional.of(rider));

        com.foodordering.order.dto.OrderTrackingDto tracking = orderTrackingService.getTracking(customerA.getId(), orderId);

        // Security check: GPS coordinates and personal phone are masked once delivery is complete
        assertNull(tracking.riderLatitude());
        assertNull(tracking.riderLongitude());
        assertNull(tracking.riderPhoneNumber());
    }

    @Test
    void testOrderTracking_ShowsRiderLocationWhenOutForDelivery() {
        UUID orderId = UUID.randomUUID();
        com.foodordering.order.Order order = new com.foodordering.order.Order();
        order.setId(orderId);
        order.setCustomerId(customerA.getId());
        order.setStatus(com.foodordering.order.OrderStatus.OUT_FOR_DELIVERY); // Active delivery!

        com.foodordering.rider.Rider rider = new com.foodordering.rider.Rider();
        rider.setId(UUID.randomUUID());
        rider.setFullName("John Rider");
        rider.setPhoneNumber("0712345678");
        rider.setCurrentLatitude(BigDecimal.valueOf(-1.286389));
        rider.setCurrentLongitude(BigDecimal.valueOf(36.817223));
        rider.setVehicleType(com.foodordering.rider.VehicleType.MOTORCYCLE);
        rider.setLicencePlate("KMCE 123X");

        com.foodordering.rider.DeliveryRequest request = new com.foodordering.rider.DeliveryRequest();
        request.setOrderId(orderId);
        request.setRiderId(rider.getId());
        request.setStatus(com.foodordering.rider.DeliveryRequestStatus.PICKED_UP);

        when(orderRepository.findByIdAndCustomerId(orderId, customerA.getId())).thenReturn(Optional.of(order));
        when(deliveryRequestRepository.findByOrderId(orderId)).thenReturn(Optional.of(request));
        when(riderRepository.findById(rider.getId())).thenReturn(Optional.of(rider));

        com.foodordering.order.dto.OrderTrackingDto tracking = orderTrackingService.getTracking(customerA.getId(), orderId);

        // Active delivery: Live GPS coordinates and phone are visible to customer
        assertEquals(BigDecimal.valueOf(-1.286389), tracking.riderLatitude());
        assertEquals(BigDecimal.valueOf(36.817223), tracking.riderLongitude());
        assertEquals("0712345678", tracking.riderPhoneNumber());
    }

    @Test
    void testLocationReverse_OutOfBoundsCoordinatesRejected() {
        com.foodordering.location.LocationController locationController =
                new com.foodordering.location.LocationController(null);

        // Latitude > 90
        assertThrows(com.foodordering.common.exception.BusinessRuleException.class, () ->
                locationController.reverse(BigDecimal.valueOf(95.0), BigDecimal.valueOf(36.0))
        );

        // Longitude > 180
        assertThrows(com.foodordering.common.exception.BusinessRuleException.class, () ->
                locationController.reverse(BigDecimal.valueOf(-1.2), BigDecimal.valueOf(185.0))
        );
    }
}
