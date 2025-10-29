package com.example.clothesshop.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
public class Voucher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiscountType discountType; // PERCENTAGE, FIXED_AMOUNT

    @Column(nullable = false)
    private BigDecimal discountValue; // Giá trị giảm (% hoặc số tiền)

    private BigDecimal maxDiscount; // Giảm tối đa (cho % discount)

    @Column(nullable = false)
    private BigDecimal minOrderValue = BigDecimal.ZERO; // Đơn hàng tối thiểu

    @Column(nullable = false)
    private Integer totalQuantity = 0; // Tổng số lượng voucher

    @Column(nullable = false)
    private Integer usedQuantity = 0; // Số lượng đã dùng

    @Column(nullable = false)
    private Integer usageLimit = 1; // Số lần mỗi user được dùng

    @Column(nullable = false)
    private LocalDate startDate; // Ngày bắt đầu

    @Column(nullable = false)
    private LocalDate endDate; // Ngày kết thúc

    @Column(nullable = false, columnDefinition = "bit default 1")
    private boolean active = true; // Trạng thái kích hoạt

    @Enumerated(EnumType.STRING)
    private VoucherType type = VoucherType.ADMIN; // ADMIN, SELLER

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @ManyToMany(mappedBy = "vouchers")
    private List<Product> applicableProducts; // Sản phẩm áp dụng (null = all)

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

