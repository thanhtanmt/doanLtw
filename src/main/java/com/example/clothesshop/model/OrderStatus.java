package com.example.clothesshop.model;

public enum OrderStatus {
    PENDING,            // chờ xác nhận
    CONFIRMED,          // đã xác nhận
    ASSIGNED,           // đã giao cho shipper
    DELIVERING,         // đang giao
    DELIVERED,          // giao thành công
    FAILED,             // giao thất bại
    CANCELED            // hủy
}
