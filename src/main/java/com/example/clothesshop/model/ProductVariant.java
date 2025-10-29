package com.example.clothesshop.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "product_variant")
public class ProductVariant extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(columnDefinition = "NVARCHAR(50)")
    private String size; // Kích thước (S, M, L, XL, XXL, etc.)

    @Column(nullable = false)
    private BigDecimal price; // Giá của variant này (có thể khác giá gốc)

    @Column(nullable = false)
    private Integer quantity; // Số lượng tồn kho

    @Column(columnDefinition = "NVARCHAR(50)")
    private String sku; // Mã SKU (Stock Keeping Unit) - mã định danh duy nhất

    @Column(columnDefinition = "NVARCHAR(500)")
    private String imageUrl; // Ảnh đại diện của variant (nếu có)

    private boolean available = true; // Còn hàng hay không

    // Relations
    @OneToMany(mappedBy = "variant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> cartItems = new ArrayList<>();

    // Helper methods
    public boolean isInStock() {
        return available && quantity > 0;
    }

    public void decreaseQuantity(int amount) {
        if (this.quantity >= amount) {
            this.quantity -= amount;
            if (this.quantity == 0) {
                this.available = false;
            }
        } else {
            throw new IllegalArgumentException("Không đủ số lượng trong kho");
        }
    }

    public void increaseQuantity(int amount) {
        this.quantity += amount;
        if (this.quantity > 0) {
            this.available = true;
        }
    }

    public String getDisplayName() {
        return String.format("Size %s", size);
    }
}
