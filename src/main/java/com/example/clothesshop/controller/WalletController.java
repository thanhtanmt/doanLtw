package com.example.clothesshop.controller;

import com.example.clothesshop.model.User;
import com.example.clothesshop.model.Wallet;
import com.example.clothesshop.model.WalletTransaction;
import com.example.clothesshop.service.WalletService;
import com.example.clothesshop.service.UserService;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/wallet")
public class WalletController {
    
    private final WalletService walletService;
    private final UserService userService;
    
    public WalletController(WalletService walletService, UserService userService) {
        this.walletService = walletService;
        this.userService = userService;
    }
    
    /**
     * Trang quản lý ví
     */
    @GetMapping
    public String walletPage(Model model) {
        User user = getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }
        
        Wallet wallet = walletService.getOrCreateWallet(user);
        List<WalletTransaction> transactions = walletService.getTransactionHistory(user);
        
        model.addAttribute("wallet", wallet);
        model.addAttribute("transactions", transactions);
        model.addAttribute("user", user);
        
        return "wallet";
    }
    
    /**
     * API lấy số dư
     */
    @GetMapping("/balance")
    @ResponseBody
    public Map<String, Object> getBalance() {
        Map<String, Object> response = new HashMap<>();
        try {
            User user = getCurrentUser();
            if (user == null) {
                response.put("success", false);
                response.put("message", "Vui lòng đăng nhập");
                return response;
            }
            
            BigDecimal balance = walletService.getBalance(user);
            response.put("success", true);
            response.put("balance", balance);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }
    
    /**
     * API nạp tiền
     */
    @PostMapping("/deposit")
    @ResponseBody
    public Map<String, Object> deposit(@RequestParam BigDecimal amount,
                                      @RequestParam(required = false) String description) {
        Map<String, Object> response = new HashMap<>();
        try {
            User user = getCurrentUser();
            if (user == null) {
                response.put("success", false);
                response.put("message", "Vui lòng đăng nhập");
                return response;
            }
            
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                response.put("success", false);
                response.put("message", "Số tiền nạp phải lớn hơn 0");
                return response;
            }
            
            if (description == null || description.trim().isEmpty()) {
                description = "Nạp tiền vào ví";
            }
            
            WalletTransaction transaction = walletService.deposit(user, amount, description);
            
            response.put("success", true);
            response.put("message", "Nạp tiền thành công");
            response.put("transaction", transaction);
            response.put("newBalance", transaction.getBalanceAfter());
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }
    
    /**
     * API kiểm tra số dư có đủ không
     */
    @GetMapping("/can-afford")
    @ResponseBody
    public Map<String, Object> canAfford(@RequestParam BigDecimal amount) {
        Map<String, Object> response = new HashMap<>();
        try {
            User user = getCurrentUser();
            if (user == null) {
                response.put("success", false);
                response.put("message", "Vui lòng đăng nhập");
                return response;
            }
            
            boolean canAfford = walletService.canAfford(user, amount);
            BigDecimal balance = walletService.getBalance(user);
            
            response.put("success", true);
            response.put("canAfford", canAfford);
            response.put("balance", balance);
            response.put("required", amount);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }
    
    /**
     * API lấy lịch sử giao dịch
     */
    @GetMapping("/transactions")
    @ResponseBody
    public Map<String, Object> getTransactions() {
        Map<String, Object> response = new HashMap<>();
        try {
            User user = getCurrentUser();
            if (user == null) {
                response.put("success", false);
                response.put("message", "Vui lòng đăng nhập");
                return response;
            }
            
            List<WalletTransaction> transactions = walletService.getTransactionHistory(user);
            
            response.put("success", true);
            response.put("transactions", transactions);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }
    
    /**
     * Lấy user hiện tại
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || 
            authentication.getPrincipal().equals("anonymousUser")) {
            return null;
        }
        
        String username = authentication.getName();
        return userService.findByUsername(username).orElse(null);
    }
}
