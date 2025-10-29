package com.example.clothesshop.controller;

import com.example.clothesshop.dto.AddToCartRequest;
import com.example.clothesshop.dto.CartResponse;
import com.example.clothesshop.model.User;
import com.example.clothesshop.service.CartService;
import com.example.clothesshop.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    private final UserService userService;

    public CartController(CartService cartService, UserService userService) {
        this.cartService = cartService;
        this.userService = userService;
    }

    /**
     * Hiển thị trang giỏ hàng
     */
    @GetMapping
    public String cart(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            User user = userService.findByUsername(authentication.getName()).orElse(null);
            CartResponse cartResponse = cartService.getCart(user);
            model.addAttribute("cart", cartResponse);
        } else {
            model.addAttribute("cart", new CartResponse());
        }
        return "cart";
    }

    /**
     * API: Thêm sản phẩm vào giỏ hàng
     */
    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity<?> addToCart(@RequestBody AddToCartRequest request, 
                                       Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body(createErrorResponse("Vui lòng đăng nhập để thêm sản phẩm vào giỏ hàng"));
            }

            User user = userService.findByUsername(authentication.getName())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));

            CartResponse cartResponse = cartService.addToCart(user, request);
            return ResponseEntity.ok(createSuccessResponse("Đã thêm sản phẩm vào giỏ hàng", cartResponse));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("Có lỗi xảy ra: " + e.getMessage()));
        }
    }

    /**
     * API: Lấy thông tin giỏ hàng
     */
    @GetMapping("/api")
    @ResponseBody
    public ResponseEntity<?> getCart(Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.ok(new CartResponse());
            }

            User user = userService.findByUsername(authentication.getName()).orElse(null);
            CartResponse cartResponse = cartService.getCart(user);
            return ResponseEntity.ok(cartResponse);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("Có lỗi xảy ra: " + e.getMessage()));
        }
    }

    /**
     * API: Xóa sản phẩm khỏi giỏ hàng
     */
    @DeleteMapping("/remove/{itemId}")
    @ResponseBody
    public ResponseEntity<?> removeFromCart(@PathVariable Long itemId, 
                                           Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body(createErrorResponse("Vui lòng đăng nhập"));
            }

            User user = userService.findByUsername(authentication.getName())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));

            CartResponse cartResponse = cartService.removeFromCart(user, itemId);
            return ResponseEntity.ok(createSuccessResponse("Đã xóa sản phẩm khỏi giỏ hàng", cartResponse));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("Có lỗi xảy ra: " + e.getMessage()));
        }
    }

    /**
     * API: Cập nhật số lượng sản phẩm trong giỏ hàng
     */
    @PutMapping("/update/{itemId}")
    @ResponseBody
    public ResponseEntity<?> updateCartItem(@PathVariable Long itemId,
                                           @RequestParam int quantity,
                                           Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body(createErrorResponse("Vui lòng đăng nhập"));
            }

            User user = userService.findByUsername(authentication.getName())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));

            CartResponse cartResponse = cartService.updateCartItemQuantity(user, itemId, quantity);
            return ResponseEntity.ok(createSuccessResponse("Đã cập nhật giỏ hàng", cartResponse));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("Có lỗi xảy ra: " + e.getMessage()));
        }
    }

    /**
     * API: Xóa toàn bộ giỏ hàng
     */
    @DeleteMapping("/clear")
    @ResponseBody
    public ResponseEntity<?> clearCart(Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body(createErrorResponse("Vui lòng đăng nhập"));
            }

            User user = userService.findByUsername(authentication.getName())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));

            cartService.clearCart(user);
            return ResponseEntity.ok(createSuccessResponse("Đã xóa toàn bộ giỏ hàng", null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("Có lỗi xảy ra: " + e.getMessage()));
        }
    }

    /**
     * API: Lấy số lượng items trong giỏ hàng
     */
    @GetMapping("/count")
    @ResponseBody
    public ResponseEntity<?> getCartCount(Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.ok(Map.of("count", 0));
            }

            User user = userService.findByUsername(authentication.getName()).orElse(null);
            int count = cartService.getCartItemCount(user);
            return ResponseEntity.ok(Map.of("count", count));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("Có lỗi xảy ra: " + e.getMessage()));
        }
    }

    // Helper methods
    private Map<String, Object> createSuccessResponse(String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        if (data != null) {
            response.put("data", data);
        }
        return response;
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        return response;
    }
}


