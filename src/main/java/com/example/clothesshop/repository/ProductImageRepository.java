package com.example.clothesshop.repository;

import com.example.clothesshop.model.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    
    List<ProductImage> findByProductId(Long productId);
    
    void deleteByIdIn(List<Long> ids);
}
