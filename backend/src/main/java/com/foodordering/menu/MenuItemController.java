package com.foodordering.menu;

import java.util.List;
import java.util.UUID;

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

import com.foodordering.common.exception.ForbiddenOperationException;
import com.foodordering.common.exception.ResourceNotFoundException;
import com.foodordering.restaurant.Restaurant;
import com.foodordering.restaurant.RestaurantRepository;
import com.foodordering.security.JwtUtil;

import jakarta.validation.Valid;

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
        this.menuItemRepository =
                menuItemRepository;

        this.restaurantRepository =
                restaurantRepository;

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
                        .map(MenuItemDto::new)
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
                normalizeOptional(
                        dto.getCategory()
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
                        new MenuItemDto(saved)
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
                normalizeOptional(
                        dto.getCategory()
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
                new MenuItemDto(saved)
        );
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