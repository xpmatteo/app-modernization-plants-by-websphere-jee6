// ABOUTME: Controller for the promotional/landing page featuring plant specials and tips
// ABOUTME: Handles the main promo page display and navigation to product details
package it.xpug.pbw.promo;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PromoController {

    @GetMapping("/promo")
    public String promo(Model model) {
        // Set page title
        model.addAttribute("title", "Plants By WebSphere Promo");

        // Shopping cart data - for now using static data
        model.addAttribute("cartEmpty", true);
        model.addAttribute("cartNotEmpty", false);
        model.addAttribute("cartSize", 0);
        model.addAttribute("cartTotal", "$0.00");

        return "promo";
    }

    @GetMapping("/")
    public String home(Model model) {
        return promo(model);  // Redirect home to promo page
    }
}
