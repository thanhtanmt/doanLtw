package com.example.clothesshop.repository;

import com.example.clothesshop.model.Cart;
import com.example.clothesshop.model.CartItem;
import com.example.clothesshop.model.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartAndVariant(Cart cart, ProductVariant variant);
    List<CartItem> findByCart(Cart cart);
    void deleteByCartIdAndId(Long cartId, Long itemId);
}
