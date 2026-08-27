package com.foodordering.menu;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.foodordering.common.exception.ForbiddenOperationException;
import com.foodordering.common.exception.ResourceNotFoundException;
import com.foodordering.restaurant.Restaurant;
import com.foodordering.restaurant.RestaurantRepository;
import com.foodordering.review.ReviewRepository;
import com.foodordering.security.JwtUtil;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/menu-items")
public class MenuItemController {

    private final MenuItemRepository menuItemRepository;
    private final RestaurantRepository restaurantRepository;
    private final ReviewRepository reviewRepository;
    private final JwtUtil jwtUtil;

    public MenuItemController(
            MenuItemRepository menuItemRepository,
            RestaurantRepository restaurantRepository,
            ReviewRepository reviewRepository,
            JwtUtil jwtUtil
    ) {
        this.menuItemRepository =
                menuItemRepository;

        this.restaurantRepository =
                restaurantRepository;

        this.reviewRepository =
                reviewRepository;

        this.jwtUtil =
                jwtUtil;
    }

    @GetMapping(
            "/restaurant/{restaurantId}"
    )
    public ResponseEntity<List<MenuItemDto>>
    getMenuByRestaurant(
            @PathVariable
            UUID restaurantId
    ) {

        if (
                !restaurantRepository
                        .existsById(
                                restaurantId
                        )
        ) {
            throw new ResourceNotFoundException(
                    "Restaurant not found"
            );
        }

        List<MenuItemDto> menuItems =
                menuItemRepository
                        .findByRestaurant_Id(
                                restaurantId
                        )
                        .stream()
                        .map(this::toDto)
                        .toList();

        return ResponseEntity.ok(
                menuItems
        );
    }

    @PostMapping
    public ResponseEntity<MenuItemDto>
    createMenuItem(

            @RequestHeader(
                    "Authorization"
            )
            String authHeader,

            @Valid
            @RequestBody
            MenuItemDto dto
    ) {

        UUID ownerId =
                extractUserId(
                        authHeader
                );

        Restaurant restaurant =
                restaurantRepository
                        .findByOwnerId(ownerId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Restaurant not found for this owner"
                                )
                        );

        MenuItem item =
                new MenuItem();

        item.setRestaurant(
                restaurant
        );

        item.setName(
                dto
                        .getName()
                        .trim()
        );

        item.setDescription(
                normalizeOptional(
                        dto.getDescription()
                )
        );

        item.setPrice(
                dto.getPrice()
        );

        item.setCategory(
                normalizeCategory(
                        dto.getCategory()
                )
        );

        item.setAddOns(
                normalizeAddOns(
                        dto.getAddOns()
                )
        );

        item.setAvailable(
                dto.isAvailable()
        );

        item.setImageUrl(
                normalizeOptional(
                        dto.getImageUrl()
                )
        );

        MenuItem saved =
                menuItemRepository.save(
                        item
                );

