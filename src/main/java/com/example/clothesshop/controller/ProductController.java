package com.example.clothesshop.controller;

import com.example.clothesshop.model.Product;
import com.example.clothesshop.repository.ProductRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ProductController {

    private final ProductRepository productRepository;

    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping("/products")
    public String products(Model model) {
        return "products";
    }

    @GetMapping("/product/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        try {
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + id));
            model.addAttribute("product", product);
        } catch (Exception e) {
            System.err.println("Error loading product: " + e.getMessage());
            return "redirect:/products";
        }
        return "product-detail";
    }

    @GetMapping("/search")
    public String searchResults(String query, Model model) {
        model.addAttribute("query", query);
        return "search-results";
    }
}
