// ABOUTME: Page Object Model for the login page
// ABOUTME: Provides methods to interact with login form using resilient selectors
package com.ibm.websphere.samples.pbw.e2e.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

/**
 * Page Object Model for the Login page.
 * Uses semantic selectors (labels, roles, text) for resilience against UI changes.
 */
public class LoginPage {
    private final Page page;

    public LoginPage(Page page) {
        this.page = page;
    }

    /**
     * Navigate to the login page
     */
    public void navigate(String baseUrl) {
        // JSF pages must be accessed via .jsf extension, not .xhtml
        page.navigate(baseUrl + "/login.jsf");
        // Wait for the page to be fully loaded (JSF pages may need this)
        page.waitForLoadState();
    }

    /**
     * Fill in the email field
     */
    public void fillEmail(String email) {
        // The label has "for='email'" but JSF transforms it to "login:email"
        // Use a flexible locator that works with the actual rendered HTML
        Locator emailInput = page.locator("input[id*='email'][type='text']");
        emailInput.fill(email);
    }

    /**
     * Fill in the password field
     */
    public void fillPassword(String password) {
        // Find the password input field
        Locator passwordInput = page.locator("input[id*='password'][type='password']");
        passwordInput.fill(password);
    }

    /**
     * Click the Sign In button
     */
    public void clickSignIn() {
        // The sign in button is an image with alt text "Sign in"
        page.getByRole(AriaRole.IMG, new Page.GetByRoleOptions().setName("Sign in")).click();
    }

    /**
     * Perform complete login flow
     */
    public void login(String email, String password) {
        fillEmail(email);
        fillPassword(password);
        clickSignIn();
    }

    /**
     * Get the error message displayed on the page
     */
    public String getErrorMessage() {
        // JSF outputs error messages in the outputText with the message binding
        Locator errorLocator = page.locator("span[style*='color'][style*='ff0033']");
        if (errorLocator.isVisible()) {
            return errorLocator.textContent();
        }
        return "";
    }

    /**
     * Check if we're on the login page
     */
    public boolean isOnLoginPage() {
        return page.url().contains("login.jsf") || page.url().contains("login.xhtml");
    }

    /**
     * Click the register link
     */
    public void clickRegisterLink() {
        page.getByText("register for your own account here").click();
    }
}