        return ResponseEntity
                .status(
                        HttpStatus.CREATED
                )
                .body(
                        toDto(saved)
                );
    }

    @PutMapping("/{menuItemId}")
    public ResponseEntity<MenuItemDto>
    updateMenuItem(

            @RequestHeader(
                    "Authorization"
            )
            String authHeader,

            @PathVariable
            UUID menuItemId,

            @Valid
            @RequestBody
            MenuItemDto dto
    ) {

        UUID ownerId =
                extractUserId(
                        authHeader
                );

        Restaurant restaurant =
                restaurantRepository
                        .findByOwnerId(ownerId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Restaurant not found for this owner"
                                )
                        );

        MenuItem item =
                menuItemRepository
                        .findById(menuItemId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Menu item not found"
                                )
                        );

        validateMenuOwnership(
                item,
                restaurant
        );

        item.setName(
                dto
                        .getName()
                        .trim()
        );

        item.setDescription(
                normalizeOptional(
                        dto.getDescription()
                )
        );

        item.setPrice(
                dto.getPrice()
        );

        item.setCategory(
                normalizeCategory(
                        dto.getCategory()
                )
        );

        item.setAddOns(
                normalizeAddOns(
                        dto.getAddOns()
                )
        );

        item.setAvailable(
                dto.isAvailable()
        );

        item.setImageUrl(
                normalizeOptional(
                        dto.getImageUrl()
                )
        );

        MenuItem saved =
                menuItemRepository.save(
                        item
                );

        return ResponseEntity.ok(
                toDto(saved)
        );
    }

    @PatchMapping("/{menuItemId}/availability")
    public ResponseEntity<MenuItemDto> toggleAvailability(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID menuItemId,
            @RequestParam(name = "available", required = false) Boolean available,
            @RequestBody(required = false) java.util.Map<String, Object> body
    ) {
        UUID ownerId = extractUserId(authHeader);
        Restaurant restaurant = restaurantRepository.findByOwnerId(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found for this owner"));

        MenuItem item = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));

        validateMenuOwnership(item, restaurant);

        boolean newAvailable;
        if (available != null) {
            newAvailable = available;
        } else if (body != null && body.containsKey("available")) {
            newAvailable = Boolean.parseBoolean(String.valueOf(body.get("available")));
        } else {
            newAvailable = !item.isAvailable();
        }

        item.setAvailable(newAvailable);
        MenuItem saved = menuItemRepository.save(item);
        return ResponseEntity.ok(toDto(saved));
    }

    private MenuItemDto toDto(
            MenuItem item
    ) {

        MenuItemDto dto =
                new MenuItemDto(item);

        dto.setAverageRating(
                reviewRepository
                        .getAverageMenuItemRating(
                                item.getId()
                        )
        );

        dto.setReviewCount(
                reviewRepository
                        .countByMenuItemId(
                                item.getId()
                        )
        );

        return dto;
    }

    @DeleteMapping("/{menuItemId}")
    public ResponseEntity<Void>
    deleteMenuItem(

            @RequestHeader(
                    "Authorization"
            )
            String authHeader,

            @PathVariable
            UUID menuItemId
    ) {

        UUID ownerId =
                extractUserId(
                        authHeader
                );

        Restaurant restaurant =
                restaurantRepository
                        .findByOwnerId(ownerId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Restaurant not found for this owner"
                                )
                        );

        MenuItem item =
                menuItemRepository
                        .findById(menuItemId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Menu item not found"
                                )
                        );

        validateMenuOwnership(
                item,
                restaurant
        );

        menuItemRepository.delete(
                item
        );

        return ResponseEntity
                .noContent()
                .build();
    }

    private void validateMenuOwnership(
            MenuItem menuItem,
            Restaurant restaurant
    ) {

        if (
                menuItem.getRestaurantId() == null
                || !menuItem
                        .getRestaurantId()
                        .equals(
                                restaurant.getId()
                        )
        ) {

            throw new ForbiddenOperationException(
                    "You are not allowed to modify this menu item"
            );
        }
    }

    private String normalizeOptional(
            String value
    ) {

        if (
                value == null
                || value.isBlank()
        ) {
            return null;
        }

        return value.trim();
    }

    private String normalizeCategory(
            String value
    ) {

        String normalized =
                normalizeOptional(value);

        if (normalized == null) {
            return null;
        }

        String lower =
                normalized.toLowerCase(Locale.ROOT);

        if (
                !"drinks".equals(lower)
                && !"meals".equals(lower)
                && !"dessert".equals(lower)
        ) {
            throw new ForbiddenOperationException(
                    "Menu category must be Drinks, Meals or Dessert"
            );
        }

        return switch (lower) {
            case "drinks" -> "Drinks";
            case "dessert" -> "Dessert";
            default -> "Meals";
        };
    }

    private List<String> normalizeAddOns(
            List<String> addOns
    ) {

        if (addOns == null) {
            return List.of();
        }

        return addOns
                .stream()
                .map(this::normalizeOptional)
                .filter(addOn -> addOn != null)
                .distinct()
                .limit(10)
                .toList();
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

        return jwtUtil.extractUserId(
                authHeader.substring(7)
        );
    }
}
