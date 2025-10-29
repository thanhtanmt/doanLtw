package com.example.clothesshop.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryHierarchyDto {
    private Long id;
    private String name;
    private String description;
    private List<CategoryDto> children;
}
