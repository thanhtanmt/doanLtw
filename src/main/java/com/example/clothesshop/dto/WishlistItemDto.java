package com.example.clothesshop.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WishlistItemDto {
    private Long favoriteId;      // ID của Favorite (để xóa)
    private Long productId;       // ID của Product
    private String productName;   // Tên sản phẩm
    private String imageUrl;      // Hình ảnh
    private BigDecimal minPrice;  // Giá thấp nhất
    private boolean hasStock;     // Còn hàng hay không
    private LocalDateTime addedDate; // Ngày thêm (optional)
}
