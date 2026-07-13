package com.foodordering.restaurant;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

@Service
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;

    public RestaurantService(
            RestaurantRepository restaurantRepository
    ) {
        this.restaurantRepository = restaurantRepository;
    }

    public RestaurantDto createRestaurant(
            UUID ownerId,
            RestaurantDto dto
    ) {
        if (restaurantRepository.existsByOwnerId(ownerId)) {
            throw new RuntimeException(
                    "This account already has a registered restaurant"
            );
        }

        Restaurant restaurant = new Restaurant();

        restaurant.setOwnerId(ownerId);
        restaurant.setName(dto.getName());
        restaurant.setDescription(dto.getDescription());
        restaurant.setAddress(dto.getAddress());
        restaurant.setOpeningTime(dto.getOpeningTime());
        restaurant.setClosingTime(dto.getClosingTime());
        restaurant.setCategory(dto.getCategory());

        if (dto.getStatus() != null) {
            restaurant.setStatus(dto.getStatus());
        }

        Restaurant savedRestaurant =
                restaurantRepository.save(restaurant);

        return new RestaurantDto(savedRestaurant);
    }

    public RestaurantDto getMyRestaurant(UUID ownerId) {
        Restaurant restaurant = restaurantRepository
                .findByOwnerId(ownerId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "No restaurant found for this account"
                        )
                );

        return new RestaurantDto(restaurant);
    }

    public RestaurantDto updateMyRestaurant(
            UUID ownerId,
            RestaurantDto dto
    ) {
        Restaurant restaurant = restaurantRepository
                .findByOwnerId(ownerId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "No restaurant found for this account"
                        )
                );

        if (dto.getName() != null) {
            restaurant.setName(dto.getName());
        }

        if (dto.getDescription() != null) {
            restaurant.setDescription(dto.getDescription());
        }

        if (dto.getAddress() != null) {
            restaurant.setAddress(dto.getAddress());
        }

        if (dto.getOpeningTime() != null) {
            restaurant.setOpeningTime(dto.getOpeningTime());
        }

        if (dto.getClosingTime() != null) {
            restaurant.setClosingTime(dto.getClosingTime());
        }

        if (dto.getStatus() != null) {
            restaurant.setStatus(dto.getStatus());
        }

        if (dto.getCategory() != null) {
            restaurant.setCategory(dto.getCategory());
        }

        Restaurant savedRestaurant =
                restaurantRepository.save(restaurant);

        return new RestaurantDto(savedRestaurant);
    }

    public List<RestaurantDto> getAllRestaurants() {
        return restaurantRepository
                .findAll()
                .stream()
                .map(RestaurantDto::new)
                .collect(Collectors.toList());
    }

    public RestaurantDto getRestaurantById(UUID id) {
        Restaurant restaurant = restaurantRepository
                .findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Restaurant not found"
                        )
                );

        return new RestaurantDto(restaurant);
    }

    public List<RestaurantDto> searchRestaurants(
            String search,
            String category
    ) {
        String normalizedSearch =
                search == null || search.isBlank()
                        ? null
                        : search.toLowerCase();

        String normalizedCategory =
                category == null || category.isBlank()
                        ? null
                        : category;

        return restaurantRepository
                .searchRestaurants(
                        normalizedSearch,
                        normalizedCategory
                )
                .stream()
                .map(RestaurantDto::new)
                .collect(Collectors.toList());
    }

    public List<String> getAllCategories() {
        return restaurantRepository
                .findDistinctCategories();
    }
}