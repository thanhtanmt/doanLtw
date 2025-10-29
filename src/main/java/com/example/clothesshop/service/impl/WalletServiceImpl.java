package com.example.clothesshop.service.impl;

import com.example.clothesshop.model.Wallet;
import com.example.clothesshop.model.WalletTransaction;
import com.example.clothesshop.model.WalletTransaction.TransactionType;
import com.example.clothesshop.model.User;
import com.example.clothesshop.model.Order;
import com.example.clothesshop.repository.WalletRepository;
import com.example.clothesshop.repository.WalletTransactionRepository;
import com.example.clothesshop.service.WalletService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class WalletServiceImpl implements WalletService {
    
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;
    
    public WalletServiceImpl(WalletRepository walletRepository, 
                             WalletTransactionRepository transactionRepository) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }
    
    @Override
    public Wallet getOrCreateWallet(User user) {
        return walletRepository.findByUser(user)
            .orElseGet(() -> {
                Wallet wallet = new Wallet();
                wallet.setUser(user);
                wallet.setBalance(BigDecimal.ZERO);
                return walletRepository.save(wallet);
            });
    }
    
    @Override
    public BigDecimal getBalance(User user) {
        Wallet wallet = getOrCreateWallet(user);
        return wallet.getBalance();
    }
    
    @Override
    public WalletTransaction deposit(User user, BigDecimal amount, String description) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Số tiền nạp phải lớn hơn 0");
        }
        
        Wallet wallet = getOrCreateWallet(user);
        BigDecimal balanceBefore = wallet.getBalance();
        BigDecimal balanceAfter = balanceBefore.add(amount);
        
        wallet.setBalance(balanceAfter);
        walletRepository.save(wallet);
        
        WalletTransaction transaction = new WalletTransaction();
        transaction.setWallet(wallet);
        transaction.setType(TransactionType.DEPOSIT);
        transaction.setAmount(amount);
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(balanceAfter);
        transaction.setDescription(description);
        
        return transactionRepository.save(transaction);
    }
    
    @Override
    public WalletTransaction withdraw(User user, BigDecimal amount, String description, Order order) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Số tiền thanh toán phải lớn hơn 0");
        }
        
        Wallet wallet = getOrCreateWallet(user);
        BigDecimal balanceBefore = wallet.getBalance();
        
        if (balanceBefore.compareTo(amount) < 0) {
            throw new IllegalArgumentException("Số dư không đủ để thanh toán");
        }
        
        BigDecimal balanceAfter = balanceBefore.subtract(amount);
        wallet.setBalance(balanceAfter);
        walletRepository.save(wallet);
        
        WalletTransaction transaction = new WalletTransaction();
        transaction.setWallet(wallet);
        transaction.setType(TransactionType.PAYMENT);
        transaction.setAmount(amount);
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(balanceAfter);
        transaction.setDescription(description);
        transaction.setOrder(order);
        
        return transactionRepository.save(transaction);
    }
    
    @Override
    public WalletTransaction refund(User user, BigDecimal amount, String description, Order order) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Số tiền hoàn phải lớn hơn 0");
        }
        
        Wallet wallet = getOrCreateWallet(user);
        BigDecimal balanceBefore = wallet.getBalance();
        BigDecimal balanceAfter = balanceBefore.add(amount);
        
        wallet.setBalance(balanceAfter);
        walletRepository.save(wallet);
        
        WalletTransaction transaction = new WalletTransaction();
        transaction.setWallet(wallet);
        transaction.setType(TransactionType.REFUND);
        transaction.setAmount(amount);
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(balanceAfter);
        transaction.setDescription(description);
        transaction.setOrder(order);
        
        return transactionRepository.save(transaction);
    }
    
    @Override
    public boolean canAfford(User user, BigDecimal amount) {
        BigDecimal balance = getBalance(user);
        return balance.compareTo(amount) >= 0;
    }
    
    @Override
    public List<WalletTransaction> getTransactionHistory(User user) {
        Wallet wallet = getOrCreateWallet(user);
        return transactionRepository.findByWalletOrderByCreatedAtDesc(wallet);
    }
    
    @Override
    public Wallet getWalletByUserId(Long userId) {
        return walletRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy ví của người dùng"));
    }
}
