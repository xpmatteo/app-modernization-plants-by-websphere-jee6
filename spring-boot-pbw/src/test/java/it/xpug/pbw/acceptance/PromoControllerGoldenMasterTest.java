// ABOUTME: Golden master test for the promotional landing page HTML output
// ABOUTME: Ensures no regressions in the rendered HTML content of the promo page
package it.xpug.pbw.acceptance;

import it.xpug.pbw.promo.PromoController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PromoController.class)
public class PromoControllerGoldenMasterTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String GOLDEN_MASTER_PATH = "src/test/resources/golden-masters/";
    private static final String PROMO_GOLDEN_FILE = "promo-page.html";
    private static final String HOME_GOLDEN_FILE = "home-page.html";

    @Test
    public void promoPageShouldMatchGoldenMaster() throws Exception {
        String actualHtml = getPageHtml("/promo");
        assertMatchesGoldenMaster(actualHtml, PROMO_GOLDEN_FILE);
    }

    @Test
    public void homePageShouldMatchGoldenMaster() throws Exception {
        String actualHtml = getPageHtml("/");
        assertMatchesGoldenMaster(actualHtml, HOME_GOLDEN_FILE);
    }

    private String getPageHtml(String url) throws Exception {
        MvcResult result = mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andReturn();

        return result.getResponse().getContentAsString();
    }

    private void assertMatchesGoldenMaster(String actualHtml, String goldenFileName) throws IOException {
        Path goldenMasterPath = Paths.get(GOLDEN_MASTER_PATH + goldenFileName);

        // Normalize whitespace to avoid issues with different line endings and spacing
        String normalizedActual = normalizeHtml(actualHtml);

        if (!Files.exists(goldenMasterPath)) {
            // Create the golden master directory if it doesn't exist
            Files.createDirectories(goldenMasterPath.getParent());

            // Write the first time to create the golden master
            Files.write(goldenMasterPath, normalizedActual.getBytes());

            // Fail the test to indicate that the golden master was created
            throw new AssertionError("Golden master file created at: " + goldenMasterPath +
                    ". Please review the content and run the test again.");
        }

        String expectedHtml = Files.readString(goldenMasterPath);
        String normalizedExpected = normalizeHtml(expectedHtml);

        assertEquals(normalizedExpected, normalizedActual,
                "HTML output does not match golden master. " +
                "If this change is intentional, delete the golden master file and run the test again to regenerate it.");
    }

    private String normalizeHtml(String html) {
        return html
                // Remove extra whitespace between tags
                .replaceAll(">\\s+<", "><")
                // Normalize line endings
                .replaceAll("\\r\\n", "\n")
                .replaceAll("\\r", "\n")
                // Trim whitespace at start and end
                .trim();
    }
}
