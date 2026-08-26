package com.foodordering.location;

import java.math.BigDecimal;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import jakarta.annotation.PostConstruct;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@Service
public class GoogleMapsLocationService {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    GoogleMapsLocationService.class
            );

    private final RestClient restClient;
    private final String apiKey;

    GoogleMapsLocationService(
            @Value("${google.maps.base-url}")
            String baseUrl,
            @Value("${google.maps.api-key:}")
            String apiKey
    ) {

        SimpleClientHttpRequestFactory requestFactory =
                new SimpleClientHttpRequestFactory();

        requestFactory.setConnectTimeout(
                Duration.ofSeconds(5)
        );
        requestFactory.setReadTimeout(
                Duration.ofSeconds(10)
        );

        this.restClient =
                RestClient
                        .builder()
                        .baseUrl(baseUrl)
                        .requestFactory(
                                requestFactory
                        )
                        .build();

        this.apiKey =
                apiKey == null
                        ? ""
                        : apiKey.trim();
    }

    @PostConstruct
    void logConfigurationStatus() {

        if (apiKey == null || apiKey.isBlank()) {
            LOGGER.warn(
                    "Google Maps API key is not configured. Set GOOGLE_MAPS_API_KEY before starting the backend."
            );
        } else {
            LOGGER.info(
                    "Google Maps API key loaded."
            );
        }
    }

    public ReverseGeocodeResponse reverseGeocode(
            BigDecimal latitude,
            BigDecimal longitude
    ) {
        // 1. Try Google Maps if API key is provided
        if (apiKey != null && !apiKey.isBlank()) {
            try {
                JsonNode response =
                        restClient
                                .get()
                                .uri(uriBuilder ->
                                        uriBuilder
                                                .path("/maps/api/geocode/json")
                                                .queryParam(
                                                        "latlng",
                                                        latitude + "," + longitude
                                                )
                                                .queryParam(
                                                        "key",
                                                        apiKey
                                                )
                                                .build()
                                )
                                .retrieve()
                                .body(JsonNode.class);

                String status =
                        response != null
                                ? response.path("status").asText("")
                                : "";

                if ("OK".equals(status)) {
                    String displayName =
                            response
                                    .path("results")
                                    .path(0)
                                    .path("formatted_address")
                                    .asText("");

                    if (!displayName.isBlank()) {
                        return new ReverseGeocodeResponse(displayName.trim());
                    }
                } else {
                    LOGGER.warn(
                            "Google Maps geocoding status '{}': {}",
                            status,
                            extractGoogleStatusMessage(response, status)
                    );
                }
            } catch (Exception ex) {
                LOGGER.warn("Google Maps reverse geocoding call failed: {}", ex.getMessage());
            }
        }

        // 2. Resilient fallback to OpenStreetMap Nominatim
        String osmAddress = reverseGeocodeWithOsm(latitude, longitude);
        if (osmAddress != null && !osmAddress.isBlank()) {
            return new ReverseGeocodeResponse(osmAddress);
        }

        // 3. Fallback to GPS coordinates representation
        return new ReverseGeocodeResponse(
                String.format("GPS Location (%.6f, %.6f)", latitude.doubleValue(), longitude.doubleValue())
        );
    }

    private String reverseGeocodeWithOsm(BigDecimal latitude, BigDecimal longitude) {
        try {
            RestClient osmClient =
                    RestClient.builder()
                            .baseUrl("https://nominatim.openstreetmap.org")
                            .defaultHeader("User-Agent", "FoodOrderingSystem/1.0 (contact@foodordering.local)")
                            .build();

            JsonNode osmNode =
                    osmClient.get()
                            .uri(uriBuilder -> uriBuilder
                                    .path("/reverse")
                                    .queryParam("format", "json")
                                    .queryParam("lat", latitude)
                                    .queryParam("lon", longitude)
                                    .queryParam("zoom", "18")
                                    .queryParam("addressdetails", "1")
                                    .build()
                            )
                            .retrieve()
                            .body(JsonNode.class);

            if (osmNode != null && osmNode.has("display_name")) {
                String address = osmNode.path("display_name").asText("");
                if (!address.isBlank()) {
                    return address.trim();
                }
            }
        } catch (Exception e) {
            LOGGER.warn("OSM fallback reverse geocoding failed: {}", e.getMessage());
        }
        return null;
    }

    private String extractGoogleStatusMessage(
            JsonNode response,
            String status
    ) {

        String message =
                response != null
                        ? response
                                .path("error_message")
                                .asText("")
                        : "";

        if (!message.isBlank()) {
            return message;
        }

        return status == null || status.isBlank()
                ? "Reverse geocoding request was rejected."
                : status;
    }

    private String extractErrorMessage(
            String responseBody
    ) {

        if (
                responseBody == null
                || responseBody.isBlank()
        ) {
            return "Reverse geocoding request was rejected.";
        }

        try {
            JsonNode error =
                    JsonMapper
                            .builder()
                            .build()
                            .readTree(responseBody);

            String message =
                    error
                            .path("error_message")
                            .asText("");

            if (message.isBlank()) {
                message =
                        error
                                .path("error")
                                .path("message")
                                .asText("");
            }

            if (message.isBlank()) {
                message =
                        error
                                .path("message")
                                .asText("");
            }

            if (!message.isBlank()) {
                return message;
            }
        } catch (RuntimeException ignored) {
        }

        return responseBody.length() > 300
                ? responseBody.substring(0, 300)
                : responseBody;
    }
}

