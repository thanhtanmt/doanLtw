-- =============================================
-- Script tạo database và dữ liệu demo cho ClothesShop
-- =============================================

-- Tạo database
CREATE DATABASE IF NOT EXISTS clothesshop CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE clothesshop;

-- =============================================
-- Tạo các bảng
-- =============================================

-- Bảng roles
CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- Bảng users
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    enabled BOOLEAN DEFAULT TRUE,
    verification_code VARCHAR(255),
    verification_code_expiry DATETIME,
    email_verified BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Bảng user_roles
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- Bảng categories
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Bảng products
CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    price DECIMAL(15,2) NOT NULL,
    stock_quantity INT NOT NULL DEFAULT 0,
    image_url VARCHAR(500),
    category_id BIGINT,
    seller_id BIGINT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,
    FOREIGN KEY (seller_id) REFERENCES users(id) ON DELETE SET NULL
);

-- Bảng orders
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_code VARCHAR(50) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    shipping_address TEXT NOT NULL,
    shipping_phone VARCHAR(20) NOT NULL,
    shipping_name VARCHAR(100) NOT NULL,
    status ENUM('PENDING', 'CONFIRMED', 'SHIPPING', 'DELIVERED', 'FAILED', 'CANCELLED') DEFAULT 'PENDING',
    payment_method ENUM('COD', 'BANK_TRANSFER', 'CREDIT_CARD') NOT NULL,
    total_amount DECIMAL(15,2) NOT NULL,
    cod_amount DECIMAL(15,2),
    shipper_id BIGINT,
    assigned_at DATETIME,
    delivered_at DATETIME,
    delivery_notes TEXT,
    failure_reason TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (shipper_id) REFERENCES users(id) ON DELETE SET NULL
);

