package com.foodordering.menu;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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

import com.foodordering.restaurant.Restaurant;
import com.foodordering.restaurant.RestaurantRepository;
import com.foodordering.security.JwtUtil;

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

    // Public — anyone can view a restaurant's menu
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

    // Owner-only — add a new menu item to their own restaurant
    @PostMapping
    public ResponseEntity<?> createMenuItem(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody MenuItemDto dto
    ) {
        try {
            UUID ownerId = extractUserId(authHeader);
            Restaurant restaurant = getOwnedRestaurantOrThrow(ownerId);

            MenuItem item = new MenuItem();
            item.setRestaurantId(restaurant.getId());
            item.setName(dto.getName());
            item.setDescription(dto.getDescription());
            item.setPrice(dto.getPrice());
            item.setCategory(dto.getCategory());
            item.setAvailable(dto.isAvailable());
            item.setImageUrl(dto.getImageUrl());

            MenuItem saved = menuItemRepository.save(item);

            return ResponseEntity.ok(new MenuItemDto(saved));

        } catch (RuntimeException exception) {
            return ResponseEntity.badRequest()
                    .body(exception.getMessage());
        }
    }

    // Owner-only — update their own menu item
    @PutMapping("/{id}")
    public ResponseEntity<?> updateMenuItem(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID id,
            @RequestBody MenuItemDto dto
    ) {
        try {
            UUID ownerId = extractUserId(authHeader);
            Restaurant restaurant = getOwnedRestaurantOrThrow(ownerId);

            MenuItem item = menuItemRepository
                    .findById(id)
                    .orElseThrow(() ->
                            new RuntimeException("Menu item not found")
                    );

            if (!item.getRestaurantId().equals(restaurant.getId())) {
                throw new RuntimeException(
                        "You do not have permission to edit this menu item"
                );
            }

            if (dto.getName() != null) {
                item.setName(dto.getName());
            }

            if (dto.getDescription() != null) {
                item.setDescription(dto.getDescription());
            }

            if (dto.getPrice() != null) {
                item.setPrice(dto.getPrice());
            }

            if (dto.getCategory() != null) {
                item.setCategory(dto.getCategory());
            }

            item.setAvailable(dto.isAvailable());

            if (dto.getImageUrl() != null) {
                item.setImageUrl(dto.getImageUrl());
            }

            MenuItem saved = menuItemRepository.save(item);

            return ResponseEntity.ok(new MenuItemDto(saved));

        } catch (RuntimeException exception) {
            return ResponseEntity.badRequest()
                    .body(exception.getMessage());
        }
    }

    // Owner-only — delete their own menu item
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMenuItem(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID id
    ) {
        try {
            UUID ownerId = extractUserId(authHeader);
            Restaurant restaurant = getOwnedRestaurantOrThrow(ownerId);

            MenuItem item = menuItemRepository
                    .findById(id)
                    .orElseThrow(() ->
                            new RuntimeException("Menu item not found")
                    );

            if (!item.getRestaurantId().equals(restaurant.getId())) {
                throw new RuntimeException(
                        "You do not have permission to delete this menu item"
                );
            }

            menuItemRepository.deleteById(id);

            return ResponseEntity.ok().build();

        } catch (RuntimeException exception) {
            return ResponseEntity.badRequest()
                    .body(exception.getMessage());
        }
    }

    private Restaurant getOwnedRestaurantOrThrow(UUID ownerId) {
        return restaurantRepository
                .findByOwnerId(ownerId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "No restaurant found for this account"
                        )
                );
    }

    private UUID extractUserId(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        return jwtUtil.extractUserId(token);
    }
}