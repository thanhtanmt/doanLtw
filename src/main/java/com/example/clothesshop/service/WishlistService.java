package com.example.clothesshop.service;

import com.example.clothesshop.dto.WishlistResponse;
import com.example.clothesshop.model.User;

public interface WishlistService {
    
    /**
     * Thêm sản phẩm vào wishlist
     */
    WishlistResponse addToWishlist(User user, Long productId);
    
    /**
     * Xóa sản phẩm khỏi wishlist
     */
    WishlistResponse removeFromWishlist(User user, Long productId);
    
    /**
     * Toggle wishlist - nếu đã có thì xóa, chưa có thì thêm
     */
    WishlistResponse toggleWishlist(User user, Long productId);
    
    /**
     * Lấy danh sách wishlist của user
     */
    WishlistResponse getWishlist(User user);
    
    /**
     * Kiểm tra sản phẩm có trong wishlist không
     */
    boolean isInWishlist(User user, Long productId);
    
    /**
     * Đếm số lượng sản phẩm trong wishlist
     */
    int getWishlistCount(User user);
    
    /**
     * Xóa toàn bộ wishlist
     */
    void clearWishlist(User user);
}
