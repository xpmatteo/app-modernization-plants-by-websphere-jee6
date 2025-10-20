// ABOUTME: Controller for serving product images from the database IMGBYTES column
// ABOUTME: Mimics legacy /servlet/ImageServlet URL pattern for compatibility
package it.xpug.pbw.catalog;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ImageController {

    private final ImageRepository imageRepository;

    public ImageController(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    /**
     * Serve product images from database
     * URL pattern: /servlet/ImageServlet?action=getimage&inventoryID=T0003
     * This mimics the legacy JSF ImageServlet for compatibility
     */
    @GetMapping("/servlet/ImageServlet")
    public ResponseEntity<byte[]> getImage(
            @RequestParam(required = false, defaultValue = "getimage") String action,
            @RequestParam String inventoryID) {

        return imageRepository.getImageBytes(inventoryID)
                .filter(bytes -> bytes.length > 0)
                .map(bytes -> ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .body(bytes))
                .orElse(ResponseEntity.notFound().build());
    }
}
