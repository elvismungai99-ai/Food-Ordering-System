package com.foodordering.restaurant;

import com.foodordering.common.exception.BusinessRuleException;
import com.foodordering.common.exception.ConflictException;
import com.foodordering.common.exception.ResourceNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;

    public RestaurantService(
            RestaurantRepository restaurantRepository
    ) {
        this.restaurantRepository =
                restaurantRepository;
    }

    @Transactional
    public RestaurantDto createRestaurant(
            UUID ownerId,
            RestaurantDto dto
    ) {

        if (ownerId == null) {
            throw new BusinessRuleException(
                    "Restaurant owner ID is required"
            );
        }

        if (
                restaurantRepository
                        .existsByOwnerId(ownerId)
        ) {
            throw new ConflictException(
                    "A restaurant already exists for this owner"
            );
        }

        validateRestaurantTimes(dto);

        Restaurant restaurant =
                new Restaurant();

        restaurant.setOwnerId(
                ownerId
        );

        restaurant.setName(
                dto
                        .getName()
                        .trim()
        );

        restaurant.setDescription(
                normalizeOptional(
                        dto.getDescription()
                )
        );

        restaurant.setAddress(
                dto
                        .getAddress()
                        .trim()
        );

        restaurant.setOpeningTime(
                dto.getOpeningTime()
        );

        restaurant.setClosingTime(
                dto.getClosingTime()
        );

        restaurant.setStatus(
                dto.getStatus()
        );

        restaurant.setCategory(
                normalizeOptional(
                        dto.getCategory()
                )
        );

        Restaurant saved =
                restaurantRepository.save(
                        restaurant
                );

        return new RestaurantDto(
                saved
        );
    }

    @Transactional(readOnly = true)
    public RestaurantDto getMyRestaurant(
            UUID ownerId
    ) {

        Restaurant restaurant =
                restaurantRepository
                        .findByOwnerId(ownerId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Restaurant not found for this owner"
                                )
                        );

        return new RestaurantDto(
                restaurant
        );
    }

    @Transactional(readOnly = true)
    public RestaurantDto getRestaurant(
            UUID restaurantId
    ) {

        Restaurant restaurant =
                restaurantRepository
                        .findById(restaurantId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Restaurant not found"
                                )
                        );

        return new RestaurantDto(
                restaurant
        );
    }

    @Transactional(readOnly = true)
    public List<RestaurantDto> searchRestaurants(
            String search,
            String category
    ) {

        String safeSearch =
                normalizeOptional(search);

        String safeCategory =
                normalizeOptional(category);

        return restaurantRepository
                .searchRestaurants(
                        safeSearch,
                        safeCategory
                )
                .stream()
                .map(RestaurantDto::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<String> getCategories() {

        return restaurantRepository
                .findDistinctCategories();
    }

    private void validateRestaurantTimes(
            RestaurantDto dto
    ) {

        if (
                dto.getOpeningTime() != null
                && dto.getClosingTime() != null
                && dto
                        .getOpeningTime()
                        .equals(
                                dto.getClosingTime()
                        )
        ) {
            throw new BusinessRuleException(
                    "Opening time and closing time cannot be the same"
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
}