package com.example.clothesshop.repository;

import com.example.clothesshop.model.Favorite;
import com.example.clothesshop.model.Product;
import com.example.clothesshop.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    
    /**
     * Tìm tất cả sản phẩm yêu thích của user
     */
    List<Favorite> findByUser(User user);
    
    /**
     * Tìm favorite theo user và product
     */
    Optional<Favorite> findByUserAndProduct(User user, Product product);
    
    /**
     * Kiểm tra xem user đã yêu thích product chưa
     */
    boolean existsByUserAndProduct(User user, Product product);
    
    /**
     * Xóa favorite theo user và product
     */
    void deleteByUserAndProduct(User user, Product product);
    
    /**
     * Đếm số lượng sản phẩm yêu thích của user
     */
    long countByUser(User user);
}
