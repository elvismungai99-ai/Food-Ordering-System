package com.foodordering.restaurant;

import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;

    public RestaurantService(RestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }

    public RestaurantDto createRestaurant(UUID ownerId, RestaurantDto dto) {
        if (restaurantRepository.existsByOwnerId(ownerId)) {
            throw new RuntimeException("This account already has a registered restaurant");
        }

        Restaurant restaurant = new Restaurant();
        restaurant.setOwnerId(ownerId);
        restaurant.setName(dto.getName());
        restaurant.setDescription(dto.getDescription());
        restaurant.setAddress(dto.getAddress());
        restaurant.setOpeningTime(dto.getOpeningTime());
        restaurant.setClosingTime(dto.getClosingTime());

        Restaurant saved = restaurantRepository.save(restaurant);
        return new RestaurantDto(saved);
    }

    public RestaurantDto getMyRestaurant(UUID ownerId) {
        Restaurant restaurant = restaurantRepository.findByOwnerId(ownerId)
                .orElseThrow(() -> new RuntimeException("No restaurant found for this account"));
        return new RestaurantDto(restaurant);
    }
}