package com.example.clothesshop.controller;

import com.example.clothesshop.dto.CheckoutRequest;
import com.example.clothesshop.dto.CartResponse;
import com.example.clothesshop.model.*;
import com.example.clothesshop.repository.ProductRepository;
import com.example.clothesshop.repository.ProductVariantRepository;
import com.example.clothesshop.repository.VoucherRepository;
import com.example.clothesshop.service.OrderService;
import com.example.clothesshop.service.CartService;
import com.example.clothesshop.service.WalletService;
import com.example.clothesshop.service.UserService;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
public class OrderController {
    
    private final OrderService orderService;
    private final CartService cartService;
    private final WalletService walletService;
    private final UserService userService;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final VoucherRepository voucherRepository;
    
    public OrderController(OrderService orderService, 
                          CartService cartService,
                          WalletService walletService,
                          UserService userService,
                          ProductRepository productRepository,
                          ProductVariantRepository productVariantRepository,
                          VoucherRepository voucherRepository) {
        this.orderService = orderService;
        this.cartService = cartService;
        this.walletService = walletService;
        this.userService = userService;
        this.productRepository = productRepository;
        this.productVariantRepository = productVariantRepository;
        this.voucherRepository = voucherRepository;
    }

    @GetMapping("/checkout")
    public String checkout(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        
        User user = getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }
        
        // Lấy thông tin giỏ hàng
        CartResponse cart = cartService.getCart(user);
        
        // Kiểm tra giỏ hàng có trống không
        if (cart == null || cart.getTotalItems() == 0) {
            return "redirect:/cart";
        }
        
        // Lấy số dư ví
        BigDecimal walletBalance = walletService.getBalance(user);
        
        model.addAttribute("cart", cart);
        model.addAttribute("user", user);
        model.addAttribute("walletBalance", walletBalance);
        
