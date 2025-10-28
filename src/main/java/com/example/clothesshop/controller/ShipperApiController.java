package com.example.clothesshop.controller;

import com.example.clothesshop.model.Order;
import com.example.clothesshop.model.OrderStatus;
import com.example.clothesshop.model.User;
import com.example.clothesshop.dto.OrderDto;
import com.example.clothesshop.dto.ShipperStatsDto;
import com.example.clothesshop.dto.DeliveryUpdateDto;
import com.example.clothesshop.service.OrderService;
import com.example.clothesshop.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/shipper")
@CrossOrigin(origins = "*")
public class ShipperApiController {

    @Autowired
    private OrderService orderService;
    
    @Autowired
    private UserService userService;

    // Lấy thông tin shipper hiện tại
    private User getCurrentShipper() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userService.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy shipper"));
    }

    // API Dashboard - Lấy thống kê tổng quan
    @GetMapping("/dashboard/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        try {
            User shipper = getCurrentShipper();
            ShipperStatsDto stats = orderService.getShipperStats(shipper);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", stats);
            response.put("message", "Lấy thống kê thành công");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // API Lấy danh sách đơn hàng chờ giao
    @GetMapping("/orders/pending")
    public ResponseEntity<Map<String, Object>> getPendingOrders() {
        try {
            User shipper = null;
            try { shipper = getCurrentShipper(); } catch (Exception ignored) {}
            List<Order> pendingOrders = orderService.findPendingOrders();
            if (shipper != null) {
                List<Order> shippingOrders = orderService.findOrdersByShipperAndStatus(shipper, OrderStatus.DELIVERING);
                pendingOrders.addAll(shippingOrders);
            }
            List<OrderDto> orderDtos = orderService.convertToDtoList(pendingOrders);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", orderDtos);
            response.put("message", "Lấy danh sách đơn hàng chờ giao thành công");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // API Lấy danh sách đơn hàng đang giao của shipper
    @GetMapping("/orders/shipping")
    public ResponseEntity<Map<String, Object>> getShippingOrders() {
        try {
            User shipper = getCurrentShipper();
            List<Order> shippingOrders = orderService.findOrdersByShipperAndStatus(shipper, OrderStatus.DELIVERING);
            List<OrderDto> orderDtos = orderService.convertToDtoList(shippingOrders);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", orderDtos);
            response.put("message", "Lấy danh sách đơn hàng đang giao thành công");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // API Lấy danh sách đơn hàng đã giao của shipper
    @GetMapping("/orders/delivered")
    public ResponseEntity<Map<String, Object>> getDeliveredOrders() {
        try {
            User shipper = getCurrentShipper();
            List<Order> deliveredOrders = orderService.findOrdersByShipperAndStatus(shipper, OrderStatus.DELIVERED);
            List<OrderDto> orderDtos = orderService.convertToDtoList(deliveredOrders);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", orderDtos);
            response.put("message", "Lấy danh sách đơn hàng đã giao thành công");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // API Lấy danh sách đơn hàng giao thất bại của shipper
    @GetMapping("/orders/failed")
    public ResponseEntity<Map<String, Object>> getFailedOrders() {
        try {
            User shipper = getCurrentShipper();
            List<Order> failedOrders = orderService.findOrdersByShipperAndStatus(shipper, OrderStatus.FAILED);
            List<OrderDto> orderDtos = orderService.convertToDtoList(failedOrders);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", orderDtos);
            response.put("message", "Lấy danh sách đơn hàng thất bại thành công");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // API Nhận đơn hàng để giao
    @PostMapping("/orders/{orderCode}/assign")
    public ResponseEntity<Map<String, Object>> assignOrder(@PathVariable String orderCode) {
        try {
            User shipper = getCurrentShipper();
            Order order = orderService.assignOrderToShipper(orderCode, shipper);
            OrderDto orderDto = orderService.convertToDto(order);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", orderDto);
            response.put("message", "Nhận đơn hàng thành công");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // API Cập nhật trạng thái giao hàng
    @PutMapping("/orders/delivery-status")
    public ResponseEntity<Map<String, Object>> updateDeliveryStatus(@RequestBody DeliveryUpdateDto deliveryUpdate) {
        try {
            User shipper = getCurrentShipper();
            Order order = orderService.updateDeliveryStatus(deliveryUpdate, shipper);
            OrderDto orderDto = orderService.convertToDto(order);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", orderDto);
            response.put("message", "Cập nhật trạng thái giao hàng thành công");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // API Lấy chi tiết đơn hàng
    @GetMapping("/orders/{orderCode}")
    public ResponseEntity<Map<String, Object>> getOrderDetails(@PathVariable String orderCode) {
        try {
            Order order = orderService.findByOrderCode(orderCode)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
            
            OrderDto orderDto = orderService.convertToDto(order);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", orderDto);
            response.put("message", "Lấy chi tiết đơn hàng thành công");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // API Tìm kiếm đơn hàng
    @GetMapping("/orders/search")
    public ResponseEntity<Map<String, Object>> searchOrders(@RequestParam String searchTerm) {
        try {
            List<Order> orders = orderService.searchOrders(searchTerm);
            List<OrderDto> orderDtos = orderService.convertToDtoList(orders);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", orderDtos);
            response.put("message", "Tìm kiếm đơn hàng thành công");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // API Lấy thông tin profile shipper
    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getShipperProfile() {
        try {
            User shipper = getCurrentShipper();
            
            Map<String, Object> profile = new HashMap<>();
            profile.put("id", shipper.getId());
            profile.put("username", shipper.getUsername());
            profile.put("email", shipper.getEmail());
            profile.put("firstName", shipper.getFirstName());
            profile.put("lastName", shipper.getLastName());
            profile.put("phone", shipper.getPhone());
            profile.put("enabled", shipper.isEnabled());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", profile);
            response.put("message", "Lấy thông tin profile thành công");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Đổi mật khẩu cho shipper
    @PostMapping("/profile/change-password")
    public ResponseEntity<Map<String, Object>> changePassword(@RequestParam String currentPassword,
            @RequestParam String newPassword) {
        Map<String, Object> response = new HashMap<>();
        try {
            User shipper = getCurrentShipper();
            // simple check: verify current matches
            if (!userService.matchesPassword(currentPassword, shipper.getPassword())) {
                response.put("success", false);
                response.put("message", "Mật khẩu hiện tại không đúng");
                return ResponseEntity.badRequest().body(response);
            }
            shipper.setPassword(newPassword);
            userService.save(shipper);
            response.put("success", true);
            response.put("message", "Cập nhật mật khẩu thành công");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Cập nhật thông tin cá nhân
    @PostMapping("/profile/update")
    public ResponseEntity<Map<String, Object>> updateProfile(@RequestBody Map<String, String> payload) {
        Map<String, Object> response = new HashMap<>();
        try {
            User shipper = getCurrentShipper();
            shipper.setFirstName(payload.getOrDefault("firstName", shipper.getFirstName()));
            shipper.setLastName(payload.getOrDefault("lastName", shipper.getLastName()));
            shipper.setPhone(payload.getOrDefault("phone", shipper.getPhone()));
            userService.save(shipper);
            response.put("success", true);
            response.put("message", "Cập nhật hồ sơ thành công");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Upload avatar
    @PostMapping(value = "/profile/avatar", consumes = { "multipart/form-data" })
    public ResponseEntity<Map<String, Object>> uploadAvatar(@RequestPart("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (file.isEmpty()) {
                response.put("success", false);
                response.put("message", "File rỗng");
                return ResponseEntity.badRequest().body(response);
            }
            User shipper = getCurrentShipper();
            // For demo, we just store filename as avatarUrl; real apps should save to storage
            shipper.setAvatarUrl("/uploads/" + file.getOriginalFilename());
            userService.save(shipper);
            response.put("success", true);
            response.put("data", shipper.getAvatarUrl());
            response.put("message", "Cập nhật ảnh đại diện thành công");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
