package com.example.clothesshop.controller;

import com.example.clothesshop.model.Product;
import com.example.clothesshop.repository.ProductRepository;
import com.example.clothesshop.service.ReviewService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;

@Controller
public class ProductController {

    private final ProductRepository productRepository;
    private final ReviewService reviewService;

    public ProductController(ProductRepository productRepository, ReviewService reviewService) {
        this.productRepository = productRepository;
        this.reviewService = reviewService;
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
            
            // Load reviews
            List<Map<String, Object>> reviews = reviewService.getProductReviews(id);
            Map<String, Object> reviewStats = reviewService.getProductReviewStats(id);
            
            model.addAttribute("reviews", reviews);
            model.addAttribute("reviewStats", reviewStats);
            
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
