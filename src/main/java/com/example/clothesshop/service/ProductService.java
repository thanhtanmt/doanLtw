package com.example.clothesshop.service;

import com.example.clothesshop.dto.ProductDto;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

/**
 * Interface cho Product Service
 * Định nghĩa các phương thức nghiệp vụ liên quan đến sản phẩm
 */
public interface ProductService {
    
    /**
     * Lấy tất cả sản phẩm đang hoạt động
     */
    List<ProductDto> getAllActiveProducts();
    
    /**
     * Lấy sản phẩm đang hoạt động với phân trang
     */
    Page<ProductDto> getAllActiveProducts(int page, int size, String sortBy, String sortDir);
    
    /**
     * Lấy chi tiết sản phẩm theo ID
     */
    Optional<ProductDto> getProductById(Long id);
    
    /**
     * Tìm kiếm sản phẩm theo tên
     */
    List<ProductDto> searchProductsByName(String name);
    
    /**
     * Lấy sản phẩm theo category
     */
    List<ProductDto> getProductsByCategory(Long categoryId);
    
    /**
     * Lấy sản phẩm theo category với phân trang
     */
    Page<ProductDto> getProductsByCategory(Long categoryId, int page, int size, String sortBy, String sortDir);
    
    /**
     * Lấy sản phẩm theo seller
     */
    List<ProductDto> getProductsBySeller(Long sellerId);
    
    /**
     * Lấy sản phẩm theo thương hiệu
     */
    List<ProductDto> getProductsByBrand(String brand);
    
    /**
     * Lấy sản phẩm theo giới tính
     */
    List<ProductDto> getProductsByGender(String gender);
    
    /**
     * Tìm kiếm sản phẩm với nhiều tiêu chí
     */
    Page<ProductDto> searchProducts(String name, Long categoryId, String brand, 
                                   String gender, int page, int size, 
                                   String sortBy, String sortDir);
    
    /**
     * Lấy sản phẩm có tồn kho
     */
    List<ProductDto> getProductsInStock();
    
    /**
     * Lấy sản phẩm mới nhất
     */
    List<ProductDto> getNewProducts();
    
    /**
     * Lấy sản phẩm nổi bật
     */
    List<ProductDto> getFeaturedProducts(int limit);
}
