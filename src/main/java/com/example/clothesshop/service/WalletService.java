package com.example.clothesshop.service;

import com.example.clothesshop.model.Wallet;
import com.example.clothesshop.model.WalletTransaction;
import com.example.clothesshop.model.User;
import com.example.clothesshop.model.Order;

import java.math.BigDecimal;
import java.util.List;

public interface WalletService {
    /**
     * Lấy hoặc tạo ví cho user
     */
    Wallet getOrCreateWallet(User user);
    
    /**
     * Lấy số dư ví
     */
    BigDecimal getBalance(User user);
    
    /**
     * Nạp tiền vào ví
     */
    WalletTransaction deposit(User user, BigDecimal amount, String description);
    
    /**
     * Trừ tiền từ ví (thanh toán)
     */
    WalletTransaction withdraw(User user, BigDecimal amount, String description, Order order);
    
    /**
     * Hoàn tiền vào ví
     */
    WalletTransaction refund(User user, BigDecimal amount, String description, Order order);
    
    /**
     * Kiểm tra user có đủ tiền không
     */
    boolean canAfford(User user, BigDecimal amount);
    
    /**
     * Lấy lịch sử giao dịch
     */
    List<WalletTransaction> getTransactionHistory(User user);
    
    /**
     * Lấy ví theo user ID
     */
    Wallet getWalletByUserId(Long userId);
}
