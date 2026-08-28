package com.foodordering.review;

import java.util.List;
import java.util.UUID;

import com.foodordering.User.entity.User;
import com.foodordering.security.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.foodordering.review.dto.CreateReviewRequest;
import com.foodordering.review.dto.ReviewDto;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;
    private final SecurityUtils securityUtils;

    public ReviewController(
            ReviewService reviewService,
            SecurityUtils securityUtils
    ) {
        this.reviewService = reviewService;
        this.securityUtils = securityUtils;
    }

    @PostMapping
    public ResponseEntity<ReviewDto> createReview(
            @Valid @RequestBody CreateReviewRequest request
    ) {
        User customer = securityUtils.requireCustomer();

        ReviewDto review = reviewService.createReview(
                customer.getId(),
                request
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(review);
    }

    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<List<ReviewDto>> getRestaurantReviews(
            @PathVariable UUID restaurantId
    ) {
        return ResponseEntity.ok(
                reviewService.getRestaurantReviews(restaurantId)
        );
    }

    @GetMapping("/menu-item/{menuItemId}")
    public ResponseEntity<List<ReviewDto>> getMenuItemReviews(
            @PathVariable UUID menuItemId
    ) {
        return ResponseEntity.ok(
                reviewService.getMenuItemReviews(menuItemId)
        );
    }
}
