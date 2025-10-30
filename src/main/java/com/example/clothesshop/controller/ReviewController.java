package com.example.clothesshop.controller;

import com.example.clothesshop.model.Review;
import com.example.clothesshop.model.User;
import com.example.clothesshop.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private com.example.clothesshop.repository.UserRepository userRepository;

    /**
     * Tạo review mới cho sản phẩm
     */
    @PostMapping
    @ResponseBody
    public ResponseEntity<?> createReview(
            @RequestParam Long productId,
            @RequestParam int rating,
            @RequestParam(required = false) String comment,
            @RequestParam(required = false) List<MultipartFile> images,
            @RequestParam(required = false) List<MultipartFile> videos,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        try {
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));

            Review review = reviewService.createReview(user, productId, rating, comment, images, videos);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đánh giá thành công!");
            response.put("reviewId", review.getId());
            
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Có lỗi xảy ra: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Cập nhật review
     */
    @PutMapping("/{reviewId}")
    @ResponseBody
    public ResponseEntity<?> updateReview(
            @PathVariable Long reviewId,
            @RequestParam int rating,
            @RequestParam(required = false) String comment,
            @RequestParam(required = false) List<MultipartFile> images,
            @RequestParam(required = false) List<MultipartFile> videos,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        try {
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));

            reviewService.updateReview(reviewId, user, rating, comment, images, videos);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cập nhật đánh giá thành công!");
            
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Có lỗi xảy ra: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Xóa review
     */
    @DeleteMapping("/{reviewId}")
    @ResponseBody
    public ResponseEntity<?> deleteReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        try {
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));

            reviewService.deleteReview(reviewId, user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Xóa đánh giá thành công!");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Có lỗi xảy ra: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Lấy danh sách review của sản phẩm
     */
    @GetMapping("/product/{productId}")
    @ResponseBody
    public ResponseEntity<?> getProductReviews(@PathVariable Long productId) {
        try {
            List<Map<String, Object>> reviews = reviewService.getProductReviews(productId);
            Map<String, Object> stats = reviewService.getProductReviewStats(productId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("reviews", reviews);
            response.put("stats", stats);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Có lỗi xảy ra: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Kiểm tra user có thể review sản phẩm không và lấy review hiện có (nếu có)
     */
    @GetMapping("/check/{productId}")
    @ResponseBody
    public ResponseEntity<?> checkReviewStatus(
            @PathVariable Long productId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        try {
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));

            boolean canReview = reviewService.canUserReviewProduct(user.getId(), productId);
            Optional<Review> existingReview = reviewService.getUserReviewForProduct(user.getId(), productId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("canReview", canReview);
            response.put("hasReviewed", existingReview.isPresent());
            
            if (existingReview.isPresent()) {
                Review review = existingReview.get();
                Map<String, Object> reviewData = new HashMap<>();
                reviewData.put("id", review.getId());
                reviewData.put("rating", review.getRating());
                reviewData.put("comment", review.getComment());
                reviewData.put("createdAt", review.getCreatedAt());
                response.put("review", reviewData);
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Có lỗi xảy ra: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
