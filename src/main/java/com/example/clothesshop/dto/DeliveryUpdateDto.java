package com.example.clothesshop.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryUpdateDto {
    private String orderCode;
    private String status; // DELIVERED, FAILED
    private String deliveryNotes;
    private String failureReason;
}
