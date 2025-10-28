-- =============================================
-- Script cập nhật password hash cho tất cả user (Alternative)
-- Password: 123456
-- BCrypt Hash: $2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi
-- =============================================

USE ClothesShop;
GO

-- Xóa tất cả user cũ và tạo lại với password hash mới
DELETE FROM user_roles;
DELETE FROM order_details;
DELETE FROM orders;
DELETE FROM products;
DELETE FROM categories;
DELETE FROM users;
DELETE FROM roles;

-- Thêm roles
INSERT INTO roles (name) VALUES 
('ROLE_USER'),
('ROLE_ADMIN'),
('ROLE_SELLER'),
('ROLE_SHIPPER');

-- Thêm users với password hash mới cho "123456"
INSERT INTO users (username, password, email, first_name, last_name, phone, enabled, email_verified) VALUES 
-- Admin
('admin', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'admin@clothesshop.com', N'Admin', N'System', '0123456789', 1, 1),

-- Seller
('seller1', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'seller1@clothesshop.com', N'Nguyễn', N'Bán Hàng', '0123456788', 1, 1),

-- Shipper
('shipper1', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'shipper1@clothesshop.com', N'Nguyễn', N'Văn Shipper', '090xxxx123', 1, 1),
('shipper2', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'shipper2@clothesshop.com', N'Trần', N'Thị Giao Hàng', '091xxxx456', 1, 1),

-- Customers
('customer1', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'customer1@email.com', N'Nguyễn', N'Văn A', '090xxxx123', 1, 1),
('customer2', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'customer2@email.com', N'Trần', N'Thị B', '091xxxx456', 1, 1),
('customer3', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'customer3@email.com', N'Lê', N'Văn C', '098xxxx789', 1, 1);

-- Thêm user roles
INSERT INTO user_roles (user_id, role_id) VALUES 
(1, 2), -- admin có role ADMIN
(2, 3), -- seller1 có role SELLER
(3, 4), -- shipper1 có role SHIPPER
(4, 4), -- shipper2 có role SHIPPER
(5, 1), -- customer1 có role USER
(6, 1), -- customer2 có role USER
(7, 1); -- customer3 có role USER

-- Thêm categories
INSERT INTO categories (name, description) VALUES 
(N'Áo thun', N'Áo thun nam nữ các loại'),
(N'Quần jean', N'Quần jean nam nữ'),
(N'Áo sơ mi', N'Áo sơ mi công sở'),
(N'Váy', N'Váy nữ các kiểu'),
(N'Giày dép', N'Giày dép nam nữ'),
(N'Phụ kiện', N'Túi xách, ví, thắt lưng');

-- Thêm products
INSERT INTO products (name, description, price, stock_quantity, category_id, seller_id) VALUES 
(N'Áo thun nam trắng', N'Áo thun nam chất liệu cotton 100%, màu trắng', 150000, 50, 1, 2),
(N'Áo thun nữ hồng', N'Áo thun nữ chất liệu cotton, màu hồng', 120000, 30, 1, 2),
(N'Quần jean nam xanh', N'Quần jean nam slim fit, màu xanh', 350000, 25, 2, 2),
(N'Quần jean nữ đen', N'Quần jean nữ skinny, màu đen', 320000, 20, 2, 2),
(N'Áo sơ mi nam trắng', N'Áo sơ mi nam công sở, chất liệu cotton', 250000, 40, 3, 2),
(N'Váy dài nữ đỏ', N'Váy dài nữ dự tiệc, màu đỏ', 450000, 15, 4, 2),
(N'Giày sneaker nam', N'Giày sneaker nam thể thao', 800000, 20, 5, 2),
(N'Túi xách nữ', N'Túi xách nữ da thật', 600000, 10, 6, 2);

-- Thêm orders
INSERT INTO orders (order_code, user_id, shipping_address, shipping_phone, shipping_name, status, payment_method, total_amount, cod_amount, shipper_id, assigned_at) VALUES 
('DH3001', 5, N'123 Đường ABC, Phường XYZ, Quận 1, TP. HCM', '090xxxx123', N'Nguyễn Văn A', 'CONFIRMED', 'COD', 350000, 350000, NULL, NULL),
('DH3002', 6, N'456 Đường DEF, Phường UVW, Quận 7, TP. HCM', '091xxxx456', N'Trần Thị B', 'CONFIRMED', 'BANK_TRANSFER', 320000, NULL, NULL, NULL),
('DH3003', 7, N'789 Đường GHI, Phường RST, Quận Tân Bình, TP. HCM', '098xxxx789', N'Lê Văn C', 'CONFIRMED', 'COD', 850000, 850000, NULL, NULL),
('DH3004', 5, N'111 Đường KLM, Phường NOP, Quận 3, TP. HCM', '090xxxx123', N'Nguyễn Văn A', 'SHIPPING', 'COD', 250000, 250000, 3, '2025-01-26 10:00:00'),
('DH3005', 6, N'222 Đường XYZ, Phường QRS, Quận Bình Thạnh, TP. HCM', '091xxxx456', N'Trần Thị B', 'SHIPPING', 'BANK_TRANSFER', 600000, NULL, 4, '2025-01-26 11:00:00'),
('DH3006', 7, N'333 Đường ABC, Phường DEF, Quận 10, TP. HCM', '098xxxx789', N'Lê Văn C', 'DELIVERED', 'COD', 450000, 450000, 3, '2025-01-25 09:00:00'),
('DH3007', 5, N'444 Đường MNO, Phường PQR, Quận 2, TP. HCM', '090xxxx123', N'Nguyễn Văn A', 'DELIVERED', 'BANK_TRANSFER', 800000, NULL, 4, '2025-01-25 14:00:00'),
('DH3008', 6, N'555 Đường STU, Phường VWX, Quận 5, TP. HCM', '091xxxx456', N'Trần Thị B', 'FAILED', 'COD', 320000, 320000, 3, '2025-01-24 15:00:00');

-- Cập nhật thời gian giao hàng cho các đơn đã giao
UPDATE orders SET delivered_at = '2025-01-25 16:00:00' WHERE order_code = 'DH3006';
UPDATE orders SET delivered_at = '2025-01-25 18:00:00' WHERE order_code = 'DH3007';
UPDATE orders SET failure_reason = N'Khách hẹn lại' WHERE order_code = 'DH3008';

-- Thêm order_details
INSERT INTO order_details (order_id, product_id, quantity, unit_price, total_price) VALUES 
(1, 3, 1, 350000, 350000), -- DH3001: Quần jean nam xanh
(2, 4, 1, 320000, 320000), -- DH3002: Quần jean nữ đen
(3, 7, 1, 800000, 800000), -- DH3003: Giày sneaker nam
(3, 8, 1, 600000, 600000), -- DH3003: Túi xách nữ (tổng 1.4M nhưng chỉ thu hộ 850k)
(4, 5, 1, 250000, 250000), -- DH3004: Áo sơ mi nam trắng
(5, 8, 1, 600000, 600000), -- DH3005: Túi xách nữ
(6, 6, 1, 450000, 450000), -- DH3006: Váy dài nữ đỏ
(7, 7, 1, 800000, 800000), -- DH3007: Giày sneaker nam
(8, 4, 1, 320000, 320000); -- DH3008: Quần jean nữ đen

-- Kiểm tra kết quả
SELECT username, password FROM users;

