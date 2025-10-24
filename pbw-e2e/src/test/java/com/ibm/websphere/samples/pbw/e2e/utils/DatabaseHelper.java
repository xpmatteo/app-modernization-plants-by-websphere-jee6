// ABOUTME: Helper class for database operations in E2E tests
// ABOUTME: Handles database population via AdminServlet endpoint
package com.ibm.websphere.samples.pbw.e2e.utils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Helper for database operations during testing.
 * Uses the AdminServlet populate endpoint to initialize test data.
 */
public class DatabaseHelper {

    private static final HttpClient httpClient = HttpClient.newHttpClient();

    /**
     * Populate the database with test data via AdminServlet
     */
    public static void populateDatabase() throws IOException, InterruptedException {
        String populateUrl = TestConfig.getPopulateUrl();

        // Don't follow redirects - we just need to hit the endpoint
        HttpClient noRedirectClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(populateUrl))
                .GET()
                .build();

        HttpResponse<String> response = noRedirectClient.send(request, HttpResponse.BodyHandlers.ofString());

        // Accept 200 (OK), 302 (redirect), or any 2xx/3xx
        // The servlet populates the DB and then redirects, which is success
        if (response.statusCode() < 200 || response.statusCode() >= 400) {
            throw new RuntimeException("Failed to populate database. Status: " + response.statusCode());
        }

        // Give the database a moment to finish populating
        Thread.sleep(2000);
    }

    /**
     * Check if the application is reachable
     */
    public static boolean isApplicationReady() {
        try {
            String baseUrl = TestConfig.getBaseUrl();

            // Use a client with short timeout for faster failure
            HttpClient timeoutClient = HttpClient.newBuilder()
                    .connectTimeout(java.time.Duration.ofSeconds(3))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl))
                    .timeout(java.time.Duration.ofSeconds(3))
                    .GET()
                    .build();

            HttpResponse<String> response = timeoutClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }
}
