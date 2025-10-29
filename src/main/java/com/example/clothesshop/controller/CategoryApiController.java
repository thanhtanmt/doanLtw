package com.example.clothesshop.controller;

import com.example.clothesshop.dto.CategoryDto;
import com.example.clothesshop.dto.CategoryHierarchyDto;
import com.example.clothesshop.model.Category;
import com.example.clothesshop.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "*")
public class CategoryApiController {

    @Autowired
    private CategoryRepository categoryRepository;

    /**
     * Lấy tất cả categories
     * GET /api/categories
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllCategories() {
        try {
            List<Category> categories = categoryRepository.findAll();
            List<CategoryDto> categoryDtos = categories.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Lấy danh sách categories thành công");
            response.put("data", categoryDtos);
            response.put("total", categoryDtos.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Lấy tất cả parent categories (categories gốc)
     * GET /api/categories/parents
     */
    @GetMapping("/parents")
    public ResponseEntity<Map<String, Object>> getParentCategories() {
        try {
            List<Category> parents = categoryRepository.findByParentIsNull();
            List<CategoryDto> parentDtos = parents.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Lấy danh sách parent categories thành công");
            response.put("data", parentDtos);
            response.put("total", parentDtos.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Lấy children categories theo parent ID
     * GET /api/categories/parent/{parentId}/children
     */
    @GetMapping("/parent/{parentId}/children")
    public ResponseEntity<Map<String, Object>> getChildrenByParent(@PathVariable Long parentId) {
        try {
            List<Category> children = categoryRepository.findByParentId(parentId);
            List<CategoryDto> childrenDtos = children.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Lấy danh sách children categories thành công");
            response.put("data", childrenDtos);
            response.put("total", childrenDtos.size());
            response.put("parentId", parentId);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Lấy cả hierarchy (parent + children)
     * GET /api/categories/hierarchy
     */
    @GetMapping("/hierarchy")
    public ResponseEntity<Map<String, Object>> getCategoryHierarchy() {
        try {
            List<Category> parents = categoryRepository.findByParentIsNull();
            List<CategoryHierarchyDto> hierarchyDtos = parents.stream()
                    .map(parent -> {
                        List<CategoryDto> childrenDtos = parent.getChildren().stream()
                                .map(this::convertToDto)
                                .collect(Collectors.toList());
                        return new CategoryHierarchyDto(
                                parent.getId(),
                                parent.getName(),
                                parent.getDescription(),
                                childrenDtos
                        );
                    })
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Lấy category hierarchy thành công");
            response.put("data", hierarchyDtos);
            response.put("total", hierarchyDtos.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Lấy category theo ID
     * GET /api/categories/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getCategoryById(@PathVariable Long id) {
        try {
            Category category = categoryRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy category với ID: " + id));

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Lấy chi tiết category thành công");
            response.put("data", convertToDto(category));

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
     * Convert Category entity to CategoryDto
     */
    private CategoryDto convertToDto(Category category) {
        CategoryDto dto = new CategoryDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        
        if (category.getParent() != null) {
            dto.setParentId(category.getParent().getId());
            dto.setParentName(category.getParent().getName());
        }
        
        return dto;
    }
}
