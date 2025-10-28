-- =============================================
-- Script tạo database và dữ liệu demo cho ClothesShop (SQL Server)
-- =============================================

-- Tạo database
IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'ClothesShop')
BEGIN
    CREATE DATABASE ClothesShop;
END
GO

USE ClothesShop;
GO

-- =============================================
-- Tạo các bảng
-- =============================================

-- Bảng roles
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='roles' AND xtype='U')
BEGIN
    CREATE TABLE roles (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        name NVARCHAR(50) NOT NULL UNIQUE
    );
END
GO

-- Bảng users
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='users' AND xtype='U')
BEGIN
    CREATE TABLE users (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        username NVARCHAR(50) NOT NULL UNIQUE,
        password NVARCHAR(255) NOT NULL,
        email NVARCHAR(100) NOT NULL UNIQUE,
        first_name NVARCHAR(50) NOT NULL,
        last_name NVARCHAR(50) NOT NULL,
        phone NVARCHAR(20) NOT NULL,
        enabled BIT DEFAULT 1,
        verification_code NVARCHAR(255),
        verification_code_expiry DATETIME2,
        email_verified BIT DEFAULT 0,
        created_at DATETIME2 DEFAULT GETDATE(),
        updated_at DATETIME2 DEFAULT GETDATE()
    );
END
GO

-- Bảng user_roles
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='user_roles' AND xtype='U')
BEGIN
    CREATE TABLE user_roles (
        user_id BIGINT NOT NULL,
        role_id BIGINT NOT NULL,
        PRIMARY KEY (user_id, role_id),
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
        FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
    );
END
GO

-- Bảng categories
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='categories' AND xtype='U')
BEGIN
    CREATE TABLE categories (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        name NVARCHAR(100) NOT NULL UNIQUE,
        description NVARCHAR(MAX),
        is_active BIT DEFAULT 1,
        created_at DATETIME2 DEFAULT GETDATE(),
        updated_at DATETIME2 DEFAULT GETDATE()
    );
END
GO

-- Bảng products
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='products' AND xtype='U')
BEGIN
    CREATE TABLE products (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        name NVARCHAR(200) NOT NULL,
        description NVARCHAR(MAX),
        price DECIMAL(15,2) NOT NULL,
        stock_quantity INT NOT NULL DEFAULT 0,
        image_url NVARCHAR(500),
        category_id BIGINT,
        seller_id BIGINT,
        is_active BIT DEFAULT 1,
        created_at DATETIME2 DEFAULT GETDATE(),
        updated_at DATETIME2 DEFAULT GETDATE(),
        FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,
        FOREIGN KEY (seller_id) REFERENCES users(id) ON DELETE SET NULL
    );
END
GO

-- Bảng orders
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='orders' AND xtype='U')
BEGIN
    CREATE TABLE orders (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        order_code NVARCHAR(50) NOT NULL UNIQUE,
        user_id BIGINT NOT NULL,
        shipping_address NVARCHAR(MAX) NOT NULL,
        shipping_phone NVARCHAR(20) NOT NULL,
        shipping_name NVARCHAR(100) NOT NULL,
        status NVARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'CONFIRMED', 'SHIPPING', 'DELIVERED', 'FAILED', 'CANCELLED')),
        payment_method NVARCHAR(20) NOT NULL CHECK (payment_method IN ('COD', 'BANK_TRANSFER', 'CREDIT_CARD')),
        total_amount DECIMAL(15,2) NOT NULL,
        cod_amount DECIMAL(15,2),
        shipper_id BIGINT,
        assigned_at DATETIME2,
        delivered_at DATETIME2,
        delivery_notes NVARCHAR(MAX),
        failure_reason NVARCHAR(MAX),
        created_at DATETIME2 DEFAULT GETDATE(),
        updated_at DATETIME2 DEFAULT GETDATE(),
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
        FOREIGN KEY (shipper_id) REFERENCES users(id) ON DELETE SET NULL
    );
END
GO

-- Bảng order_details
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='order_details' AND xtype='U')
BEGIN
    CREATE TABLE order_details (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        order_id BIGINT NOT NULL,
        product_id BIGINT NOT NULL,
        quantity INT NOT NULL,
        unit_price DECIMAL(15,2) NOT NULL,
        total_price DECIMAL(15,2) NOT NULL,
        FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
        FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
    );
END
GO

-- =============================================
-- Thêm dữ liệu demo
-- =============================================

-- Thêm roles
IF NOT EXISTS (SELECT * FROM roles WHERE name = 'ROLE_USER')
BEGIN
    INSERT INTO roles (name) VALUES 
    ('ROLE_USER'),
    ('ROLE_ADMIN'),
    ('ROLE_SELLER'),
    ('ROLE_SHIPPER');
END
GO

