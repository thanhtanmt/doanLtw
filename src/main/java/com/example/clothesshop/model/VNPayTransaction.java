package com.example.clothesshop.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Model lưu thông tin giao dịch VNPay
 */
@Entity
@Getter
@Setter
@Table(name = "vnpay_transaction")
public class VNPayTransaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "txn_ref", unique = true, nullable = false, length = 100)
    private String txnRef; // Mã giao dịch
    
    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount; // Số tiền
    
    @Column(name = "order_info", columnDefinition = "NVARCHAR(500)")
    private String orderInfo; // Thông tin đơn hàng
    
    @Column(name = "bank_code", length = 20)
    private String bankCode; // Mã ngân hàng
    
    @Column(name = "transaction_no", length = 50)
    private String transactionNo; // Mã giao dịch VNPay
    
    @Column(name = "card_type", length = 20)
    private String cardType; // Loại thẻ
    
    @Column(name = "pay_date")
    private LocalDateTime payDate; // Thời gian thanh toán
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TransactionStatus status; // Trạng thái
    
    @Column(name = "response_code", length = 10)
    private String responseCode; // Mã phản hồi từ VNPay
    
    @Column(name = "response_message", columnDefinition = "NVARCHAR(500)")
    private String responseMessage; // Thông báo phản hồi
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    public enum TransactionStatus {
        PENDING,    // Chờ thanh toán
        SUCCESS,    // Thành công
        FAILED,     // Thất bại
        CANCELLED   // Đã hủy
    }
}
