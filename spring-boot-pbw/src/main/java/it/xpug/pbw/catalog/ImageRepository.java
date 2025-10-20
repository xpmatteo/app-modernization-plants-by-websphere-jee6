// ABOUTME: Repository for accessing product images from the INVENTORY table IMGBYTES column
// ABOUTME: Retrieves binary image data stored in the database
package it.xpug.pbw.catalog;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class ImageRepository {

    private final JdbcTemplate jdbcTemplate;

    public ImageRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Get image bytes for a product from the INVENTORY table
     * @param inventoryId The product inventory ID (e.g., "T0003")
     * @return Optional containing byte array of image data, or empty if not found or no image
     */
    public Optional<byte[]> getImageBytes(String inventoryId) {
        if (inventoryId == null) {
            return Optional.empty();
        }

        String sql = "SELECT IMGBYTES FROM INVENTORY WHERE INVENTORYID = ?";

        try {
            byte[] imageBytes = jdbcTemplate.queryForObject(sql, byte[].class, inventoryId);
            return Optional.ofNullable(imageBytes);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
