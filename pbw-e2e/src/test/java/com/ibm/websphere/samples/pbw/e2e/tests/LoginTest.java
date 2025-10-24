// ABOUTME: E2E tests for login functionality
// ABOUTME: Tests successful login and validation using Playwright
package com.ibm.websphere.samples.pbw.e2e.tests;

import com.ibm.websphere.samples.pbw.e2e.pages.LoginPage;
import com.ibm.websphere.samples.pbw.e2e.utils.DatabaseHelper;
import com.ibm.websphere.samples.pbw.e2e.utils.TestConfig;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end tests for the login flow.
 * Tests use resilient selectors based on semantic elements (labels, roles, text).
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LoginTest {

    private static Playwright playwright;
    private static Browser browser;
    private BrowserContext context;
    private Page page;
    private LoginPage loginPage;

    // Test credentials from pbw.properties
    private static final String TEST_USER_EMAIL = "plants@plantsbywebsphere.ibm.com";
    private static final String TEST_USER_PASSWORD = "plants";

    @BeforeAll
    static void launchBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions()
                        .setHeadless(true) // Set to false to see browser during test development
                        .setTimeout(3000) // 3 second timeout
                        .setArgs(java.util.Arrays.asList(
                                // Prevent Chrome from showing "controlled by automated software" banner
                                "--disable-blink-features=AutomationControlled",
                                // Disable Chrome password manager's "generate password" feature
                                "--disable-password-generation",
                                // Disable Chrome's "Save password?" prompt that can block form submission
                                "--disable-save-password-bubble"
                        ))
        );

        // Check if application is ready
        if (!DatabaseHelper.isApplicationReady()) {
            throw new RuntimeException(
                    "Application is not reachable at " + TestConfig.getBaseUrl() +
                    ". Please ensure the application is running (e.g., via docker-compose up)"
            );
        }

        // Populate database with test data
        try {
            DatabaseHelper.populateDatabase();
        } catch (Exception e) {
            throw new RuntimeException("Failed to populate database", e);
        }
    }

    @AfterAll
    static void closeBrowser() {
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }

    @BeforeEach
    void createContextAndPage() {
        // Create a new context and page for each test to ensure isolation
        context = browser.newContext();
        page = context.newPage();

        // Set shorter timeout for faster feedback (3 seconds as suggested by Captain Matt)
        page.setDefaultTimeout(3000);

        loginPage = new LoginPage(page);
    }

    @AfterEach
    void closeContext() {
        if (context != null) {
            context.close();
        }
    }

    @Test
    @DisplayName("User can successfully log in with valid credentials")
    void testSuccessfulLogin() {
        // Navigate directly to login page
        loginPage.navigate(TestConfig.getBaseUrl());

        // Verify we're on the login page
        assertThat(loginPage.isOnLoginPage())
                .as("Should be on login page")
                .isTrue();

        // Fill in the login credentials
        loginPage.fillEmail(TEST_USER_EMAIL);
        loginPage.fillPassword(TEST_USER_PASSWORD);

        // Click the sign in button
        loginPage.clickSignIn();

        // Wait for navigation to complete (JSF pages may take a moment)
        page.waitForLoadState();

        // Verify no error message is displayed
        String errorMessage = loginPage.getErrorMessage();
        assertThat(errorMessage)
                .as("Should not have error message after successful login")
                .isEmpty();

        // After successful login, verify the header shows "Logged in as:" with the user's name
        // This text only appears when user is logged in (LOGIN link is replaced)
        assertThat(page.locator("text=/Logged in as:/i").isVisible())
                .as("Header should show 'Logged in as:' after successful login")
                .isTrue();

        // Verify the page title indicates we're on the promo page
        assertThat(page.title())
                .as("Page title should indicate promo page after successful login")
                .contains("Plants By WebSphere Promo");
    }

    @Test
    @DisplayName("Login fails with invalid credentials")
    void testLoginWithInvalidCredentials() {
        // Navigate to login page
        loginPage.navigate(TestConfig.getBaseUrl());

        // Verify we're on the login page
        assertThat(loginPage.isOnLoginPage())
                .as("Should be on login page")
                .isTrue();

        // Attempt to log in with invalid credentials
        // Use password that meets validation rules (6-10 chars) but is wrong
        loginPage.fillEmail("invalid@example.com");
        loginPage.fillPassword("wrong123");

        // Click the sign in button
        loginPage.clickSignIn();

        // Wait for the page to process the request
        page.waitForLoadState();

        // Verify we're still on the login page (not redirected to promo page)
        assertThat(loginPage.isOnLoginPage())
                .as("Should remain on login page after failed login")
                .isTrue();

        // Verify an error message is displayed
        String errorMessage = loginPage.getErrorMessage();
        assertThat(errorMessage)
                .as("Error message should be displayed for invalid credentials")
                .isNotEmpty();

        // Verify the user is NOT logged in (no "Logged in as:" text)
        assertThat(page.locator("text=/Logged in as:/i").isVisible())
                .as("Should not show 'Logged in as:' after failed login")
                .isFalse();
    }
}
