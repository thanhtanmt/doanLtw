package com.example.clothesshop.controller;

import com.example.clothesshop.model.*;
import com.example.clothesshop.repository.*;
import com.example.clothesshop.service.CloudinaryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/seller")
public class SellerController {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final CloudinaryService cloudinaryService;
    private final OrderRepository orderRepository;
    private final VoucherRepository voucherRepository;

    public SellerController(UserRepository userRepository, ProductRepository productRepository, 
                          CategoryRepository categoryRepository, CloudinaryService cloudinaryService,
                          OrderRepository orderRepository, VoucherRepository voucherRepository) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.cloudinaryService = cloudinaryService;
        this.orderRepository = orderRepository;
        this.voucherRepository = voucherRepository;
    }

    @GetMapping
    public String seller(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            User seller = userRepository.findByUsername(username)
                    .or(() -> userRepository.findByEmail(username))
                    .orElse(null);
            
            if (seller != null) {
                // Get seller's products
                List<Product> products = productRepository.findBySeller(seller);
                model.addAttribute("products", products);
                model.addAttribute("seller", seller);
                
                // Calculate statistics
                long totalProducts = products.size();
                long activeProducts = products.stream().filter(Product::isActive).count();
                int totalStock = products.stream()
                        .filter(p -> p.getVariants() != null)
                        .flatMap(p -> p.getVariants().stream())
                        .filter(v -> v.getQuantity() != null)
                        .mapToInt(ProductVariant::getQuantity)
                        .sum();
                
                // Get orders containing seller's products
                List<Order> allOrders = orderRepository.findAll();
                List<Order> sellerOrders = allOrders.stream()
                        .filter(order -> order.getOrderDetails().stream()
                                .anyMatch(detail -> detail.getVariant() != null && 
                                         detail.getVariant().getProduct().getSeller() != null &&
                                         detail.getVariant().getProduct().getSeller().getId().equals(seller.getId())))
                        .collect(Collectors.toList());
                
                long totalOrders = sellerOrders.size();
                BigDecimal totalRevenue = sellerOrders.stream()
                        .filter(order -> order.getStatus() == OrderStatus.DELIVERED)
                        .map(Order::getTotalPrice)
                        .filter(price -> price != null)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                model.addAttribute("totalProducts", totalProducts);
                model.addAttribute("activeProducts", activeProducts);
                model.addAttribute("totalStock", totalStock);
                model.addAttribute("totalOrders", totalOrders);
                model.addAttribute("totalRevenue", totalRevenue);
                model.addAttribute("sellerOrders", sellerOrders);
            }
        }
        return "seller";
    }
    
    // ===== PRODUCT MANAGEMENT APIS =====
    
    /**
     * API: Get all products of current seller
     * GET /seller/api/products
     */
    @GetMapping("/api/products")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getSellerProducts() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User seller = getCurrentSeller();
            List<Product> products = productRepository.findBySeller(seller);
            
            List<Map<String, Object>> productData = products.stream()
                    .map(this::convertProductToMap)
                    .collect(Collectors.toList());
            
            response.put("success", true);
            response.put("data", productData);
            response.put("total", products.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * API: Get product by ID (only seller's products)
     * GET /seller/api/products/{id}
     */
    @GetMapping("/api/products/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getProductById(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User seller = getCurrentSeller();
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm"));
            
            // Check ownership
            if (product.getSeller() == null || !product.getSeller().getId().equals(seller.getId())) {
                response.put("success", false);
                response.put("message", "Bạn không có quyền truy cập sản phẩm này");
                return ResponseEntity.status(403).body(response);
            }
            
            response.put("success", true);
            response.put("data", convertProductToMap(product));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * API: Create new product
     * POST /seller/api/products
     */
    @PostMapping("/api/products")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createProduct(
            @RequestParam String name,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String detail,
            @RequestParam(required = false) String specification,
            @RequestParam(required = false) String material,
            @RequestParam Long categoryId,
            @RequestParam(required = false) List<MultipartFile> images,
            @RequestParam(required = false) String variantsJson) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            User seller = getCurrentSeller();
            
            // Create product
            Product product = new Product();
            product.setName(name);
            product.setBrand(brand);
            product.setGender(gender);
            product.setDescription(description);
            product.setDetail(detail);
            product.setSpecification(specification);
            product.setMaterial(material);
            product.setActive(true);
            product.setSeller(seller);
            
            // Set category
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục"));
            product.setCategory(category);
            
            // Save product first to get ID
            product = productRepository.save(product);
            
            // Upload images
            if (images != null && !images.isEmpty()) {
                for (MultipartFile file : images) {
                    if (!file.isEmpty()) {
                        String imageUrl = cloudinaryService.uploadImage(file, "products");
                        ProductImage productImage = new ProductImage();
                        productImage.setProduct(product);
                        productImage.setUrl(imageUrl);
                        product.getImages().add(productImage);
                    }
                }
            }
            
            // Parse and add variants
            if (variantsJson != null && !variantsJson.trim().isEmpty()) {
                // Expected format: [{"size":"S","price":250000,"quantity":50,"sku":"PRO-S"}]
                // Simple parsing - in production use Jackson
                List<Map<String, String>> variants = parseVariantsJson(variantsJson);
                for (Map<String, String> variantData : variants) {
                    ProductVariant variant = new ProductVariant();
                    variant.setProduct(product);
                    variant.setSize(variantData.get("size"));
                    variant.setPrice(new BigDecimal(variantData.get("price")));
                    variant.setQuantity(Integer.parseInt(variantData.get("quantity")));
                    variant.setSku(variantData.get("sku"));
                    variant.setAvailable(true);
                    product.getVariants().add(variant);
                }
            }
            
            // Save with images and variants
            product = productRepository.save(product);
            
            response.put("success", true);
            response.put("message", "Tạo sản phẩm thành công");
            response.put("data", convertProductToMap(product));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * API: Update product
     * PUT /seller/api/products/{id}
     */
    @PutMapping("/api/products/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateProduct(
            @PathVariable Long id,
            @RequestParam String name,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String detail,
            @RequestParam(required = false) String specification,
            @RequestParam(required = false) String material,
            @RequestParam Long categoryId,
            @RequestParam(required = false) List<MultipartFile> images,
            @RequestParam(required = false) String deletedImageIds,
            @RequestParam(required = false) String variantsJson) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            User seller = getCurrentSeller();
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm"));
            
            // Check ownership
            if (product.getSeller() == null || !product.getSeller().getId().equals(seller.getId())) {
                response.put("success", false);
                response.put("message", "Bạn không có quyền sửa sản phẩm này");
                return ResponseEntity.status(403).body(response);
            }
            
            // Update product info
            product.setName(name);
            product.setBrand(brand);
            product.setGender(gender);
            product.setDescription(description);
            product.setDetail(detail);
            product.setSpecification(specification);
            product.setMaterial(material);
            product.setUpdatedAt(LocalDateTime.now());
            
            // Update category
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục"));
            product.setCategory(category);
            
            // Delete images if requested
            if (deletedImageIds != null && !deletedImageIds.trim().isEmpty()) {
                try {
                    // Parse JSON array: [1, 2, 3]
                    String ids = deletedImageIds.trim();
                    if (ids.startsWith("[") && ids.endsWith("]")) {
                        ids = ids.substring(1, ids.length() - 1);
                    }
                    if (!ids.isEmpty()) {
                        String[] idArray = ids.split(",");
                        for (String idStr : idArray) {
                            Long imageId = Long.parseLong(idStr.trim());
                            // Remove from product's images list
                            product.getImages().removeIf(img -> img.getId().equals(imageId));
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Error parsing deleted image IDs: " + e.getMessage());
                }
            }
            
            // Add new images if provided
            if (images != null && !images.isEmpty()) {
                for (MultipartFile file : images) {
                    if (!file.isEmpty()) {
                        String imageUrl = cloudinaryService.uploadImage(file, "products");
                        ProductImage productImage = new ProductImage();
                        productImage.setProduct(product);
                        productImage.setUrl(imageUrl);
                        product.getImages().add(productImage);
                    }
                }
            }
            
            // Update variants if provided
            if (variantsJson != null && !variantsJson.trim().isEmpty()) {
                // Clear existing variants
                product.getVariants().clear();
                
                // Add new variants
                List<Map<String, String>> variants = parseVariantsJson(variantsJson);
                for (Map<String, String> variantData : variants) {
                    ProductVariant variant = new ProductVariant();
                    variant.setProduct(product);
                    variant.setSize(variantData.get("size"));
                    variant.setPrice(new BigDecimal(variantData.get("price")));
                    variant.setQuantity(Integer.parseInt(variantData.get("quantity")));
                    variant.setSku(variantData.get("sku"));
                    variant.setAvailable(true);
                    product.getVariants().add(variant);
                }
            }
            
            product = productRepository.save(product);
            
            response.put("success", true);
            response.put("message", "Cập nhật sản phẩm thành công");
            response.put("data", convertProductToMap(product));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * API: Delete product (soft delete - set active=false)
     * DELETE /seller/api/products/{id}
     */
    @DeleteMapping("/api/products/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteProduct(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User seller = getCurrentSeller();
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm"));
            
            // Check ownership
            if (product.getSeller() == null || !product.getSeller().getId().equals(seller.getId())) {
                response.put("success", false);
                response.put("message", "Bạn không có quyền xóa sản phẩm này");
                return ResponseEntity.status(403).body(response);
            }
            
            // Soft delete
            product.setActive(false);
            product.setUpdatedAt(LocalDateTime.now());
            productRepository.save(product);
            
            response.put("success", true);
            response.put("message", "Xóa sản phẩm thành công");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * API: Toggle product active status
     * POST /seller/api/products/{id}/toggle
     */
    @PostMapping("/api/products/{id}/toggle")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleProductStatus(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User seller = getCurrentSeller();
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm"));
            
            // Check ownership
            if (product.getSeller() == null || !product.getSeller().getId().equals(seller.getId())) {
                response.put("success", false);
                response.put("message", "Bạn không có quyền thay đổi trạng thái sản phẩm này");
                return ResponseEntity.status(403).body(response);
            }
            
            // Toggle active status
            product.setActive(!product.isActive());
            product.setUpdatedAt(LocalDateTime.now());
            productRepository.save(product);
            
            String status = product.isActive() ? "kích hoạt" : "vô hiệu hóa";
            response.put("success", true);
            response.put("message", "Đã " + status + " sản phẩm thành công");
            response.put("active", product.isActive());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // ===== HELPER METHODS =====
    
    private User getCurrentSeller() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Bạn chưa đăng nhập");
        }
        
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy người dùng"));
    }
    
    private Map<String, Object> convertProductToMap(Product product) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", product.getId());
        map.put("name", product.getName());
        map.put("brand", product.getBrand());
        map.put("gender", product.getGender());
        map.put("description", product.getDescription());
        map.put("detail", product.getDetail());
        map.put("specification", product.getSpecification());
        map.put("material", product.getMaterial());
        map.put("active", product.isActive());
        map.put("createdAt", product.getCreatedAt());
        map.put("updatedAt", product.getUpdatedAt());
        
        if (product.getCategory() != null) {
            Map<String, Object> categoryMap = new HashMap<>();
            categoryMap.put("id", product.getCategory().getId());
            categoryMap.put("name", product.getCategory().getName());
            map.put("category", categoryMap);
        }
        
        List<Map<String, Object>> images = product.getImages().stream()
                .map(img -> {
                    Map<String, Object> imgMap = new HashMap<>();
                    imgMap.put("id", img.getId());
                    imgMap.put("url", img.getUrl());
                    return imgMap;
                })
                .collect(Collectors.toList());
        map.put("images", images);
        
        List<Map<String, Object>> variants = product.getVariants().stream()
                .map(v -> {
                    Map<String, Object> vMap = new HashMap<>();
                    vMap.put("id", v.getId());
                    vMap.put("size", v.getSize());
                    vMap.put("price", v.getPrice());
                    vMap.put("quantity", v.getQuantity());
                    vMap.put("sku", v.getSku());
                    vMap.put("available", v.isAvailable());
                    return vMap;
                })
                .collect(Collectors.toList());
        map.put("variants", variants);
        
        map.put("minPrice", product.getMinPrice());
        map.put("maxPrice", product.getMaxPrice());
        map.put("totalQuantity", product.getTotalQuantity());
        map.put("hasStock", product.hasStock());
        
        return map;
    }
    
    private List<Map<String, String>> parseVariantsJson(String json) {
        // Simple JSON parsing - in production use Jackson or Gson
        List<Map<String, String>> variants = new ArrayList<>();
        
        // Remove brackets and split by objects
        json = json.trim().replaceAll("^\\[|\\]$", "");
        if (json.isEmpty()) return variants;
        
        String[] objects = json.split("\\},\\s*\\{");
        for (String obj : objects) {
            obj = obj.replaceAll("^\\{|\\}$", "");
            Map<String, String> variant = new HashMap<>();
            
            String[] pairs = obj.split(",");
            for (String pair : pairs) {
                String[] keyValue = pair.split(":");
                if (keyValue.length == 2) {
                    String key = keyValue[0].trim().replaceAll("\"", "");
                    String value = keyValue[1].trim().replaceAll("\"", "");
                    variant.put(key, value);
                }
            }
            if (!variant.isEmpty()) {
                variants.add(variant);
            }
        }
        
        return variants;
    }
    
    // ===== ORDER MANAGEMENT APIs =====
    
    /**
     * API: Get seller's orders (orders containing seller's products)
     * GET /seller/api/orders
     */
    @GetMapping("/api/orders")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getSellerOrders(
            @RequestParam(required = false) String status) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User seller = getCurrentSeller();
            
            // Get all orders with order details containing seller's products
            List<Order> allOrders = orderRepository.findAll();
            
            List<Map<String, Object>> sellerOrders = allOrders.stream()
                    .filter(order -> {
                        // Check if order contains any product from this seller
                        boolean hasSellersProduct = order.getOrderDetails().stream()
                                .anyMatch(detail -> detail.getProduct() != null 
                                        && detail.getProduct().getSeller() != null
                                        && detail.getProduct().getSeller().getId().equals(seller.getId()));
                        
                        // Filter by status if provided
                        if (status != null && !status.isEmpty()) {
                            return hasSellersProduct && order.getStatus().name().equals(status);
                        }
                        return hasSellersProduct;
                    })
                    .map(this::convertOrderToMap)
                    .collect(Collectors.toList());
            
            response.put("success", true);
            response.put("data", sellerOrders);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * API: Get order details
     * GET /seller/api/orders/{id}
     */
    @GetMapping("/api/orders/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getOrderDetail(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User seller = getCurrentSeller();
            Order order = orderRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng"));
            
            // Check if order contains seller's products
            boolean hasSellersProduct = order.getOrderDetails().stream()
                    .anyMatch(detail -> detail.getProduct() != null 
                            && detail.getProduct().getSeller() != null
                            && detail.getProduct().getSeller().getId().equals(seller.getId()));
            
            if (!hasSellersProduct) {
                response.put("success", false);
                response.put("message", "Bạn không có quyền xem đơn hàng này");
                return ResponseEntity.status(403).body(response);
            }
            
            response.put("success", true);
            response.put("data", convertOrderToDetailMap(order, seller));
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * API: Confirm order (change status from PENDING to CONFIRMED)
     * POST /seller/api/orders/{id}/confirm
     */
    @PostMapping("/api/orders/{id}/confirm")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> confirmOrder(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User seller = getCurrentSeller();
            Order order = orderRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng"));
            
            // Check if order contains seller's products
            boolean hasSellersProduct = order.getOrderDetails().stream()
                    .anyMatch(detail -> detail.getProduct() != null 
                            && detail.getProduct().getSeller() != null
                            && detail.getProduct().getSeller().getId().equals(seller.getId()));
            
            if (!hasSellersProduct) {
                response.put("success", false);
                response.put("message", "Bạn không có quyền xác nhận đơn hàng này");
                return ResponseEntity.status(403).body(response);
            }
            
            // Check current status
            if (order.getStatus() != OrderStatus.PENDING) {
                response.put("success", false);
                response.put("message", "Chỉ có thể xác nhận đơn hàng ở trạng thái chờ xác nhận");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Update status
            order.setStatus(OrderStatus.CONFIRMED);
            orderRepository.save(order);
            
            response.put("success", true);
            response.put("message", "Đã xác nhận đơn hàng #" + order.getOrderCode());
            response.put("data", convertOrderToMap(order));
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * API: Cancel order
     * POST /seller/api/orders/{id}/cancel
     */
    @PostMapping("/api/orders/{id}/cancel")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> cancelOrder(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User seller = getCurrentSeller();
            Order order = orderRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng"));
            
            // Check if order contains seller's products
            boolean hasSellersProduct = order.getOrderDetails().stream()
                    .anyMatch(detail -> detail.getProduct() != null 
                            && detail.getProduct().getSeller() != null
                            && detail.getProduct().getSeller().getId().equals(seller.getId()));
            
            if (!hasSellersProduct) {
                response.put("success", false);
                response.put("message", "Bạn không có quyền hủy đơn hàng này");
                return ResponseEntity.status(403).body(response);
            }
            
            // Check if order can be canceled
            if (order.getStatus() == OrderStatus.DELIVERED || 
                order.getStatus() == OrderStatus.CANCELED) {
                response.put("success", false);
                response.put("message", "Không thể hủy đơn hàng ở trạng thái này");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Update status
            order.setStatus(OrderStatus.CANCELED);
            if (reason != null && !reason.trim().isEmpty()) {
                order.setFailureReason(reason);
            }
            orderRepository.save(order);
            
            response.put("success", true);
            response.put("message", "Đã hủy đơn hàng #" + order.getOrderCode());
            response.put("data", convertOrderToMap(order));
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // Helper methods for order conversion
    private Map<String, Object> convertOrderToMap(Order order) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", order.getId());
        map.put("orderCode", order.getOrderCode());
        map.put("status", order.getStatus().name());
        map.put("statusDisplay", getStatusDisplay(order.getStatus()));
        map.put("totalAmount", order.getTotalAmount());
        map.put("paymentMethod", order.getPaymentMethod());
        map.put("shippingName", order.getShippingName());
        map.put("shippingPhone", order.getShippingPhone());
        map.put("shippingAddress", order.getShippingAddress());
        map.put("createdAt", order.getCreatedAt());
        
        // Count items
        int totalItems = order.getOrderDetails().stream()
                .mapToInt(OrderDetail::getQuantity)
                .sum();
        map.put("totalItems", totalItems);
        
        return map;
    }
    
    private Map<String, Object> convertOrderToDetailMap(Order order, User seller) {
        Map<String, Object> map = convertOrderToMap(order);
        
        // Add customer info
        if (order.getUser() != null) {
            Map<String, Object> customer = new HashMap<>();
            customer.put("name", order.getUser().getFirstName() + " " + order.getUser().getLastName());
            customer.put("email", order.getUser().getEmail());
            customer.put("phone", order.getUser().getPhone());
            map.put("customer", customer);
        }
        
        // Add order details (only seller's products)
        List<Map<String, Object>> details = order.getOrderDetails().stream()
                .filter(detail -> detail.getProduct() != null 
                        && detail.getProduct().getSeller() != null
                        && detail.getProduct().getSeller().getId().equals(seller.getId()))
                .map(detail -> {
                    Map<String, Object> detailMap = new HashMap<>();
                    detailMap.put("id", detail.getId());
                    detailMap.put("productName", detail.getProduct().getName());
                    detailMap.put("size", detail.getSizeAtOrder());
                    detailMap.put("quantity", detail.getQuantity());
                    detailMap.put("unitPrice", detail.getUnitPrice());
                    detailMap.put("totalPrice", detail.getTotalPrice());
                    
                    // Add product image
                    if (detail.getProduct().getImages() != null && !detail.getProduct().getImages().isEmpty()) {
                        detailMap.put("image", detail.getProduct().getImages().get(0).getUrl());
                    }
                    
                    return detailMap;
                })
                .collect(Collectors.toList());
        map.put("orderDetails", details);
        
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
    
    // ===== DASHBOARD STATISTICS API =====
    
    /**
     * API: Get seller dashboard statistics
     * GET /seller/api/dashboard-stats
     */
    @GetMapping("/api/dashboard-stats")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User seller = getCurrentSeller();
            
            // Get seller's products
            List<Product> products = productRepository.findBySeller(seller);
            long totalProducts = products.size();
            long activeProducts = products.stream().filter(Product::isActive).count();
            
            // Calculate total stock
            int totalStock = products.stream()
                    .filter(p -> p.getVariants() != null)
                    .flatMap(p -> p.getVariants().stream())
                    .filter(v -> v.getQuantity() != null)
                    .mapToInt(ProductVariant::getQuantity)
                    .sum();
            
            // Get orders containing seller's products
            List<Order> allOrders = orderRepository.findAll();
            List<Order> sellerOrders = allOrders.stream()
                    .filter(order -> order.getOrderDetails().stream()
                            .anyMatch(detail -> detail.getVariant() != null && 
                                     detail.getVariant().getProduct().getSeller() != null &&
                                     detail.getVariant().getProduct().getSeller().getId().equals(seller.getId())))
                    .collect(Collectors.toList());
            
            long totalOrders = sellerOrders.size();
            
            // Calculate total revenue (only delivered orders)
            BigDecimal totalRevenue = sellerOrders.stream()
                    .filter(order -> order.getStatus() == OrderStatus.DELIVERED)
                    .map(Order::getTotalPrice)
                    .filter(price -> price != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // Calculate this month's revenue
            LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            BigDecimal monthlyRevenue = sellerOrders.stream()
                    .filter(order -> order.getStatus() == OrderStatus.DELIVERED)
                    .filter(order -> order.getCreatedAt() != null && order.getCreatedAt().isAfter(startOfMonth))
                    .map(Order::getTotalPrice)
                    .filter(price -> price != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // Count orders by status
            long pendingOrders = sellerOrders.stream()
                    .filter(order -> order.getStatus() == OrderStatus.PENDING)
                    .count();
            
            long confirmedOrders = sellerOrders.stream()
                    .filter(order -> order.getStatus() == OrderStatus.CONFIRMED)
                    .count();
            
            long deliveringOrders = sellerOrders.stream()
                    .filter(order -> order.getStatus() == OrderStatus.DELIVERING || 
                                   order.getStatus() == OrderStatus.ASSIGNED)
                    .count();
            
            long deliveredOrders = sellerOrders.stream()
                    .filter(order -> order.getStatus() == OrderStatus.DELIVERED)
                    .count();
            
            // Get vouchers count
            long totalVouchers = voucherRepository.findByCreatedByAndType(seller, VoucherType.SELLER).size();
            long activeVouchers = voucherRepository.findByCreatedByAndType(seller, VoucherType.SELLER).stream()
                    .filter(Voucher::isActive)
                    .count();
            
            // Build response
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalProducts", totalProducts);
            stats.put("activeProducts", activeProducts);
            stats.put("totalStock", totalStock);
            stats.put("totalOrders", totalOrders);
            stats.put("totalRevenue", totalRevenue);
            stats.put("monthlyRevenue", monthlyRevenue);
            stats.put("pendingOrders", pendingOrders);
            stats.put("confirmedOrders", confirmedOrders);
            stats.put("deliveringOrders", deliveringOrders);
            stats.put("deliveredOrders", deliveredOrders);
            stats.put("totalVouchers", totalVouchers);
            stats.put("activeVouchers", activeVouchers);
            
            response.put("success", true);
            response.put("data", stats);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // ===== VOUCHER MANAGEMENT APIs =====
    
    /**
     * API: Get seller's vouchers
     * GET /seller/api/vouchers
     */
    @GetMapping("/api/vouchers")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getSellerVouchers() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User seller = getCurrentSeller();
            
            // Get vouchers created by this seller
            List<Voucher> vouchers = voucherRepository.findByCreatedByAndType(seller, VoucherType.SELLER);
            
            List<Map<String, Object>> voucherList = vouchers.stream()
                    .map(this::convertVoucherToMap)
                    .collect(Collectors.toList());
            
            response.put("success", true);
            response.put("data", voucherList);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * API: Get voucher detail
     * GET /seller/api/vouchers/{id}
     */
    @GetMapping("/api/vouchers/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getVoucherDetail(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User seller = getCurrentSeller();
            Voucher voucher = voucherRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy voucher"));
            
            // Check ownership
            if (voucher.getCreatedBy() == null || !voucher.getCreatedBy().getId().equals(seller.getId())) {
                response.put("success", false);
                response.put("message", "Bạn không có quyền xem voucher này");
                return ResponseEntity.status(403).body(response);
            }
            
            response.put("success", true);
            response.put("data", convertVoucherToMap(voucher));
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * API: Create voucher
     * POST /seller/api/vouchers
     */
    @PostMapping("/api/vouchers")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createVoucher(
            @RequestParam String code,
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam String discountType,
            @RequestParam BigDecimal discountValue,
            @RequestParam(required = false) BigDecimal maxDiscount,
            @RequestParam(defaultValue = "0") BigDecimal minOrderValue,
            @RequestParam Integer totalQuantity,
            @RequestParam(defaultValue = "1") Integer usageLimit,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(defaultValue = "true") Boolean active) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            User seller = getCurrentSeller();
            
            // Check if code already exists
            if (voucherRepository.existsByCode(code)) {
                response.put("success", false);
                response.put("message", "Mã voucher đã tồn tại");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Create voucher
            Voucher voucher = new Voucher();
            voucher.setCode(code.toUpperCase());
            voucher.setName(name);
            voucher.setDescription(description);
            voucher.setDiscountType(DiscountType.valueOf(discountType));
            voucher.setDiscountValue(discountValue);
            voucher.setMaxDiscount(maxDiscount);
            voucher.setMinOrderValue(minOrderValue);
            voucher.setTotalQuantity(totalQuantity);
            voucher.setUsageLimit(usageLimit);
            voucher.setStartDate(LocalDate.parse(startDate));
            voucher.setEndDate(LocalDate.parse(endDate));
            voucher.setActive(active);
            voucher.setType(VoucherType.SELLER);
            voucher.setCreatedBy(seller);
            
            voucher = voucherRepository.save(voucher);
            
            response.put("success", true);
            response.put("message", "Tạo voucher thành công");
            response.put("data", convertVoucherToMap(voucher));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * API: Update voucher
     * PUT /seller/api/vouchers/{id}
     */
    @PutMapping("/api/vouchers/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateVoucher(
            @PathVariable Long id,
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam String discountType,
            @RequestParam BigDecimal discountValue,
            @RequestParam(required = false) BigDecimal maxDiscount,
            @RequestParam(defaultValue = "0") BigDecimal minOrderValue,
            @RequestParam Integer totalQuantity,
            @RequestParam(defaultValue = "1") Integer usageLimit,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(defaultValue = "true") Boolean active) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            User seller = getCurrentSeller();
            Voucher voucher = voucherRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy voucher"));
            
            // Check ownership
            if (voucher.getCreatedBy() == null || !voucher.getCreatedBy().getId().equals(seller.getId())) {
                response.put("success", false);
                response.put("message", "Bạn không có quyền sửa voucher này");
                return ResponseEntity.status(403).body(response);
            }
            
            // Update voucher
            voucher.setName(name);
            voucher.setDescription(description);
            voucher.setDiscountType(DiscountType.valueOf(discountType));
            voucher.setDiscountValue(discountValue);
            voucher.setMaxDiscount(maxDiscount);
            voucher.setMinOrderValue(minOrderValue);
            voucher.setTotalQuantity(totalQuantity);
            voucher.setUsageLimit(usageLimit);
            voucher.setStartDate(LocalDate.parse(startDate));
            voucher.setEndDate(LocalDate.parse(endDate));
            voucher.setActive(active);
            
            voucher = voucherRepository.save(voucher);
            
            response.put("success", true);
            response.put("message", "Cập nhật voucher thành công");
            response.put("data", convertVoucherToMap(voucher));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * API: Delete voucher
     * DELETE /seller/api/vouchers/{id}
     */
    @DeleteMapping("/api/vouchers/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteVoucher(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User seller = getCurrentSeller();
            Voucher voucher = voucherRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy voucher"));
            
            // Check ownership
            if (voucher.getCreatedBy() == null || !voucher.getCreatedBy().getId().equals(seller.getId())) {
                response.put("success", false);
                response.put("message", "Bạn không có quyền xóa voucher này");
                return ResponseEntity.status(403).body(response);
            }
            
            // Check if voucher is being used
            if (voucher.getUsedQuantity() > 0) {
                response.put("success", false);
                response.put("message", "Không thể xóa voucher đã được sử dụng. Bạn có thể vô hiệu hóa nó thay thế.");
                return ResponseEntity.badRequest().body(response);
            }
            
            voucherRepository.delete(voucher);
            
            response.put("success", true);
            response.put("message", "Xóa voucher thành công");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * API: Toggle voucher status
     * POST /seller/api/vouchers/{id}/toggle
     */
    @PostMapping("/api/vouchers/{id}/toggle")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleVoucherStatus(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User seller = getCurrentSeller();
            Voucher voucher = voucherRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy voucher"));
            
            // Check ownership
            if (voucher.getCreatedBy() == null || !voucher.getCreatedBy().getId().equals(seller.getId())) {
                response.put("success", false);
                response.put("message", "Bạn không có quyền thay đổi voucher này");
                return ResponseEntity.status(403).body(response);
            }
            
            voucher.setActive(!voucher.isActive());
            voucher = voucherRepository.save(voucher);
            
            response.put("success", true);
            response.put("message", voucher.isActive() ? "Đã kích hoạt voucher" : "Đã vô hiệu hóa voucher");
            response.put("data", convertVoucherToMap(voucher));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // Helper method to convert Voucher to Map
    private Map<String, Object> convertVoucherToMap(Voucher voucher) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", voucher.getId());
        map.put("code", voucher.getCode());
        map.put("name", voucher.getName());
        map.put("description", voucher.getDescription());
        map.put("discountType", voucher.getDiscountType().name());
        map.put("discountTypeDisplay", voucher.getDiscountType() == DiscountType.PERCENTAGE ? "Phần trăm" : "Số tiền cố định");
        map.put("discountValue", voucher.getDiscountValue());
        map.put("maxDiscount", voucher.getMaxDiscount());
        map.put("minOrderValue", voucher.getMinOrderValue());
        map.put("totalQuantity", voucher.getTotalQuantity());
        map.put("usedQuantity", voucher.getUsedQuantity());
        map.put("remainingQuantity", voucher.getTotalQuantity() - voucher.getUsedQuantity());
        map.put("usageLimit", voucher.getUsageLimit());
        map.put("startDate", voucher.getStartDate());
        map.put("endDate", voucher.getEndDate());
        map.put("active", voucher.isActive());
        map.put("createdAt", voucher.getCreatedAt());
        return map;
    }
}


