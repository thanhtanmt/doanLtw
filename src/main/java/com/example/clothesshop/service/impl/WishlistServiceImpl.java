package com.example.clothesshop.service.impl;

import com.example.clothesshop.dto.WishlistItemDto;
import com.example.clothesshop.dto.WishlistResponse;
import com.example.clothesshop.model.Favorite;
import com.example.clothesshop.model.Product;
import com.example.clothesshop.model.User;
import com.example.clothesshop.repository.FavoriteRepository;
import com.example.clothesshop.repository.ProductRepository;
import com.example.clothesshop.service.WishlistService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class WishlistServiceImpl implements WishlistService {

    private final FavoriteRepository favoriteRepository;
    private final ProductRepository productRepository;

    public WishlistServiceImpl(FavoriteRepository favoriteRepository,
                              ProductRepository productRepository) {
        this.favoriteRepository = favoriteRepository;
        this.productRepository = productRepository;
    }

    @Override
    public WishlistResponse addToWishlist(User user, Long productId) {
        if (user == null) {
            throw new IllegalArgumentException("Vui lòng đăng nhập để thêm sản phẩm yêu thích");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm"));

        // Kiểm tra đã có trong wishlist chưa
        if (favoriteRepository.existsByUserAndProduct(user, product)) {
            throw new IllegalArgumentException("Sản phẩm đã có trong danh sách yêu thích");
        }

        // Tạo favorite mới
        Favorite favorite = new Favorite();
        favorite.setUser(user);
        favorite.setProduct(product);
        favoriteRepository.save(favorite);

        return getWishlist(user);
    }

    @Override
    public WishlistResponse removeFromWishlist(User user, Long productId) {
        if (user == null) {
            throw new IllegalArgumentException("Vui lòng đăng nhập");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm"));

        // Xóa favorite
        favoriteRepository.deleteByUserAndProduct(user, product);

        return getWishlist(user);
    }

    @Override
    public WishlistResponse toggleWishlist(User user, Long productId) {
        if (user == null) {
            throw new IllegalArgumentException("Vui lòng đăng nhập");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm"));

        // Kiểm tra đã có chưa
        if (favoriteRepository.existsByUserAndProduct(user, product)) {
            // Đã có -> Xóa
            favoriteRepository.deleteByUserAndProduct(user, product);
        } else {
            // Chưa có -> Thêm
            Favorite favorite = new Favorite();
            favorite.setUser(user);
            favorite.setProduct(product);
            favoriteRepository.save(favorite);
        }

        return getWishlist(user);
    }

    @Override
    @Transactional(readOnly = true)
    public WishlistResponse getWishlist(User user) {
        if (user == null) {
            return new WishlistResponse(List.of(), 0);
        }

        List<Favorite> favorites = favoriteRepository.findByUser(user);

        List<WishlistItemDto> items = favorites.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return new WishlistResponse(items, items.size());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isInWishlist(User user, Long productId) {
        if (user == null || productId == null) {
            return false;
        }

        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) {
            return false;
        }

        return favoriteRepository.existsByUserAndProduct(user, product);
    }

    @Override
    @Transactional(readOnly = true)
    public int getWishlistCount(User user) {
        if (user == null) {
            return 0;
        }

        return (int) favoriteRepository.countByUser(user);
    }

    @Override
    public void clearWishlist(User user) {
        if (user == null) {
            throw new IllegalArgumentException("Vui lòng đăng nhập");
        }

        List<Favorite> favorites = favoriteRepository.findByUser(user);
        favoriteRepository.deleteAll(favorites);
    }

    /**
     * Chuyển đổi Favorite entity sang WishlistItemDto
     */
    private WishlistItemDto convertToDto(Favorite favorite) {
        Product product = favorite.getProduct();

        String imageUrl = "/images/no-image.png";
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            imageUrl = product.getImages().get(0).getUrl();
        }

        return new WishlistItemDto(
                favorite.getId(),
                product.getId(),
                product.getName(),
                imageUrl,
                product.getMinPrice(),
                product.hasStock(),
                LocalDateTime.now() // Nếu Favorite không có createdAt
        );
    }
}
