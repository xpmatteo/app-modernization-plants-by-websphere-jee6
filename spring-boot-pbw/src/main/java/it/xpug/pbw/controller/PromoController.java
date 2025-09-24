// ABOUTME: Promotional controller for Spring Boot Plants by WebSphere application
// ABOUTME: Serves the /promo route using Mustache templates to render promotional content
package it.xpug.pbw.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PromoController {

    @GetMapping("/promo")
    public String promo(Model model) {
        model.addAttribute("message", "Hello by Plants by WebSphere");
        model.addAttribute("version", "Spring Boot Edition");
        return "promo";
    }
}