-- Thêm categories
IF NOT EXISTS (SELECT * FROM categories WHERE name = 'Áo thun')
BEGIN
    INSERT INTO categories (name, description) VALUES 
    (N'Áo thun', N'Áo thun nam nữ các loại'),
    (N'Quần jean', N'Quần jean nam nữ'),
    (N'Áo sơ mi', N'Áo sơ mi công sở'),
    (N'Váy', N'Váy nữ các kiểu'),
    (N'Giày dép', N'Giày dép nam nữ'),
    (N'Phụ kiện', N'Túi xách, ví, thắt lưng');
END
GO

-- Thêm users (password đã được hash bằng BCrypt)
-- Password mặc định cho tất cả user: 123456
IF NOT EXISTS (SELECT * FROM users WHERE username = 'admin')
BEGIN
    INSERT INTO users (username, password, email, first_name, last_name, phone, enabled, email_verified) VALUES 
    -- Admin
    ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'admin@clothesshop.com', N'Admin', N'System', '0123456789', 1, 1),

    -- Seller
    ('seller1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'seller1@clothesshop.com', N'Nguyễn', N'Bán Hàng', '0123456788', 1, 1),

    -- Shipper
    ('shipper1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'shipper1@clothesshop.com', N'Nguyễn', N'Văn Shipper', '090xxxx123', 1, 1),
    ('shipper2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'shipper2@clothesshop.com', N'Trần', N'Thị Giao Hàng', '091xxxx456', 1, 1),

    -- Customers
    ('customer1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'customer1@email.com', N'Nguyễn', N'Văn A', '090xxxx123', 1, 1),
    ('customer2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'customer2@email.com', N'Trần', N'Thị B', '091xxxx456', 1, 1),
    ('customer3', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'customer3@email.com', N'Lê', N'Văn C', '098xxxx789', 1, 1);
END
GO

-- Thêm user roles
IF NOT EXISTS (SELECT * FROM user_roles WHERE user_id = 1)
BEGIN
    INSERT INTO user_roles (user_id, role_id) VALUES 
    (1, 2), -- admin có role ADMIN
    (2, 3), -- seller1 có role SELLER
    (3, 4), -- shipper1 có role SHIPPER
    (4, 4), -- shipper2 có role SHIPPER
    (5, 1), -- customer1 có role USER
    (6, 1), -- customer2 có role USER
    (7, 1); -- customer3 có role USER
END
GO

-- Thêm products
IF NOT EXISTS (SELECT * FROM products WHERE name = N'Áo thun nam trắng')
BEGIN
    INSERT INTO products (name, description, price, stock_quantity, category_id, seller_id) VALUES 
    (N'Áo thun nam trắng', N'Áo thun nam chất liệu cotton 100%, màu trắng', 150000, 50, 1, 2),
    (N'Áo thun nữ hồng', N'Áo thun nữ chất liệu cotton, màu hồng', 120000, 30, 1, 2),
    (N'Quần jean nam xanh', N'Quần jean nam slim fit, màu xanh', 350000, 25, 2, 2),
    (N'Quần jean nữ đen', N'Quần jean nữ skinny, màu đen', 320000, 20, 2, 2),
    (N'Áo sơ mi nam trắng', N'Áo sơ mi nam công sở, chất liệu cotton', 250000, 40, 3, 2),
    (N'Váy dài nữ đỏ', N'Váy dài nữ dự tiệc, màu đỏ', 450000, 15, 4, 2),
    (N'Giày sneaker nam', N'Giày sneaker nam thể thao', 800000, 20, 5, 2),
    (N'Túi xách nữ', N'Túi xách nữ da thật', 600000, 10, 6, 2);
END
GO

-- Thêm orders
IF NOT EXISTS (SELECT * FROM orders WHERE order_code = 'DH3001')
BEGIN
    INSERT INTO orders (order_code, user_id, shipping_address, shipping_phone, shipping_name, status, payment_method, total_amount, cod_amount, shipper_id, assigned_at) VALUES 
    ('DH3001', 5, N'123 Đường ABC, Phường XYZ, Quận 1, TP. HCM', '090xxxx123', N'Nguyễn Văn A', 'CONFIRMED', 'COD', 350000, 350000, NULL, NULL),
    ('DH3002', 6, N'456 Đường DEF, Phường UVW, Quận 7, TP. HCM', '091xxxx456', N'Trần Thị B', 'CONFIRMED', 'BANK_TRANSFER', 320000, NULL, NULL, NULL),
    ('DH3003', 7, N'789 Đường GHI, Phường RST, Quận Tân Bình, TP. HCM', '098xxxx789', N'Lê Văn C', 'CONFIRMED', 'COD', 850000, 850000, NULL, NULL),
    ('DH3004', 5, N'111 Đường KLM, Phường NOP, Quận 3, TP. HCM', '090xxxx123', N'Nguyễn Văn A', 'SHIPPING', 'COD', 250000, 250000, 3, '2025-01-26 10:00:00'),
    ('DH3005', 6, N'222 Đường XYZ, Phường QRS, Quận Bình Thạnh, TP. HCM', '091xxxx456', N'Trần Thị B', 'SHIPPING', 'BANK_TRANSFER', 600000, NULL, 4, '2025-01-26 11:00:00'),
    ('DH3006', 7, N'333 Đường ABC, Phường DEF, Quận 10, TP. HCM', '098xxxx789', N'Lê Văn C', 'DELIVERED', 'COD', 450000, 450000, 3, '2025-01-25 09:00:00'),
    ('DH3007', 5, N'444 Đường MNO, Phường PQR, Quận 2, TP. HCM', '090xxxx123', N'Nguyễn Văn A', 'DELIVERED', 'BANK_TRANSFER', 800000, NULL, 4, '2025-01-25 14:00:00'),
    ('DH3008', 6, N'555 Đường STU, Phường VWX, Quận 5, TP. HCM', '091xxxx456', N'Trần Thị B', 'FAILED', 'COD', 320000, 320000, 3, '2025-01-24 15:00:00');
END
GO

-- Cập nhật thời gian giao hàng cho các đơn đã giao
UPDATE orders SET delivered_at = '2025-01-25 16:00:00' WHERE order_code = 'DH3006';
UPDATE orders SET delivered_at = '2025-01-25 18:00:00' WHERE order_code = 'DH3007';
UPDATE orders SET failure_reason = N'Khách hẹn lại' WHERE order_code = 'DH3008';
GO

-- Thêm order_details
IF NOT EXISTS (SELECT * FROM order_details WHERE order_id = 1)
BEGIN
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
END
GO

-- =============================================
-- Tạo indexes để tối ưu hiệu suất
-- =============================================

-- Indexes cho bảng orders
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'idx_orders_status')
    CREATE INDEX idx_orders_status ON orders(status);

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'idx_orders_shipper_id')
    CREATE INDEX idx_orders_shipper_id ON orders(shipper_id);

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'idx_orders_user_id')
    CREATE INDEX idx_orders_user_id ON orders(user_id);

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'idx_orders_order_code')
    CREATE INDEX idx_orders_order_code ON orders(order_code);

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'idx_orders_created_at')
    CREATE INDEX idx_orders_created_at ON orders(created_at);

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'idx_orders_delivered_at')
    CREATE INDEX idx_orders_delivered_at ON orders(delivered_at);

