package com.example.clothesshop.service;

import com.example.clothesshop.dto.AddToCartRequest;
import com.example.clothesshop.dto.CartResponse;
import com.example.clothesshop.model.User;

public interface CartService {
    /**
     * Thêm sản phẩm vào giỏ hàng
     * @param user Người dùng hiện tại
     * @param request Thông tin sản phẩm cần thêm
     * @return CartResponse chứa thông tin giỏ hàng sau khi thêm
     */
    CartResponse addToCart(User user, AddToCartRequest request);

    /**
     * Lấy thông tin giỏ hàng của người dùng
     * @param user Người dùng hiện tại
     * @return CartResponse chứa thông tin giỏ hàng
     */
    CartResponse getCart(User user);

    /**
     * Xóa một item khỏi giỏ hàng
     * @param user Người dùng hiện tại
     * @param itemId ID của item cần xóa
     * @return CartResponse chứa thông tin giỏ hàng sau khi xóa
     */
    CartResponse removeFromCart(User user, Long itemId);

    /**
     * Cập nhật số lượng sản phẩm trong giỏ hàng
     * @param user Người dùng hiện tại
     * @param itemId ID của item cần cập nhật
     * @param quantity Số lượng mới
     * @return CartResponse chứa thông tin giỏ hàng sau khi cập nhật
     */
    CartResponse updateCartItemQuantity(User user, Long itemId, int quantity);

    /**
     * Xóa toàn bộ giỏ hàng
     * @param user Người dùng hiện tại
     */
    void clearCart(User user);

    /**
     * Lấy số lượng items trong giỏ hàng
     * @param user Người dùng hiện tại
     * @return Số lượng items
     */
    int getCartItemCount(User user);
}
