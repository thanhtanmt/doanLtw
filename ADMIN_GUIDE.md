# Hướng Dẫn Sử Dụng Giao Diện Admin Mới

## Tổng Quan
Giao diện admin đã được thiết kế lại hoàn toàn với:
- **Sidebar menu** hiện đại, dễ sử dụng
- **Dashboard** với thống kê tổng quan
- **Quản lý người dùng** với khả năng chuyển đổi role
- **Quản lý shop** với báo cáo doanh thu
- **Quản lý shipper** với theo dõi đơn hàng
- **Quản lý voucher** áp dụng cho nhiều sản phẩm

## Các Tính Năng Chính

### 1. Dashboard (Trang chủ Admin)
**URL:** `/admin` hoặc `/admin/dashboard`

**Chức năng:**
- Hiển thị thống kê tổng quan hệ thống
- Thẻ thống kê: Doanh thu, Đơn hàng, Người dùng, Sản phẩm
- Thao tác nhanh đến các trang quản lý
- Đơn hàng gần đây
- Trạng thái hệ thống

### 2. Quản Lý Người Dùng
**URL:** `/admin/users`

**Chức năng:**
- Xem danh sách tất cả người dùng
- **Thống kê:** Tổng người dùng, Người bán, Shipper, Người đang hoạt động
- **Tìm kiếm:** Theo tên, email, username
- **Lọc:** Theo vai trò (ROLE_USER, ROLE_SELLER, ROLE_SHIPPER, ROLE_ADMIN)

**Thao tác trên người dùng:**
- **Xem chi tiết:** Click nút mắt (👁️) để xem thông tin chi tiết
- **Đổi vai trò:** Click nút khiên (🛡️) để mở modal chuyển đổi vai trò
  - Chọn vai trò mới: USER, SELLER, SHIPPER, hoặc ADMIN
  - Lưu thay đổi
- **Khóa/Mở khóa:** Click nút khóa (🔒/🔓) để thay đổi trạng thái tài khoản

**API Endpoints:**
- `GET /admin/users` - Danh sách người dùng
- `GET /admin/users/{id}` - Chi tiết người dùng (JSON)
- `POST /admin/users/change-role` - Thay đổi vai trò
- `POST /admin/users/{id}/toggle-status` - Khóa/Mở khóa tài khoản

### 3. Quản Lý Shop & Doanh Thu
**URL:** `/admin/shops`

**Chức năng:**
- Xem danh sách tất cả shop
- **Thống kê:**
  - Tổng số shop
  - Tổng doanh thu
  - Tổng đơn hàng
  - Doanh thu trung bình/Shop
- **Bộ lọc:** Theo thời gian (Hôm nay, Tuần, Tháng, Năm)
- **Xếp hạng shop** theo doanh thu
- **Top sản phẩm bán chạy**

**Thông tin hiển thị:**
- Tên shop, Người bán
- Số lượng sản phẩm
- Số đơn hàng (Tổng & Hoàn thành)
- Doanh thu
- Hoa hồng (% và số tiền)
- Trạng thái hoạt động

**Thao tác:**
- **Xem chi tiết:** Thông tin đầy đủ về shop
- **Xem sản phẩm:** Danh sách sản phẩm của shop
- **Xem đơn hàng:** Lịch sử đơn hàng

**API Endpoints (TODO):**
- `GET /admin/shops?period=month` - Danh sách shop với doanh thu
- `GET /admin/shops/{sellerId}/details` - Chi tiết shop

### 4. Quản Lý Shipper
**URL:** `/admin/shippers`

**Chức năng:**
- Xem danh sách tất cả shipper
- **Thống kê:**
  - Tổng shipper
  - Đơn giao thành công
  - Đơn đang giao
  - Đơn thất bại
- **Hiệu suất shipper:**
  - Tổng đơn đã nhận
  - Số đơn thành công
  - Số đơn thất bại
  - Tỷ lệ thành công (%)
  - Tổng thu nhập

**Bảng đơn hàng gần đây:**
- Mã đơn, Shipper, Khách hàng
- Địa chỉ giao, Giá trị đơn
- Phí ship, Trạng thái
- Ngày cập nhật

**Lọc trạng thái:**
- Chờ lấy hàng
- Đã lấy hàng
- Đang giao
- Đã giao
- Thất bại

**API Endpoints (TODO):**
- `GET /admin/shippers` - Danh sách shipper
- `GET /admin/shippers/{id}/details` - Chi tiết shipper
- `GET /admin/shippers/{id}/orders` - Lịch sử đơn hàng

### 5. Quản Lý Voucher
**URL:** `/admin/vouchers`

**Chức năng:**
- Xem danh sách voucher
- **Thống kê:**
  - Tổng voucher
  - Đang hoạt động
  - Đã sử dụng
  - Tổng giảm giá
- **Tạo voucher mới**
- **Chỉnh sửa voucher**
- **Kích hoạt/Vô hiệu hóa voucher**

**Tạo Voucher Mới:**

Click nút **"Tạo voucher mới"** và điền thông tin:

1. **Thông tin cơ bản:**
   - Mã voucher (4-20 ký tự, chỉ chữ IN HOA và số)
   - Tên voucher
   - Mô tả

