# Hướng Dẫn Cấu Hình Cloudinary

## 🎯 Tổng Quan
Cloudinary là dịch vụ lưu trữ và quản lý ảnh trên cloud. Project đã được tích hợp sẵn Cloudinary để upload avatar.

## 📋 Bước 1: Đăng ký Cloudinary

1. Truy cập: https://cloudinary.com/
2. Click **Sign Up for Free**
3. Đăng ký với email hoặc Google
4. Xác nhận email

## 🔑 Bước 2: Lấy Credentials

1. Sau khi đăng nhập, vào **Dashboard**
2. Bạn sẽ thấy:
   ```
   Cloud name: your_cloud_name
   API Key: 123456789012345
   API Secret: ABC...XYZ
   ```

## ⚙️ Bước 3: Cấu Hình Project

### Cập nhật `application.properties`:

Mở file: `src/main/resources/application.properties`

Thay thế các giá trị:
```properties
# Cloudinary Configuration
cloudinary.cloud-name=YOUR_CLOUD_NAME_HERE
cloudinary.api-key=YOUR_API_KEY_HERE
cloudinary.api-secret=YOUR_API_SECRET_HERE
```

**Ví dụ:**
```properties
cloudinary.cloud-name=dxyz123abc
cloudinary.api-key=123456789012345
cloudinary.api-secret=AbCdEfGhIjKlMnOpQrStUvWxYz
```

## 🚀 Bước 4: Test

1. Restart Spring Boot application
2. Đăng nhập vào admin: http://localhost:8080/admin
3. Vào **Profile** (góc phải → Click avatar → Profile)
4. Upload ảnh avatar mới
5. Kiểm tra trên Cloudinary Dashboard → Media Library

## 📁 Cấu Trúc Thư Mục

Ảnh sẽ được lưu trong folder:
- **admin-avatars/** - Avatar của admin
- **user-avatars/** - Avatar của user (nếu thêm sau)
- **products/** - Ảnh sản phẩm (nếu thêm sau)

## ✨ Tính Năng Đã Implement

### 1. Upload Ảnh
```java
cloudinaryService.uploadImage(file, "admin-avatars")
```
- ✅ Tự động compress
- ✅ Chuyển đổi format tối ưu
- ✅ Trả về HTTPS URL
- ✅ Lưu trong folder cụ thể

### 2. Xóa Ảnh Cũ
```java
cloudinaryService.deleteImage(publicId)
```
- ✅ Tự động xóa ảnh cũ khi upload ảnh mới
- ✅ Tiết kiệm storage

### 3. Validation
- ✅ Kiểm tra file không rỗng
- ✅ Chỉ accept file ảnh (jpg, png, gif, webp...)
- ✅ Bắt lỗi và hiển thị message

## 🔧 Sử Dụng Cho Module Khác

### Upload ảnh sản phẩm:
```java
@Autowired
private CloudinaryService cloudinaryService;

// Upload
String imageUrl = cloudinaryService.uploadImage(file, "products");
product.setImageUrl(imageUrl);

// Xóa
String publicId = cloudinaryService.extractPublicId(product.getImageUrl());
cloudinaryService.deleteImage(publicId);
```

## 💰 Giới Hạn Free Plan

Cloudinary Free Plan:
- ✅ 25 GB Storage
- ✅ 25 GB Bandwidth/tháng
- ✅ 25,000 transformations/tháng
- ✅ Đủ cho project học tập và demo

## 🛠️ Troubleshooting

### Lỗi: "Invalid API credentials"
➡️ Kiểm tra lại `cloud-name`, `api-key`, `api-secret` trong `application.properties`

### Lỗi: "File is too large"
➡️ Cloudinary free plan giới hạn 10MB/file
➡️ Nén ảnh trước khi upload

### Lỗi: "Bandwidth quota exceeded"
➡️ Đã dùng hết 25GB/tháng
➡️ Chờ tháng sau hoặc upgrade plan

## 📝 Ghi Chú

- Ảnh được lưu trên cloud, không lưu trên server
- URL ảnh dạng: `https://res.cloudinary.com/your-cloud/image/upload/v123456/admin-avatars/abc.jpg`
- Có thể truy cập từ bất kỳ đâu (không cần config CORS)
- Tự động có CDN (nhanh trên toàn cầu)

## 🔗 Tài Liệu

- Dashboard: https://cloudinary.com/console
- Docs: https://cloudinary.com/documentation
- Java SDK: https://cloudinary.com/documentation/java_integration
