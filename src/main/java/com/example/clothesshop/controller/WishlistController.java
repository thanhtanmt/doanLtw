package com.example.clothesshop.controller;

import com.example.clothesshop.dto.WishlistResponse;
import com.example.clothesshop.model.User;
import com.example.clothesshop.service.UserService;
import com.example.clothesshop.service.WishlistService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;
    private final UserService userService;

    public WishlistController(WishlistService wishlistService, UserService userService) {
        this.wishlistService = wishlistService;
        this.userService = userService;
    }

    /**
     * Hiển thị trang wishlist
     */
    @GetMapping
    public String wishlistPage(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            User user = userService.findByUsername(authentication.getName()).orElse(null);
            WishlistResponse wishlist = wishlistService.getWishlist(user);
            model.addAttribute("wishlist", wishlist);
        } else {
            model.addAttribute("wishlist", new WishlistResponse());
        }
        return "wishlist";
    }

    /**
     * API: Toggle wishlist (thêm hoặc xóa)
     * Đây là API chính để dùng cho nút trái tim
     */
    @PostMapping("/toggle/{productId}")
    @ResponseBody
    public ResponseEntity<?> toggleWishlist(@PathVariable Long productId,
                                           Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401)
                        .body(createResponse(false, "Vui lòng đăng nhập để thêm sản phẩm yêu thích", null));
            }

            User user = userService.findByUsername(authentication.getName())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));

            // Toggle: nếu đã có thì xóa, chưa có thì thêm
            boolean wasInWishlist = wishlistService.isInWishlist(user, productId);
            WishlistResponse response = wishlistService.toggleWishlist(user, productId);
            
            String message = wasInWishlist 
                    ? "Đã xóa khỏi danh sách yêu thích" 
                    : "Đã thêm vào danh sách yêu thích";

            Map<String, Object> data = new HashMap<>();
            data.put("wishlist", response);
            data.put("inWishlist", !wasInWishlist);
            data.put("count", response.getTotalItems());

            return ResponseEntity.ok(createResponse(true, message, data));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(createResponse(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(createResponse(false, "Có lỗi xảy ra: " + e.getMessage(), null));
        }
    }

    /**
     * API: Kiểm tra sản phẩm có trong wishlist không
     */
    @GetMapping("/check/{productId}")
    @ResponseBody
    public ResponseEntity<?> checkInWishlist(@PathVariable Long productId,
                                             Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.ok(Map.of("inWishlist", false));
            }

            User user = userService.findByUsername(authentication.getName()).orElse(null);
            boolean inWishlist = wishlistService.isInWishlist(user, productId);
            
            return ResponseEntity.ok(Map.of("inWishlist", inWishlist));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("inWishlist", false));
        }
    }

    /**
     * API: Lấy số lượng sản phẩm trong wishlist
     */
    @GetMapping("/count")
    @ResponseBody
    public ResponseEntity<?> getWishlistCount(Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.ok(Map.of("count", 0));
            }

            User user = userService.findByUsername(authentication.getName()).orElse(null);
            int count = wishlistService.getWishlistCount(user);
            
            return ResponseEntity.ok(Map.of("count", count));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("count", 0));
        }
    }

    /**
     * API: Lấy toàn bộ wishlist
     */
    @GetMapping("/api")
    @ResponseBody
    public ResponseEntity<?> getWishlist(Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.ok(new WishlistResponse());
            }

            User user = userService.findByUsername(authentication.getName()).orElse(null);
            WishlistResponse response = wishlistService.getWishlist(user);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(createResponse(false, "Có lỗi xảy ra: " + e.getMessage(), null));
        }
    }

    /**
     * API: Xóa sản phẩm khỏi wishlist (theo product ID)
     */
    @DeleteMapping("/remove/{productId}")
    @ResponseBody
    public ResponseEntity<?> removeFromWishlist(@PathVariable Long productId,
                                                Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401)
                        .body(createResponse(false, "Vui lòng đăng nhập", null));
            }

            User user = userService.findByUsername(authentication.getName())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));

            WishlistResponse response = wishlistService.removeFromWishlist(user, productId);
            
            return ResponseEntity.ok(createResponse(true, "Đã xóa khỏi danh sách yêu thích", 
                    Map.of("wishlist", response, "count", response.getTotalItems())));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(createResponse(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(createResponse(false, "Có lỗi xảy ra: " + e.getMessage(), null));
        }
    }

    /**
     * Helper method để tạo response JSON chuẩn
     */
    private Map<String, Object> createResponse(boolean success, String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", message);
        if (data != null) {
            response.put("data", data);
        }
        return response;
    }
}
