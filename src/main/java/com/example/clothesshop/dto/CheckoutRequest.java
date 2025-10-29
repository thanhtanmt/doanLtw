package com.example.clothesshop.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CheckoutRequest {
    private String shippingName;
    private String shippingPhone;
    private String shippingAddress;
    private String paymentMethod; // "COD" or "WALLET"
    private String notes;
    private BigDecimal totalAmount;
    private String voucherCode; // optional voucher code
}
