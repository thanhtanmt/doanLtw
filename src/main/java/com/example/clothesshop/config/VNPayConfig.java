package com.example.clothesshop.config;

import org.springframework.context.annotation.Configuration;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Configuration
public class VNPayConfig {
    
    // VNPay Sandbox Configuration
    public static final String vnp_PayUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    public static final String vnp_ReturnUrl = "http://localhost:8080/wallet/vnpay-return";
    public static final String vnp_TmnCode = "3PBWE1UY"; // Mã website tại VNPay
    public static final String vnp_HashSecret = "24JVU96XBZG7QHBJ6GHE96G9ZKWYA9MG"; // Secret key
    public static final String vnp_ApiUrl = "https://sandbox.vnpayment.vn/merchant_webapi/api/transaction";
    
    public static final String vnp_Version = "2.1.0";
    public static final String vnp_Command = "pay";
    public static final String vnp_OrderType = "other";
    
    /**
     * Tạo URL thanh toán VNPay
     */
    public static String createPaymentUrl(String orderId, long amount, String orderInfo, String ipAddress) throws Exception {
        Map<String, String> vnp_Params = new HashMap<>();
        
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount * 100)); // VNPay yêu cầu số tiền x100
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", orderId);
        vnp_Params.put("vnp_OrderInfo", orderInfo);
        vnp_Params.put("vnp_OrderType", vnp_OrderType);
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", ipAddress);
        
        // Sử dụng timezone Asia/Ho_Chi_Minh thay vì Etc/GMT+7
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        String vnp_CreateDate = String.format("%1$tY%1$tm%1$td%1$tH%1$tM%1$tS", cld);
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
        
        // Debug logging
        System.out.println("=== VNPAY PAYMENT DEBUG ===");
        System.out.println("Transaction ID: " + orderId);
        System.out.println("Amount: " + amount);
        System.out.println("Create Date: " + vnp_CreateDate);
        
        cld.add(Calendar.MINUTE, 15); // Thời gian hết hạn 15 phút
        String vnp_ExpireDate = String.format("%1$tY%1$tm%1$td%1$tH%1$tM%1$tS", cld);
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);
        
        System.out.println("Expire Date: " + vnp_ExpireDate);
        System.out.println("Timezone: " + TimeZone.getDefault().getID());
        System.out.println("==========================");
        
        // Sắp xếp params theo thứ tự alphabet
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                // Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                
                // Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        
        String queryUrl = query.toString();
        String vnp_SecureHash = hmacSHA512(vnp_HashSecret, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        
        return vnp_PayUrl + "?" + queryUrl;
    }
    
    /**
     * Xác thực chữ ký từ VNPay
     */
    public static boolean verifySignature(Map<String, String> params, String secureHash) {
        try {
            params.remove("vnp_SecureHash");
            params.remove("vnp_SecureHashType");
            
            List<String> fieldNames = new ArrayList<>(params.keySet());
            Collections.sort(fieldNames);
            
            StringBuilder hashData = new StringBuilder();
            Iterator<String> itr = fieldNames.iterator();
            
            while (itr.hasNext()) {
                String fieldName = itr.next();
                String fieldValue = params.get(fieldName);
                if ((fieldValue != null) && (fieldValue.length() > 0)) {
                    hashData.append(fieldName);
                    hashData.append('=');
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    if (itr.hasNext()) {
                        hashData.append('&');
                    }
                }
            }
            
            String vnp_SecureHash = hmacSHA512(vnp_HashSecret, hashData.toString());
            return vnp_SecureHash.equals(secureHash);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Mã hóa HMAC SHA512
     */
    public static String hmacSHA512(String key, String data) {
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKey);
            byte[] result = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder sb = new StringBuilder();
            for (byte b : result) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }
    
    /**
     * Lấy địa chỉ IP từ request
     */
    public static String getIpAddress(jakarta.servlet.http.HttpServletRequest request) {
        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }
}
