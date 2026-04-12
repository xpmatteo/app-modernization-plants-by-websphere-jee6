// ABOUTME: Approved-fixture tests for the legacy JSF app using Playwright aria snapshots.
// ABOUTME: Requires the legacy app to be running at http://localhost:9080 (see `make restart`).
package it.xpug.pbw.acceptance;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.LoadState;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class LegacyPromoPageSnapshotTest {

    private static final String LEGACY_URL = "http://localhost:9080/promo.jsf";
    private static final Path FIXTURE_DIR = Paths.get("src/test/resources/fixtures");

    private static Playwright playwright;
    private static Browser browser;

    @BeforeAll
    static void launchBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch();
    }

    @AfterAll
    static void closeBrowser() {
        browser.close();
        playwright.close();
    }

    @Test
    public void promoPageMatchesApprovedSnapshot() throws IOException {
        Page page = browser.newPage();
        page.navigate(LEGACY_URL);

        assertMatchesFixture(page.locator("body").ariaSnapshot(), "legacy-promo.approved.yaml");
    }

    @Test
    public void bonsaiTreeProductPageMatchesApprovedSnapshot() throws IOException {
        Page page = browser.newPage();
        page.navigate(LEGACY_URL);
        page.getByRole(AriaRole.LINK,
                new Page.GetByRoleOptions().setName("Bonsai Tree $30.00 each")).click();
        page.waitForLoadState(LoadState.NETWORKIDLE);

        assertMatchesFixture(page.locator("body").ariaSnapshot(), "legacy-bonsai-product.approved.yaml");
    }

    private void assertMatchesFixture(String actual, String fixtureName) throws IOException {
        Path fixture = FIXTURE_DIR.resolve(fixtureName);

        if (!Files.exists(fixture)) {
            Files.createDirectories(fixture.getParent());
            Files.writeString(fixture, actual);
            fail("Approved fixture created at " + fixture + ". Review it and re-run the test.");
        }

        String expected = Files.readString(fixture);
        assertThat(actual)
                .as("Aria snapshot does not match %s. "
                        + "If the change is intentional, delete the fixture and re-run.", fixture)
                .isEqualTo(expected);
    }
}
