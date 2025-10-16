// ABOUTME: Repository for accessing product images from the INVENTORY table IMGBYTES column
// ABOUTME: Retrieves binary image data stored in the database
package it.xpug.pbw.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ImageRepository {

    private final JdbcTemplate jdbcTemplate;

    public ImageRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Get image bytes for a product from the INVENTORY table
     * @param inventoryId The product inventory ID (e.g., "T0003")
     * @return byte array of image data, or null if not found or no image
     */
    public byte[] getImageBytes(String inventoryId) {
        if (inventoryId == null) {
            return null;
        }

        String sql = "SELECT IMGBYTES FROM INVENTORY WHERE INVENTORYID = ?";

        try {
            return jdbcTemplate.queryForObject(sql, byte[].class, inventoryId);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return null;
        }
    }
}
