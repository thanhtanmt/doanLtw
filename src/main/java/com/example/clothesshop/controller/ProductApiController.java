package com.example.clothesshop.controller;

import com.example.clothesshop.dto.ProductDto;
import com.example.clothesshop.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductApiController {

    @Autowired
    private ProductService productService;

    /**
     * API lấy tất cả sản phẩm đang hoạt động (không phân trang)
     * GET /api/products/all
     */
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllProducts() {
        try {
            List<ProductDto> products = productService.getAllActiveProducts();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Lấy danh sách sản phẩm thành công");
            response.put("data", products);
            response.put("total", products.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * API lấy danh sách sản phẩm với phân trang và sắp xếp
     * GET /api/products?page=0&size=10&sortBy=createdAt&sortDir=desc
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            Page<ProductDto> productsPage = productService.getAllActiveProducts(page, size, sortBy, sortDir);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Lấy danh sách sản phẩm thành công");
            response.put("data", productsPage.getContent());
            response.put("currentPage", productsPage.getNumber());
            response.put("totalItems", productsPage.getTotalElements());
            response.put("totalPages", productsPage.getTotalPages());
            response.put("pageSize", productsPage.getSize());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * API lấy chi tiết sản phẩm theo ID
     * GET /api/products/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getProductById(@PathVariable Long id) {
        try {
            ProductDto product = productService.getProductById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + id));
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Lấy chi tiết sản phẩm thành công");
            response.put("data", product);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * API tìm kiếm sản phẩm theo tên
     * GET /api/products/search?name=áo
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchProducts(@RequestParam String name) {
        try {
            List<ProductDto> products = productService.searchProductsByName(name);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Tìm kiếm sản phẩm thành công");
            response.put("data", products);
            response.put("total", products.size());
            response.put("searchTerm", name);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * API tìm kiếm sản phẩm nâng cao với nhiều tiêu chí
     * GET /api/products/advanced-search?name=áo&categoryId=1&brand=Nike&gender=Nam&page=0&size=10
     */
    @GetMapping("/advanced-search")
    public ResponseEntity<Map<String, Object>> advancedSearch(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String gender,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            Page<ProductDto> productsPage = productService.searchProducts(
                name, categoryId, brand, gender, page, size, sortBy, sortDir);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Tìm kiếm sản phẩm thành công");
            response.put("data", productsPage.getContent());
            response.put("currentPage", productsPage.getNumber());
            response.put("totalItems", productsPage.getTotalElements());
            response.put("totalPages", productsPage.getTotalPages());
            response.put("pageSize", productsPage.getSize());
            
            // Thêm thông tin filter
            Map<String, Object> filters = new HashMap<>();
            if (name != null) filters.put("name", name);
            if (categoryId != null) filters.put("categoryId", categoryId);
            if (brand != null) filters.put("brand", brand);
            if (gender != null) filters.put("gender", gender);
            response.put("filters", filters);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * API lấy sản phẩm theo danh mục
     * GET /api/products/category/{categoryId}?page=0&size=10
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Map<String, Object>> getProductsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            Page<ProductDto> productsPage = productService.getProductsByCategory(
                categoryId, page, size, sortBy, sortDir);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Lấy sản phẩm theo danh mục thành công");
            response.put("data", productsPage.getContent());
            response.put("currentPage", productsPage.getNumber());
            response.put("totalItems", productsPage.getTotalElements());
            response.put("totalPages", productsPage.getTotalPages());
            response.put("pageSize", productsPage.getSize());
            response.put("categoryId", categoryId);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * API lấy sản phẩm theo seller
     * GET /api/products/seller/{sellerId}
     */
    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<Map<String, Object>> getProductsBySeller(@PathVariable Long sellerId) {
        try {
            List<ProductDto> products = productService.getProductsBySeller(sellerId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Lấy sản phẩm theo người bán thành công");
            response.put("data", products);
            response.put("total", products.size());
            response.put("sellerId", sellerId);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * API lấy sản phẩm theo thương hiệu
     * GET /api/products/brand/{brand}
     */
    @GetMapping("/brand/{brand}")
    public ResponseEntity<Map<String, Object>> getProductsByBrand(@PathVariable String brand) {
        try {
            List<ProductDto> products = productService.getProductsByBrand(brand);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Lấy sản phẩm theo thương hiệu thành công");
            response.put("data", products);
            response.put("total", products.size());
            response.put("brand", brand);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * API lấy sản phẩm theo giới tính
     * GET /api/products/gender/{gender}
     */
    @GetMapping("/gender/{gender}")
    public ResponseEntity<Map<String, Object>> getProductsByGender(@PathVariable String gender) {
        try {
            List<ProductDto> products = productService.getProductsByGender(gender);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Lấy sản phẩm theo giới tính thành công");
            response.put("data", products);
            response.put("total", products.size());
            response.put("gender", gender);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * API lấy sản phẩm còn hàng
     * GET /api/products/in-stock
     */
    @GetMapping("/in-stock")
    public ResponseEntity<Map<String, Object>> getProductsInStock() {
        try {
            List<ProductDto> products = productService.getProductsInStock();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Lấy sản phẩm còn hàng thành công");
            response.put("data", products);
            response.put("total", products.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * API lấy sản phẩm mới nhất
     * GET /api/products/new
     */
    @GetMapping("/new")
    public ResponseEntity<Map<String, Object>> getNewProducts() {
        try {
            List<ProductDto> products = productService.getNewProducts();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Lấy sản phẩm mới thành công");
            response.put("data", products);
            response.put("total", products.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * API lấy sản phẩm nổi bật
     * GET /api/products/featured?limit=8
     */
    @GetMapping("/featured")
    public ResponseEntity<Map<String, Object>> getFeaturedProducts(
            @RequestParam(defaultValue = "8") int limit) {
        try {
            List<ProductDto> products = productService.getFeaturedProducts(limit);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Lấy sản phẩm nổi bật thành công");
            response.put("data", products);
            response.put("total", products.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
