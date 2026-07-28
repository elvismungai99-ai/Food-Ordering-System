package com.foodordering.location;

import java.math.BigDecimal;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import jakarta.annotation.PostConstruct;

@Service
public class OpenRouteServiceLocationService {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    OpenRouteServiceLocationService.class
            );

    private final RestClient restClient;
    private final String apiKey;

    public OpenRouteServiceLocationService(
            @Value("${openrouteservice.base-url}")
            String baseUrl,
            @Value("${openrouteservice.api-key:}")
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
                    "Openrouteservice API key is not configured. Set ORS_API_KEY before starting the backend."
            );
        } else {
            LOGGER.info(
                    "Openrouteservice API key loaded."
            );
        }
    }

    public ReverseGeocodeResponse reverseGeocode(
            BigDecimal latitude,
            BigDecimal longitude
    ) {

        if (apiKey == null || apiKey.isBlank()) {
            LOGGER.warn(
                    "Openrouteservice API key is not configured."
            );

            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Openrouteservice API key is not configured."
            );
        }

        JsonNode response;

        try {
            response =
                    restClient
                            .get()
                            .uri(uriBuilder ->
                                    uriBuilder
                                            .path("/geocode/reverse")
                                            .queryParam(
                                                    "point.lon",
                                                    longitude
                                            )
                                            .queryParam(
                                                    "point.lat",
                                                    latitude
                                            )
                                            .queryParam(
                                                    "size",
                                                    1
                                            )
                                            .queryParam(
                                                    "api_key",
                                                    apiKey
                                            )
                                            .build()
                            )
                            .retrieve()
                            .body(JsonNode.class);
        } catch (RestClientResponseException exception) {
            String orsMessage =
                    extractErrorMessage(
                            exception.getResponseBodyAsString()
                    );

            LOGGER.warn(
                    "Openrouteservice reverse geocoding failed with status {} and body {}",
                    exception.getStatusCode(),
                    exception.getResponseBodyAsString()
            );

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Openrouteservice error: "
                    + orsMessage
            );
        } catch (RestClientException exception) {
            LOGGER.warn(
                    "Openrouteservice reverse geocoding request failed",
                    exception
            );

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Unable to contact Openrouteservice."
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

        String displayName =
                response != null
                        ? response
                                .path("features")
                                .path(0)
                                .path("properties")
                                .path("label")
                                .asText("")
                        : "";

        if (displayName.isBlank()) {
            displayName =
                    response != null
                            ? response
                                    .path("features")
                                    .path(0)
                                    .path("properties")
                                    .path("name")
                                    .asText("")
                            : "";
        }

        if (displayName.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Openrouteservice did not return a place name for this location."
            );
        }

        return new ReverseGeocodeResponse(
                displayName
        );
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
                            .path("error")
                            .path("message")
                            .asText("");

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
