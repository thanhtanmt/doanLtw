package com.example.clothesshop.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShipperStatsDto {
    private Long totalPendingOrders;
    private Long totalDeliveredOrders;
    private Long totalFailedOrders;
    private Double estimatedIncome;
    private Double rating;
    private LocalDateTime lastDeliveryDate;
}

