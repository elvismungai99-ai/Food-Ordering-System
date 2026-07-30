package com.foodordering.restaurant;

import com.foodordering.common.exception.BusinessRuleException;
import com.foodordering.common.exception.ConflictException;
import com.foodordering.common.exception.ResourceNotFoundException;
import com.foodordering.review.ReviewRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final ReviewRepository reviewRepository;

    public RestaurantService(
            RestaurantRepository restaurantRepository,
            ReviewRepository reviewRepository
    ) {
        this.restaurantRepository =
                restaurantRepository;
        this.reviewRepository =
                reviewRepository;
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
                RestaurantStatus.PENDING_APPROVAL
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

        return toDto(saved);
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

        return toDto(restaurant);
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

        if (restaurant.getStatus() != RestaurantStatus.APPROVED) {
            throw new ResourceNotFoundException(
                    "Restaurant not found"
            );
        }

        return toDto(restaurant);
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
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<String> getCategories() {

        return restaurantRepository
                .findDistinctCategories();
    }

    @Transactional(readOnly = true)
    public List<RestaurantDto> getRestaurantsByStatus(
            RestaurantStatus status
    ) {

        return restaurantRepository
                .findByStatusOrderByCreatedAtAsc(status)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public RestaurantDto updateApprovalStatus(
            UUID restaurantId,
            RestaurantStatus status
    ) {

        if (status == null) {
            throw new BusinessRuleException(
                    "Restaurant status is required"
            );
        }

        Restaurant restaurant =
                restaurantRepository
                        .findById(restaurantId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Restaurant not found"
                                )
                        );

        restaurant.setStatus(status);

        return toDto(
                restaurantRepository.save(restaurant)
        );
    }

    public boolean isApprovedAndOpen(
            Restaurant restaurant
    ) {

        return restaurant != null
                && restaurant.getStatus()
                == RestaurantStatus.APPROVED
                && isWithinOpeningHours(restaurant);
    }

    public boolean isWithinOpeningHours(
            Restaurant restaurant
    ) {

        if (
                restaurant == null
                || restaurant.getOpeningTime() == null
                || restaurant.getClosingTime() == null
        ) {
            return false;
        }

        LocalTime now =
                LocalTime.now();

        LocalTime openingTime =
                restaurant.getOpeningTime();

        LocalTime closingTime =
                restaurant.getClosingTime();

        if (openingTime.equals(closingTime)) {
            return false;
        }

        if (openingTime.isBefore(closingTime)) {
            return !now.isBefore(openingTime)
                    && now.isBefore(closingTime);
        }

        return !now.isBefore(openingTime)
                || now.isBefore(closingTime);
    }

    private RestaurantDto toDto(
            Restaurant restaurant
    ) {

        RestaurantDto dto =
                new RestaurantDto(restaurant);

        dto.setOpenNow(
                restaurant.getStatus()
                == RestaurantStatus.APPROVED
                && isWithinOpeningHours(restaurant)
        );

        dto.setAverageRating(
                reviewRepository
                        .getAverageRestaurantRating(
                                restaurant.getId()
                        )
        );

        dto.setReviewCount(
                reviewRepository
                        .countByRestaurantIdAndMenuItemIdIsNull(
                                restaurant.getId()
                        )
        );

        return dto;
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
