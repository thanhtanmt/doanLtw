package com.example.clothesshop.dto;

import lombok.Data;

@Data
public class AddToCartRequest {
    private Long variantId;
    private int quantity;
}
