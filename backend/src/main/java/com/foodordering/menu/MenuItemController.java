package com.foodordering.menu;

import com.foodordering.restaurant.Restaurant;
import com.foodordering.restaurant.RestaurantRepository;
import com.foodordering.security.JwtUtil;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/menu-items")
public class MenuItemController {

    private final MenuItemRepository menuItemRepository;
    private final RestaurantRepository restaurantRepository;
    private final JwtUtil jwtUtil;

    public MenuItemController(
            MenuItemRepository menuItemRepository,
            RestaurantRepository restaurantRepository,
            JwtUtil jwtUtil
    ) {
        this.menuItemRepository = menuItemRepository;
        this.restaurantRepository = restaurantRepository;
        this.jwtUtil = jwtUtil;
    }

    // Public: view all menu items belonging to a restaurant.
    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<List<MenuItemDto>> getMenuByRestaurant(
            @PathVariable UUID restaurantId
    ) {
        List<MenuItemDto> items = menuItemRepository
                .findByRestaurantId(restaurantId)
                .stream()
                .map(MenuItemDto::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(items);
    }

    // Restaurant owner: create a menu item for their own restaurant.
    @PostMapping
    public ResponseEntity<?> createMenuItem(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody MenuItemDto dto
    ) {
        try {
            UUID ownerId = extractUserId(authHeader);
            Restaurant restaurant =
                    getOwnedRestaurantOrThrow(ownerId);

            validateRequiredFields(dto);

            MenuItem item = new MenuItem();
            item.setRestaurantId(restaurant.getId());
            item.setName(dto.getName().trim());
            item.setDescription(trimToNull(dto.getDescription()));
            item.setPrice(dto.getPrice());
            item.setCategory(trimToNull(dto.getCategory()));
            item.setAvailable(
                    dto.getAvailable() == null
                            || dto.isAvailable()
            );
            item.setImageUrl(trimToNull(dto.getImageUrl()));

            MenuItem saved =
                    menuItemRepository.save(item);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(new MenuItemDto(saved));

        } catch (RuntimeException exception) {
            return ResponseEntity.badRequest()
                    .body(exception.getMessage());
        }
    }

    // Restaurant owner: update their own menu item.
    @PutMapping("/{id}")
    public ResponseEntity<?> updateMenuItem(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID id,
            @RequestBody MenuItemDto dto
    ) {
        try {
            UUID ownerId = extractUserId(authHeader);

            Restaurant restaurant =
                    getOwnedRestaurantOrThrow(ownerId);

            MenuItem item = menuItemRepository
                    .findById(id)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Menu item not found"
                            )
                    );

            if (!item.getRestaurantId()
                    .equals(restaurant.getId())) {
                throw new RuntimeException(
                        "You do not have permission to edit this menu item"
                );
            }

            if (dto.getName() != null) {
                String name = dto.getName().trim();

                if (name.isEmpty()) {
                    throw new RuntimeException(
                            "Menu item name cannot be empty"
                    );
                }

                item.setName(name);
            }

            if (dto.getDescription() != null) {
                item.setDescription(
                        trimToNull(dto.getDescription())
                );
            }

            if (dto.getPrice() != null) {
                validatePrice(dto.getPrice());
                item.setPrice(dto.getPrice());
            }

            if (dto.getCategory() != null) {
                item.setCategory(
                        trimToNull(dto.getCategory())
                );
            }

            if (dto.getAvailable() != null) {
                item.setAvailable(dto.isAvailable());
            }

            if (dto.getImageUrl() != null) {
                item.setImageUrl(
                        trimToNull(dto.getImageUrl())
                );
            }

            MenuItem saved =
                    menuItemRepository.save(item);

            return ResponseEntity.ok(
                    new MenuItemDto(saved)
            );

        } catch (RuntimeException exception) {
            return ResponseEntity.badRequest()
                    .body(exception.getMessage());
        }
    }

    // Restaurant owner: delete one of their own menu items.
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMenuItem(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID id
    ) {
        try {
            UUID ownerId = extractUserId(authHeader);

            Restaurant restaurant =
                    getOwnedRestaurantOrThrow(ownerId);

            MenuItem item = menuItemRepository
                    .findById(id)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Menu item not found"
                            )
                    );

            if (!item.getRestaurantId()
                    .equals(restaurant.getId())) {
                throw new RuntimeException(
                        "You do not have permission to delete this menu item"
                );
            }

            menuItemRepository.delete(item);

            return ResponseEntity.noContent()
                    .build();

        } catch (RuntimeException exception) {
            return ResponseEntity.badRequest()
                    .body(exception.getMessage());
        }
    }

    private Restaurant getOwnedRestaurantOrThrow(
            UUID ownerId
    ) {
        return restaurantRepository
                .findByOwnerId(ownerId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "No restaurant found for this account"
                        )
                );
    }

    private UUID extractUserId(String authHeader) {
        if (
                authHeader == null
                || !authHeader.startsWith("Bearer ")
        ) {
            throw new RuntimeException(
                    "Authorization token is missing or invalid"
            );
        }

        String token = authHeader.substring(7);

        return jwtUtil.extractUserId(token);
    }

    private void validateRequiredFields(
            MenuItemDto dto
    ) {
        if (
                dto.getName() == null
                || dto.getName().isBlank()
        ) {
            throw new RuntimeException(
                    "Menu item name is required"
            );
        }

        if (dto.getPrice() == null) {
            throw new RuntimeException(
                    "Menu item price is required"
            );
        }

        validatePrice(dto.getPrice());
    }

    private void validatePrice(BigDecimal price) {
        if (
                price == null
                || price.compareTo(BigDecimal.ZERO) <= 0
        ) {
            throw new RuntimeException(
                    "Price must be greater than zero"
            );
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();

        return trimmed.isEmpty() ? null : trimmed;
    }
}