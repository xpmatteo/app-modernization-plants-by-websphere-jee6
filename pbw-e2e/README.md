# Plants By WebSphere E2E Tests

End-to-end tests for the Plants By WebSphere application using Playwright for Java.

## Overview

This module contains E2E tests that verify critical user journeys through browser automation. The tests use:

- **Playwright for Java** - Browser automation framework
- **JUnit 5** - Test framework
- **AssertJ** - Fluent assertions
- **Page Object Model** - Design pattern for maintainability

## Test Philosophy

These tests follow resilient testing patterns to minimize fragility:

1. **Semantic Selectors**: Use labels, roles, and visible text instead of CSS classes or generated IDs
2. **Outcome-Based Assertions**: Test behavior (e.g., "user is logged in") not implementation details
3. **Page Object Model**: Centralize selectors so changes only need updates in one place
4. **Test Isolation**: Each test runs in a fresh browser context

## Prerequisites

1. **Java 21** - Required for running tests
2. **Maven 3.x** - Build tool
3. **Docker & Docker Compose** - For running the application
4. **Running Application** - The app must be running before tests execute

## Running the Application

Before running tests, start the application:

```bash
# From project root directory
cd ..

# Start MariaDB + Liberty
docker-compose up -d

# Wait for services to be ready (about 30 seconds)
docker-compose logs -f liberty

# Populate the database (required on first run)
curl "http://localhost:9080/servlet/AdminServlet?admintype=populate"
```

Verify the application is accessible at http://localhost:9080

## Running the Tests

### Option 1: Run from Maven (Recommended)

```bash
# From project root
mvn verify -pl pbw-e2e

# Or from pbw-e2e directory
cd pbw-e2e
mvn verify
```

### Option 2: Run from IDE

1. Open the project in your IDE (IntelliJ IDEA, Eclipse, VS Code)
2. Navigate to `src/test/java/com/ibm/websphere/samples/pbw/e2e/tests/LoginTest.java`
3. Right-click and select "Run LoginTest"

### First Time Setup

The first time you run tests, Playwright will download browser binaries:

```bash
# From pbw-e2e directory
mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install chromium"
```

## Test Configuration

Tests can be configured via system properties:

| Property | Default | Description |
|----------|---------|-------------|
| `app.baseUrl` | `http://localhost:9080` | Base URL of the application |

### Override Base URL

```bash
# Test against different environment
mvn verify -Dapp.baseUrl=http://staging.example.com:8080
```

## Test Structure

```
pbw-e2e/
├── src/test/java/
│   └── com/ibm/websphere/samples/pbw/e2e/
│       ├── pages/           # Page Object Models
│       │   └── LoginPage.java
│       ├── tests/           # Test classes
│       │   └── LoginTest.java
│       └── utils/           # Test utilities
│           ├── TestConfig.java
│           └── DatabaseHelper.java
└── pom.xml
```

## Current Test Coverage

### Login Flow Tests (`LoginTest.java`)

✅ **Successful Login** - Verifies user can log in with valid credentials
✅ **Login Validation** - Verifies error message appears for invalid credentials

**Test User Credentials** (from `pbw.properties`):
- Email: `plants@plantsbywebsphere.ibm.com`
- Password: `plants`

## Writing New Tests

### 1. Create a Page Object

```java
// src/test/java/.../pages/CartPage.java
public class CartPage {
    private final Page page;

    public CartPage(Page page) {
        this.page = page;
    }

    public void navigate(String baseUrl) {
        page.navigate(baseUrl + "/cart.xhtml");
    }

    public void clickCheckout() {
        // Use semantic selector - resilient to UI changes
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions()
            .setName("Checkout")).click();
    }
}
```

### 2. Write a Test

```java
@Test
void testAddToCart() {
    // Arrange
    ProductPage productPage = new ProductPage(page);
    productPage.navigate(TestConfig.getBaseUrl());

    // Act
    productPage.addToCart("African Orchid", 2);

    // Assert - test outcome, not implementation
    assertThat(page.url()).contains("cart.xhtml");
    assertThat(page.getByText("African Orchid")).isVisible();
}
```

## Resilient Selector Patterns

### ✅ Good - Semantic Selectors

```java
// Use labels (users see these)
page.getByLabel("Email Address").fill("user@example.com");

// Use roles (accessibility-friendly)
page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions()
    .setName("Sign in")).click();

// Use visible text
page.getByText("Welcome back!").isVisible();
```

### ❌ Avoid - Brittle Selectors

```java
// CSS classes change
page.locator(".btn-primary-submit-v2").click();

// Generated IDs change
page.locator("#form\\:j_id123").fill("text");
```

## Troubleshooting

### Tests fail with "Application is not reachable"

Ensure the application is running:
```bash
docker-compose ps
curl http://localhost:9080
```

### Tests fail with "Failed to populate database"

Manually populate the database:
```bash
curl "http://localhost:9080/servlet/AdminServlet?admintype=populate"
```

### Browser doesn't launch

Install Playwright browsers:
```bash
mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install"
```

### Tests are flaky

1. Check for timing issues - add explicit waits:
   ```java
   page.waitForURL("**/cart.xhtml");
   ```

2. Ensure test isolation - verify `@BeforeEach` creates new context

3. Check database state - ensure populate runs before tests

### Want to see the browser

Set headless to false in test:
```java
browser = playwright.chromium().launch(
    new BrowserType.LaunchOptions().setHeadless(false)
);
```

## CI/CD Integration

### Jenkins/GitLab CI

```yaml
test:
  script:
    - docker-compose up -d
    - sleep 30
    - curl "http://localhost:9080/servlet/AdminServlet?admintype=populate"
    - mvn verify -pl pbw-e2e
  after_script:
    - docker-compose down
```

## Future Test Ideas

- **Shopping Flow**: Browse → Add to cart → Update quantity → Checkout
- **Registration**: Create new account
- **Order Completion**: Complete full purchase flow
- **Visual Regression**: Screenshot comparison tests
- **Accessibility**: Automated a11y scanning with axe-core

## Resources

- [Playwright Java Documentation](https://playwright.dev/java/)
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [AssertJ Documentation](https://assertj.github.io/doc/)
- [Page Object Model Pattern](https://playwright.dev/java/docs/pom)
