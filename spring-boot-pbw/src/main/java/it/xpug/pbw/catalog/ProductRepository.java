// ABOUTME: Repository for accessing product data from the INVENTORY database table
// ABOUTME: Uses Spring JDBC Template for database queries
package it.xpug.pbw.catalog;

import it.xpug.pbw.domain.Product;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;

@Repository
public class ProductRepository {

    private final JdbcTemplate jdbcTemplate;

    public ProductRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Find a product by its inventory ID
     * @param inventoryId The product inventory ID (e.g., "T0003"), must not be null
     * @return Optional containing the Product if found, empty otherwise
     * @throws NullPointerException if inventoryId is null
     */
    public Optional<Product> findByInventoryId(String inventoryId) {
        Objects.requireNonNull(inventoryId, "inventoryId must not be null");

        String sql = "SELECT INVENTORYID, NAME, HEADING, DESCRIPTION, PKGINFO, IMAGE, " +
                    "PRICE, COST, QUANTITY, CATEGORY, NOTES, ISPUBLIC " +
                    "FROM INVENTORY WHERE INVENTORYID = ?";

        try {
            Product product = jdbcTemplate.queryForObject(sql, new ProductRowMapper(), inventoryId);
            return Optional.ofNullable(product);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    /**
     * RowMapper to convert database rows to Product objects
     */
    private static class ProductRowMapper implements RowMapper<Product> {
        @Override
        public Product mapRow(ResultSet rs, int rowNum) throws SQLException {
            Product product = new Product();
            product.setInventoryId(rs.getString("INVENTORYID"));
            product.setName(rs.getString("NAME"));
            product.setHeading(rs.getString("HEADING"));
            product.setDescription(rs.getString("DESCRIPTION"));
            product.setPkginfo(rs.getString("PKGINFO"));
            product.setImage(rs.getString("IMAGE"));
            product.setPrice(rs.getFloat("PRICE"));
            product.setCost(rs.getFloat("COST"));
            product.setQuantity(rs.getInt("QUANTITY"));
            product.setCategory(rs.getInt("CATEGORY"));
            product.setNotes(rs.getString("NOTES"));
            product.setPublic(rs.getInt("ISPUBLIC") == 1);
            return product;
        }
    }
}
