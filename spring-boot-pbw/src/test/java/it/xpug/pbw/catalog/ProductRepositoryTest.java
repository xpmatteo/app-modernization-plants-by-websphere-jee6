// ABOUTME: Unit tests for ProductRepository database access layer
// ABOUTME: Tests JDBC queries for fetching product data from INVENTORY table
package it.xpug.pbw.catalog;

import it.xpug.pbw.datasource.TestDataSource;
import it.xpug.pbw.domain.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

public class ProductRepositoryTest {

    private ProductRepository productRepository;

    @BeforeEach
    public void setUp() {
        JdbcTemplate jdbcTemplate = TestDataSource.createJdbcTemplate();
        productRepository = new ProductRepository(jdbcTemplate);
    }

    @Test
    public void shouldFindBonsaiTreeByInventoryId() {
        // Arrange: Known product in database (T0003 = Bonsai)
        String inventoryId = "T0003";

        // Act
        Optional<Product> result = productRepository.findByInventoryId(inventoryId);

        // Assert
        assertThat(result).isPresent();
        Product product = result.get();
        assertThat(product.getInventoryId()).isEqualTo("T0003");
        assertThat(product.getName()).isEqualTo("Bonsai");
        assertThat(product.getHeading()).isEqualTo("Tabletop Fun");
        assertThat(product.getPrice()).isCloseTo(30.00f, within(0.01f));
        assertThat(product.getPkginfo()).isEqualTo("0.5 gallon mature tree");
        assertThat(product.getCategory()).isEqualTo(2); // Trees category
        assertThat(product.getDescription()).contains("miniature replicas");
    }

    @Test
    public void shouldFindStrawberriesByInventoryId() {
        // Arrange: Known product in database (V0006 = Strawberries)
        String inventoryId = "V0006";

        // Act
        Optional<Product> result = productRepository.findByInventoryId(inventoryId);

        // Assert
        assertThat(result).isPresent();
        Product product = result.get();
        assertThat(product.getInventoryId()).isEqualTo("V0006");
        assertThat(product.getName()).isEqualTo("Strawberries");
        assertThat(product.getPrice()).isCloseTo(3.50f, within(0.01f));
        assertThat(product.getPkginfo()).isEqualTo("1 pkt. (50 seeds)");
        assertThat(product.getCategory()).isEqualTo(1); // Fruits & Vegetables category
    }

    @Test
    public void shouldReturnEmptyForInvalidInventoryId() {
        // Arrange: Invalid product ID
        String inventoryId = "INVALID123";

        // Act
        Optional<Product> result = productRepository.findByInventoryId(inventoryId);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    public void shouldThrowNullPointerExceptionForNullInventoryId() {
        // Arrange: Null input
        String inventoryId = null;

        // Act & Assert
        assertThatThrownBy(() -> productRepository.findByInventoryId(inventoryId))
                .isInstanceOf(NullPointerException.class);
    }
}
