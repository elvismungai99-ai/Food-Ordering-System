package com.foodordering.restaurant;

import java.util.List;
import java.util.UUID;

import com.foodordering.User.entity.User;
import com.foodordering.common.exception.ResourceNotFoundException;
import com.foodordering.security.SecurityUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

@Validated
@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {

    private final RestaurantService restaurantService;
    private final RestaurantRepository restaurantRepository;
    private final SecurityUtils securityUtils;

    public RestaurantController(
            RestaurantService restaurantService,
            RestaurantRepository restaurantRepository,
            SecurityUtils securityUtils
    ) {
        this.restaurantService = restaurantService;
        this.restaurantRepository = restaurantRepository;
        this.securityUtils = securityUtils;
    }

    @GetMapping
    public ResponseEntity<List<RestaurantDto>> searchRestaurants(
            @RequestParam(defaultValue = "")
            @Size(max = 100, message = "Search text must not exceed 100 characters")
            String search,

            @RequestParam(defaultValue = "")
            @Size(max = 100, message = "Category must not exceed 100 characters")
            String category
    ) {
        return ResponseEntity.ok(
                restaurantService.searchRestaurants(search, category)
        );
    }

    @GetMapping("/me")
    public ResponseEntity<RestaurantDto> getMyRestaurant() {
        User owner = securityUtils.requireOwner();
        return ResponseEntity.ok(
                restaurantService.getMyRestaurant(owner.getId())
        );
    }

    @PostMapping
    public ResponseEntity<RestaurantDto> createRestaurant(
            @Valid @RequestBody RestaurantDto dto
    ) {
        User owner = securityUtils.requireOwner();
        return ResponseEntity.ok(
                restaurantService.createRestaurant(owner.getId(), dto)
        );
    }

    @PutMapping("/{restaurantId}")
    public ResponseEntity<RestaurantDto> updateRestaurant(
            @PathVariable UUID restaurantId,
            @Valid @RequestBody RestaurantDto dto
    ) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        securityUtils.requireOwnerOrSuperAdmin(restaurant.getOwnerId());

        return ResponseEntity.ok(
                restaurantService.createRestaurant(restaurant.getOwnerId(), dto)
        );
    }

    @GetMapping("/{restaurantId}")
    public ResponseEntity<RestaurantDto> getRestaurant(
            @PathVariable UUID restaurantId
    ) {
        return ResponseEntity.ok(
                restaurantService.getRestaurant(restaurantId)
        );
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories() {
        return ResponseEntity.ok(
                restaurantService.getCategories()
        );
    }
}
