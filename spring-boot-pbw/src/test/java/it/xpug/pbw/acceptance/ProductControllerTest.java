// ABOUTME: Acceptance test for product detail page functionality
// ABOUTME: Integration test that verifies the full product page rendering with database access
package it.xpug.pbw.acceptance;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void shouldDisplayProductDetailsForValidItemID() throws Exception {
        // Acceptance test: User navigates to product page for Bonsai Tree (T0003)
        MvcResult result = mockMvc.perform(get("/product").param("itemID", "T0003"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/html"))
                // Verify product name
                .andExpect(content().string(containsString("Bonsai")))
                // Verify price
                .andExpect(content().string(containsString("$30.00")))
                // Verify package info
                .andExpect(content().string(containsString("0.5 gallon mature tree")))
                // Verify heading
                .andExpect(content().string(containsString("Tabletop Fun")))
                // Verify description contains key text
                .andExpect(content().string(containsString("miniature replicas")))
                // Verify image tag is present
                .andExpect(content().string(containsString("ImageServlet")))
                .andExpect(content().string(containsString("inventoryID=T0003")))
                .andReturn();
    }

    @Test
    public void shouldDisplayProductDetailsForStrawberries() throws Exception {
        // Acceptance test: User navigates to product page for Strawberries (V0006)
        mockMvc.perform(get("/product").param("itemID", "V0006"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Strawberries")))
                .andExpect(content().string(containsString("$3.50")))
                .andExpect(content().string(containsString("1 pkt. (50 seeds)")));
    }

    @Test
    public void shouldReturn404ForInvalidItemID() throws Exception {
        // Acceptance test: User tries to access non-existent product
        mockMvc.perform(get("/product").param("itemID", "INVALID"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldIncludeBreadcrumbNavigation() throws Exception {
        // Acceptance test: Product page should have navigation back to home/category
        mockMvc.perform(get("/product").param("itemID", "T0003"))
                .andExpect(status().isOk())
                // Should have a link back to home
                .andExpect(content().string(containsString("Home")));
    }
}
