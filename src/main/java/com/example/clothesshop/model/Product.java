package com.example.clothesshop.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;

@Entity
@Getter
@Setter
public class Product extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String name;
    
    @Column(columnDefinition = "NVARCHAR(100)")
    private String brand; // Thương hiệu
    
    @Column(columnDefinition = "NVARCHAR(50)")
    private String gender; // Nam/Nữ/Unisex
    
    @Lob
    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String description; // Giới thiệu ngắn
    
    @Lob
    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String detail; // Mô tả chi tiết
    
    @Lob
    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String specification; // Thông số kỹ thuật (chất liệu, hướng dẫn bảo quản, etc.)
    
    @Column(columnDefinition = "NVARCHAR(100)")
    private String material; // Chất liệu (Cotton, Polyester, etc.)
    
    private boolean active = true; // Sản phẩm còn kinh doanh hay không

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id")
    private User seller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductVariant> variants = new ArrayList<>();

    @OneToMany(mappedBy = "product")
    private List<Review> reviews = new ArrayList<>();

    @OneToMany(mappedBy = "product")
    private List<Favorite> favorites = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> images = new ArrayList<>();

    @ManyToMany
    @JoinTable(
        name = "product_voucher",
        joinColumns = @JoinColumn(name = "product_id"),
        inverseJoinColumns = @JoinColumn(name = "voucher_id")
    )
    private List<Voucher> vouchers = new ArrayList<>();

    // Helper methods
    public BigDecimal getMinPrice() {
        return variants.stream()
            .map(ProductVariant::getPrice)
            .min(BigDecimal::compareTo)
            .orElse(BigDecimal.ZERO);
    }

    public BigDecimal getMaxPrice() {
        return variants.stream()
            .map(ProductVariant::getPrice)
            .max(BigDecimal::compareTo)
            .orElse(BigDecimal.ZERO);
    }

    public int getTotalQuantity() {
        return variants.stream()
            .mapToInt(ProductVariant::getQuantity)
            .sum();
    }

    public boolean hasStock() {
        return variants.stream().anyMatch(ProductVariant::isInStock);
    }

    public List<String> getAvailableSizes() {
        return variants.stream()
            .map(ProductVariant::getSize)
            .distinct()
            .toList();
    }
}