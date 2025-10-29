package com.example.clothesshop.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantDto {
    private Long id;
    private String size;
    private BigDecimal price;
    private Integer quantity;
    private String sku;
    private String imageUrl;
    private boolean available;
    private boolean inStock;
}
