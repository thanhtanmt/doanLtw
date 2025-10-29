package com.example.clothesshop.repository;

import com.example.clothesshop.model.Product;

import com.example.clothesshop.model.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    

    // First query: Load products with images and seller
    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.images LEFT JOIN FETCH p.seller")
    List<Product> findAllWithImages();
    
    // Second query: Load variants for products (will be executed separately)
    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.variants WHERE p.id IN :productIds")
    List<Product> findAllWithVariants(@Param("productIds") List<Long> productIds);
    
    List<Product> findBySeller(User seller);
    
    List<Product> findBySellerAndActive(User seller, boolean active);
    
    long countBySeller(User seller);
    
    @Query("SELECT COUNT(p) FROM Product p WHERE p.seller = :seller AND p.active = true")
    long countActiveProductsBySeller(@Param("seller") User seller);

    // Tìm sản phẩm theo tên (tìm kiếm không phân biệt chữ hoa/thường)
    List<Product> findByNameContainingIgnoreCase(String name);
    
    // Tìm sản phẩm theo category
    List<Product> findByCategoryId(Long categoryId);
    
    // Tìm sản phẩm theo seller
    List<Product> findBySellerId(Long sellerId);
    
    // Tìm sản phẩm đang hoạt động
    List<Product> findByActiveTrue();
    
    // Tìm sản phẩm đang hoạt động với phân trang
    Page<Product> findByActiveTrue(Pageable pageable);
    
    // Tìm sản phẩm theo category và đang hoạt động
    Page<Product> findByCategoryIdAndActiveTrue(Long categoryId, Pageable pageable);
    
    // Tìm sản phẩm theo thương hiệu
    List<Product> findByBrand(String brand);
    
    // Tìm sản phẩm theo giới tính
    List<Product> findByGender(String gender);
    
    // Tìm kiếm sản phẩm với nhiều tiêu chí
    @Query("SELECT p FROM Product p WHERE " +
           "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
           "(:brand IS NULL OR p.brand = :brand) AND " +
           "(:gender IS NULL OR p.gender = :gender) AND " +
           "p.active = true")
    Page<Product> searchProducts(@Param("name") String name,
                                  @Param("categoryId") Long categoryId,
                                  @Param("brand") String brand,
                                  @Param("gender") String gender,
                                  Pageable pageable);
    
    // Tìm kiếm sản phẩm với nhiều categories
    @Query("SELECT p FROM Product p WHERE " +
           "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:categoryIds IS NULL OR p.category.id IN :categoryIds) AND " +
           "(:brand IS NULL OR p.brand = :brand) AND " +
           "(:gender IS NULL OR p.gender = :gender) AND " +
           "p.active = true")
    Page<Product> searchProductsByCategories(@Param("name") String name,
                                             @Param("categoryIds") List<Long> categoryIds,
                                             @Param("brand") String brand,
                                             @Param("gender") String gender,
                                             Pageable pageable);
    
    // Tìm sản phẩm có tồn kho
    @Query("SELECT p FROM Product p WHERE p.active = true AND " +
           "EXISTS (SELECT v FROM ProductVariant v WHERE v.product = p AND v.quantity > 0)")
    List<Product> findProductsInStock();
    
    // Tìm sản phẩm mới nhất
    List<Product> findTop10ByActiveTrueOrderByCreatedAtDesc();
    
    // Tìm sản phẩm nổi bật (có nhiều đánh giá tốt)
    @Query("SELECT p FROM Product p WHERE p.active = true " +
           "ORDER BY SIZE(p.reviews) DESC, p.createdAt DESC")
    List<Product> findFeaturedProducts(Pageable pageable);
    
    // Tìm sản phẩm theo ID với tất cả relationships (tối ưu cho trang chi tiết)
    @Query("SELECT DISTINCT p FROM Product p " +
           "LEFT JOIN FETCH p.images " +
           "LEFT JOIN FETCH p.variants " +
           "LEFT JOIN FETCH p.category " +
           "LEFT JOIN FETCH p.seller " +
           "WHERE p.id = :id")
    Product findByIdWithDetails(@Param("id") Long id);
    
    // Tìm sản phẩm nổi bật với images và variants (tối ưu cho trang chủ)
    @Query("SELECT DISTINCT p FROM Product p " +
           "LEFT JOIN FETCH p.images " +
           "LEFT JOIN FETCH p.variants " +
           "WHERE p.active = true " +
           "ORDER BY SIZE(p.reviews) DESC, p.createdAt DESC")
    List<Product> findFeaturedProductsWithDetails(Pageable pageable);
}

