package com.foodordering.review;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.foodordering.common.exception.ForbiddenOperationException;
import com.foodordering.review.dto.CreateReviewRequest;
import com.foodordering.review.dto.ReviewDto;
import com.foodordering.security.JwtUtil;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;
    private final JwtUtil jwtUtil;

    public ReviewController(
            ReviewService reviewService,
            JwtUtil jwtUtil
    ) {
        this.reviewService =
                reviewService;
        this.jwtUtil =
                jwtUtil;
    }

    @PostMapping
    public ResponseEntity<ReviewDto> createReview(
            @RequestHeader("Authorization")
            String authHeader,

            @Valid
            @RequestBody
            CreateReviewRequest request
    ) {

        requireCustomer(authHeader);

        ReviewDto review =
                reviewService.createReview(
                        extractUserId(authHeader),
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
                reviewService.getRestaurantReviews(
                        restaurantId
                )
        );
    }

    @GetMapping("/menu-item/{menuItemId}")
    public ResponseEntity<List<ReviewDto>> getMenuItemReviews(
            @PathVariable UUID menuItemId
    ) {

        return ResponseEntity.ok(
                reviewService.getMenuItemReviews(
                        menuItemId
                )
        );
    }

    private UUID extractUserId(
            String authHeader
    ) {

        return jwtUtil.extractUserId(
                authHeader.substring(7)
        );
    }

    private void requireCustomer(
            String authHeader
    ) {

        String role =
                normalizeRole(
                        jwtUtil.extractRole(
                                authHeader.substring(7)
                        )
                );

        if (!"CUSTOMER".equals(role)) {
            throw new ForbiddenOperationException(
                    "Only customers can create reviews"
            );
        }
    }

    private String normalizeRole(
            String role
    ) {

        if (role == null || role.isBlank()) {
            return "";
        }

        String normalized =
                role.trim().toUpperCase(Locale.ROOT);

        if (normalized.startsWith("ROLE_")) {
            normalized =
                    normalized.substring(5);
        }

        return normalized;
    }
}
