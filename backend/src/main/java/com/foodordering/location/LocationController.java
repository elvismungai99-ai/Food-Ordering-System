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

        return locationService.reverseGeocode(
                lat,
                lon
        );
    }
}

