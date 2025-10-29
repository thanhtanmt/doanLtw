package com.example.clothesshop.service.impl;

import com.example.clothesshop.dto.AddToCartRequest;
import com.example.clothesshop.dto.CartItemDto;
import com.example.clothesshop.dto.CartResponse;
import com.example.clothesshop.model.*;
import com.example.clothesshop.repository.CartRepository;
import com.example.clothesshop.repository.CartItemRepository;
import com.example.clothesshop.repository.ProductVariantRepository;
import com.example.clothesshop.service.CartService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductVariantRepository productVariantRepository;

    public CartServiceImpl(CartRepository cartRepository, 
                          CartItemRepository cartItemRepository,
                          ProductVariantRepository productVariantRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productVariantRepository = productVariantRepository;
    }

    @Override
    public CartResponse addToCart(User user, AddToCartRequest request) {
        if (user == null) {
            throw new IllegalArgumentException("Vui lòng đăng nhập để thêm sản phẩm vào giỏ hàng");
        }

        // Lấy hoặc tạo giỏ hàng cho user
        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });

        // Tìm product variant
        ProductVariant variant = productVariantRepository.findById(request.getVariantId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm"));

        // Kiểm tra số lượng tồn kho
        if (!variant.isInStock() || variant.getQuantity() < request.getQuantity()) {
            throw new IllegalArgumentException("Sản phẩm không đủ số lượng trong kho");
        }

        // Kiểm tra xem item đã có trong giỏ hàng chưa
        CartItem cartItem = cartItemRepository.findByCartAndVariant(cart, variant)
                .orElseGet(() -> {
                    CartItem newItem = new CartItem();
                    newItem.setCart(cart);
                    newItem.setVariant(variant);
                    newItem.setProduct(variant.getProduct());
                    newItem.setPriceAtAddTime(variant.getPrice());
                    newItem.setQuantity(0);
                    return newItem;
                });

        // Cập nhật số lượng
        int newQuantity = cartItem.getQuantity() + request.getQuantity();
        if (newQuantity > variant.getQuantity()) {
            throw new IllegalArgumentException("Số lượng vượt quá tồn kho");
        }

        cartItem.setQuantity(newQuantity);
        cartItemRepository.save(cartItem);

        return getCart(user);
    }

    @Override
    @Transactional(readOnly = true)
    public CartResponse getCart(User user) {
        if (user == null) {
            return new CartResponse(null, List.of(), BigDecimal.ZERO, 0);
        }

        Cart cart = cartRepository.findByUser(user).orElse(null);
        if (cart == null) {
            return new CartResponse(null, List.of(), BigDecimal.ZERO, 0);
        }

        List<CartItem> items = cartItemRepository.findByCart(cart);
        
        List<CartItemDto> itemDtos = items.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        BigDecimal totalAmount = items.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalItems = items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();

        return new CartResponse(cart.getId(), itemDtos, totalAmount, totalItems);
    }

    @Override
    public CartResponse removeFromCart(User user, Long itemId) {
        if (user == null) {
            throw new IllegalArgumentException("Vui lòng đăng nhập");
        }

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy giỏ hàng"));

        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm trong giỏ hàng"));

        // Kiểm tra xem item có thuộc về giỏ hàng của user không
        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new IllegalArgumentException("Không có quyền xóa sản phẩm này");
        }

        cartItemRepository.delete(cartItem);

        return getCart(user);
    }

    @Override
    public CartResponse updateCartItemQuantity(User user, Long itemId, int quantity) {
        if (user == null) {
            throw new IllegalArgumentException("Vui lòng đăng nhập");
        }

        if (quantity <= 0) {
            return removeFromCart(user, itemId);
        }

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy giỏ hàng"));

        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm trong giỏ hàng"));

        // Kiểm tra quyền sở hữu
        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new IllegalArgumentException("Không có quyền cập nhật sản phẩm này");
        }

        // Kiểm tra tồn kho
        ProductVariant variant = cartItem.getVariant();
        if (quantity > variant.getQuantity()) {
            throw new IllegalArgumentException("Số lượng vượt quá tồn kho");
        }

        cartItem.setQuantity(quantity);
        cartItemRepository.save(cartItem);

        return getCart(user);
    }

    @Override
    public void clearCart(User user) {
        if (user == null) {
            throw new IllegalArgumentException("Vui lòng đăng nhập");
        }

        Cart cart = cartRepository.findByUser(user).orElse(null);
        if (cart != null) {
            cartItemRepository.deleteAll(cart.getItems());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public int getCartItemCount(User user) {
        if (user == null) {
            return 0;
        }

        Cart cart = cartRepository.findByUser(user).orElse(null);
        if (cart == null) {
            return 0;
        }

        return cartItemRepository.findByCart(cart).stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    /**
     * Chuyển đổi CartItem entity sang CartItemDto
     */
    private CartItemDto convertToDto(CartItem item) {
        Product product = item.getProduct();
        ProductVariant variant = item.getVariant();
        
        String imageUrl = product.getImages().isEmpty() 
                ? "/images/no-image.png" 
                : product.getImages().get(0).getUrl();

        return new CartItemDto(
                item.getId(),
                product.getId(),
                product.getName(),
                variant.getId(),
                variant.getDisplayName(),
                item.getQuantity(),
                item.getPriceAtAddTime(),
                item.getSubtotal(),
                imageUrl
        );
    }
}
