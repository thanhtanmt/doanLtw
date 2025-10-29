package com.example.clothesshop.service.impl;

import com.example.clothesshop.dto.ProductDto;
import com.example.clothesshop.dto.ProductImageDto;
import com.example.clothesshop.dto.ProductVariantDto;
import com.example.clothesshop.model.Product;
import com.example.clothesshop.model.ProductImage;
import com.example.clothesshop.model.ProductVariant;
import com.example.clothesshop.repository.ProductRepository;
import com.example.clothesshop.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    /**
     * Lấy tất cả sản phẩm đang hoạt động
     */
    @Override
    public List<ProductDto> getAllActiveProducts() {
        List<Product> products = productRepository.findByActiveTrue();
        return products.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Lấy sản phẩm đang hoạt động với phân trang
     */
    @Override
    public Page<ProductDto> getAllActiveProducts(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") 
            ? Sort.by(sortBy).ascending() 
            : Sort.by(sortBy).descending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> products = productRepository.findByActiveTrue(pageable);
        
        return products.map(this::convertToDto);
    }

    /**
     * Lấy chi tiết sản phẩm theo ID
     */
    @Override
    public Optional<ProductDto> getProductById(Long id) {
        return productRepository.findById(id)
                .map(this::convertToDto);
    }

    /**
     * Tìm kiếm sản phẩm theo tên
     */
    @Override
    public List<ProductDto> searchProductsByName(String name) {
        List<Product> products = productRepository.findByNameContainingIgnoreCase(name);
        return products.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Lấy sản phẩm theo category
     */
    @Override
    public List<ProductDto> getProductsByCategory(Long categoryId) {
        List<Product> products = productRepository.findByCategoryId(categoryId);
        return products.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Lấy sản phẩm theo category với phân trang
     */
    @Override
    public Page<ProductDto> getProductsByCategory(Long categoryId, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") 
            ? Sort.by(sortBy).ascending() 
            : Sort.by(sortBy).descending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> products = productRepository.findByCategoryIdAndActiveTrue(categoryId, pageable);
        
        return products.map(this::convertToDto);
    }

    /**
     * Lấy sản phẩm theo seller
     */
    @Override
    public List<ProductDto> getProductsBySeller(Long sellerId) {
        List<Product> products = productRepository.findBySellerId(sellerId);
        return products.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Lấy sản phẩm theo thương hiệu
     */
    @Override
    public List<ProductDto> getProductsByBrand(String brand) {
        List<Product> products = productRepository.findByBrand(brand);
        return products.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Lấy sản phẩm theo giới tính
     */
    @Override
    public List<ProductDto> getProductsByGender(String gender) {
        List<Product> products = productRepository.findByGender(gender);
        return products.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Tìm kiếm sản phẩm với nhiều tiêu chí
     */
    @Override
    public Page<ProductDto> searchProducts(String name, Long categoryId, String brand, 
                                          String gender, int page, int size, 
                                          String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") 
            ? Sort.by(sortBy).ascending() 
            : Sort.by(sortBy).descending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> products = productRepository.searchProducts(name, categoryId, brand, gender, pageable);
        
        return products.map(this::convertToDto);
    }

    /**
     * Lấy sản phẩm có tồn kho
     */
    @Override
    public List<ProductDto> getProductsInStock() {
        List<Product> products = productRepository.findProductsInStock();
        return products.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Lấy sản phẩm mới nhất
     */
    @Override
    public List<ProductDto> getNewProducts() {
        List<Product> products = productRepository.findTop10ByActiveTrueOrderByCreatedAtDesc();
        return products.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Lấy sản phẩm nổi bật
     */
    @Override
    public List<ProductDto> getFeaturedProducts(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Product> products = productRepository.findFeaturedProducts(pageable);
        return products.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Chuyển đổi Product entity sang ProductDto
     */
    private ProductDto convertToDto(Product product) {
        ProductDto dto = new ProductDto();
        
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setBrand(product.getBrand());
        dto.setGender(product.getGender());
        dto.setDescription(product.getDescription());
        dto.setDetail(product.getDetail());
        dto.setSpecification(product.getSpecification());
        dto.setMaterial(product.getMaterial());
        dto.setActive(product.isActive());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());
        
        // Tính toán giá min/max
        dto.setMinPrice(product.getMinPrice());
        dto.setMaxPrice(product.getMaxPrice());
        dto.setTotalQuantity(product.getTotalQuantity());
        dto.setHasStock(product.hasStock());
        dto.setAvailableSizes(product.getAvailableSizes());
        
        // Category info
        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
            dto.setCategoryName(product.getCategory().getName());
        }
        
        // Seller info
        if (product.getSeller() != null) {
            dto.setSellerId(product.getSeller().getId());
            dto.setSellerName(product.getSeller().getFirstName() + " " + product.getSeller().getLastName());
        }
        
        // Images
        List<ProductImageDto> imageDtos = product.getImages().stream()
                .map(this::convertImageToDto)
                .collect(Collectors.toList());
        dto.setImages(imageDtos);
        
        // Variants
        List<ProductVariantDto> variantDtos = product.getVariants().stream()
                .map(this::convertVariantToDto)
                .collect(Collectors.toList());
        dto.setVariants(variantDtos);
        
        // Reviews statistics
        if (product.getReviews() != null) {
            dto.setTotalReviews(product.getReviews().size());
            double avgRating = product.getReviews().stream()
                    .mapToInt(review -> review.getRating())
                    .average()
                    .orElse(0.0);
            dto.setAverageRating(Math.round(avgRating * 10.0) / 10.0);
        }
        
        return dto;
    }

    /**
     * Chuyển đổi ProductImage entity sang ProductImageDto
     */
    private ProductImageDto convertImageToDto(ProductImage image) {
        ProductImageDto dto = new ProductImageDto();
        dto.setId(image.getId());
        dto.setUrl(image.getUrl());
        dto.setPosition(image.getPosition());
        dto.setIsPrimary(image.getIsPrimary());
        return dto;
    }

    /**
     * Chuyển đổi ProductVariant entity sang ProductVariantDto
     */
    private ProductVariantDto convertVariantToDto(ProductVariant variant) {
        ProductVariantDto dto = new ProductVariantDto();
        dto.setId(variant.getId());
        dto.setSize(variant.getSize());
        dto.setPrice(variant.getPrice());
        dto.setQuantity(variant.getQuantity());
        dto.setSku(variant.getSku());
        dto.setImageUrl(variant.getImageUrl());
        dto.setAvailable(variant.isAvailable());
        dto.setInStock(variant.isInStock());
        return dto;
    }
}
