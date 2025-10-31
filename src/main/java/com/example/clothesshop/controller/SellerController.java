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
    private final ProductImageRepository productImageRepository;

    public SellerController(UserRepository userRepository, ProductRepository productRepository,
                            CategoryRepository categoryRepository, CloudinaryService cloudinaryService,
                            OrderRepository orderRepository, VoucherRepository voucherRepository,
                            ProductImageRepository productImageRepository) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.cloudinaryService = cloudinaryService;
        this.orderRepository = orderRepository;
        this.voucherRepository = voucherRepository;
        this.productImageRepository = productImageRepository;
    }

    // ===== DASHBOARD PAGE =====
    @GetMapping
    public String seller(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "seller"; // unauthenticated view (same as before)
        }

        String username = authentication.getName();
        User seller = userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .orElse(null);

        if (seller == null) return "seller";

        List<Product> products = productRepository.findBySeller(seller);
        model.addAttribute("products", products);
        model.addAttribute("seller", seller);

        long totalProducts = products.size();
        long activeProducts = products.stream().filter(Product::isActive).count();
        int totalStock = products.stream()
                .filter(p -> p.getVariants() != null)
                .flatMap(p -> p.getVariants().stream())
                .filter(v -> v.getQuantity() != null)
                .mapToInt(ProductVariant::getQuantity)
                .sum();

        List<Order> allOrders = orderRepository.findAll();
        List<Order> sellerOrders = allOrders.stream()
                .filter(o -> o.getOrderDetails().stream()
                        .anyMatch(d -> d.getVariant() != null &&
                                d.getVariant().getProduct().getSeller() != null &&
                                d.getVariant().getProduct().getSeller().getId().equals(seller.getId())))
                .collect(Collectors.toList());

        long totalOrders = sellerOrders.size();
        BigDecimal totalRevenue = sellerOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
                .map(Order::getTotalPrice)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("activeProducts", activeProducts);
        model.addAttribute("totalStock", totalStock);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("sellerOrders", sellerOrders);

        return "seller";
    }

    // ===== COMMON HELPERS =====
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

    private ResponseEntity<Map<String, Object>> forbidden(String message) {
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", false);
        resp.put("message", message);
        return ResponseEntity.status(403).body(resp);
    }

    private ResponseEntity<Map<String, Object>> errorResponse(Exception e) {
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", false);
        resp.put("message", "Lỗi: " + e.getMessage());
        return ResponseEntity.badRequest().body(resp);
    }

    private Map<String, Object> successMap(String message, Object data) {
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        if (message != null) resp.put("message", message);
        if (data != null) resp.put("data", data);
        return resp;
    }

    // ===== PRODUCT MANAGEMENT APIS =====

    @GetMapping("/api/products")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getSellerProducts() {
        try {
            User seller = getCurrentSeller();
            List<Product> products = productRepository.findBySeller(seller);
            List<Map<String, Object>> productData = products.stream()
                    .map(this::convertProductToMap)
                    .collect(Collectors.toList());

            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("data", productData);
            resp.put("total", products.size());
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return errorResponse(e);
        }
    }

    @GetMapping("/api/products/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getProductById(@PathVariable Long id) {
        try {
            User seller = getCurrentSeller();
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm"));

            if (product.getSeller() == null || !product.getSeller().getId().equals(seller.getId())) {
                return forbidden("Bạn không có quyền truy cập sản phẩm này");
            }

            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("data", convertProductToMap(product));
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return errorResponse(e);
        }
    }

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

        try {
            User seller = getCurrentSeller();

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

            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục"));
            product.setCategory(category);

            product = productRepository.save(product);

            if (images != null) {
                for (MultipartFile file : images) {
                    if (file != null && !file.isEmpty()) {
                        String imageUrl = cloudinaryService.uploadImage(file, "products");
                        ProductImage productImage = new ProductImage();
                        productImage.setProduct(product);
                        productImage.setUrl(imageUrl);
                        product.getImages().add(productImage);
                    }
                }
            }

            if (variantsJson != null && !variantsJson.trim().isEmpty()) {
                List<Map<String, String>> variants = parseVariantsJson(variantsJson);
                for (Map<String, String> v : variants) {
                    ProductVariant variant = new ProductVariant();
                    variant.setProduct(product);
                    variant.setSize(v.get("size"));
                    variant.setPrice(new BigDecimal(v.get("price")));
                    variant.setQuantity(Integer.parseInt(v.get("quantity")));
                    variant.setSku(v.get("sku"));
                    variant.setAvailable(true);
                    product.getVariants().add(variant);
                }
            }

            product = productRepository.save(product);

            return ResponseEntity.ok(successMap("Tạo sản phẩm thành công", convertProductToMap(product)));
        } catch (Exception e) {
            e.printStackTrace();
            return errorResponse(e);
        }
    }

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

        try {
            User seller = getCurrentSeller();
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm"));

            if (product.getSeller() == null || !product.getSeller().getId().equals(seller.getId())) {
                return forbidden("Bạn không có quyền sửa sản phẩm này");
            }

            product.setName(name);
            product.setBrand(brand);
            product.setGender(gender);
            product.setDescription(description);
            product.setDetail(detail);
            product.setSpecification(specification);
            product.setMaterial(material);
            product.setUpdatedAt(LocalDateTime.now());

            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục"));
            product.setCategory(category);

            // Delete images
            if (deletedImageIds != null && !deletedImageIds.trim().isEmpty()) {
                try {
                    String ids = deletedImageIds.trim();
                    if (ids.startsWith("[") && ids.endsWith("]")) ids = ids.substring(1, ids.length() - 1);
                    if (!ids.isEmpty()) {
                        String[] idArray = ids.split(",");
                        List<Long> toDelete = new ArrayList<>();
                        for (String s : idArray) {
                            Long imgId = Long.parseLong(s.trim());
                            toDelete.add(imgId);
                            productImageRepository.findById(imgId).ifPresent(image -> {
                                if (image.getUrl() != null && image.getUrl().contains("cloudinary.com")) {
                                    try {
                                        String publicId = cloudinaryService.extractPublicId(image.getUrl());
                                        cloudinaryService.deleteImage(publicId);
                                    } catch (Exception ex) {
                                        System.err.println("Không thể xóa ảnh trên Cloudinary: " + ex.getMessage());
                                    }
                                }
                            });
                        }
                        if (!toDelete.isEmpty()) {
                            productImageRepository.deleteAllById(toDelete);
                            product.getImages().removeIf(img -> img.getId() != null && toDelete.contains(img.getId()));
                        }
                    }
                } catch (Exception ex) {
                    System.err.println("Error deleting images: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }

            // Add new images
            if (images != null) {
                for (MultipartFile file : images) {
                    if (file != null && !file.isEmpty()) {
                        String imageUrl = cloudinaryService.uploadImage(file, "products");
                        ProductImage productImage = new ProductImage();
                        productImage.setProduct(product);
                        productImage.setUrl(imageUrl);
                        product.getImages().add(productImage);
                    }
                }
            }

            // Variants update
            if (variantsJson != null && !variantsJson.trim().isEmpty()) {
                List<Map<String, String>> newVariants = parseVariantsJson(variantsJson);
                List<ProductVariant> existing = product.getVariants();
                List<String> newSkus = newVariants.stream().map(v -> v.get("sku")).collect(Collectors.toList());

                // Mark missing SKUs as unavailable
                for (ProductVariant ev : existing) {
                    if (!newSkus.contains(ev.getSku())) {
                        ev.setAvailable(false);
                    }
                }

                for (Map<String, String> vData : newVariants) {
                    String sku = vData.get("sku");
                    ProductVariant found = existing.stream()
                            .filter(v -> sku.equals(v.getSku()))
                            .findFirst()
                            .orElse(null);
                    if (found != null) {
                        found.setSize(vData.get("size"));
                        found.setPrice(new BigDecimal(vData.get("price")));
                        found.setQuantity(Integer.parseInt(vData.get("quantity")));
                        found.setAvailable(true);
                    } else {
                        ProductVariant nv = new ProductVariant();
                        nv.setProduct(product);
                        nv.setSize(vData.get("size"));
                        nv.setPrice(new BigDecimal(vData.get("price")));
                        nv.setQuantity(Integer.parseInt(vData.get("quantity")));
                        nv.setSku(sku);
                        nv.setAvailable(true);
                        product.getVariants().add(nv);
                    }
                }
            }

            product = productRepository.save(product);
            return ResponseEntity.ok(successMap("Cập nhật sản phẩm thành công", convertProductToMap(product)));
        } catch (Exception e) {
            e.printStackTrace();
            return errorResponse(e);
        }
    }

    @DeleteMapping("/api/products/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteProduct(@PathVariable Long id) {
        try {
            User seller = getCurrentSeller();
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm"));

            if (product.getSeller() == null || !product.getSeller().getId().equals(seller.getId())) {
                return forbidden("Bạn không có quyền xóa sản phẩm này");
            }

            product.setActive(false);
            product.setUpdatedAt(LocalDateTime.now());
            productRepository.save(product);

            return ResponseEntity.ok(successMap("Xóa sản phẩm thành công", null));
        } catch (Exception e) {
            return errorResponse(e);
        }
    }

    @PostMapping("/api/products/{id}/toggle")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleProductStatus(@PathVariable Long id) {
        try {
            User seller = getCurrentSeller();
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm"));

            if (product.getSeller() == null || !product.getSeller().getId().equals(seller.getId())) {
                return forbidden("Bạn không có quyền thay đổi trạng thái sản phẩm này");
            }

            product.setActive(!product.isActive());
            product.setUpdatedAt(LocalDateTime.now());
            productRepository.save(product);

            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            String status = product.isActive() ? "kích hoạt" : "vô hiệu hóa";
            resp.put("message", "Đã " + status + " sản phẩm thành công");
            resp.put("active", product.isActive());
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return errorResponse(e);
        }
    }

    // ===== ORDER MANAGEMENT =====

    @GetMapping("/api/orders")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getSellerOrders(@RequestParam(required = false) String status) {
        try {
            User seller = getCurrentSeller();
            List<Order> allOrders = orderRepository.findAll();

            List<Map<String, Object>> sellerOrders = allOrders.stream()
                    .filter(order -> {
                        boolean hasSellerProduct = order.getOrderDetails().stream()
                                .anyMatch(detail -> detail.getProduct() != null
                                        && detail.getProduct().getSeller() != null
                                        && detail.getProduct().getSeller().getId().equals(seller.getId()));
                        if (status != null && !status.isEmpty()) {
                            return hasSellerProduct && order.getStatus().name().equals(status);
                        }
                        return hasSellerProduct;
                    })
                    .map(this::convertOrderToMap)
                    .collect(Collectors.toList());

            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("data", sellerOrders);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            e.printStackTrace();
            return errorResponse(e);
        }
    }

    @GetMapping("/api/orders/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getOrderDetail(@PathVariable Long id) {
        try {
            User seller = getCurrentSeller();
            Order order = orderRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng"));

            boolean hasSellerProduct = order.getOrderDetails().stream()
                    .anyMatch(detail -> detail.getProduct() != null
                            && detail.getProduct().getSeller() != null
                            && detail.getProduct().getSeller().getId().equals(seller.getId()));

            if (!hasSellerProduct) return forbidden("Bạn không có quyền xem đơn hàng này");

            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("data", convertOrderToDetailMap(order, seller));
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            e.printStackTrace();
            return errorResponse(e);
        }
    }

    @PostMapping("/api/orders/{id}/confirm")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> confirmOrder(@PathVariable Long id) {
        try {
            User seller = getCurrentSeller();
            Order order = orderRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng"));

            boolean hasSellerProduct = order.getOrderDetails().stream()
                    .anyMatch(detail -> detail.getProduct() != null
                            && detail.getProduct().getSeller() != null
                            && detail.getProduct().getSeller().getId().equals(seller.getId()));

            if (!hasSellerProduct) return forbidden("Bạn không có quyền xác nhận đơn hàng này");
            if (order.getStatus() != OrderStatus.PENDING) {
                Map<String, Object> resp = new HashMap<>();
                resp.put("success", false);
                resp.put("message", "Chỉ có thể xác nhận đơn hàng ở trạng thái chờ xác nhận");
                return ResponseEntity.badRequest().body(resp);
            }

            order.setStatus(OrderStatus.CONFIRMED);
            orderRepository.save(order);

            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("message", "Đã xác nhận đơn hàng #" + order.getOrderCode());
            resp.put("data", convertOrderToMap(order));
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            e.printStackTrace();
            return errorResponse(e);
        }
    }

    @PostMapping("/api/orders/{id}/cancel")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> cancelOrder(@PathVariable Long id,
                                                           @RequestParam(required = false) String reason) {
        try {
            User seller = getCurrentSeller();
            Order order = orderRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng"));

            boolean hasSellerProduct = order.getOrderDetails().stream()
                    .anyMatch(detail -> detail.getProduct() != null
                            && detail.getProduct().getSeller() != null
                            && detail.getProduct().getSeller().getId().equals(seller.getId()));

            if (!hasSellerProduct) return forbidden("Bạn không có quyền hủy đơn hàng này");
            if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.CANCELED) {
                Map<String, Object> resp = new HashMap<>();
                resp.put("success", false);
                resp.put("message", "Không thể hủy đơn hàng ở trạng thái này");
                return ResponseEntity.badRequest().body(resp);
            }

            order.setStatus(OrderStatus.CANCELED);
            if (reason != null && !reason.trim().isEmpty()) order.setFailureReason(reason);
            orderRepository.save(order);

            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("message", "Đã hủy đơn hàng #" + order.getOrderCode());
            resp.put("data", convertOrderToMap(order));
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            e.printStackTrace();
            return errorResponse(e);
        }
    }

    // ===== CONVERTERS =====

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
        // Keep the same simple parser used previously (not production-ready)
        List<Map<String, String>> variants = new ArrayList<>();
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
            if (!variant.isEmpty()) variants.add(variant);
        }
        return variants;
    }

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

        int totalItems = order.getOrderDetails().stream().mapToInt(OrderDetail::getQuantity).sum();
        map.put("totalItems", totalItems);
        return map;
    }

    private Map<String, Object> convertOrderToDetailMap(Order order, User seller) {
        Map<String, Object> map = convertOrderToMap(order);
        if (order.getUser() != null) {
            Map<String, Object> customer = new HashMap<>();
            customer.put("name", order.getUser().getFirstName() + " " + order.getUser().getLastName());
            customer.put("email", order.getUser().getEmail());
            customer.put("phone", order.getUser().getPhone());
            map.put("customer", customer);
        }

        List<Map<String, Object>> details = order.getOrderDetails().stream()
                .filter(d -> d.getProduct() != null && d.getProduct().getSeller() != null
                        && d.getProduct().getSeller().getId().equals(seller.getId()))
                .map(detail -> {
                    Map<String, Object> detailMap = new HashMap<>();
                    detailMap.put("id", detail.getId());
                    detailMap.put("productName", detail.getProduct().getName());
                    detailMap.put("size", detail.getSizeAtOrder());
                    detailMap.put("quantity", detail.getQuantity());
                    detailMap.put("unitPrice", detail.getUnitPrice());
                    detailMap.put("totalPrice", detail.getTotalPrice());
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

    // ===== DASHBOARD STATS =====
    @GetMapping("/api/dashboard-stats")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        try {
            User seller = getCurrentSeller();
            List<Product> products = productRepository.findBySeller(seller);
            long totalProducts = products.size();
            long activeProducts = products.stream().filter(Product::isActive).count();
            int totalStock = products.stream()
                    .filter(p -> p.getVariants() != null)
                    .flatMap(p -> p.getVariants().stream())
                    .filter(v -> v.getQuantity() != null)
                    .mapToInt(ProductVariant::getQuantity)
                    .sum();

            List<Order> allOrders = orderRepository.findAll();
            List<Order> sellerOrders = allOrders.stream()
                    .filter(order -> order.getOrderDetails().stream()
                            .anyMatch(detail -> detail.getVariant() != null &&
                                    detail.getVariant().getProduct().getSeller() != null &&
                                    detail.getVariant().getProduct().getSeller().getId().equals(seller.getId())))
                    .collect(Collectors.toList());

            long totalOrders = sellerOrders.size();
            BigDecimal totalRevenue = sellerOrders.stream()
                    .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
                    .map(Order::getTotalPrice)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            BigDecimal monthlyRevenue = sellerOrders.stream()
                    .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
                    .filter(o -> o.getCreatedAt() != null && o.getCreatedAt().isAfter(startOfMonth))
                    .map(Order::getTotalPrice)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            long pendingOrders = sellerOrders.stream().filter(o -> o.getStatus() == OrderStatus.PENDING).count();
            long confirmedOrders = sellerOrders.stream().filter(o -> o.getStatus() == OrderStatus.CONFIRMED).count();
            long deliveringOrders = sellerOrders.stream().filter(o -> o.getStatus() == OrderStatus.DELIVERING || o.getStatus() == OrderStatus.ASSIGNED).count();
            long deliveredOrders = sellerOrders.stream().filter(o -> o.getStatus() == OrderStatus.DELIVERED).count();

            long totalVouchers = voucherRepository.findByCreatedByAndType(seller, VoucherType.SELLER).size();
            long activeVouchers = voucherRepository.findByCreatedByAndType(seller, VoucherType.SELLER).stream().filter(Voucher::isActive).count();

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

            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("data", stats);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            e.printStackTrace();
            return errorResponse(e);
        }
    }

    // ===== VOUCHERS =====
    @GetMapping("/api/vouchers")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getSellerVouchers() {
        try {
            User seller = getCurrentSeller();
            List<Voucher> vouchers = voucherRepository.findByCreatedByAndType(seller, VoucherType.SELLER);
            List<Map<String, Object>> voucherList = vouchers.stream().map(this::convertVoucherToMap).collect(Collectors.toList());
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("data", voucherList);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            e.printStackTrace();
            return errorResponse(e);
        }
    }

    @GetMapping("/api/vouchers/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getVoucherDetail(@PathVariable Long id) {
        try {
            User seller = getCurrentSeller();
            Voucher voucher = voucherRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy voucher"));
            if (voucher.getCreatedBy() == null || !voucher.getCreatedBy().getId().equals(seller.getId())) {
                return forbidden("Bạn không có quyền xem voucher này");
            }
            return ResponseEntity.ok(successMap(null, convertVoucherToMap(voucher)));
        } catch (Exception e) {
            e.printStackTrace();
            return errorResponse(e);
        }
    }

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
        try {
            User seller = getCurrentSeller();
            if (voucherRepository.existsByCode(code)) {
                Map<String, Object> resp = new HashMap<>();
                resp.put("success", false);
                resp.put("message", "Mã voucher đã tồn tại");
                return ResponseEntity.badRequest().body(resp);
            }

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
            return ResponseEntity.ok(successMap("Tạo voucher thành công", convertVoucherToMap(voucher)));
        } catch (Exception e) {
            e.printStackTrace();
            return errorResponse(e);
        }
    }

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
        try {
            User seller = getCurrentSeller();
            Voucher voucher = voucherRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy voucher"));
            if (voucher.getCreatedBy() == null || !voucher.getCreatedBy().getId().equals(seller.getId())) {
                return forbidden("Bạn không có quyền sửa voucher này");
            }

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
            return ResponseEntity.ok(successMap("Cập nhật voucher thành công", convertVoucherToMap(voucher)));
        } catch (Exception e) {
            e.printStackTrace();
            return errorResponse(e);
        }
    }

    @DeleteMapping("/api/vouchers/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteVoucher(@PathVariable Long id) {
        try {
            User seller = getCurrentSeller();
            Voucher voucher = voucherRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy voucher"));
            if (voucher.getCreatedBy() == null || !voucher.getCreatedBy().getId().equals(seller.getId())) {
                return forbidden("Bạn không có quyền xóa voucher này");
            }
            if (voucher.getUsedQuantity() > 0) {
                Map<String, Object> resp = new HashMap<>();
                resp.put("success", false);
                resp.put("message", "Không thể xóa voucher đã được sử dụng. Bạn có thể vô hiệu hóa nó thay thế.");
                return ResponseEntity.badRequest().body(resp);
            }
            voucherRepository.delete(voucher);
            return ResponseEntity.ok(successMap("Xóa voucher thành công", null));
        } catch (Exception e) {
            e.printStackTrace();
            return errorResponse(e);
        }
    }

    @PostMapping("/api/vouchers/{id}/toggle")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleVoucherStatus(@PathVariable Long id) {
        try {
            User seller = getCurrentSeller();
            Voucher voucher = voucherRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy voucher"));
            if (voucher.getCreatedBy() == null || !voucher.getCreatedBy().getId().equals(seller.getId())) {
                return forbidden("Bạn không có quyền thay đổi trạng thái voucher này");
            }
            voucher.setActive(!voucher.isActive());
            voucherRepository.save(voucher);
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("message", voucher.isActive() ? "Kích hoạt voucher thành công" : "Đã vô hiệu hóa voucher");
            resp.put("active", voucher.isActive());
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            e.printStackTrace();
            return errorResponse(e);
        }
    }

    private Map<String, Object> convertVoucherToMap(Voucher v) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", v.getId());
        map.put("code", v.getCode());
        map.put("name", v.getName());
        map.put("description", v.getDescription());
        map.put("discountType", v.getDiscountType());
        map.put("discountValue", v.getDiscountValue());
        map.put("maxDiscount", v.getMaxDiscount());
        map.put("minOrderValue", v.getMinOrderValue());
        map.put("totalQuantity", v.getTotalQuantity());
        map.put("usageLimit", v.getUsageLimit());
        map.put("startDate", v.getStartDate());
        map.put("endDate", v.getEndDate());
        map.put("active", v.isActive());
        map.put("usedQuantity", v.getUsedQuantity());
        return map;
    }
}