-- Bảng order_details
CREATE TABLE IF NOT EXISTS order_details (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(15,2) NOT NULL,
    total_price DECIMAL(15,2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- =============================================
-- Thêm dữ liệu demo
-- =============================================

-- Thêm roles
INSERT INTO roles (name) VALUES 
('ROLE_USER'),
('ROLE_ADMIN'),
('ROLE_SELLER'),
('ROLE_SHIPPER');

-- Thêm categories
INSERT INTO categories (name, description) VALUES 
('Áo thun', 'Áo thun nam nữ các loại'),
('Quần jean', 'Quần jean nam nữ'),
('Áo sơ mi', 'Áo sơ mi công sở'),
('Váy', 'Váy nữ các kiểu'),
('Giày dép', 'Giày dép nam nữ'),
('Phụ kiện', 'Túi xách, ví, thắt lưng');

-- Thêm users (password đã được hash bằng BCrypt)
-- Password mặc định cho tất cả user: 123456
INSERT INTO users (username, password, email, first_name, last_name, phone, enabled, email_verified) VALUES 
-- Admin
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'admin@clothesshop.com', 'Admin', 'System', '0123456789', TRUE, TRUE),

-- Seller
('seller1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'seller1@clothesshop.com', 'Nguyễn', 'Bán Hàng', '0123456788', TRUE, TRUE),

-- Shipper
('shipper1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'shipper1@clothesshop.com', 'Nguyễn', 'Văn Shipper', '090xxxx123', TRUE, TRUE),
('shipper2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'shipper2@clothesshop.com', 'Trần', 'Thị Giao Hàng', '091xxxx456', TRUE, TRUE),

-- Customers
('customer1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'customer1@email.com', 'Nguyễn', 'Văn A', '090xxxx123', TRUE, TRUE),
('customer2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'customer2@email.com', 'Trần', 'Thị B', '091xxxx456', TRUE, TRUE),
('customer3', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'customer3@email.com', 'Lê', 'Văn C', '098xxxx789', TRUE, TRUE);

-- Thêm user roles
INSERT INTO user_roles (user_id, role_id) VALUES 
(1, 2), -- admin có role ADMIN
(2, 3), -- seller1 có role SELLER
(3, 4), -- shipper1 có role SHIPPER
(4, 4), -- shipper2 có role SHIPPER
(5, 1), -- customer1 có role USER
(6, 1), -- customer2 có role USER
(7, 1); -- customer3 có role USER

-- Thêm products
INSERT INTO products (name, description, price, stock_quantity, category_id, seller_id) VALUES 
('Áo thun nam trắng', 'Áo thun nam chất liệu cotton 100%, màu trắng', 150000, 50, 1, 2),
('Áo thun nữ hồng', 'Áo thun nữ chất liệu cotton, màu hồng', 120000, 30, 1, 2),
('Quần jean nam xanh', 'Quần jean nam slim fit, màu xanh', 350000, 25, 2, 2),
('Quần jean nữ đen', 'Quần jean nữ skinny, màu đen', 320000, 20, 2, 2),
('Áo sơ mi nam trắng', 'Áo sơ mi nam công sở, chất liệu cotton', 250000, 40, 3, 2),
('Váy dài nữ đỏ', 'Váy dài nữ dự tiệc, màu đỏ', 450000, 15, 4, 2),
('Giày sneaker nam', 'Giày sneaker nam thể thao', 800000, 20, 5, 2),
('Túi xách nữ', 'Túi xách nữ da thật', 600000, 10, 6, 2);

-- Thêm orders
INSERT INTO orders (order_code, user_id, shipping_address, shipping_phone, shipping_name, status, payment_method, total_amount, cod_amount, shipper_id, assigned_at) VALUES 
('DH3001', 5, '123 Đường ABC, Phường XYZ, Quận 1, TP. HCM', '090xxxx123', 'Nguyễn Văn A', 'CONFIRMED', 'COD', 350000, 350000, NULL, NULL),
('DH3002', 6, '456 Đường DEF, Phường UVW, Quận 7, TP. HCM', '091xxxx456', 'Trần Thị B', 'CONFIRMED', 'BANK_TRANSFER', 320000, NULL, NULL, NULL),
('DH3003', 7, '789 Đường GHI, Phường RST, Quận Tân Bình, TP. HCM', '098xxxx789', 'Lê Văn C', 'CONFIRMED', 'COD', 850000, 850000, NULL, NULL),
('DH3004', 5, '111 Đường KLM, Phường NOP, Quận 3, TP. HCM', '090xxxx123', 'Nguyễn Văn A', 'SHIPPING', 'COD', 250000, 250000, 3, '2025-01-26 10:00:00'),
('DH3005', 6, '222 Đường XYZ, Phường QRS, Quận Bình Thạnh, TP. HCM', '091xxxx456', 'Trần Thị B', 'SHIPPING', 'BANK_TRANSFER', 600000, NULL, 4, '2025-01-26 11:00:00'),
('DH3006', 7, '333 Đường ABC, Phường DEF, Quận 10, TP. HCM', '098xxxx789', 'Lê Văn C', 'DELIVERED', 'COD', 450000, 450000, 3, '2025-01-25 09:00:00'),
('DH3007', 5, '444 Đường MNO, Phường PQR, Quận 2, TP. HCM', '090xxxx123', 'Nguyễn Văn A', 'DELIVERED', 'BANK_TRANSFER', 800000, NULL, 4, '2025-01-25 14:00:00'),
('DH3008', 6, '555 Đường STU, Phường VWX, Quận 5, TP. HCM', '091xxxx456', 'Trần Thị B', 'FAILED', 'COD', 320000, 320000, 3, '2025-01-24 15:00:00');

-- Cập nhật thời gian giao hàng cho các đơn đã giao
UPDATE orders SET delivered_at = '2025-01-25 16:00:00' WHERE order_code = 'DH3006';
UPDATE orders SET delivered_at = '2025-01-25 18:00:00' WHERE order_code = 'DH3007';
UPDATE orders SET failure_reason = 'Khách hẹn lại' WHERE order_code = 'DH3008';

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

-- =============================================
-- Tạo indexes để tối ưu hiệu suất
-- =============================================

CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_shipper_id ON orders(shipper_id);
CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_order_code ON orders(order_code);
CREATE INDEX idx_orders_created_at ON orders(created_at);
CREATE INDEX idx_orders_delivered_at ON orders(delivered_at);

CREATE INDEX idx_products_category_id ON products(category_id);
CREATE INDEX idx_products_seller_id ON products(seller_id);
CREATE INDEX idx_products_is_active ON products(is_active);

CREATE INDEX idx_order_details_order_id ON order_details(order_id);
CREATE INDEX idx_order_details_product_id ON order_details(product_id);

-- =============================================
-- Kiểm tra dữ liệu
-- =============================================

-- Xem tổng quan dữ liệu
SELECT 'Users' as table_name, COUNT(*) as count FROM users
UNION ALL
SELECT 'Roles', COUNT(*) FROM roles
UNION ALL
SELECT 'Categories', COUNT(*) FROM categories
UNION ALL
SELECT 'Products', COUNT(*) FROM products
UNION ALL
SELECT 'Orders', COUNT(*) FROM orders
UNION ALL
SELECT 'Order Details', COUNT(*) FROM order_details;

-- Xem danh sách shipper
SELECT u.id, u.username, u.first_name, u.last_name, u.email, u.phone
FROM users u 
JOIN user_roles ur ON u.id = ur.user_id 
JOIN roles r ON ur.role_id = r.id 
WHERE r.name = 'ROLE_SHIPPER';

-- Xem đơn hàng chờ giao
SELECT o.order_code, o.shipping_name, o.shipping_address, o.shipping_phone, 
       o.total_amount, o.cod_amount, o.payment_method, o.created_at
FROM orders o 
WHERE o.status = 'CONFIRMED' AND o.shipper_id IS NULL
ORDER BY o.created_at ASC;

-- Xem thống kê shipper
SELECT 
    CONCAT(s.first_name, ' ', s.last_name) as shipper_name,
    COUNT(CASE WHEN o.status = 'SHIPPING' THEN 1 END) as pending_orders,
    COUNT(CASE WHEN o.status = 'DELIVERED' AND o.delivered_at >= DATE_SUB(NOW(), INTERVAL 1 MONTH) THEN 1 END) as delivered_this_month,
    COUNT(CASE WHEN o.status = 'FAILED' AND o.delivered_at >= DATE_SUB(NOW(), INTERVAL 1 MONTH) THEN 1 END) as failed_this_month,
    COALESCE(SUM(CASE WHEN o.status = 'DELIVERED' AND o.delivered_at >= DATE_SUB(NOW(), INTERVAL 1 MONTH) THEN o.cod_amount END), 0) as estimated_income
FROM users s
JOIN user_roles ur ON s.id = ur.user_id
JOIN roles r ON ur.role_id = r.id
LEFT JOIN orders o ON s.id = o.shipper_id
WHERE r.name = 'ROLE_SHIPPER'
GROUP BY s.id, s.first_name, s.last_name;

-- =============================================
-- Hướng dẫn sử dụng
-- =============================================

/*
HƯỚNG DẪN SỬ DỤNG:

1. Chạy script này để tạo database và dữ liệu demo
2. Cập nhật application.properties với thông tin database:
   - spring.datasource.url=jdbc:mysql://localhost:3306/clothesshop
   - spring.datasource.username=root
   - spring.datasource.password=your_password

3. Tài khoản demo:
   - Admin: admin / 123456
   - Seller: seller1 / 123456
   - Shipper: shipper1 / 123456, shipper2 / 123456
   - Customer: customer1 / 123456, customer2 / 123456, customer3 / 123456

4. Dữ liệu demo bao gồm:
   - 8 sản phẩm thuộc 6 danh mục
   - 8 đơn hàng với các trạng thái khác nhau
   - 2 shipper để test chức năng giao hàng

5. Để test API shipper:
   - Đăng nhập với tài khoản shipper1 hoặc shipper2
   - Truy cập /shipper/dashboard để xem thống kê
   - Truy cập /shipper/shipped để xem đơn hàng chờ giao
   - Sử dụng các API endpoint để nhận và cập nhật đơn hàng
*/

