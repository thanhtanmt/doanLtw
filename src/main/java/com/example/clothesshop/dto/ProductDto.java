package com.example.clothesshop.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {
    private Long id;
    private String name;
    private String brand;
    private String gender;
    private String description;
    private String detail;
    private String specification;
    private String material;
    private boolean active;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private int totalQuantity;
    private boolean hasStock;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Category info
    private Long categoryId;
    private String categoryName;
    
    // Seller info
    private Long sellerId;
    private String sellerName;
    
    // Images
    private List<ProductImageDto> images = new ArrayList<>();
    
    // Variants
    private List<ProductVariantDto> variants = new ArrayList<>();
    
    // Available sizes
    private List<String> availableSizes = new ArrayList<>();
    
    // Reviews statistics
    private int totalReviews;
    private double averageRating;
}
