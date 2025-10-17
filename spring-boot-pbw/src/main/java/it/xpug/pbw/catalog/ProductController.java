// ABOUTME: Controller for the product detail page display
// ABOUTME: Handles GET requests to /product with itemID parameter and renders product details
package it.xpug.pbw.catalog;

import it.xpug.pbw.domain.Product;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class ProductController {

    private final ProductRepository productRepository;

    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping("/product")
    public String showProduct(@RequestParam String itemID, Model model) {
        Product product = productRepository.findByInventoryId(itemID)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        model.addAttribute("product", product);
        model.addAttribute("title", "Plants By WebSphere Product Detail");

        // Shopping cart data - for now using static data (no cart functionality)
        model.addAttribute("cartEmpty", true);
        model.addAttribute("cartNotEmpty", false);
        model.addAttribute("cartSize", 0);
        model.addAttribute("cartTotal", "$0.00");

        return "product";
    }
}
