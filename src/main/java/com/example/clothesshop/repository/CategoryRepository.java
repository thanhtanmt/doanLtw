package com.example.clothesshop.repository;

import com.example.clothesshop.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    // Lấy parent categories (parent_id = NULL)
    List<Category> findByParentIsNull();
    
    // Lấy children theo parent ID
    List<Category> findByParentId(Long parentId);
    
    // Tìm theo tên
    Optional<Category> findByName(String name);
}
