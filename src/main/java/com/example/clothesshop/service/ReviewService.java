package com.example.clothesshop.service;

import com.example.clothesshop.model.Order;
import com.example.clothesshop.model.OrderStatus;
import com.example.clothesshop.model.Product;
import com.example.clothesshop.model.Review;
import com.example.clothesshop.model.User;
import com.example.clothesshop.repository.OrderRepository;
import com.example.clothesshop.repository.ProductRepository;
import com.example.clothesshop.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private CloudinaryService cloudinaryService;

    @Transactional
    public Review createReview(User user, Long productId, int rating, String comment, 
                               List<MultipartFile> images, List<MultipartFile> videos) {
        // Kiểm tra xem user đã mua và nhận sản phẩm này chưa
        if (!hasUserReceivedProduct(user.getId(), productId)) {
            throw new IllegalStateException("Bạn chỉ có thể đánh giá sản phẩm đã mua và nhận hàng");
        }

        // Kiểm tra xem đã review chưa
        if (reviewRepository.existsByUserIdAndProductId(user.getId(), productId)) {
            throw new IllegalStateException("Bạn đã đánh giá sản phẩm này rồi");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm"));

        Review review = new Review();
        review.setUser(user);
        review.setProduct(product);
        review.setRating(rating);
        review.setComment(comment);
        
        // Upload images
        if (images != null && !images.isEmpty()) {
            List<String> imageUrls = new ArrayList<>();
            for (MultipartFile image : images) {
                try {
                    String imageUrl = cloudinaryService.uploadImage(image, "reviews");
                    imageUrls.add(imageUrl);
                } catch (Exception e) {
                    System.err.println("Error uploading review image: " + e.getMessage());
                }
            }
            review.setImages(imageUrls);
        }
        
        // Upload videos
        if (videos != null && !videos.isEmpty()) {
            List<String> videoUrls = new ArrayList<>();
            for (MultipartFile video : videos) {
                try {
                    String videoUrl = cloudinaryService.uploadVideo(video, "reviews");
                    videoUrls.add(videoUrl);
                } catch (Exception e) {
                    System.err.println("Error uploading review video: " + e.getMessage());
                }
            }
            review.setVideos(videoUrls);
        }

        return reviewRepository.save(review);
    }

    @Transactional
    public Review updateReview(Long reviewId, User user, int rating, String comment,
                              List<MultipartFile> images, List<MultipartFile> videos) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đánh giá"));

        // Kiểm tra quyền sở hữu
        if (!review.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("Bạn không có quyền chỉnh sửa đánh giá này");
        }

        review.setRating(rating);
        review.setComment(comment);
        
        // Upload new images if provided
        if (images != null && !images.isEmpty()) {
            List<String> imageUrls = new ArrayList<>();
            for (MultipartFile image : images) {
                try {
                    String imageUrl = cloudinaryService.uploadImage(image, "reviews");
                    imageUrls.add(imageUrl);
                } catch (Exception e) {
                    System.err.println("Error uploading review image: " + e.getMessage());
                }
            }
            // Add to existing images
            if (review.getImages() == null) {
                review.setImages(imageUrls);
            } else {
                review.getImages().addAll(imageUrls);
            }
        }
        
        // Upload new videos if provided
        if (videos != null && !videos.isEmpty()) {
            List<String> videoUrls = new ArrayList<>();
            for (MultipartFile video : videos) {
                try {
                    String videoUrl = cloudinaryService.uploadVideo(video, "reviews");
                    videoUrls.add(videoUrl);
                } catch (Exception e) {
                    System.err.println("Error uploading review video: " + e.getMessage());
                }
            }
            // Add to existing videos
            if (review.getVideos() == null) {
                review.setVideos(videoUrls);
            } else {
                review.getVideos().addAll(videoUrls);
            }
        }

        return reviewRepository.save(review);
    }

    @Transactional
    public void deleteReview(Long reviewId, User user) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đánh giá"));

        // Kiểm tra quyền sở hữu
        if (!review.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("Bạn không có quyền xóa đánh giá này");
        }

        reviewRepository.delete(review);
    }

    public List<Map<String, Object>> getProductReviews(Long productId) {
        List<Review> reviews = reviewRepository.findByProductId(productId);
        return reviews.stream()
                .map(this::convertReviewToMap)
                .collect(Collectors.toList());
    }

    public Map<String, Object> getProductReviewStats(Long productId) {
        List<Review> reviews = reviewRepository.findByProductId(productId);
        Map<String, Object> stats = new HashMap<>();

        long totalReviews = reviews.size();
        double averageRating = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

        // Đếm số review theo từng mức sao
        Map<Integer, Long> ratingCounts = reviews.stream()
                .collect(Collectors.groupingBy(Review::getRating, Collectors.counting()));

        stats.put("totalReviews", totalReviews);
        stats.put("averageRating", Math.round(averageRating * 10.0) / 10.0);
        stats.put("ratingCounts", ratingCounts);
        stats.put("fiveStar", ratingCounts.getOrDefault(5, 0L));
        stats.put("fourStar", ratingCounts.getOrDefault(4, 0L));
        stats.put("threeStar", ratingCounts.getOrDefault(3, 0L));
        stats.put("twoStar", ratingCounts.getOrDefault(2, 0L));
        stats.put("oneStar", ratingCounts.getOrDefault(1, 0L));

        return stats;
    }

    public Optional<Review> getUserReviewForProduct(Long userId, Long productId) {
        return reviewRepository.findByUserIdAndProductId(userId, productId);
    }

    public boolean canUserReviewProduct(Long userId, Long productId) {
        // Kiểm tra đã review chưa
        if (reviewRepository.existsByUserIdAndProductId(userId, productId)) {
            return false;
        }
        // Kiểm tra đã mua và nhận hàng chưa
        return hasUserReceivedProduct(userId, productId);
    }

    private boolean hasUserReceivedProduct(Long userId, Long productId) {
        // Tìm tất cả đơn hàng đã giao của user chứa sản phẩm này
        List<Order> deliveredOrders = orderRepository.findByUser_IdOrderByCreatedAtDesc(userId).stream()
                .filter(order -> order.getStatus() == OrderStatus.DELIVERED)
                .toList();
        
        return deliveredOrders.stream()
                .anyMatch(order -> order.getOrderDetails().stream()
                        .anyMatch(detail -> detail.getVariant() != null 
                                && detail.getVariant().getProduct() != null
                                && detail.getVariant().getProduct().getId().equals(productId)));
    }

    private Map<String, Object> convertReviewToMap(Review review) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", review.getId());
        map.put("rating", review.getRating());
        map.put("comment", review.getComment());
        map.put("createdAt", review.getCreatedAt());
        map.put("images", review.getImages() != null ? review.getImages() : new ArrayList<>());
        map.put("videos", review.getVideos() != null ? review.getVideos() : new ArrayList<>());
        
        if (review.getUser() != null) {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", review.getUser().getId());
            userMap.put("fullName", review.getUser().getFirstName() + " " + review.getUser().getLastName());
            userMap.put("email", review.getUser().getEmail());
            map.put("user", userMap);
        }
        
        return map;
    }
}