        return "checkout";
    }

    @PostMapping("/place-order")
    @ResponseBody
    public Map<String, Object> placeOrder(@RequestBody CheckoutRequest request, 
                                         Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                response.put("success", false);
                response.put("message", "Vui lòng đăng nhập để đặt hàng");
                return response;
            }
            
            User user = getCurrentUser();
            if (user == null) {
                response.put("success", false);
                response.put("message", "Không tìm thấy thông tin người dùng");
                return response;
            }
            
            // Lấy giỏ hàng
            CartResponse cart = cartService.getCart(user);
            if (cart == null || cart.getTotalItems() == 0) {
                response.put("success", false);
                response.put("message", "Giỏ hàng trống");
                return response;
            }
            
            // Validate payment method
            String paymentMethod = request.getPaymentMethod();
            if (paymentMethod == null || (!paymentMethod.equals("COD") && !paymentMethod.equals("WALLET"))) {
                response.put("success", false);
                response.put("message", "Phương thức thanh toán không hợp lệ");
                return response;
            }
            
            BigDecimal subtotal = cart.getTotalAmount();
            BigDecimal discount = BigDecimal.ZERO;
            Voucher voucher = null;
            
            // Xử lý voucher nếu có
            if (request.getVoucherCode() != null && !request.getVoucherCode().trim().isEmpty()) {
                String code = request.getVoucherCode().trim();
                voucher = voucherRepository.findByCode(code).orElse(null);
                
                if (voucher == null) {
                    response.put("success", false);
                    response.put("message", "Mã giảm giá không tồn tại");
                    return response;
                }
                
                // Validate voucher
                if (!voucher.isActive()) {
                    response.put("success", false);
                    response.put("message", "Mã giảm giá đã bị vô hiệu hóa");
                    return response;
                }
                
                LocalDate today = LocalDate.now();
                if (today.isBefore(voucher.getStartDate()) || today.isAfter(voucher.getEndDate())) {
                    response.put("success", false);
                    response.put("message", "Mã giảm giá đã hết hạn hoặc chưa bắt đầu");
                    return response;
                }
                
                if (voucher.getUsedQuantity() >= voucher.getTotalQuantity()) {
                    response.put("success", false);
                    response.put("message", "Mã giảm giá đã hết lượt sử dụng");
                    return response;
                }
                
                if (subtotal.compareTo(voucher.getMinOrderValue()) < 0) {
                    response.put("success", false);
                    response.put("message", "Đơn hàng chưa đủ giá trị tối thiểu để áp dụng mã giảm giá: " + voucher.getMinOrderValue() + "đ");
                    return response;
                }
                
                // Tính discount
                if (voucher.getDiscountType() == DiscountType.PERCENTAGE) {
                    discount = subtotal.multiply(voucher.getDiscountValue())
                                      .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                    if (voucher.getMaxDiscount() != null && discount.compareTo(voucher.getMaxDiscount()) > 0) {
                        discount = voucher.getMaxDiscount();
                    }
                } else if (voucher.getDiscountType() == DiscountType.FIXED_AMOUNT) {
                    discount = voucher.getDiscountValue();
                    if (discount.compareTo(subtotal) > 0) {
                        discount = subtotal; // không giảm quá tổng tiền
                    }
                }
                
                // Tăng usedQuantity
                voucher.setUsedQuantity(voucher.getUsedQuantity() + 1);
                voucherRepository.save(voucher);
            }
            
            BigDecimal finalAmount = subtotal.subtract(discount);
            if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
                finalAmount = BigDecimal.ZERO;
            }
            
            // Nếu thanh toán bằng ví, kiểm tra số dư
            if (paymentMethod.equals("WALLET")) {
                if (!walletService.canAfford(user, finalAmount)) {
                    BigDecimal balance = walletService.getBalance(user);
                    response.put("success", false);
                    response.put("message", "Số dư ví không đủ. Số dư hiện tại: " + balance + "đ");
                    response.put("balance", balance);
                    response.put("required", finalAmount);
                    return response;
                }
            }
            
            // Tạo đơn hàng
            Order order = new Order();
            order.setOrderCode("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            order.setUser(user);
            order.setShippingName(request.getShippingName());
            order.setShippingPhone(request.getShippingPhone());
            order.setShippingAddress(request.getShippingAddress());
            order.setDeliveryNotes(request.getNotes()); // Lưu ghi chú
            order.setPaymentMethod(paymentMethod);
            order.setTotalPrice(subtotal); // Giá gốc trước giảm
            order.setTotalAmount(finalAmount); // Giá sau giảm (giá thực tế phải trả)
            order.setStatus(OrderStatus.PENDING);
            
            // Lưu thông tin voucher và discount
            if (voucher != null) {
                order.setVoucher(voucher);
                order.setVoucherCode(voucher.getCode());
                order.setDiscountAmount(discount);
            }
            
            if (paymentMethod.equals("COD")) {
                order.setCodAmount(finalAmount);
            } else {
                order.setCodAmount(BigDecimal.ZERO);
            }
            
            // Kiểm tra tồn kho trước khi tạo đơn hàng
            for (var item : cart.getItems()) {
                if (item.getVariantId() != null) {
                    ProductVariant variant = productVariantRepository.findById(item.getVariantId()).orElse(null);
                    if (variant == null) {
                        response.put("success", false);
                        response.put("message", "Sản phẩm '" + item.getProductName() + "' không tồn tại");
                        return response;
                    }
                    
                    if (variant.getQuantity() < item.getQuantity()) {
                        response.put("success", false);
                        response.put("message", "Sản phẩm '" + item.getProductName() + "' chỉ còn " + variant.getQuantity() + " sản phẩm");
                        return response;
                    }
                }
            }
            
            // Thêm order details từ cart items
            cart.getItems().forEach(item -> {
                OrderDetail detail = new OrderDetail();
                
                // Load Product và ProductVariant entities
                if (item.getProductId() != null) {
                    Product product = productRepository.findById(item.getProductId()).orElse(null);
                    detail.setProduct(product);
                }
                
                if (item.getVariantId() != null) {
                    ProductVariant variant = productVariantRepository.findById(item.getVariantId()).orElse(null);
                    detail.setVariant(variant);
                }
                
                detail.setQuantity(item.getQuantity());
                detail.setUnitPrice(item.getPrice());
                detail.setTotalPrice(item.getSubtotal());
                detail.setOrder(order);
                order.getOrderDetails().add(detail);
            });
            
            // Lưu đơn hàng
            Order savedOrder = orderService.save(order);
            
            // Trừ số lượng sản phẩm (quantity) trong kho sau khi đặt hàng thành công
            for (var item : cart.getItems()) {
                if (item.getVariantId() != null) {
                    ProductVariant variant = productVariantRepository.findById(item.getVariantId()).orElse(null);
                    if (variant != null) {
                        int newQuantity = variant.getQuantity() - item.getQuantity();
                        variant.setQuantity(newQuantity);
                        productVariantRepository.save(variant);
                    }
                }
            }
            
            // Nếu thanh toán bằng ví, trừ tiền và tạo wallet transaction
            if (paymentMethod.equals("WALLET")) {
                walletService.withdraw(user, finalAmount, "Thanh toán đơn hàng #" + savedOrder.getOrderCode(), savedOrder);
            }
            
            // Xóa giỏ hàng
            cartService.clearCart(user);
            
            response.put("success", true);
            response.put("message", "Đặt hàng thành công");
            response.put("orderId", savedOrder.getId());
            response.put("orderCode", savedOrder.getOrderCode());
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra: " + e.getMessage());
            e.printStackTrace();
        }
        
        return response;
    }

    @GetMapping("/orders")
    public String listOrders() {
        return "orders";
    }
    
    /**
     * API: Get user's orders
     * GET /api/user/orders
     */
    @GetMapping("/api/user/orders")
    @ResponseBody
    public Map<String, Object> getUserOrders() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User user = getCurrentUser();
            if (user == null) {
                response.put("success", false);
                response.put("message", "Vui lòng đăng nhập");
                return response;
            }
            
            List<Order> orders = orderService.findByUserId(user.getId());
            
            // Convert to simple maps to avoid circular reference
            List<Map<String, Object>> orderMaps = orders.stream()
                    .map(this::convertOrderToMap)
                    .collect(Collectors.toList());
            
            response.put("success", true);
            response.put("data", orderMaps);
            return response;
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return response;
        }
    }
    
    /**
     * API: Cancel order
     * POST /api/user/orders/{id}/cancel
     */
    @PostMapping("/api/user/orders/{id}/cancel")
    @ResponseBody
    public Map<String, Object> cancelOrder(@PathVariable Long id, 
                                          @RequestParam(required = false) String reason) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User user = getCurrentUser();
            if (user == null) {
                response.put("success", false);
                response.put("message", "Vui lòng đăng nhập");
                return response;
            }
            
            Order order = orderService.findById(id).orElse(null);
            if (order == null) {
                response.put("success", false);
                response.put("message", "Không tìm thấy đơn hàng");
                return response;
            }
            
            // Check ownership
            if (!order.getUser().getId().equals(user.getId())) {
                response.put("success", false);
                response.put("message", "Bạn không có quyền hủy đơn hàng này");
                return response;
            }
            
            // Check if order can be canceled (only PENDING and CONFIRMED)
            if (order.getStatus() != OrderStatus.PENDING && 
                order.getStatus() != OrderStatus.CONFIRMED) {
                response.put("success", false);
                response.put("message", "Không thể hủy đơn hàng ở trạng thái hiện tại");
                return response;
            }
            
            // Change order status to CANCELED
            order.setStatus(OrderStatus.CANCELED);
            if (reason != null && !reason.trim().isEmpty()) {
                order.setFailureReason("Khách hàng hủy: " + reason);
            } else {
                order.setFailureReason("Khách hàng hủy đơn hàng");
            }
            
            // Refund if paid by wallet
            if ("WALLET".equalsIgnoreCase(order.getPaymentMethod())) {
                // Hoàn lại số tiền thực tế khách đã trả (sau khi áp dụng voucher)
                BigDecimal refundAmount = order.getTotalAmount() != null ? 
                    order.getTotalAmount() : 
                    (order.getTotalPrice() != null ? order.getTotalPrice() : BigDecimal.ZERO);
                    
                if (refundAmount.compareTo(BigDecimal.ZERO) > 0) {
                    walletService.refund(user, refundAmount, 
                        "Hoàn tiền đơn hàng #" + order.getOrderCode(), order);
                    
                    response.put("refunded", true);
                    response.put("refundAmount", refundAmount);
                }
            }
            
            orderService.save(order);
            
            response.put("success", true);
            response.put("message", "Đã hủy đơn hàng thành công" + 
                ("WALLET".equalsIgnoreCase(order.getPaymentMethod()) ? 
                    ". Tiền đã được hoàn vào ví." : ""));
            response.put("order", convertOrderToMap(order));
            
            return response;
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return response;
        }
    }
    
    /**
     * Helper method to convert Order to Map for JSON response
     */
    private Map<String, Object> convertOrderToMap(Order order) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", order.getId());
        map.put("orderCode", order.getOrderCode());
        
        // Use totalPrice first, fallback to totalAmount
        BigDecimal total = order.getTotalPrice() != null ? order.getTotalPrice() : 
                          (order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO);
        map.put("totalPrice", total);
        map.put("totalAmount", order.getTotalAmount());
        
        map.put("status", order.getStatus().name());
        map.put("statusDisplay", getStatusDisplay(order.getStatus()));
        map.put("paymentMethod", order.getPaymentMethod());
        map.put("shippingAddress", order.getShippingAddress());
        map.put("shippingPhone", order.getShippingPhone());
        map.put("shippingName", order.getShippingName());
        map.put("createdAt", order.getCreatedAt());
        map.put("failureReason", order.getFailureReason());
        
        // Thêm thông tin voucher và discount
        map.put("voucherCode", order.getVoucherCode());
        map.put("discountAmount", order.getDiscountAmount());
        
        return map;
    }
    
    private String getStatusDisplay(OrderStatus status) {
        switch (status) {
            case PENDING: return "Chờ xác nhận";
            case CONFIRMED: return "Đã xác nhận";
            case ASSIGNED: return "Đã giao cho shipper";
            case DELIVERING: return "Đang giao hàng";
            case DELIVERED: return "Đã giao hàng";
            case FAILED: return "Giao thất bại";
            case CANCELED: return "Đã hủy";
            default: return status.name();
        }
    }

    @GetMapping("/order/{id}")
    public String orderDetails(@PathVariable Long id, Model model) {
        User user = getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }
        
        Order order = orderService.findById(id).orElse(null);
        if (order == null) {
            return "redirect:/orders";
        }
        
        // Check ownership
        if (!order.getUser().getId().equals(user.getId())) {
            return "redirect:/orders";
        }
        
        model.addAttribute("order", order);
        return "order-detail";
    }
    
    /**
     * API: Get order detail
     * GET /api/user/orders/{id}
     */
    @GetMapping("/api/user/orders/{id}")
    @ResponseBody
    public Map<String, Object> getOrderDetail(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User user = getCurrentUser();
            if (user == null) {
                response.put("success", false);
                response.put("message", "Vui lòng đăng nhập");
                return response;
            }
            
            Order order = orderService.findById(id).orElse(null);
            if (order == null) {
                response.put("success", false);
                response.put("message", "Không tìm thấy đơn hàng");
                return response;
            }
            
            // Check ownership
            if (!order.getUser().getId().equals(user.getId())) {
                response.put("success", false);
                response.put("message", "Bạn không có quyền xem đơn hàng này");
                return response;
            }
            
            Map<String, Object> orderMap = convertOrderToDetailMap(order);
            
            response.put("success", true);
            response.put("data", orderMap);
            return response;
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return response;
        }
    }
    
    /**
     * Convert Order to detailed Map including order details
     */
    private Map<String, Object> convertOrderToDetailMap(Order order) {
        Map<String, Object> map = convertOrderToMap(order);
        
        // Add order details (products)
        List<Map<String, Object>> items = new java.util.ArrayList<>();
        if (order.getOrderDetails() != null) {
            for (OrderDetail detail : order.getOrderDetails()) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", detail.getId());
                item.put("quantity", detail.getQuantity());
                item.put("unitPrice", detail.getUnitPrice());
                item.put("subtotal", detail.getUnitPrice().multiply(new BigDecimal(detail.getQuantity())));
                
                if (detail.getVariant() != null) {
                    ProductVariant variant = detail.getVariant();
                    item.put("variantId", variant.getId());
                    item.put("size", variant.getSize());
                    
                    if (variant.getProduct() != null) {
                        Product product = variant.getProduct();
                        item.put("productId", product.getId());
                        item.put("productName", product.getName());
                        
                        // Get first image
                        if (product.getImages() != null && !product.getImages().isEmpty()) {
                            item.put("imageUrl", product.getImages().get(0).getUrl());
                        }
                    }
                }
                
                items.add(item);
            }
        }
        map.put("items", items);
        
        return map;
    }

    @GetMapping("/order-success")
    public String orderSuccess(@RequestParam(required = false) String orderCode, Model model) {
        if (orderCode != null) {
            orderService.findByOrderCode(orderCode).ifPresent(order -> {
                model.addAttribute("order", order);
            });
        }
        return "order-success";
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
