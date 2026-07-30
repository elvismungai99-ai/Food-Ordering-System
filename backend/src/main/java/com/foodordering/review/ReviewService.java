package com.foodordering.review;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.foodordering.common.exception.BusinessRuleException;
import com.foodordering.common.exception.ResourceNotFoundException;
import com.foodordering.order.Order;
import com.foodordering.order.OrderItem;
import com.foodordering.order.OrderRepository;
import com.foodordering.order.OrderStatus;
import com.foodordering.review.dto.CreateReviewRequest;
import com.foodordering.review.dto.ReviewDto;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;

    public ReviewService(
            ReviewRepository reviewRepository,
            OrderRepository orderRepository
    ) {
        this.reviewRepository =
                reviewRepository;
        this.orderRepository =
                orderRepository;
    }

    @Transactional
    public ReviewDto createReview(
            UUID customerId,
            CreateReviewRequest request
    ) {

        Order order =
                orderRepository
                        .findByIdAndCustomerId(
                                request.getOrderId(),
                                customerId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Delivered order not found"
                                )
                        );

        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new BusinessRuleException(
                    "You can review only delivered orders"
            );
        }

        UUID menuItemId =
                request.getMenuItemId();

        if (menuItemId != null) {
            boolean itemWasOrdered =
                    order.getItems()
                            .stream()
                            .map(OrderItem::getMenuItemId)
                            .anyMatch(menuItemId::equals);

            if (!itemWasOrdered) {
                throw new BusinessRuleException(
                        "You can review only menu items from this order"
                );
            }

            if (
                    reviewRepository
                            .existsByOrderIdAndCustomerIdAndRestaurantIdAndMenuItemId(
                                    order.getId(),
                                    customerId,
                                    order.getRestaurantId(),
                                    menuItemId
                            )
            ) {
                throw new BusinessRuleException(
                        "You already reviewed this menu item for this order"
                );
            }
        } else if (
                reviewRepository
                        .existsByOrderIdAndCustomerIdAndRestaurantIdAndMenuItemIdIsNull(
                                order.getId(),
                                customerId,
                                order.getRestaurantId()
                        )
        ) {
            throw new BusinessRuleException(
                    "You already reviewed this restaurant for this order"
            );
        }

        Review review =
                new Review();

        review.setOrderId(order.getId());
        review.setCustomerId(customerId);
        review.setRestaurantId(order.getRestaurantId());
        review.setMenuItemId(menuItemId);
        review.setRating(request.getRating());
        review.setComment(
                request.getComment() == null
                        || request.getComment().isBlank()
                        ? null
                        : request.getComment().trim()
        );

        return new ReviewDto(
                reviewRepository.save(review)
        );
    }

    @Transactional(readOnly = true)
    public List<ReviewDto> getRestaurantReviews(
            UUID restaurantId
    ) {

        return reviewRepository
                .findByRestaurantIdAndMenuItemIdIsNullOrderByCreatedAtDesc(
                        restaurantId
                )
                .stream()
                .map(ReviewDto::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReviewDto> getMenuItemReviews(
            UUID menuItemId
    ) {

        return reviewRepository
                .findByMenuItemIdOrderByCreatedAtDesc(
                        menuItemId
                )
                .stream()
                .map(ReviewDto::new)
                .toList();
    }
}
