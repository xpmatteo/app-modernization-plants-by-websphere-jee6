// ABOUTME: Unit tests for ProductController with mocked repository
// ABOUTME: Tests controller logic for handling product requests without database access
package it.xpug.pbw.catalog;

import it.xpug.pbw.domain.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductRepository productRepository;

    @Test
    public void shouldDisplayProductWhenFound() throws Exception {
        // Arrange: Mock repository to return a product
        Product bonsai = new Product("T0003", "Bonsai", "Tabletop Fun",
                "Bonsais are great miniature replicas of your favorite yard tree.",
                "0.5 gallon mature tree", "trees_bonsai.jpg",
                30.00f, 12.00f, 100, 2, "NOTES and stuff", true);

        when(productRepository.findByInventoryId("T0003")).thenReturn(Optional.of(bonsai));

        // Act & Assert
        mockMvc.perform(get("/product").param("itemID", "T0003"))
                .andExpect(status().isOk())
                .andExpect(view().name("product"))
                .andExpect(model().attributeExists("product"))
                .andExpect(model().attribute("title", "Plants By WebSphere Product Detail"));
    }

    @Test
    public void shouldReturn404WhenProductNotFound() throws Exception {
        // Arrange: Mock repository to return null
        when(productRepository.findByInventoryId("INVALID")).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/product").param("itemID", "INVALID"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldHandleMissingItemIDParameter() throws Exception {
        // Act & Assert: Request without itemID parameter should return 400 Bad Request
        mockMvc.perform(get("/product"))
                .andExpect(status().isBadRequest());
    }
}
