package com.foodordering.location;

import java.math.BigDecimal;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/location")
public class LocationController {

    private final GoogleMapsLocationService locationService;

    public LocationController(
            GoogleMapsLocationService locationService
    ) {

        this.locationService =
                locationService;
    }

    @GetMapping("/reverse")
    public ReverseGeocodeResponse reverse(
            @RequestParam BigDecimal lat,
            @RequestParam BigDecimal lon
    ) {
        if (lat == null || lon == null) {
            throw new com.foodordering.common.exception.BusinessRuleException("Latitude and longitude coordinates are required");
        }
        if (lat.compareTo(BigDecimal.valueOf(-90)) < 0 || lat.compareTo(BigDecimal.valueOf(90)) > 0) {
            throw new com.foodordering.common.exception.BusinessRuleException("Latitude must be between -90.0 and +90.0");
        }
        if (lon.compareTo(BigDecimal.valueOf(-180)) < 0 || lon.compareTo(BigDecimal.valueOf(180)) > 0) {
            throw new com.foodordering.common.exception.BusinessRuleException("Longitude must be between -180.0 and +180.0");
        }

        return locationService.reverseGeocode(
                lat,
                lon
        );
    }
}

