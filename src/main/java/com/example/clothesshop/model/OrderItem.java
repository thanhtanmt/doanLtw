package com.example.clothesshop.model;

import java.math.BigDecimal;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant variant;

    private int quantity;
    private BigDecimal unitPrice;

    @Column(columnDefinition = "NVARCHAR(50)")
    private String sizeAtOrder; // Lưu kích thước tại thời điểm đặt hàng

    // Helper methods
    public BigDecimal getSubtotal() {
        return unitPrice.multiply(new BigDecimal(quantity));
    }

    public String getVariantInfo() {
        return String.format("Size %s", sizeAtOrder);
    }
}
