package com.example.clothesshop.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Getter
@Setter
@Table(name = "order_details")
public class OrderDetail {
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
    @JoinColumn(name = "variant_id")
    private ProductVariant variant;

    private Integer quantity;
    
    @Column(name = "unit_price")
    private BigDecimal unitPrice;
    
    @Column(name = "total_price")
    private BigDecimal totalPrice;

    @Column(columnDefinition = "NVARCHAR(50)")
    private String sizeAtOrder;
}
