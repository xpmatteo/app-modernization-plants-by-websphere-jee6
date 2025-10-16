// ABOUTME: Unit tests for ProductRepository database access layer
// ABOUTME: Tests JDBC queries for fetching product data from INVENTORY table
package it.xpug.pbw.repository;

import it.xpug.pbw.domain.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

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
        Optional<Product> result = productRepository.findByInventoryId(inventoryId);

        // Assert
        assertTrue(result.isPresent(), "Product should be found");
        Product product = result.get();
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
        Optional<Product> result = productRepository.findByInventoryId(inventoryId);

        // Assert
        assertTrue(result.isPresent(), "Product should be found");
        Product product = result.get();
        assertEquals("V0006", product.getInventoryId());
        assertEquals("Strawberries", product.getName());
        assertEquals(3.50f, product.getPrice(), 0.01f);
        assertEquals("1 pkt. (50 seeds)", product.getPkginfo());
        assertEquals(1, product.getCategory()); // Fruits & Vegetables category
    }

    @Test
    public void shouldReturnEmptyForInvalidInventoryId() {
        // Arrange: Invalid product ID
        String inventoryId = "INVALID123";

        // Act
        Optional<Product> result = productRepository.findByInventoryId(inventoryId);

        // Assert
        assertFalse(result.isPresent(), "Should return empty Optional for non-existent product");
    }

    @Test
    public void shouldReturnEmptyForNullInventoryId() {
        // Arrange: Null input
        String inventoryId = null;

        // Act
        Optional<Product> result = productRepository.findByInventoryId(inventoryId);

        // Assert
        assertFalse(result.isPresent(), "Should handle null input gracefully");
    }
}
