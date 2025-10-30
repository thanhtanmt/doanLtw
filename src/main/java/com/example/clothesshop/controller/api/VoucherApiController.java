package com.example.clothesshop.controller.api;

import com.example.clothesshop.model.Voucher;
import com.example.clothesshop.model.DiscountType;
import com.example.clothesshop.repository.VoucherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/vouchers")
public class VoucherApiController {

    @Autowired
    private VoucherRepository voucherRepository;

    /**
     * Validate và tính toán discount amount cho voucher
     */
    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateVoucher(
            @RequestParam String code,
            @RequestParam BigDecimal orderAmount) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Tìm voucher by code
            Optional<Voucher> voucherOpt = voucherRepository.findByCodeAndActive(code.toUpperCase(), true);
            
            if (voucherOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Mã giảm giá không tồn tại!");
                return ResponseEntity.ok(response);
            }
            
            Voucher voucher = voucherOpt.get();
            
            // Kiểm tra còn số lượng không
            if (voucher.getUsedQuantity() >= voucher.getTotalQuantity()) {
                response.put("success", false);
                response.put("message", "Mã giảm giá đã hết lượt sử dụng!");
                return ResponseEntity.ok(response);
            }
            
            // Kiểm tra thời gian
            LocalDate today = LocalDate.now();
            if (today.isBefore(voucher.getStartDate())) {
                response.put("success", false);
                response.put("message", "Mã giảm giá chưa đến thời gian sử dụng!");
                return ResponseEntity.ok(response);
            }
            
            if (today.isAfter(voucher.getEndDate())) {
                response.put("success", false);
                response.put("message", "Mã giảm giá đã hết hạn!");
                return ResponseEntity.ok(response);
            }
            
            // Kiểm tra giá trị đơn hàng tối thiểu
            if (orderAmount.compareTo(voucher.getMinOrderValue()) < 0) {
                response.put("success", false);
                response.put("message", String.format("Đơn hàng tối thiểu %,.0fđ để sử dụng mã này!", 
                    voucher.getMinOrderValue()));
                return ResponseEntity.ok(response);
            }
            
            // Tính toán discount amount
            BigDecimal discountAmount;
            
            if (voucher.getDiscountType() == DiscountType.PERCENTAGE) {
                // Giảm theo phần trăm
                discountAmount = orderAmount.multiply(voucher.getDiscountValue())
                                           .divide(BigDecimal.valueOf(100));
                
                // Áp dụng giảm tối đa nếu có
                if (voucher.getMaxDiscount() != null && 
                    voucher.getMaxDiscount().compareTo(BigDecimal.ZERO) > 0) {
                    if (discountAmount.compareTo(voucher.getMaxDiscount()) > 0) {
                        discountAmount = voucher.getMaxDiscount();
                    }
                }
            } else {
                // Giảm số tiền cố định
                discountAmount = voucher.getDiscountValue();
                
                // Không được giảm quá tổng đơn hàng
                if (discountAmount.compareTo(orderAmount) > 0) {
                    discountAmount = orderAmount;
                }
            }
            
            // Trả về thành công
            response.put("success", true);
            response.put("message", String.format("Áp dụng thành công! Giảm %,.0fđ", discountAmount));
            response.put("voucherCode", voucher.getCode());
            response.put("voucherName", voucher.getName());
            response.put("discountAmount", discountAmount);
            response.put("discountType", voucher.getDiscountType().name());
            response.put("discountValue", voucher.getDiscountValue());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
}
