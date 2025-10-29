package com.example.clothesshop.controller;

import com.example.clothesshop.repository.UserRepository;
import com.example.clothesshop.repository.RoleRepository;
import com.example.clothesshop.repository.OrderRepository;
import com.example.clothesshop.repository.ProductRepository;
import com.example.clothesshop.repository.VoucherRepository;
import com.example.clothesshop.model.User;
import com.example.clothesshop.model.Role;
import com.example.clothesshop.model.Order;
import com.example.clothesshop.model.OrderStatus;
import com.example.clothesshop.model.Product;
import com.example.clothesshop.model.Voucher;
import com.example.clothesshop.model.DiscountType;
import com.example.clothesshop.service.CloudinaryService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.ResponseEntity;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.DayOfWeek;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final VoucherRepository voucherRepository;
    private final CloudinaryService cloudinaryService;
    private final PasswordEncoder passwordEncoder;

    public AdminController(UserRepository userRepository, RoleRepository roleRepository, 
                          OrderRepository orderRepository, ProductRepository productRepository,
                          VoucherRepository voucherRepository, CloudinaryService cloudinaryService,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.voucherRepository = voucherRepository;
        this.cloudinaryService = cloudinaryService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping({"", "/", "/dashboard"})
    public String adminDashboard(Model model) {
        // Lấy dữ liệu người dùng
        List<User> allUsers = userRepository.findAll();
        long totalUsers = allUsers.size();
        long activeUsers = allUsers.stream().filter(User::isEnabled).count();
        
        // Lấy dữ liệu đơn hàng
        List<Order> allOrders = orderRepository.findAll();
        
        // Tính doanh thu tháng này
        YearMonth currentMonth = YearMonth.now();
        LocalDateTime startOfMonth = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = currentMonth.atEndOfMonth().atTime(23, 59, 59);
        
        BigDecimal monthlyRevenue = allOrders.stream()
            .filter(o -> o.getCreatedAt() != null && 
                        o.getCreatedAt().isAfter(startOfMonth) && 
                        o.getCreatedAt().isBefore(endOfMonth) &&
                        o.getStatus() == OrderStatus.DELIVERED)
            .map(o -> o.getTotalAmount() != null ? o.getTotalAmount() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Đếm đơn hàng mới (pending)
        long newOrders = allOrders.stream()
            .filter(o -> o.getStatus() == OrderStatus.PENDING || o.getStatus() == OrderStatus.CONFIRMED)
            .count();
        
        // Lấy 5 đơn hàng gần nhất
        List<Order> recentOrders = allOrders.stream()
            .sorted((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()))
            .limit(5)
            .collect(Collectors.toList());
        
        // Tính tỷ lệ shipper hoạt động
        long totalShippers = allUsers.stream()
            .filter(u -> u.getRoles() != null && u.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ROLE_SHIPPER")))
            .count();
        long activeShippers = allUsers.stream()
            .filter(u -> u.isEnabled() && u.getRoles() != null && u.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ROLE_SHIPPER")))
            .count();
        double shipperActiveRate = totalShippers > 0 ? (activeShippers * 100.0 / totalShippers) : 0;
        
        // Tính tỷ lệ shop hoạt động
        long totalSellers = allUsers.stream()
            .filter(u -> u.getRoles() != null && u.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ROLE_SELLER")))
            .count();
        long activeSellers = allUsers.stream()
            .filter(u -> u.isEnabled() && u.getRoles() != null && u.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ROLE_SELLER")))
            .count();
        double sellerActiveRate = totalSellers > 0 ? (activeSellers * 100.0 / totalSellers) : 0;
        
        // Tính tỷ lệ đơn hàng thành công
        long successOrders = allOrders.stream()
            .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
            .count();
        double successRate = allOrders.size() > 0 ? (successOrders * 100.0 / allOrders.size()) : 0;
        
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("activeUsers", activeUsers);
        model.addAttribute("monthlyRevenue", monthlyRevenue);
        model.addAttribute("newOrders", newOrders);
        model.addAttribute("totalOrders", allOrders.size());
        model.addAttribute("recentOrders", recentOrders);
        model.addAttribute("shipperActiveRate", Math.round(shipperActiveRate));
        model.addAttribute("sellerActiveRate", Math.round(sellerActiveRate));
        model.addAttribute("successRate", Math.round(successRate));
        
        return "admin-home";
    }

    // ===== USER MANAGEMENT =====
    @GetMapping("/users")
    public String adminUsers(Model model) {
        List<User> users = userRepository.findAll();
        model.addAttribute("users", users);
        
        model.addAttribute("totalUsers", users.size());
        model.addAttribute("totalSellers", users.stream()
            .filter(u -> u.getRoles() != null && u.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ROLE_SELLER"))).count());
        model.addAttribute("totalShippers", users.stream()
            .filter(u -> u.getRoles() != null && u.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ROLE_SHIPPER"))).count());
        model.addAttribute("activeUsers", users.stream().filter(User::isEnabled).count());
        
        return "admin/users";
    }

    @GetMapping("/users/{id}")
    @ResponseBody
    public ResponseEntity<?> getUserDetails(@PathVariable Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            Map<String, Object> userData = new HashMap<>();
            userData.put("id", user.getId());
            userData.put("username", user.getUsername());
            userData.put("email", user.getEmail());
            userData.put("firstName", user.getFirstName());
            userData.put("lastName", user.getLastName());
            userData.put("phone", user.getPhone());
            userData.put("enabled", user.isEnabled());
            userData.put("emailVerified", user.isEmailVerified());
            userData.put("createdAt", user.getCreatedAt());
            userData.put("updatedAt", user.getUpdatedAt());
            
            if (user.getRoles() != null && !user.getRoles().isEmpty()) {
                List<Map<String, String>> roles = user.getRoles().stream()
                    .map(role -> {
                        Map<String, String> roleMap = new HashMap<>();
                        roleMap.put("name", role.getName());
                        return roleMap;
                    })
                    .collect(Collectors.toList());
                userData.put("roles", roles);
            } else {
                userData.put("roles", new ArrayList<>());
            }
            
            return ResponseEntity.ok(userData);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/users/change-role")
    @ResponseBody
    public ResponseEntity<?> changeUserRole(@RequestBody Map<String, Object> payload) {
        try {
            Long userId = Long.valueOf(payload.get("userId").toString());
            String roleName = payload.get("roleName").toString();
            
            Optional<User> userOpt = userRepository.findById(userId);
            Optional<Role> roleOpt = roleRepository.findByName(roleName);
            
            if (userOpt.isPresent() && roleOpt.isPresent()) {
                User user = userOpt.get();
                user.getRoles().clear();
                user.getRoles().add(roleOpt.get());
                user.setUpdatedAt(LocalDateTime.now());
                userRepository.save(user);
                
                return ResponseEntity.ok(Map.of(
                    "success", true, 
                    "message", "Đã thay đổi vai trò thành công!",
                    "newRole", roleName.replace("ROLE_", "")
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                    "success", false, 
                    "message", "Không tìm thấy người dùng hoặc vai trò!"
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "success", false, 
                "message", "Có lỗi xảy ra: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/users/{id}/toggle-status")
    @ResponseBody
    public ResponseEntity<?> toggleUserStatus(@PathVariable Long id) {
        try {
            Optional<User> userOpt = userRepository.findById(id);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.setEnabled(!user.isEnabled());
                user.setUpdatedAt(LocalDateTime.now());
                userRepository.save(user);
                return ResponseEntity.ok(Map.of("success", true, "enabled", user.isEnabled()));
            }
            return ResponseEntity.ok(Map.of("success", false, "message", "Không tìm thấy người dùng"));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // ===== SHOP MANAGEMENT =====
    @GetMapping("/shops")
    public String adminShops(Model model, @RequestParam(required = false, defaultValue = "all") String period) {
        // Calculate date range based on period
        LocalDateTime startDate = null;
        LocalDateTime endDate = LocalDateTime.now();
        
        switch (period) {
            case "today":
                startDate = LocalDateTime.now().toLocalDate().atStartOfDay();
                break;
            case "week":
                startDate = LocalDateTime.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).toLocalDate().atStartOfDay();
                break;
            case "month":
                startDate = YearMonth.now().atDay(1).atStartOfDay();
                break;
            case "year":
                startDate = LocalDateTime.now().withDayOfYear(1).toLocalDate().atStartOfDay();
                break;
            case "all":
            default:
                startDate = LocalDateTime.of(2000, 1, 1, 0, 0); // All time
                break;
        }
        
        // Get all sellers
        List<User> sellers = userRepository.findAll().stream()
            .filter(u -> u.getRoles() != null && u.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ROLE_SELLER")))
            .collect(Collectors.toList());
        
        // Calculate shop statistics
        List<Map<String, Object>> shops = new ArrayList<>();
        BigDecimal totalRevenue = BigDecimal.ZERO;
        int totalOrders = 0;
        
        for (User seller : sellers) {
            // Count products
            long productCount = productRepository.countBySeller(seller);
            
            // Get orders containing this seller's products in the period
            List<Order> sellerOrders = orderRepository.findOrdersBySellerAndDateRange(seller, startDate, endDate);
            long orderCount = sellerOrders.size();
            
            // Count completed orders
            long completedOrders = orderRepository.countOrdersBySellerAndStatusAndDateRange(
                seller, OrderStatus.DELIVERED, startDate, endDate);
            
            // Calculate revenue (sum of order items for this seller's products)
            BigDecimal revenue = orderRepository.calculateRevenueBySellerAndStatusAndDateRange(
                seller, OrderStatus.DELIVERED, startDate, endDate);
            
            if (revenue == null) {
                revenue = BigDecimal.ZERO;
            }
            
            double commissionRate = 5.0;
            BigDecimal commission = revenue.multiply(BigDecimal.valueOf(commissionRate / 100));
            
            totalRevenue = totalRevenue.add(revenue);
            totalOrders += orderCount;
            
            Map<String, Object> shop = new HashMap<>();
            shop.put("sellerId", seller.getId());
            shop.put("shopName", seller.getUsername() + "'s Shop");
            shop.put("sellerName", seller.getFirstName() + " " + seller.getLastName());
            shop.put("sellerEmail", seller.getEmail());
            shop.put("description", "Chuyên cung cấp thời trang chất lượng");
            shop.put("productCount", productCount);
            shop.put("orderCount", orderCount);
            shop.put("completedOrders", completedOrders);
            shop.put("totalRevenue", revenue.doubleValue());
            shop.put("commission", commission.doubleValue());
            shop.put("commissionRate", commissionRate);
            shop.put("isActive", seller.isEnabled());
            shops.add(shop);
        }
        
        // Sort shops by revenue (highest first)
        shops.sort((a, b) -> Double.compare((Double)b.get("totalRevenue"), (Double)a.get("totalRevenue")));
        
        double avgRevenuePerShop = shops.size() > 0 ? totalRevenue.doubleValue() / shops.size() : 0.0;
        
        model.addAttribute("shops", shops);
        model.addAttribute("topProducts", new ArrayList<>());
        model.addAttribute("totalShops", shops.size());
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("avgRevenuePerShop", avgRevenuePerShop);
        model.addAttribute("period", period);
        
        return "admin/shops";
    }

    @GetMapping("/shops/{sellerId}/details")
    @ResponseBody
    public ResponseEntity<?> getShopDetails(@PathVariable Long sellerId) {
        Optional<User> sellerOpt = userRepository.findById(sellerId);
        if (sellerOpt.isPresent()) {
            User seller = sellerOpt.get();
            
            // Get all-time statistics
            LocalDateTime startDate = LocalDateTime.of(2000, 1, 1, 0, 0);
            LocalDateTime endDate = LocalDateTime.now();
            
            long productCount = productRepository.countBySeller(seller);
            List<Order> sellerOrders = orderRepository.findOrdersBySellerAndDateRange(seller, startDate, endDate);
            long orderCount = sellerOrders.size();
            
            BigDecimal revenue = orderRepository.calculateRevenueBySellerAndStatusAndDateRange(
                seller, OrderStatus.DELIVERED, startDate, endDate);
            if (revenue == null) {
                revenue = BigDecimal.ZERO;
            }
            
            double commissionRate = 5.0;
            BigDecimal commission = revenue.multiply(BigDecimal.valueOf(commissionRate / 100));
            
            Map<String, Object> shopDetails = new HashMap<>();
            shopDetails.put("shopName", seller.getUsername() + "'s Shop");
            shopDetails.put("sellerName", seller.getFirstName() + " " + seller.getLastName());
            shopDetails.put("sellerEmail", seller.getEmail());
            shopDetails.put("phone", seller.getPhone() != null ? seller.getPhone() : "Chưa cập nhật");
            shopDetails.put("description", "Chuyên cung cấp thời trang chất lượng cao");
            shopDetails.put("productCount", productCount);
            shopDetails.put("orderCount", orderCount);
            shopDetails.put("totalRevenue", revenue.doubleValue());
            shopDetails.put("commission", commission.doubleValue());
            shopDetails.put("commissionRate", commissionRate);
            shopDetails.put("isActive", seller.isEnabled());
            shopDetails.put("createdAt", seller.getCreatedAt());
            return ResponseEntity.ok(shopDetails);
        }
        return ResponseEntity.notFound().build();
    }

    // ===== SHIPPER MANAGEMENT =====
    @GetMapping("/shippers")
    public String adminShippers(Model model) {
        // Get all shipper users
        List<User> shipperUsers = userRepository.findAll().stream()
            .filter(u -> u.getRoles() != null && u.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ROLE_SHIPPER")))
            .collect(Collectors.toList());
        
        // Get all orders
        List<Order> allOrders = orderRepository.findAll();
        
        // Calculate statistics
        long totalDelivered = allOrders.stream()
            .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
            .count();
        long totalInProgress = allOrders.stream()
            .filter(o -> o.getStatus() == OrderStatus.DELIVERING || o.getStatus() == OrderStatus.CONFIRMED || o.getStatus() == OrderStatus.ASSIGNED)
            .count();
        long totalFailed = allOrders.stream()
            .filter(o -> o.getStatus() == OrderStatus.CANCELED || o.getStatus() == OrderStatus.FAILED)
            .count();
        
        // Build shipper data with real statistics
        List<Map<String, Object>> shippers = new ArrayList<>();
        for (User shipper : shipperUsers) {
            List<Order> shipperOrders = allOrders.stream()
                .filter(o -> o.getShipper() != null && o.getShipper().getId().equals(shipper.getId()))
                .collect(Collectors.toList());
            
            long totalOrders = shipperOrders.size();
            long successOrders = shipperOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
                .count();
            long failedOrders = shipperOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.CANCELED || o.getStatus() == OrderStatus.FAILED)
                .count();
            double successRate = totalOrders > 0 ? (successOrders * 100.0 / totalOrders) : 0;
            
            // Calculate earnings (assume 20,000 VND per delivered order as shipping fee)
            double totalEarnings = successOrders * 20000;
            
            Map<String, Object> shipperData = new HashMap<>();
            shipperData.put("shipperId", shipper.getId());
            shipperData.put("fullName", shipper.getFirstName() + " " + shipper.getLastName());
            shipperData.put("email", shipper.getEmail());
            shipperData.put("phone", shipper.getPhone() != null ? shipper.getPhone() : "Chưa cập nhật");
            shipperData.put("totalOrders", totalOrders);
            shipperData.put("successOrders", successOrders);
            shipperData.put("failedOrders", failedOrders);
            shipperData.put("successRate", Math.round(successRate));
            shipperData.put("totalEarnings", totalEarnings);
            shipperData.put("isActive", shipper.isEnabled());
            shippers.add(shipperData);
        }
        
        // Sort by total orders descending
        shippers.sort((a, b) -> Long.compare((Long)b.get("totalOrders"), (Long)a.get("totalOrders")));
        
        // Get recent deliveries (last 10 orders with shipper)
        List<Map<String, Object>> recentDeliveries = allOrders.stream()
            .filter(o -> o.getShipper() != null)
            .sorted((a, b) -> b.getUpdatedAt().compareTo(a.getUpdatedAt()))
            .limit(10)
            .map(order -> {
                Map<String, Object> delivery = new HashMap<>();
                delivery.put("orderId", order.getId());
                delivery.put("shipperName", order.getShipper().getFirstName() + " " + order.getShipper().getLastName());
                delivery.put("customerName", order.getShippingName());
                delivery.put("customerPhone", order.getShippingPhone());
                delivery.put("deliveryAddress", order.getShippingAddress());
                delivery.put("orderValue", order.getTotalPrice());
                delivery.put("shippingFee", 20000.0);
                delivery.put("status", order.getStatus().name());
                delivery.put("statusText", getOrderStatusText(order.getStatus()));
                delivery.put("updatedAt", order.getUpdatedAt());
                return delivery;
            })
            .collect(Collectors.toList());
        
        model.addAttribute("shippers", shippers);
        model.addAttribute("recentDeliveries", recentDeliveries);
        model.addAttribute("totalShippers", shippers.size());
        model.addAttribute("totalDelivered", totalDelivered);
        model.addAttribute("totalInProgress", totalInProgress);
        model.addAttribute("totalFailed", totalFailed);
        
        return "admin/shippers";
    }
    
    private String getOrderStatusText(OrderStatus status) {
        switch (status) {
            case PENDING: return "Chờ xác nhận";
            case CONFIRMED: return "Đã xác nhận";
            case ASSIGNED: return "Đã giao cho shipper";
            case DELIVERING: return "Đang giao";
            case DELIVERED: return "Đã giao";
            case FAILED: return "Giao thất bại";
            case CANCELED: return "Đã hủy";
            default: return status.name();
        }
    }

    @GetMapping("/shippers/{shipperId}/details")
    @ResponseBody
    public ResponseEntity<?> getShipperDetails(@PathVariable Long shipperId) {
        Optional<User> shipperOpt = userRepository.findById(shipperId);
        if (shipperOpt.isPresent()) {
            User shipper = shipperOpt.get();
            
            // Get shipper's orders
            List<Order> shipperOrders = orderRepository.findAll().stream()
                .filter(o -> o.getShipper() != null && o.getShipper().getId().equals(shipperId))
                .collect(Collectors.toList());
            
            long totalOrders = shipperOrders.size();
            long successOrders = shipperOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
                .count();
            long failedOrders = shipperOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.CANCELED || o.getStatus() == OrderStatus.FAILED)
                .count();
            double totalEarnings = successOrders * 20000;
            
            Map<String, Object> shipperDetails = new HashMap<>();
            shipperDetails.put("shipperId", shipper.getId());
            shipperDetails.put("fullName", shipper.getFirstName() + " " + shipper.getLastName());
            shipperDetails.put("email", shipper.getEmail());
            shipperDetails.put("phone", shipper.getPhone() != null ? shipper.getPhone() : "Chưa cập nhật");
            shipperDetails.put("totalOrders", totalOrders);
            shipperDetails.put("successOrders", successOrders);
            shipperDetails.put("failedOrders", failedOrders);
            shipperDetails.put("totalEarnings", totalEarnings);
            shipperDetails.put("isActive", shipper.isEnabled());
            shipperDetails.put("createdAt", shipper.getCreatedAt());
            shipperDetails.put("updatedAt", shipper.getUpdatedAt());
            return ResponseEntity.ok(shipperDetails);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/shippers/{shipperId}/orders")
    public String shipperOrders(@PathVariable Long shipperId, Model model) {
        Optional<User> shipperOpt = userRepository.findById(shipperId);
        if (!shipperOpt.isPresent()) {
            return "redirect:/admin/shippers";
        }
        
        User shipper = shipperOpt.get();
        List<Order> shipperOrders = orderRepository.findAll().stream()
            .filter(o -> o.getShipper() != null && o.getShipper().getId().equals(shipperId))
            .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
            .collect(Collectors.toList());
        
        model.addAttribute("shipper", shipper);
        model.addAttribute("orders", shipperOrders);
        model.addAttribute("shipperName", shipper.getFirstName() + " " + shipper.getLastName());
        
        return "admin/shipper-orders";
    }

    // ===== VOUCHER MANAGEMENT =====
    @GetMapping("/vouchers")
    public String adminVouchers(Model model) {
        // Lấy tất cả vouchers
        List<Voucher> allVouchers = voucherRepository.findAll();
        
        // Tính statistics
        long totalVouchers = allVouchers.size();
        long activeVouchers = voucherRepository.countActiveVouchers(java.time.LocalDate.now());
        
        Long totalUsedRaw = voucherRepository.getTotalUsedQuantity();
        long totalUsed = (totalUsedRaw != null) ? totalUsedRaw : 0;
        
        // Tính tổng discount đã áp dụng (ước tính 50k/voucher)
        // Note: Sẽ chính xác hơn khi Order có field discountAmount (phần checkout sẽ implement)
        double totalDiscount = totalUsed * 50000.0;
        
        // Sort vouchers: Active trước, mới nhất trước
        List<Voucher> sortedVouchers = allVouchers.stream()
            .sorted((v1, v2) -> {
                // Active vouchers first
                if (v1.isActive() != v2.isActive()) {
                    return v1.isActive() ? -1 : 1;
                }
                // Then by created date (newest first)
                return v2.getCreatedAt().compareTo(v1.getCreatedAt());
            })
            .collect(Collectors.toList());
        
        model.addAttribute("vouchers", sortedVouchers);
        model.addAttribute("discountTypes", DiscountType.values()); // Thêm enum để hiển thị trong form
        model.addAttribute("totalVouchers", totalVouchers);
        model.addAttribute("activeVouchers", activeVouchers);
        model.addAttribute("totalUsed", totalUsed);
        model.addAttribute("totalDiscount", totalDiscount);
        
        return "admin/vouchers";
    }

    @PostMapping("/vouchers/create")
    public String createVoucher(@RequestParam String code,
                                @RequestParam String name,
                                @RequestParam(required = false) String description,
                                @RequestParam String discountType,
                                @RequestParam Double discountValue,
                                @RequestParam(required = false) Double maxDiscount,
                                @RequestParam Double minOrderValue,
                                @RequestParam Integer totalQuantity,
                                @RequestParam(required = false) Integer usageLimit,
                                @RequestParam String startDate,
                                @RequestParam String endDate,
                                @RequestParam(required = false) List<Long> productIds,
                                @RequestParam(required = false) String isActive,
                                RedirectAttributes redirectAttributes) {
        try {
            // Kiểm tra mã voucher đã tồn tại chưa
            if (voucherRepository.findByCode(code).isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Mã voucher '" + code + "' đã tồn tại!");
                return "redirect:/admin/vouchers";
            }
            
            // Tạo voucher mới
            Voucher voucher = new Voucher();
            voucher.setCode(code.toUpperCase());
            voucher.setName(name);
            voucher.setDescription(description);
            voucher.setDiscountType(DiscountType.valueOf(discountType));
            voucher.setDiscountValue(BigDecimal.valueOf(discountValue));
            
            if (maxDiscount != null && maxDiscount > 0) {
                voucher.setMaxDiscount(BigDecimal.valueOf(maxDiscount));
            }
            
            voucher.setMinOrderValue(BigDecimal.valueOf(minOrderValue));
            voucher.setTotalQuantity(totalQuantity);
            voucher.setUsedQuantity(0);
            voucher.setUsageLimit(usageLimit != null ? usageLimit : 1);
            voucher.setStartDate(java.time.LocalDate.parse(startDate));
            voucher.setEndDate(java.time.LocalDate.parse(endDate));
            voucher.setActive("on".equals(isActive) || "true".equals(isActive));
            
            // Lấy thông tin user hiện tại
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                User currentUser = userRepository.findByUsername(auth.getName()).orElse(null);
                voucher.setCreatedBy(currentUser);
            }
            
            voucher.setCreatedAt(LocalDateTime.now());
            voucher.setUpdatedAt(LocalDateTime.now());
            
            voucherRepository.save(voucher);
            redirectAttributes.addFlashAttribute("success", "Tạo voucher '" + code + "' thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }
        return "redirect:/admin/vouchers";
    }

    @GetMapping("/vouchers/{id}/edit")
    @ResponseBody
    public ResponseEntity<?> getVoucherForEdit(@PathVariable Long id) {
        try {
            Optional<Voucher> voucherOpt = voucherRepository.findById(id);
            if (voucherOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Không tìm thấy voucher"));
            }
            
            Voucher voucher = voucherOpt.get();
            Map<String, Object> response = new HashMap<>();
            response.put("id", voucher.getId());
            response.put("code", voucher.getCode());
            response.put("name", voucher.getName());
            response.put("description", voucher.getDescription());
            response.put("discountType", voucher.getDiscountType().name());
            response.put("discountValue", voucher.getDiscountValue());
            response.put("maxDiscount", voucher.getMaxDiscount());
            response.put("minOrderValue", voucher.getMinOrderValue());
            response.put("totalQuantity", voucher.getTotalQuantity());
            response.put("usageLimit", voucher.getUsageLimit());
            response.put("startDate", voucher.getStartDate().toString());
            response.put("endDate", voucher.getEndDate().toString());
            response.put("isActive", voucher.isActive());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", "Có lỗi xảy ra: " + e.getMessage()));
        }
    }

    @PostMapping("/vouchers/update")
    public String updateVoucher(@RequestParam Long voucherId,
                                @RequestParam String code,
                                @RequestParam String name,
                                @RequestParam(required = false) String description,
                                @RequestParam String discountType,
                                @RequestParam Double discountValue,
                                @RequestParam(required = false) Double maxDiscount,
                                @RequestParam Double minOrderValue,
                                @RequestParam Integer totalQuantity,
                                @RequestParam(required = false) Integer usageLimit,
                                @RequestParam String startDate,
                                @RequestParam String endDate,
                                @RequestParam(required = false) List<Long> productIds,
                                @RequestParam(required = false) String isActive,
                                RedirectAttributes redirectAttributes) {
        try {
            Optional<Voucher> voucherOpt = voucherRepository.findById(voucherId);
            if (voucherOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy voucher!");
                return "redirect:/admin/vouchers";
            }
            
            Voucher voucher = voucherOpt.get();
            
            // Kiểm tra mã voucher nếu thay đổi
            if (!voucher.getCode().equals(code.toUpperCase())) {
                if (voucherRepository.findByCode(code).isPresent()) {
                    redirectAttributes.addFlashAttribute("error", "Mã voucher '" + code + "' đã tồn tại!");
                    return "redirect:/admin/vouchers";
                }
                voucher.setCode(code.toUpperCase());
            }
            
            // Cập nhật thông tin
            voucher.setName(name);
            voucher.setDescription(description);
            voucher.setDiscountType(DiscountType.valueOf(discountType));
            voucher.setDiscountValue(BigDecimal.valueOf(discountValue));
            
            if (maxDiscount != null && maxDiscount > 0) {
                voucher.setMaxDiscount(BigDecimal.valueOf(maxDiscount));
            } else {
                voucher.setMaxDiscount(null);
            }
            
            voucher.setMinOrderValue(BigDecimal.valueOf(minOrderValue));
            voucher.setTotalQuantity(totalQuantity);
            voucher.setUsageLimit(usageLimit != null ? usageLimit : 1);
            voucher.setStartDate(java.time.LocalDate.parse(startDate));
            voucher.setEndDate(java.time.LocalDate.parse(endDate));
            voucher.setActive("on".equals(isActive) || "true".equals(isActive));
            voucher.setUpdatedAt(LocalDateTime.now());
            
            voucherRepository.save(voucher);
            redirectAttributes.addFlashAttribute("success", "Cập nhật voucher '" + code + "' thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }
        return "redirect:/admin/vouchers";
    }

    @PostMapping("/vouchers/{id}/toggle-status")
    @ResponseBody
    public ResponseEntity<?> toggleVoucherStatus(@PathVariable Long id) {
        try {
            Optional<Voucher> voucherOpt = voucherRepository.findById(id);
            if (voucherOpt.isEmpty()) {
                return ResponseEntity.ok(Map.of("success", false, "message", "Không tìm thấy voucher"));
            }
            
            Voucher voucher = voucherOpt.get();
            voucher.setActive(!voucher.isActive());
            voucher.setUpdatedAt(LocalDateTime.now());
            voucherRepository.save(voucher);
            
            String status = voucher.isActive() ? "kích hoạt" : "vô hiệu hóa";
            return ResponseEntity.ok(Map.of("success", true, "message", "Đã " + status + " voucher thành công"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(Map.of("success", false, "message", "Có lỗi xảy ra: " + e.getMessage()));
        }
    }

    @GetMapping("/products/all")
    @ResponseBody
    public ResponseEntity<?> getAllProducts() {
        try {
            List<Product> products = productRepository.findAll();
            List<Map<String, Object>> response = products.stream()
                .filter(Product::isActive)
                .map(product -> {
                    Map<String, Object> productMap = new HashMap<>();
                    productMap.put("id", product.getId());
                    productMap.put("name", product.getName());
                    productMap.put("price", product.getMinPrice());
                    return productMap;
                })
                .collect(Collectors.toList());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    // ===== EXISTING PAGES =====
    @GetMapping("/products")
    public String adminProducts(Model model,
                               @RequestParam(required = false) String search,
                               @RequestParam(required = false) Long sellerId,
                               @RequestParam(required = false) Boolean active) {
        // Load products with images and seller (first query)
        List<Product> products = productRepository.findAllWithImages();
        
        // Load variants separately (second query)
        if (!products.isEmpty()) {
            List<Long> productIds = products.stream().map(Product::getId).collect(Collectors.toList());
            productRepository.findAllWithVariants(productIds);
        }
        
        // Filter by search term
        if (search != null && !search.isEmpty()) {
            products = products.stream()
                .filter(p -> p.getName().toLowerCase().contains(search.toLowerCase()) ||
                           (p.getBrand() != null && p.getBrand().toLowerCase().contains(search.toLowerCase())))
                .collect(Collectors.toList());
        }
        
        // Filter by seller
        if (sellerId != null) {
            products = products.stream()
                .filter(p -> p.getSeller() != null && p.getSeller().getId().equals(sellerId))
                .collect(Collectors.toList());
        }
        
        // Filter by active status
        if (active != null) {
            products = products.stream()
                .filter(p -> p.isActive() == active)
                .collect(Collectors.toList());
        }
        
        // Get all sellers for filter
        List<User> sellers = userRepository.findAll().stream()
            .filter(u -> u.getRoles() != null && u.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ROLE_SELLER")))
            .collect(Collectors.toList());
        
        // Calculate statistics
        long totalProducts = products.size();
        long activeProducts = products.stream().filter(Product::isActive).count();
        long inactiveProducts = totalProducts - activeProducts;
        long totalVariants = products.stream()
            .mapToInt(p -> p.getVariants() != null ? p.getVariants().size() : 0)
            .sum();
        
        model.addAttribute("products", products);
        model.addAttribute("sellers", sellers);
        model.addAttribute("searchTerm", search);
        model.addAttribute("sellerIdFilter", sellerId);
        model.addAttribute("activeFilter", active);
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("activeProducts", activeProducts);
        model.addAttribute("inactiveProducts", inactiveProducts);
        model.addAttribute("totalVariants", totalVariants);
        
        return "admin/products";
    }

    @GetMapping("/customers")
    public String adminCustomers(Model model) {
        return "admin/customers";
    }

    @GetMapping("/transactions")
    public String adminTransactions(Model model, 
                                   @RequestParam(required = false) String search,
                                   @RequestParam(required = false) String status) {
        List<Order> orders = orderRepository.findAll();
        
        // Filter by search term
        if (search != null && !search.isEmpty()) {
            orders = orders.stream()
                .filter(o -> o.getOrderCode().toLowerCase().contains(search.toLowerCase()) ||
                           (o.getShippingName() != null && o.getShippingName().toLowerCase().contains(search.toLowerCase())) ||
                           (o.getShippingPhone() != null && o.getShippingPhone().contains(search)))
                .collect(Collectors.toList());
        }
        
        // Filter by status
        if (status != null && !status.isEmpty()) {
            OrderStatus filterStatus = OrderStatus.valueOf(status.toUpperCase());
            orders = orders.stream()
                .filter(o -> o.getStatus() == filterStatus)
                .collect(Collectors.toList());
        }
        
        // Sort by newest first
        orders.sort((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()));
        
        model.addAttribute("orders", orders);
        model.addAttribute("searchTerm", search);
        model.addAttribute("statusFilter", status);
        
        return "admin/transactions";
    }

    // ===== ADMIN PROFILE =====
    @GetMapping("/profile")
    public String adminProfile(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            String username = authentication.getName();
            User currentUser = userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .orElse(null);
            model.addAttribute("currentUser", currentUser);
        }
        return "admin/admin-profile";
    }
    
    @GetMapping("/profile/edit")
    public String editProfile(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            String username = authentication.getName();
            User currentUser = userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .orElse(null);
            model.addAttribute("currentUser", currentUser);
        }
        return "admin/edit-profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@ModelAttribute("currentUser") User formUser,
                                RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            redirectAttributes.addFlashAttribute("error", "Bạn chưa đăng nhập");
            return "redirect:/login";
        }
        
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .orElse(null);
                
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy người dùng");
            return "redirect:/admin/profile";
        }

        user.setFirstName(formUser.getFirstName());
        user.setLastName(formUser.getLastName());
        user.setPhone(formUser.getPhone());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        
        redirectAttributes.addFlashAttribute("success", "Cập nhật hồ sơ thành công");
        return "redirect:/admin/profile";
    }

    @PostMapping("/profile/change-password")
    public String changePassword(@RequestParam("currentPassword") String currentPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 @RequestParam("confirmNewPassword") String confirmNewPassword,
                                 RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            redirectAttributes.addFlashAttribute("error", "Bạn chưa đăng nhập");
            return "redirect:/login";
        }
        
        // Kiểm tra mật khẩu mới khớp với xác nhận
        if (!newPassword.equals(confirmNewPassword)) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu mới và xác nhận không khớp!");
            return "redirect:/admin/profile";
        }
        
        // Kiểm tra độ dài mật khẩu mới
        if (newPassword.length() < 6) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu mới phải có ít nhất 6 ký tự!");
            return "redirect:/admin/profile";
        }
        
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .orElse(null);
                
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy người dùng");
            return "redirect:/admin/profile";
        }
        
        // Kiểm tra mật khẩu hiện tại
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu hiện tại không đúng!");
            return "redirect:/admin/profile";
        }
        
        // Cập nhật mật khẩu mới
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        
        redirectAttributes.addFlashAttribute("success", "Đổi mật khẩu thành công!");
        return "redirect:/admin/profile";
    }

    @PostMapping("/profile/avatar")
    public String uploadAvatar(@RequestParam("file") MultipartFile file,
                               RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            redirectAttributes.addFlashAttribute("error", "Bạn chưa đăng nhập");
            return "redirect:/login";
        }
        
        if (file == null || file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng chọn ảnh hợp lệ");
            return "redirect:/admin/profile";
        }
        
        try {
            // Upload lên Cloudinary
            String imageUrl = cloudinaryService.uploadImage(file, "admin-avatars");

            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .or(() -> userRepository.findByEmail(username))
                    .orElse(null);
                    
            if (user != null) {
                // Xóa ảnh cũ trên Cloudinary nếu có
                if (user.getAvatarUrl() != null && user.getAvatarUrl().contains("cloudinary.com")) {
                    try {
                        String oldPublicId = cloudinaryService.extractPublicId(user.getAvatarUrl());
                        cloudinaryService.deleteImage(oldPublicId);
                    } catch (Exception e) {
                        // Không quan trọng nếu xóa ảnh cũ thất bại
                        System.err.println("Không thể xóa ảnh cũ: " + e.getMessage());
                    }
                }
                
                user.setAvatarUrl(imageUrl);
                user.setUpdatedAt(LocalDateTime.now());
                userRepository.save(user);
            }
            
            redirectAttributes.addFlashAttribute("success", "Cập nhật ảnh đại diện thành công");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi tải ảnh: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/admin/profile";
    }
}

