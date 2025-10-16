// ABOUTME: Unit tests for ProductRepository database access layer
// ABOUTME: Tests JDBC queries for fetching product data from INVENTORY table
package it.xpug.pbw.repository;

import it.xpug.pbw.domain.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    public void shouldFindBonsaiTreeByInventoryId() {
        // Arrange: Known product in database (T0003 = Bonsai)
        String inventoryId = "T0003";

        // Act
        Product product = productRepository.findByInventoryId(inventoryId);

        // Assert
        assertNotNull(product, "Product should be found");
        assertEquals("T0003", product.getInventoryId());
        assertEquals("Bonsai", product.getName());
        assertEquals("Tabletop Fun", product.getHeading());
        assertEquals(30.00f, product.getPrice(), 0.01f);
        assertEquals("0.5 gallon mature tree", product.getPkginfo());
        assertEquals(2, product.getCategory()); // Trees category
        assertTrue(product.getDescription().contains("miniature replicas"));
    }

    @Test
    public void shouldFindStrawberriesByInventoryId() {
        // Arrange: Known product in database (V0006 = Strawberries)
        String inventoryId = "V0006";

        // Act
        Product product = productRepository.findByInventoryId(inventoryId);

        // Assert
        assertNotNull(product, "Product should be found");
        assertEquals("V0006", product.getInventoryId());
        assertEquals("Strawberries", product.getName());
        assertEquals(3.50f, product.getPrice(), 0.01f);
        assertEquals("1 pkt. (50 seeds)", product.getPkginfo());
        assertEquals(1, product.getCategory()); // Fruits & Vegetables category
    }

    @Test
    public void shouldReturnNullForInvalidInventoryId() {
        // Arrange: Invalid product ID
        String inventoryId = "INVALID123";

        // Act
        Product product = productRepository.findByInventoryId(inventoryId);

        // Assert
        assertNull(product, "Should return null for non-existent product");
    }

    @Test
    public void shouldReturnNullForNullInventoryId() {
        // Arrange: Null input
        String inventoryId = null;

        // Act
        Product product = productRepository.findByInventoryId(inventoryId);

        // Assert
        assertNull(product, "Should handle null input gracefully");
    }
}
