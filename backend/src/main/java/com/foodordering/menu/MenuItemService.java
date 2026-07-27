package com.foodordering.menu;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.foodordering.restaurant.Restaurant;
import com.foodordering.restaurant.RestaurantRepository;

@Service
public class MenuItemService {

    private final MenuItemRepository menuItemRepository;
    private final RestaurantRepository restaurantRepository;

    public MenuItemService(
            MenuItemRepository menuItemRepository,
            RestaurantRepository restaurantRepository
    ) {
        this.menuItemRepository = menuItemRepository;
        this.restaurantRepository = restaurantRepository;
    }

    /*
     * Return all menu items belonging to one restaurant.
     *
     * This method is used by the customer-facing menu page.
     * MenuItemDto includes imageUrl, so the URL is returned
     * to React together with the other menu-item details.
     */
    @Transactional(readOnly = true)
    public List<MenuItemDto> getMenuByRestaurant(
            UUID restaurantId
    ) {
        return menuItemRepository
                .findByRestaurant_Id(restaurantId)
                .stream()
                .map(MenuItemDto::new)
                .toList();
    }

    /*
     * Create a menu item for a restaurant.
     *
     * The image URL comes from the request body sent by React.
     * Nothing is hardcoded in this service.
     */
    @Transactional
    public MenuItemDto createMenuItem(
            UUID restaurantId,
            MenuItemDto request
    ) {
        validateMenuItemRequest(request);

        Restaurant restaurant = restaurantRepository
                .findById(restaurantId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Restaurant not found"
                        )
                );

        MenuItem menuItem = new MenuItem();

        menuItem.setRestaurant(restaurant);
        menuItem.setName(request.getName().trim());

        menuItem.setDescription(
                normalizeOptionalText(
                        request.getDescription()
                )
        );

        menuItem.setPrice(request.getPrice());

        menuItem.setCategory(
                normalizeOptionalText(
                        request.getCategory()
                )
        );

        menuItem.setAvailable(
                request.isAvailable()
        );

        /*
         * Save the exact URL supplied by the restaurant owner
         * after trimming and validating it.
         */
        menuItem.setImageUrl(
                normalizeImageUrl(
                        request.getImageUrl()
                )
        );

        MenuItem savedItem =
                menuItemRepository.save(menuItem);

        return new MenuItemDto(savedItem);
    }

    /*
     * Update an existing menu item.
     *
     * This also permits the restaurant owner to replace or
     * remove the previously saved image URL.
     */
    @Transactional
    public MenuItemDto updateMenuItem(
            UUID menuItemId,
            MenuItemDto request
    ) {
        validateMenuItemRequest(request);

        MenuItem existingItem = menuItemRepository
                .findById(menuItemId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Menu item not found"
                        )
                );

        existingItem.setName(
                request.getName().trim()
        );

        existingItem.setDescription(
                normalizeOptionalText(
                        request.getDescription()
                )
        );

        existingItem.setPrice(
                request.getPrice()
        );

        existingItem.setCategory(
                normalizeOptionalText(
                        request.getCategory()
                )
        );

        existingItem.setAvailable(
                request.isAvailable()
        );

        existingItem.setImageUrl(
                normalizeImageUrl(
                        request.getImageUrl()
                )
        );

        MenuItem savedItem =
                menuItemRepository.save(existingItem);

        return new MenuItemDto(savedItem);
    }

    @Transactional
    public void deleteMenuItem(
            UUID menuItemId
    ) {
        MenuItem menuItem = menuItemRepository
                .findById(menuItemId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Menu item not found"
                        )
                );

        menuItemRepository.delete(menuItem);
    }

    private void validateMenuItemRequest(
            MenuItemDto request
    ) {
        if (request == null) {
            throw new RuntimeException(
                    "Menu item request is required"
            );
        }

        if (
                request.getName() == null
                || request.getName().isBlank()
        ) {
            throw new RuntimeException(
                    "Menu item name is required"
            );
        }

        BigDecimal price =
                request.getPrice();

        if (
                price == null
                || price.compareTo(
                        BigDecimal.ZERO
                ) < 0
        ) {
            throw new RuntimeException(
                    "Menu item price must be zero or greater"
            );
        }

        /*
         * Calling this here validates the URL before
         * anything is stored.
         */
        normalizeImageUrl(
                request.getImageUrl()
        );
    }

    /*
     * An empty image URL is allowed.
     *
     * When it is empty, PostgreSQL stores null and the
     * frontend displays "No image available".
     */
    private String normalizeImageUrl(
            String imageUrl
    ) {
        if (
                imageUrl == null
                || imageUrl.isBlank()
        ) {
            return null;
        }

        String normalized =
                imageUrl.trim();

        if (
                !normalized.startsWith("https://")
                && !normalized.startsWith("http://")
                && !normalized.startsWith("data:image/jpeg;base64,")
                && !normalized.startsWith("data:image/png;base64,")
                && !normalized.startsWith("data:image/webp;base64,")
        ) {
            throw new RuntimeException(
                    "Image must be a URL or uploaded JPEG, PNG or WebP image"
            );
        }

        return normalized;
    }

    private String normalizeOptionalText(
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
}
