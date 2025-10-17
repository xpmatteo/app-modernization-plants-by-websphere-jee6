// ABOUTME: Unit tests for ImageController serving product images from database
// ABOUTME: Tests image retrieval from INVENTORY.IMGBYTES column via servlet-style URL
package it.xpug.pbw.catalog;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ImageController.class)
public class ImageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ImageRepository imageRepository;

    @Test
    public void shouldReturnImageBytesForValidProduct() throws Exception {
        // Arrange: Mock repository to return image bytes
        byte[] mockImageBytes = new byte[]{1, 2, 3, 4, 5}; // Fake image data
        when(imageRepository.getImageBytes("T0003")).thenReturn(mockImageBytes);

        // Act & Assert
        mockMvc.perform(get("/servlet/ImageServlet")
                        .param("action", "getimage")
                        .param("inventoryID", "T0003"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/jpeg"))
                .andExpect(content().bytes(mockImageBytes));
    }

    @Test
    public void shouldReturn404WhenImageNotFound() throws Exception {
        // Arrange: Mock repository to return null (no image)
        when(imageRepository.getImageBytes("INVALID")).thenReturn(null);

        // Act & Assert
        mockMvc.perform(get("/servlet/ImageServlet")
                        .param("action", "getimage")
                        .param("inventoryID", "INVALID"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldHandleMissingInventoryIDParameter() throws Exception {
        // Act & Assert: Request without inventoryID parameter should return 400
        mockMvc.perform(get("/servlet/ImageServlet")
                        .param("action", "getimage"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldWorkWithoutActionParameter() throws Exception {
        // Arrange: action parameter is optional, defaults to "getimage"
        byte[] mockImageBytes = new byte[]{10, 20, 30};
        when(imageRepository.getImageBytes("V0006")).thenReturn(mockImageBytes);

        // Act & Assert
        mockMvc.perform(get("/servlet/ImageServlet")
                        .param("inventoryID", "V0006"))
                .andExpect(status().isOk())
                .andExpect(content().bytes(mockImageBytes));
    }
}
