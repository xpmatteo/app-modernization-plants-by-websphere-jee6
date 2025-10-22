// ABOUTME: Configuration class for E2E tests
// ABOUTME: Provides base URL and other configurable properties
package com.ibm.websphere.samples.pbw.e2e.utils;

/**
 * Test configuration holder.
 * Reads configuration from system properties with sensible defaults.
 */
public class TestConfig {

    /**
     * Get the base URL for the application under test
     * Default: http://localhost:9080
     * Override with -Dapp.baseUrl=<url>
     */
    public static String getBaseUrl() {
        String baseUrl = System.getProperty("app.baseUrl");
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            baseUrl = "http://localhost:9080";
        }
        return baseUrl;
    }

    /**
     * Get the admin servlet populate URL
     */
    public static String getPopulateUrl() {
        return getBaseUrl() + "/servlet/AdminServlet?admintype=populate";
    }
}
