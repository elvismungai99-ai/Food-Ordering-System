package com.foodordering.analytics;

import com.foodordering.User.entity.User;
import com.foodordering.analytics.dto.RestaurantAnalyticsDto;
import com.foodordering.security.SecurityUtils;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
public class RestaurantAnalyticsController {

    private final RestaurantAnalyticsService analyticsService;
    private final SecurityUtils securityUtils;

    public RestaurantAnalyticsController(
            RestaurantAnalyticsService analyticsService,
            SecurityUtils securityUtils
    ) {
        this.analyticsService = analyticsService;
        this.securityUtils = securityUtils;
    }

    @GetMapping("/restaurant")
    public ResponseEntity<RestaurantAnalyticsDto> getRestaurantAnalytics() {
        User owner = securityUtils.requireOwner();
        return ResponseEntity.ok(
                analyticsService.getMyRestaurantAnalytics(owner.getId())
        );
    }
}
