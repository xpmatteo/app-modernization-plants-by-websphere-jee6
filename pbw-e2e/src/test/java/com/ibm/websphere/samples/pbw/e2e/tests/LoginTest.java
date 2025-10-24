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
                        .setHeadless(false) // Set to false to see browser during test development
                        .setTimeout(3000) // 3 second timeout
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
    @Order(1)
    @DisplayName("Login page loads and form can be filled")
    void testLoginPageLoadsAndFormWorks() {
        // Navigate to login page
        loginPage.navigate(TestConfig.getBaseUrl());

        // Verify we're on the login page
        assertThat(loginPage.isOnLoginPage())
                .as("Should be on login page")
                .isTrue();

        // Verify page title loaded
        assertThat(page.title())
                .as("Page should have correct title")
                .contains("Plants By WebSphere");

        // Verify we can fill in the email field
        loginPage.fillEmail(TEST_USER_EMAIL);

        // Verify we can fill in the password field
        loginPage.fillPassword(TEST_USER_PASSWORD);

        // If we got here, the form is working correctly
        // (We're not testing actual login success since that requires valid test data)
    }

    @Test
    @Order(2)
    @DisplayName("Login form has sign in button")
    void testLoginFormHasSignInButton() {
        // Navigate to login page
        loginPage.navigate(TestConfig.getBaseUrl());

        // Verify the sign in button is visible
        assertThat(page.getByRole(AriaRole.IMG, new Page.GetByRoleOptions().setName("Sign in")).isVisible())
                .as("Sign in button should be visible")
                .isTrue();
    }

    @Test
    @Order(3)
    @DisplayName("User can successfully log in with valid credentials")
    void testSuccessfulLogin() {
        // Navigate directly to login page
        loginPage.navigate(TestConfig.getBaseUrl());

        // Verify we're on the login page
        assertThat(loginPage.isOnLoginPage())
                .as("Should be on login page")
                .isTrue();

        // Take a screenshot BEFORE filling the form
        page.screenshot(new Page.ScreenshotOptions().setPath(java.nio.file.Paths.get("target/login-before.png")));
        System.out.println("Screenshot before login saved to target/login-before.png");
        System.out.println("URL before login: " + page.url());

        // Fill in the login credentials
        loginPage.fillEmail(TEST_USER_EMAIL);
        loginPage.fillPassword(TEST_USER_PASSWORD);

        // Take a screenshot AFTER filling but BEFORE clicking
        page.screenshot(new Page.ScreenshotOptions().setPath(java.nio.file.Paths.get("target/login-filled.png")));
        System.out.println("Screenshot after filling form saved to target/login-filled.png");

        // Now click the sign in button
        loginPage.clickSignIn();

        // Wait for navigation to complete (JSF pages may take a moment)
        page.waitForLoadState();

        // Debug: Print page content to see what's happening
        System.out.println("Current URL after login: " + page.url());
        System.out.println("Page title: " + page.title());

        // Take a screenshot for debugging
        page.screenshot(new Page.ScreenshotOptions().setPath(java.nio.file.Paths.get("target/login-result.png")));
        System.out.println("Screenshot saved to target/login-result.png");

        // Check for JSF validation errors
        var validationErrors = page.locator("span[style*='color'][style*='ff0033'], span[style*='color'][style*='FF0033']");
        if (validationErrors.count() > 0) {
            System.out.println("Found validation errors:");
            for (int i = 0; i < validationErrors.count(); i++) {
                String errorText = validationErrors.nth(i).textContent();
                if (!errorText.trim().isEmpty()) {
                    System.out.println("  - " + errorText);
                }
            }
        }

        // Check if there's an error in the page content
        if (page.title().contains("Error")) {
            System.out.println("ERROR PAGE DETECTED!");
            System.out.println("Page content snippet:");
            System.out.println(page.content().substring(0, Math.min(500, page.content().length())));
        }

        // Check if there's an error message (for debugging)
        String errorMessage = loginPage.getErrorMessage();
        if (!errorMessage.isEmpty()) {
            throw new AssertionError("Login failed with error: " + errorMessage);
        }

        // After successful login, we should be redirected to the promo page
        assertThat(page.url())
                .as("Should be redirected to promo page after successful login")
                .contains("promo.jsf");

        // Verify the promo page loaded correctly by checking for the promo form
        assertThat(page.locator("form[id*='promo']").isVisible())
                .as("Promo page should be visible after login")
                .isTrue();
    }
}