-- Indexes cho bảng products
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'idx_products_category_id')
    CREATE INDEX idx_products_category_id ON products(category_id);

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'idx_products_seller_id')
    CREATE INDEX idx_products_seller_id ON products(seller_id);

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'idx_products_is_active')
    CREATE INDEX idx_products_is_active ON products(is_active);

-- Indexes cho bảng order_details
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'idx_order_details_order_id')
    CREATE INDEX idx_order_details_order_id ON order_details(order_id);

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'idx_order_details_product_id')
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
    COUNT(CASE WHEN o.status = 'DELIVERED' AND o.delivered_at >= DATEADD(MONTH, -1, GETDATE()) THEN 1 END) as delivered_this_month,
    COUNT(CASE WHEN o.status = 'FAILED' AND o.delivered_at >= DATEADD(MONTH, -1, GETDATE()) THEN 1 END) as failed_this_month,
    ISNULL(SUM(CASE WHEN o.status = 'DELIVERED' AND o.delivered_at >= DATEADD(MONTH, -1, GETDATE()) THEN o.cod_amount END), 0) as estimated_income
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
HƯỚNG DẪN SỬ DỤNG CHO SQL SERVER:

1. Chạy script này trong SQL Server Management Studio (SSMS)
2. Hoặc sử dụng sqlcmd:
   sqlcmd -S localhost -U sa -P 123 -i database_setup.sql

3. Cấu hình application.properties:
   spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=ClothesShop;encrypt=false;trustServerCertificate=true
   spring.datasource.username=sa
   spring.datasource.password=123

4. Tài khoản demo:
   - Admin: admin / 123456
   - Seller: seller1 / 123456
   - Shipper: shipper1 / 123456, shipper2 / 123456
   - Customer: customer1 / 123456, customer2 / 123456, customer3 / 123456

5. Dữ liệu demo bao gồm:
   - 8 sản phẩm thuộc 6 danh mục
   - 8 đơn hàng với các trạng thái khác nhau
   - 2 shipper để test chức năng giao hàng

6. Để test API shipper:
   - Đăng nhập với tài khoản shipper1 hoặc shipper2
   - Truy cập /shipper/dashboard để xem thống kê
   - Truy cập /shipper/shipped để xem đơn hàng chờ giao
   - Sử dụng các API endpoint để nhận và cập nhật đơn hàng
*/

