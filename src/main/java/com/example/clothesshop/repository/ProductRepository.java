package com.example.clothesshop.repository;

import com.example.clothesshop.model.Product;
import com.example.clothesshop.model.User;
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
}

