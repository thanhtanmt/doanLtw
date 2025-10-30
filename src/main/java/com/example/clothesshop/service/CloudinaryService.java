package com.example.clothesshop.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    /**
     * Upload ảnh lên Cloudinary
     * @param file - File ảnh cần upload
     * @param folder - Thư mục lưu trữ trên Cloudinary (vd: "avatars", "products")
     * @return URL của ảnh đã upload
     */
    @SuppressWarnings("unchecked")
    public String uploadImage(MultipartFile file, String folder) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File không được trống");
        }

        // Kiểm tra file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File phải là ảnh (jpg, png, gif, etc.)");
        }

        // Upload lên Cloudinary với các tham số cơ bản
        Map<String, Object> uploadParams = ObjectUtils.asMap(
            "folder", folder,
            "resource_type", "image"
        );

        Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);
        
        // Trả về secure URL (HTTPS)
        return (String) uploadResult.get("secure_url");
    }
    
    /**
     * Upload video lên Cloudinary
     * @param file - File video cần upload
     * @param folder - Thư mục lưu trữ trên Cloudinary
     * @return URL của video đã upload
     */
    @SuppressWarnings("unchecked")
    public String uploadVideo(MultipartFile file, String folder) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File không được trống");
        }

        // Kiểm tra file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("video/")) {
            throw new IllegalArgumentException("File phải là video (mp4, mov, avi, etc.)");
        }

        // Upload lên Cloudinary với resource_type là video
        Map<String, Object> uploadParams = ObjectUtils.asMap(
            "folder", folder,
            "resource_type", "video"
        );

        Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);
        
        // Trả về secure URL (HTTPS)
        return (String) uploadResult.get("secure_url");
    }

    /**
     * Xóa ảnh khỏi Cloudinary
     * @param publicId - Public ID của ảnh (lấy từ URL)
     */
    public void deleteImage(String publicId) throws IOException {
        if (publicId != null && !publicId.isEmpty()) {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        }
    }

    /**
     * Lấy public ID từ Cloudinary URL
     * @param imageUrl - URL của ảnh
     * @return Public ID
     */
    public String extractPublicId(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return null;
        }
        
        // URL format: https://res.cloudinary.com/{cloud_name}/image/upload/v{version}/{folder}/{public_id}.{format}
        String[] parts = imageUrl.split("/upload/");
        if (parts.length < 2) {
            return null;
        }
        
        String pathAfterUpload = parts[1];
        // Bỏ version number (vXXXXXXXXXX)
        String[] pathParts = pathAfterUpload.split("/", 2);
        if (pathParts.length < 2) {
            return null;
        }
        
        String fullPath = pathParts[1];
        // Bỏ extension (.jpg, .png, etc.)
        int lastDotIndex = fullPath.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return fullPath.substring(0, lastDotIndex);
        }
        
        return fullPath;
    }
}
