package com.example.clothesshop.service;

import com.example.clothesshop.model.User;
import com.example.clothesshop.model.VNPayTransaction;
import jakarta.servlet.http.HttpServletRequest;

import java.math.BigDecimal;
import java.util.Map;

public interface VNPayService {
    
    /**
     * Tạo URL thanh toán VNPay
     */
    String createPaymentUrl(User user, BigDecimal amount, String orderInfo, HttpServletRequest request) throws Exception;
    
    /**
     * Xử lý kết quả thanh toán từ VNPay
     */
    VNPayTransaction processPaymentReturn(Map<String, String> params);
    
    /**
     * Lấy giao dịch theo mã
     */
    VNPayTransaction getTransactionByTxnRef(String txnRef);
}
