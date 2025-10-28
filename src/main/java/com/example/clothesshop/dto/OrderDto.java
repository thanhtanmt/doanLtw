package com.example.clothesshop.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {
    private Long id;
    private String orderCode;
    private Long userId;
    private String userName;
    private String shippingAddress;
    private String shippingPhone;
    private String shippingName;
    private String status;
    private String paymentMethod;
    private BigDecimal totalAmount;
    private BigDecimal codAmount;
    private Long shipperId;
    private String shipperName;
    private LocalDateTime assignedAt;
    private LocalDateTime deliveredAt;
    private String deliveryNotes;
    private String failureReason;
    private LocalDateTime createdAt;
    private List<OrderDetailDto> orderDetails;
}
