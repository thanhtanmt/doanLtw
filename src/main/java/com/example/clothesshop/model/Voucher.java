package com.example.clothesshop.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
public class Voucher extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String code;

    private BigDecimal discountPercent;
    private BigDecimal maxDiscount;
    private LocalDate expiryDate;
    private boolean active = true;

    @Enumerated(EnumType.STRING)
    private VoucherType type; // ADMIN, SELLER

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToMany(mappedBy = "vouchers")
    private List<Product> products;
}

enum VoucherType {
    ADMIN,
    SELLER
}