2. **Loại giảm giá:**
   - **Phần trăm (%):** Giảm theo % giá trị đơn hàng
   - **Số tiền cố định (đ):** Giảm cố định số tiền

3. **Điều kiện:**
   - Giá trị giảm (% hoặc số tiền)
   - Giảm tối đa (cho loại %)
   - Đơn hàng tối thiểu

4. **Số lượng:**
   - Tổng số lượng voucher
   - Giới hạn sử dụng/người

5. **Thời hạn:**
   - Ngày bắt đầu
   - Ngày kết thúc

6. **Áp dụng:**
   - **Chọn sản phẩm:** Giữ Ctrl để chọn nhiều
   - **Để trống:** Áp dụng cho tất cả sản phẩm

7. **Kích hoạt ngay:** Checkbox để kích hoạt voucher

**API Endpoints (TODO):**
- `GET /admin/vouchers` - Danh sách voucher
- `POST /admin/vouchers/create` - Tạo voucher mới
- `GET /admin/vouchers/{id}/edit` - Lấy thông tin để edit
- `POST /admin/vouchers/update` - Cập nhật voucher
- `POST /admin/vouchers/{id}/toggle-status` - Bật/Tắt voucher
- `GET /admin/products/all` - Lấy danh sách sản phẩm

## Cấu Trúc File

### Frontend (Templates)
```
src/main/resources/templates/
├── layout/
│   └── admin.html              # Layout chính với sidebar
├── admin/
│   ├── users.html              # Quản lý người dùng
│   ├── shops.html              # Quản lý shop & doanh thu
│   ├── shippers.html           # Quản lý shipper
│   └── vouchers.html           # Quản lý voucher
└── admin-home.html             # Dashboard
```

### CSS
```
src/main/resources/static/css/
└── admin-modern.css            # CSS hiện đại cho admin
```

### Backend (Controller)
```
src/main/java/com/example/clothesshop/controller/
└── AdminController.java        # Tất cả endpoints admin
```

## Ghi Chú Quan Trọng

### TODO - Cần Hoàn Thành

Các chức năng sau đã có giao diện nhưng **backend chưa implement đầy đủ**:

1. **Shop Management:**
   - Tính toán doanh thu thực tế
   - Top products bán chạy
   - Thống kê theo thời gian

2. **Shipper Management:**
   - Lấy danh sách shipper từ database
   - Thống kê đơn hàng shipper
   - Tính thu nhập shipper

3. **Voucher Management:**
   - CRUD voucher trong database
   - Áp dụng voucher cho sản phẩm cụ thể
   - Kiểm tra điều kiện voucher
   - Tính toán giảm giá

### Để Implement Đầy Đủ, Cần:

1. **Tạo Model Voucher** (nếu chưa có đầy đủ)
2. **Tạo VoucherRepository**
3. **Tạo VoucherService** với logic:
   - Validate voucher
   - Apply discount
   - Check usage limits
4. **Tạo ShipperStatisticsService** để tính toán
5. **Tạo ShopStatisticsService** để tính doanh thu

## Tính Năng Đặc Biệt

### 1. Responsive Design
- Tự động điều chỉnh cho mobile, tablet, desktop
- Sidebar collapse trên màn hình nhỏ

### 2. Real-time Search & Filter
- Tìm kiếm không cần reload trang
- Lọc kết hợp nhiều tiêu chí

### 3. Modal Windows
- Popup hiện đại cho các form
- Click outside để đóng
- Animation mượt mà

### 4. Status Badges
- Màu sắc trực quan cho trạng thái
- Active (xanh), Inactive (đỏ), Pending (vàng), Completed (xanh dương)

### 5. Interactive Tables
- Hover effects
- Sort columns (sẵn sàng implement)
- Action buttons với tooltips

## Bảo Mật

- Tất cả endpoints đều yêu cầu **ROLE_ADMIN**
- CSRF protection cho tất cả POST requests
- Spring Security authentication required

## Khởi Động Ứng Dụng

1. Đảm bảo database đã được khởi tạo
2. Chạy Spring Boot application
3. Truy cập: `http://localhost:8080/admin`
4. Đăng nhập bằng tài khoản admin

## Tùy Chỉnh

### Thay Đổi Màu Sắc
Chỉnh sửa trong `admin-modern.css`:
```css
:root {
    --primary-color: #4f46e5;
    --success-color: #10b981;
    --danger-color: #ef4444;
    --warning-color: #f59e0b;
    /* ... */
}
```

### Thêm Menu Item Mới
Trong `layout/admin.html`, thêm vào `<nav class="sidebar-nav">`:
```html
<a th:href="@{/admin/new-feature}" class="nav-item">
    <i class="fas fa-icon"></i>
    <span>New Feature</span>
</a>
```

## Hỗ Trợ

Nếu gặp vấn đề:
1. Kiểm tra console log trong browser (F12)
2. Kiểm tra Spring Boot logs
3. Đảm bảo tất cả dependencies đã được import
4. Verify CSRF token trong forms

## Phiên Bản
- **Version:** 2.0
- **Date:** 2025-10-29
- **Framework:** Spring Boot + Thymeleaf
- **CSS:** Custom Modern Design
- **Icons:** Font Awesome 6.4.0
