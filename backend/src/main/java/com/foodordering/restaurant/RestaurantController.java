package com.foodordering.restaurant;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.foodordering.common.exception.ForbiddenOperationException;
import com.foodordering.security.JwtUtil;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

@Validated
@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {

    private final RestaurantService restaurantService;
    private final JwtUtil jwtUtil;

    public RestaurantController(
            RestaurantService restaurantService,
            JwtUtil jwtUtil
    ) {
        this.restaurantService =
                restaurantService;
        this.jwtUtil =
                jwtUtil;
    }

    @GetMapping
    public ResponseEntity<List<RestaurantDto>>
    searchRestaurants(

            @RequestParam(
                    defaultValue = ""
            )
            @Size(
                    max = 100,
                    message = "Search text must not exceed 100 characters"
            )
            String search,

            @RequestParam(
                    defaultValue = ""
            )
            @Size(
                    max = 100,
                    message = "Category must not exceed 100 characters"
            )
            String category
    ) {

        return ResponseEntity.ok(
                restaurantService
                        .searchRestaurants(
                                search,
                                category
                        )
        );
    }

    @GetMapping("/me")
    public ResponseEntity<RestaurantDto>
    getMyRestaurant(
            @RequestHeader(
                    "Authorization"
            )
            String authHeader
    ) {

        UUID ownerId =
                extractUserId(
                        authHeader
                );

        return ResponseEntity.ok(
                restaurantService.getMyRestaurant(
                        ownerId
                )
        );
    }

    @PostMapping
    public ResponseEntity<RestaurantDto>
    createRestaurant(
            @RequestHeader("Authorization")
            String authHeader,

            @Valid
            @RequestBody
            RestaurantDto dto
    ) {

        UUID ownerId =
                extractUserId(authHeader);

        return ResponseEntity.ok(
                restaurantService.createRestaurant(
                        ownerId,
                        dto
                )
        );
    }

    @GetMapping("/{restaurantId}")
    public ResponseEntity<RestaurantDto>
    getRestaurant(
            @PathVariable
            UUID restaurantId
    ) {

        return ResponseEntity.ok(
                restaurantService.getRestaurant(
                        restaurantId
                )
        );
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>>
    getCategories() {

        return ResponseEntity.ok(
                restaurantService
                        .getCategories()
        );
    }

    private UUID extractUserId(
            String authHeader
    ) {

        if (
                authHeader == null
                || !authHeader.startsWith(
                        "Bearer "
                )
        ) {
            throw new ForbiddenOperationException(
                    "Authorization token is missing or invalid"
            );
        }

        try {
            return jwtUtil.extractUserId(
                    authHeader.substring(7)
            );
        } catch (RuntimeException exception) {
            throw new ForbiddenOperationException(
                    "Authorization token is missing or invalid"
            );
        }
    }
}
