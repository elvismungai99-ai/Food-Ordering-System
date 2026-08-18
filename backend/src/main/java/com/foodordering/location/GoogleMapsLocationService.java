package com.foodordering.location;

import java.math.BigDecimal;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import jakarta.annotation.PostConstruct;

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

        if (apiKey == null || apiKey.isBlank()) {
            LOGGER.warn(
                    "Google Maps API key is not configured."
            );

            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Google Maps API key is not configured."
            );
        }

        JsonNode response;

        try {
            response =
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
        } catch (RestClientResponseException exception) {
            String googleMessage =
                    extractErrorMessage(
                            exception.getResponseBodyAsString()
                    );

            LOGGER.warn(
                    "Google Maps reverse geocoding failed with status {} and body {}",
                    exception.getStatusCode(),
                    exception.getResponseBodyAsString()
            );

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Google Maps error: "
                    + googleMessage
            );
        } catch (RestClientException exception) {
            LOGGER.warn(
                    "Google Maps reverse geocoding request failed",
                    exception
            );

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Unable to contact Google Maps."
            );
        } catch (RuntimeException exception) {
            LOGGER.warn(
                    "Unexpected reverse geocoding error",
                    exception
            );

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Unable to resolve the place name for this location."
            );
        }

        String status =
                response != null
                        ? response
                                .path("status")
                                .asText("")
                        : "";

        if (!"OK".equals(status)) {
            String message =
                    extractGoogleStatusMessage(
                            response,
                            status
                    );

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Google Maps error: " + message
            );
        }

        String displayName =
                response
                        .path("results")
                        .path(0)
                        .path("formatted_address")
                        .asText("");

        if (displayName.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Google Maps did not return a place name for this location."
            );
        }

        return new ReverseGeocodeResponse(
                displayName
        );
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

