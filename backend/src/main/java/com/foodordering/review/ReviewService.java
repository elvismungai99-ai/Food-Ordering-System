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
    private final com.foodordering.User.repository.UserRepository userRepository;

    @org.springframework.beans.factory.annotation.Autowired
    public ReviewService(
            ReviewRepository reviewRepository,
            OrderRepository orderRepository,
            @org.springframework.beans.factory.annotation.Autowired(required = false)
            com.foodordering.User.repository.UserRepository userRepository
    ) {
        this.reviewRepository = reviewRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    public ReviewService(
            ReviewRepository reviewRepository,
            OrderRepository orderRepository
    ) {
        this(reviewRepository, orderRepository, null);
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

        Review saved = reviewRepository.save(review);
        return toDto(saved);
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
                .map(this::toDto)
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
                .map(this::toDto)
                .toList();
    }

    private ReviewDto toDto(Review review) {
        String displayName = resolveCustomerDisplayName(review.getCustomerId());
        return new ReviewDto(review, displayName);
    }

    private String resolveCustomerDisplayName(UUID customerId) {
        if (customerId == null || userRepository == null) {
            return "Verified Customer";
        }
        return userRepository.findById(customerId)
                .map(user -> {
                    String first = user.getFirstName();
                    String last = user.getLastName();
                    if (first != null && !first.isBlank()) {
                        if (last != null && !last.isBlank()) {
                            return first.trim() + " " + last.trim().charAt(0) + ".";
                        }
                        return first.trim();
                    }
                    return "Verified Customer";
                })
                .orElse("Verified Customer");
    }
}
