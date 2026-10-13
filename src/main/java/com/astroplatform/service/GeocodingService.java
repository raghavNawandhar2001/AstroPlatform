package com.astroplatform.service;

import com.astroplatform.dto.LocationData;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Service
public class GeocodingService {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public LocationData getCoordinates(String locationName) {
        try {
            // 1. First attempt: Search with the full string (City, State, Country)
            LocationData loc = fetchFromApi(locationName);
            if (loc != null) return loc;

            // 2. Fallback: If full string returned no results, extract just the primary City name
            if (locationName.contains(",")) {
                String cityNameOnly = locationName.split(",")[0].trim();
                loc = fetchFromApi(cityNameOnly);
                if (loc != null) return loc;
            }

            throw new RuntimeException("Location not found: " + locationName);

        } catch (Exception e) {
            throw new RuntimeException("Geocoding failed for: " + locationName, e);
        }
    }

    private LocationData fetchFromApi(String query) throws Exception {
        // Safe URL encoding for spaces and commas
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String apiUrl = "https://geocoding-api.open-meteo.com/v1/search?name="
                + encodedQuery + "&count=1&language=en&format=json";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JsonNode root = objectMapper.readTree(response.body());
            JsonNode results = root.get("results");

            if (results != null && results.isArray() && !results.isEmpty()) {
                JsonNode firstResult = results.get(0);
                double latitude = firstResult.get("latitude").asDouble();
                double longitude = firstResult.get("longitude").asDouble();
                String timezone = firstResult.has("timezone") ? firstResult.get("timezone").asText() : "Asia/Kolkata";

                return new LocationData(query, latitude, longitude, timezone);
            }
        }
        return null;
    }

}