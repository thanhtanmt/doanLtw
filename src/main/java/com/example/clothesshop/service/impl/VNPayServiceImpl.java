package com.example.clothesshop.service.impl;

import com.example.clothesshop.config.VNPayConfig;
import com.example.clothesshop.model.User;
import com.example.clothesshop.model.VNPayTransaction;
import com.example.clothesshop.repository.VNPayTransactionRepository;
import com.example.clothesshop.service.VNPayService;
import com.example.clothesshop.service.WalletService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class VNPayServiceImpl implements VNPayService {
    
    private final VNPayTransactionRepository vnPayTransactionRepository;
    private final WalletService walletService;
    
    public VNPayServiceImpl(VNPayTransactionRepository vnPayTransactionRepository,
                           WalletService walletService) {
        this.vnPayTransactionRepository = vnPayTransactionRepository;
        this.walletService = walletService;
    }
    
    @Override
    public String createPaymentUrl(User user, BigDecimal amount, String orderInfo, HttpServletRequest request) throws Exception {
        // Tạo mã giao dịch unique
        String txnRef = "WALLET_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
        
        // Lưu thông tin giao dịch vào database với trạng thái PENDING
        VNPayTransaction transaction = new VNPayTransaction();
        transaction.setUser(user);
        transaction.setTxnRef(txnRef);
        transaction.setAmount(amount);
        transaction.setOrderInfo(orderInfo);
        transaction.setStatus(VNPayTransaction.TransactionStatus.PENDING);
        transaction.setCreatedAt(LocalDateTime.now());
        
        vnPayTransactionRepository.save(transaction);
        
        // Lấy IP address
        String ipAddress = VNPayConfig.getIpAddress(request);
        
        // Tạo URL thanh toán
        long amountInVND = amount.longValue();
        String paymentUrl = VNPayConfig.createPaymentUrl(txnRef, amountInVND, orderInfo, ipAddress);
        
        return paymentUrl;
    }
    
    @Override
    public VNPayTransaction processPaymentReturn(Map<String, String> params) {
        String txnRef = params.get("vnp_TxnRef");
        String responseCode = params.get("vnp_ResponseCode");
        String transactionNo = params.get("vnp_TransactionNo");
        String bankCode = params.get("vnp_BankCode");
        String cardType = params.get("vnp_CardType");
        String secureHash = params.get("vnp_SecureHash");
        
        // Tìm giao dịch trong database
        VNPayTransaction transaction = vnPayTransactionRepository.findByTxnRef(txnRef)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy giao dịch: " + txnRef));
        
        // Xác thực chữ ký
        boolean isValidSignature = VNPayConfig.verifySignature(params, secureHash);
        
        if (!isValidSignature) {
            transaction.setStatus(VNPayTransaction.TransactionStatus.FAILED);
            transaction.setResponseCode("99");
            transaction.setResponseMessage("Chữ ký không hợp lệ");
            vnPayTransactionRepository.save(transaction);
            throw new RuntimeException("Chữ ký không hợp lệ");
        }
        
        // Cập nhật thông tin giao dịch
        transaction.setTransactionNo(transactionNo);
        transaction.setBankCode(bankCode);
        transaction.setCardType(cardType);
        transaction.setResponseCode(responseCode);
        
        // Xử lý theo response code
        if ("00".equals(responseCode)) {
            // Thanh toán thành công
            transaction.setStatus(VNPayTransaction.TransactionStatus.SUCCESS);
            transaction.setResponseMessage("Giao dịch thành công");
            
            // Parse pay date
            String payDateStr = params.get("vnp_PayDate");
            if (payDateStr != null && !payDateStr.isEmpty()) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
                transaction.setPayDate(LocalDateTime.parse(payDateStr, formatter));
            }
            
            // Nạp tiền vào ví
            try {
                walletService.deposit(
                    transaction.getUser(), 
                    transaction.getAmount(), 
                    "Nạp tiền qua VNPay - " + transaction.getOrderInfo()
                );
            } catch (Exception e) {
                transaction.setResponseMessage("Lỗi khi nạp tiền vào ví: " + e.getMessage());
            }
            
        } else {
            // Thanh toán thất bại
            transaction.setStatus(VNPayTransaction.TransactionStatus.FAILED);
            transaction.setResponseMessage(getResponseMessage(responseCode));
        }
        
        return vnPayTransactionRepository.save(transaction);
    }
    
    @Override
    public VNPayTransaction getTransactionByTxnRef(String txnRef) {
        return vnPayTransactionRepository.findByTxnRef(txnRef)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy giao dịch"));
    }
    
    /**
     * Lấy thông báo lỗi theo mã response
     */
    private String getResponseMessage(String responseCode) {
        switch (responseCode) {
            case "00": return "Giao dịch thành công";
            case "07": return "Trừ tiền thành công. Giao dịch bị nghi ngờ (liên quan tới lừa đảo, giao dịch bất thường)";
            case "09": return "Giao dịch không thành công do: Thẻ/Tài khoản của khách hàng chưa đăng ký dịch vụ InternetBanking tại ngân hàng";
            case "10": return "Giao dịch không thành công do: Khách hàng xác thực thông tin thẻ/tài khoản không đúng quá 3 lần";
            case "11": return "Giao dịch không thành công do: Đã hết hạn chờ thanh toán";
            case "12": return "Giao dịch không thành công do: Thẻ/Tài khoản của khách hàng bị khóa";
            case "13": return "Giao dịch không thành công do Quý khách nhập sai mật khẩu xác thực giao dịch (OTP)";
            case "24": return "Giao dịch không thành công do: Khách hàng hủy giao dịch";
            case "51": return "Giao dịch không thành công do: Tài khoản của quý khách không đủ số dư để thực hiện giao dịch";
            case "65": return "Giao dịch không thành công do: Tài khoản của Quý khách đã vượt quá hạn mức giao dịch trong ngày";
            case "75": return "Ngân hàng thanh toán đang bảo trì";
            case "79": return "Giao dịch không thành công do: KH nhập sai mật khẩu thanh toán quá số lần quy định";
            default: return "Giao dịch thất bại";
        }
    }
}
