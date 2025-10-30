package com.example.clothesshop.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "orders")
public class Order extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String orderCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipper_id")
    private User shipper;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucher_id")
    private Voucher voucher;
    
    @Column(name = "voucher_code")
    private String voucherCode;
    
    @Column(name = "discount_amount")
    private BigDecimal discountAmount;

    private BigDecimal totalPrice;
    
    @Column(name = "total_amount")
    private BigDecimal totalAmount;
    
    @Column(name = "cod_amount")
    private BigDecimal codAmount;
    
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    
    @Column(columnDefinition = "NVARCHAR(500)")
    private String failureReason;
    
    @Column(columnDefinition = "NVARCHAR(500)")
    private String address;
    
    @Column(name = "shipping_address", columnDefinition = "NVARCHAR(500)")
    private String shippingAddress;
    
    @Column(name = "shipping_phone")
    private String shippingPhone;
    
    @Column(name = "shipping_name", columnDefinition = "NVARCHAR(255)")
    private String shippingName;
    
    @Column(columnDefinition = "NVARCHAR(100)")
    private String paymentMethod;
    
    @Column(name = "delivered_date")
    private LocalDateTime deliveredDate;
    
    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;
    
    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;
    
    @Column(name = "delivery_notes", columnDefinition = "NVARCHAR(MAX)")
    private String deliveryNotes;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderDetail> orderDetails = new ArrayList<>();
}
