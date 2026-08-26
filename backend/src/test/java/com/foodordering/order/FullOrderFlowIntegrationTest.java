package com.foodordering.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodordering.User.repository.UserRepository;
import com.foodordering.cart.CartRepository;
import com.foodordering.menu.MenuItem;
import com.foodordering.menu.MenuItemRepository;
import com.foodordering.restaurant.Restaurant;
import com.foodordering.restaurant.RestaurantRepository;
import com.foodordering.restaurant.RestaurantStatus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FullOrderFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void cleanDatabase() {
        orderRepository.deleteAll();
        cartRepository.deleteAll();
        menuItemRepository.deleteAll();
        restaurantRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void customerCanLoginAddToCartCheckoutAndOwnerCanUpdateStatus()
            throws Exception {

        AuthSession customer =
                registerAndLogin(
                        "Flow Customer",
                        "flow.customer@example.com",
                        "0712345678",
                        "CustomerPass1",
                        "CUSTOMER"
                );

        AuthSession owner =
                registerAndLogin(
                        "Flow Owner",
                        "flow.owner@example.com",
                        "0722345678",
                        "OwnerPass123",
                        "OWNER"
                );

        Restaurant restaurant =
                createRestaurant(
                        owner.userId()
                );

        MenuItem menuItem =
                createMenuItem(
                        restaurant
                );

        mockMvc.perform(
                        post("/api/cart/items")
                                .header(
                                        "Authorization",
                                        bearer(customer.token())
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        json(
                                                Map.of(
                                                        "menuItemId",
                                                        menuItem.getId(),
                                                        "quantity",
                                                        2
                                                )
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(2))
                .andExpect(jsonPath("$.items[0].menuItemId").value(
                        menuItem.getId().toString()
                ));

        MvcResult placedOrderResult =
                mockMvc.perform(
                                post("/api/orders")
                                        .header(
                                                "Authorization",
                                                bearer(customer.token())
                                        )
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                json(
                                                        Map.of(
                                                                "deliveryAddress",
                                                                "Flow Street, Nairobi",
                                                                "deliveryLatitude",
                                                                BigDecimal.valueOf(-1.286389),
                                                                "deliveryLongitude",
                                                                BigDecimal.valueOf(36.817223)
                                                        )
                                                )
                                        )
                        )
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.status").value("PENDING"))
                        .andExpect(jsonPath("$.deliveryLatitude").value(-1.286389))
                        .andExpect(jsonPath("$.deliveryLongitude").value(36.817223))
                        .andExpect(jsonPath("$.items[0].quantity").value(2))
                        .andReturn();

        JsonNode orderJson =
                objectMapper.readTree(
                        placedOrderResult
                                .getResponse()
                                .getContentAsString()
                );

        UUID orderId =
                UUID.fromString(
                        orderJson.get("id").asText()
                );

        assertThat(
                cartRepository
                        .findWithItemsByCustomerId(
                                customer.userId()
                        )
        )
                .isPresent()
                .get()
                .satisfies(cart ->
                        assertThat(cart.getItems()).isEmpty()
                );

        mockMvc.perform(
                        get("/api/orders")
                                .header(
                                        "Authorization",
                                        bearer(customer.token())
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(orderId.toString()));

        mockMvc.perform(
                        get(
                                "/api/orders/restaurant/{restaurantId}",
                                restaurant.getId()
                        )
                                .header(
                                        "Authorization",
                                        bearer(owner.token())
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(orderId.toString()));

        mockMvc.perform(
                        patch(
                                "/api/orders/{orderId}/status",
                                orderId
                        )
                                .header(
                                        "Authorization",
                                        bearer(owner.token())
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        json(
                                                Map.of(
                                                        "status",
                                                        "CONFIRMED"
                                                )
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        assertThat(
                orderRepository
                        .findById(orderId)
        )
                .isPresent()
                .get()
                .satisfies(order -> {
                    assertThat(order.getStatus())
                            .isEqualTo(OrderStatus.CONFIRMED);
                    assertThat(order.getDeliveryLatitude())
                            .isEqualByComparingTo("-1.286389");
                    assertThat(order.getDeliveryLongitude())
                            .isEqualByComparingTo("36.817223");
                });
    }

    private AuthSession registerAndLogin(
            String fullName,
            String email,
            String phoneNumber,
            String password,
            String role
    ) throws Exception {

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        json(
                                                Map.of(
                                                        "fullName",
                                                        fullName,
                                                        "email",
                                                        email,
                                                        "phoneNumber",
                                                        phoneNumber,
                                                        "password",
                                                        password,
                                                        "role",
                                                        role
                                                )
                                        )
                                )
                )
                .andExpect(status().isCreated());

        MvcResult loginResult =
                mockMvc.perform(
                                post("/api/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                json(
                                                        Map.of(
                                                                "email",
                                                                email,
                                                                "password",
                                                                password
                                                        )
                                                )
                                        )
                        )
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.token").isNotEmpty())
                        .andReturn();

        JsonNode loginJson =
                objectMapper.readTree(
                        loginResult
                                .getResponse()
                                .getContentAsString()
                );

        return new AuthSession(
                loginJson.get("token").asText(),
                UUID.fromString(
                        loginJson.get("userId").asText()
                )
        );
    }

    private Restaurant createRestaurant(
            UUID ownerId
    ) {

        Restaurant restaurant =
                new Restaurant();

        restaurant.setOwnerId(ownerId);
        restaurant.setName("Flow Kitchen");
        restaurant.setDescription("Integration test kitchen");
        restaurant.setAddress("Nairobi CBD");
        restaurant.setOpeningTime(LocalTime.of(0, 0, 0));
        restaurant.setClosingTime(LocalTime.of(23, 59, 59));
        restaurant.setStatus(RestaurantStatus.APPROVED);
        restaurant.setCategory("Fast Food");

        return restaurantRepository.save(
                restaurant
        );
    }

    private MenuItem createMenuItem(
            Restaurant restaurant
    ) {

        MenuItem menuItem =
                new MenuItem();

        menuItem.setRestaurant(restaurant);
        menuItem.setName("Flow Burger");
        menuItem.setDescription("Burger used by the full flow test");
        menuItem.setPrice(BigDecimal.valueOf(450));
        menuItem.setCategory("Burgers");
        menuItem.setAvailable(true);
        menuItem.setImageUrl("https://example.com/flow-burger.jpg");

        return menuItemRepository.save(
                menuItem
        );
    }

    private String json(
            Object value
    ) throws Exception {

        return objectMapper.writeValueAsString(
                value
        );
    }

    private String bearer(
            String token
    ) {

        return "Bearer " + token;
    }

    private record AuthSession(
            String token,
            UUID userId
    ) {
    }
}
