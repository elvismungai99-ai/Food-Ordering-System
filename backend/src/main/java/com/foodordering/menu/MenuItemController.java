package com.foodordering.menu;

import java.util.List;
import java.util.UUID;

import com.foodordering.User.entity.User;
import com.foodordering.common.exception.ResourceNotFoundException;
import com.foodordering.common.util.ImageSanitizer;
import com.foodordering.restaurant.Restaurant;
import com.foodordering.restaurant.RestaurantRepository;
import com.foodordering.review.ReviewRepository;
import com.foodordering.security.SecurityUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/menu-items")
public class MenuItemController {

    private final MenuItemRepository menuItemRepository;
    private final RestaurantRepository restaurantRepository;
    private final ReviewRepository reviewRepository;
    private final SecurityUtils securityUtils;
    private final ImageSanitizer imageSanitizer;

    @Autowired
    public MenuItemController(
            MenuItemRepository menuItemRepository,
            RestaurantRepository restaurantRepository,
            ReviewRepository reviewRepository,
            SecurityUtils securityUtils,
            @Autowired(required = false) ImageSanitizer imageSanitizer
    ) {
        this.menuItemRepository = menuItemRepository;
        this.restaurantRepository = restaurantRepository;
        this.reviewRepository = reviewRepository;
        this.securityUtils = securityUtils;
        this.imageSanitizer = imageSanitizer != null ? imageSanitizer : new ImageSanitizer();
    }

    public MenuItemController(
            MenuItemRepository menuItemRepository,
            RestaurantRepository restaurantRepository,
            ReviewRepository reviewRepository,
            SecurityUtils securityUtils
    ) {
        this(menuItemRepository, restaurantRepository, reviewRepository, securityUtils, new ImageSanitizer());
    }

    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<List<MenuItemDto>> getMenuByRestaurant(
            @PathVariable UUID restaurantId
    ) {
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new ResourceNotFoundException("Restaurant not found");
        }

        List<MenuItemDto> menuItems = menuItemRepository
                .findByRestaurant_Id(restaurantId)
                .stream()
                .map(this::toDto)
                .toList();

        return ResponseEntity.ok(menuItems);
    }

    @PostMapping
    public ResponseEntity<MenuItemDto> createMenuItem(
            @Valid @RequestBody MenuItemDto dto
    ) {
        User owner = securityUtils.requireOwner();

        Restaurant restaurant = restaurantRepository
                .findByOwnerId(owner.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Restaurant not found for this owner"
                        )
                );

        MenuItem item = new MenuItem();
        item.setRestaurant(restaurant);
        item.setName(dto.getName().trim());
        item.setDescription(normalizeOptional(dto.getDescription()));
        item.setPrice(dto.getPrice());
        item.setCategory(normalizeCategory(dto.getCategory()));
        item.setAddOns(normalizeAddOns(dto.getAddOns()));
        item.setAvailable(dto.isAvailable());
        item.setImageUrl(imageSanitizer.validateAndSanitizeImage(dto.getImageUrl()));

        MenuItem saved = menuItemRepository.save(item);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(saved));
    }

    @GetMapping("/{menuItemId}")
    public ResponseEntity<MenuItemDto> getMenuItem(
            @PathVariable UUID menuItemId
    ) {
        MenuItem item = menuItemRepository
                .findById(menuItemId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Menu item not found"
                        )
                );

        return ResponseEntity.ok(toDto(item));
    }

    @PutMapping("/{menuItemId}")
    public ResponseEntity<MenuItemDto> updateMenuItem(
            @PathVariable UUID menuItemId,
            @Valid @RequestBody MenuItemDto dto
    ) {
        MenuItem item = menuItemRepository
                .findById(menuItemId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Menu item not found"
                        )
                );

        securityUtils.requireOwnerOrSuperAdmin(item.getRestaurant().getOwnerId());

        item.setName(dto.getName().trim());
        item.setDescription(normalizeOptional(dto.getDescription()));
        item.setPrice(dto.getPrice());
        item.setCategory(normalizeCategory(dto.getCategory()));
        item.setAddOns(normalizeAddOns(dto.getAddOns()));
        item.setAvailable(dto.isAvailable());
        item.setImageUrl(imageSanitizer.validateAndSanitizeImage(dto.getImageUrl()));

        MenuItem saved = menuItemRepository.save(item);
        return ResponseEntity.ok(toDto(saved));
    }

    @DeleteMapping("/{menuItemId}")
    public ResponseEntity<Void> deleteMenuItem(
            @PathVariable UUID menuItemId
    ) {
        MenuItem item = menuItemRepository
                .findById(menuItemId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Menu item not found"
                        )
                );

        securityUtils.requireOwnerOrSuperAdmin(item.getRestaurant().getOwnerId());
        menuItemRepository.delete(item);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{menuItemId}/availability")
    public ResponseEntity<MenuItemDto> toggleAvailability(
            @PathVariable UUID menuItemId,
            @RequestParam(name = "available", required = false) Boolean available,
            @RequestBody(required = false) java.util.Map<String, Object> body
    ) {
        MenuItem item = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));

        securityUtils.requireOwnerOrSuperAdmin(item.getRestaurant().getOwnerId());

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

    private MenuItemDto toDto(MenuItem item) {
        MenuItemDto dto = new MenuItemDto(item);
        dto.setAverageRating(reviewRepository.getAverageMenuItemRating(item.getId()));
        dto.setReviewCount(reviewRepository.countByMenuItemId(item.getId()));
        return dto;
    }

    private String normalizeCategory(String category) {
        return (category == null || category.isBlank()) ? "General" : category.trim();
    }

    private String normalizeOptional(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }

    private List<String> normalizeAddOns(List<String> addOns) {
        if (addOns == null) {
            return List.of();
        }
        return addOns.stream()
                .map(this::normalizeOptional)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .limit(10)
                .toList();
    }
}
