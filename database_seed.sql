-- Seed data for SQL Server (T-SQL)
-- Inserts roles, many users, ~100 products, images, vouchers, favorites, carts, and reviews
-- Run this script against the ClothesShop database (SQL Server)

SET NOCOUNT ON;

BEGIN TRANSACTION;

-- 1) Roles
IF NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ROLE_USER')
    INSERT INTO roles (name) VALUES ('ROLE_USER');
IF NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ROLE_ADMIN')
    INSERT INTO roles (name) VALUES ('ROLE_ADMIN');
IF NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ROLE_SELLER')
    INSERT INTO roles (name) VALUES ('ROLE_SELLER');
IF NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ROLE_SHIPPER')
    INSERT INTO roles (name) VALUES ('ROLE_SHIPPER');

-- 2) Create base accounts (admin, one seller, some shippers)
-- Reuse a BCrypt hashed password string used in demo (password: 123456)
DECLARE @hashPwd NVARCHAR(255) = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi';

IF NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin')
BEGIN
    INSERT INTO users (username, password, email, first_name, last_name, phone, enabled, email_verified, created_at, updated_at)
    VALUES ('admin', @hashPwd, 'admin@clothesshop.com', 'Admin', 'System', '0900000000', 1, 1, GETDATE(), GETDATE());
END

IF NOT EXISTS (SELECT 1 FROM users WHERE username = 'seller_seed')
BEGIN
    INSERT INTO users (username, password, email, first_name, last_name, phone, enabled, email_verified, created_at, updated_at)
    VALUES ('seller_seed', @hashPwd, 'seller_seed@clothesshop.com', 'Nguyen', 'Seller', '0901111222', 1, 1, GETDATE(), GETDATE());
END

IF NOT EXISTS (SELECT 1 FROM users WHERE username = 'shipper1')
    INSERT INTO users (username, password, email, first_name, last_name, phone, enabled, email_verified, created_at, updated_at)
    VALUES ('shipper1', @hashPwd, 'shipper1@clothesshop.com', 'Tran', 'Shipper', '0902222333', 1, 1, GETDATE(), GETDATE());
IF NOT EXISTS (SELECT 1 FROM users WHERE username = 'shipper2')
    INSERT INTO users (username, password, email, first_name, last_name, phone, enabled, email_verified, created_at, updated_at)
    VALUES ('shipper2', @hashPwd, 'shipper2@clothesshop.com', 'Le', 'Shipper', '0903333444', 1, 1, GETDATE(), GETDATE());

-- 3) Create many customer users (e.g., 60)
DECLARE @i INT = 1;
WHILE @i <= 60
BEGIN
    DECLARE @uname NVARCHAR(100) = CONCAT('customer_seed', @i);
    IF NOT EXISTS (SELECT 1 FROM users WHERE username = @uname)
    BEGIN
        INSERT INTO users (username, password, email, first_name, last_name, phone, enabled, email_verified, created_at, updated_at)
        VALUES (@uname, @hashPwd, LOWER(@uname) + '@example.com', 'KH' + RIGHT('000' + CAST(@i AS VARCHAR(3)),3), 'Seed', '09' + RIGHT('000000' + CAST(1000 + @i AS VARCHAR(6)),6), 1, 1, GETDATE(), GETDATE());
    END
    SET @i = @i + 1;
END

-- 4) Assign roles (admin, seller, shippers, customers)
DECLARE @adminRoleId BIGINT = (SELECT id FROM roles WHERE name = 'ROLE_ADMIN');
DECLARE @sellerRoleId BIGINT = (SELECT id FROM roles WHERE name = 'ROLE_SELLER');
DECLARE @shipperRoleId BIGINT = (SELECT id FROM roles WHERE name = 'ROLE_SHIPPER');
DECLARE @userRoleId BIGINT = (SELECT id FROM roles WHERE name = 'ROLE_USER');

DECLARE @adminId BIGINT = (SELECT id FROM users WHERE username = 'admin');
DECLARE @sellerId BIGINT = (SELECT id FROM users WHERE username = 'seller_seed');
DECLARE @shipper1Id BIGINT = (SELECT id FROM users WHERE username = 'shipper1');
DECLARE @shipper2Id BIGINT = (SELECT id FROM users WHERE username = 'shipper2');

-- helper: insert user_roles if not exists
IF NOT EXISTS (SELECT 1 FROM user_roles WHERE user_id = @adminId AND role_id = @adminRoleId)
    INSERT INTO user_roles (user_id, role_id) VALUES (@adminId, @adminRoleId);
IF NOT EXISTS (SELECT 1 FROM user_roles WHERE user_id = @sellerId AND role_id = @sellerRoleId)
    INSERT INTO user_roles (user_id, role_id) VALUES (@sellerId, @sellerRoleId);
IF @shipper1Id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM user_roles WHERE user_id = @shipper1Id AND role_id = @shipperRoleId)
    INSERT INTO user_roles (user_id, role_id) VALUES (@shipper1Id, @shipperRoleId);
IF @shipper2Id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM user_roles WHERE user_id = @shipper2Id AND role_id = @shipperRoleId)
    INSERT INTO user_roles (user_id, role_id) VALUES (@shipper2Id, @shipperRoleId);

-- Assign many customers to ROLE_USER
DECLARE @custId BIGINT;
DECLARE curCust CURSOR FOR SELECT id FROM users WHERE username LIKE 'customer_seed%';
OPEN curCust;
FETCH NEXT FROM curCust INTO @custId;
WHILE @@FETCH_STATUS = 0
BEGIN
    IF NOT EXISTS (SELECT 1 FROM user_roles WHERE user_id = @custId AND role_id = @userRoleId)
        INSERT INTO user_roles (user_id, role_id) VALUES (@custId, @userRoleId);
    FETCH NEXT FROM curCust INTO @custId;
END
CLOSE curCust; DEALLOCATE curCust;

-- 5) Insert categories (if table exists)
IF OBJECT_ID('categories') IS NOT NULL
BEGIN
    IF NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Áo thun')
        INSERT INTO categories (name, description, created_at, updated_at) VALUES ('Áo thun', 'Áo thun nam nữ các loại', GETDATE(), GETDATE());
    IF NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Quần jean')
        INSERT INTO categories (name, description, created_at, updated_at) VALUES ('Quần jean', 'Quần jean nam nữ', GETDATE(), GETDATE());
    IF NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Áo sơ mi')
        INSERT INTO categories (name, description, created_at, updated_at) VALUES ('Áo sơ mi', 'Áo sơ mi công sở', GETDATE(), GETDATE());
    IF NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Váy')
        INSERT INTO categories (name, description, created_at, updated_at) VALUES ('Váy', 'Váy nữ', GETDATE(), GETDATE());
    IF NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Giày dép')
        INSERT INTO categories (name, description, created_at, updated_at) VALUES ('Giày dép', 'Giày dép', GETDATE(), GETDATE());
    IF NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Phụ kiện')
        INSERT INTO categories (name, description, created_at, updated_at) VALUES ('Phụ kiện', 'Túi xách, ví, thắt lưng', GETDATE(), GETDATE());
END

-- 6) Insert ~100 products assigned to seller_seed
DECLARE @prodCount INT = 1;
WHILE @prodCount <= 100
BEGIN
    DECLARE @pname NVARCHAR(255) = CONCAT('Sản phẩm mẫu ', @prodCount);
    DECLARE @desc NVARCHAR(MAX) = CONCAT('Mô tả sản phẩm mẫu số ', @prodCount, '. Chất liệu tốt, thiết kế đẹp.');
    DECLARE @price DECIMAL(15,2) = CAST( (50000 + (@prodCount * 500)) AS DECIMAL(15,2));
    DECLARE @stock INT = 10 + (@prodCount % 50);
    DECLARE @categoryId BIGINT = (SELECT TOP 1 id FROM categories ORDER BY NEWID());
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE name = @pname)
    BEGIN
        INSERT INTO products (name, description, price, stock_quantity, category_id, seller_id, is_active, created_at, updated_at)
        VALUES (@pname, @desc, @price, @stock, @categoryId, @sellerId, 1, GETDATE(), GETDATE());
    END
    SET @prodCount = @prodCount + 1;
END

-- 7) Insert 3 images per product
DECLARE @pid BIGINT;
DECLARE prod_cursor CURSOR FOR SELECT id FROM products WHERE seller_id = @sellerId ORDER BY id;
OPEN prod_cursor;
FETCH NEXT FROM prod_cursor INTO @pid;
WHILE @@FETCH_STATUS = 0
BEGIN
    IF OBJECT_ID('product_images') IS NOT NULL
    BEGIN
        IF NOT EXISTS (SELECT 1 FROM product_images WHERE product_id = @pid)
        BEGIN
            INSERT INTO product_images (product_id, url, position, is_primary, created_at, updated_at)
            VALUES (@pid, CONCAT('/images/products/', @pid, '_1.jpg'), 1, 1, GETDATE(), GETDATE());
            INSERT INTO product_images (product_id, url, position, is_primary, created_at, updated_at)
            VALUES (@pid, CONCAT('/images/products/', @pid, '_2.jpg'), 2, 0, GETDATE(), GETDATE());
            INSERT INTO product_images (product_id, url, position, is_primary, created_at, updated_at)
            VALUES (@pid, CONCAT('/images/products/', @pid, '_3.jpg'), 3, 0, GETDATE(), GETDATE());
        END
    END
    FETCH NEXT FROM prod_cursor INTO @pid;
END
CLOSE prod_cursor; DEALLOCATE prod_cursor;

-- 8) Create a voucher (admin created)
DECLARE @adminUserId BIGINT = @adminId;
IF OBJECT_ID('vouchers') IS NOT NULL
BEGIN
    IF NOT EXISTS (SELECT 1 FROM vouchers WHERE code = 'PROMO10')
    BEGIN
        INSERT INTO vouchers (code, discountPercent, maxDiscount, expiryDate, active, type, created_by, created_at, updated_at)
        VALUES ('PROMO10', 10, 50000, DATEADD(month, 3, GETDATE()), 1, 'ADMIN', @adminUserId, GETDATE(), GETDATE());
    END
END

-- 9) Add favorites randomly for some customers
DECLARE @custCursorId BIGINT;
DECLARE cust_cursor CURSOR FOR SELECT id FROM users WHERE username LIKE 'customer_seed%';
OPEN cust_cursor; FETCH NEXT FROM cust_cursor INTO @custCursorId;
WHILE @@FETCH_STATUS = 0
BEGIN
    DECLARE @randomProductId BIGINT = (SELECT TOP 1 id FROM products ORDER BY NEWID());
    IF OBJECT_ID('favorites') IS NOT NULL
    BEGIN
        IF NOT EXISTS (SELECT 1 FROM favorites WHERE user_id = @custCursorId AND product_id = @randomProductId)
            INSERT INTO favorites (user_id, product_id, created_at, updated_at) VALUES (@custCursorId, @randomProductId, GETDATE(), GETDATE());
    END
    FETCH NEXT FROM cust_cursor INTO @custCursorId;
END
CLOSE cust_cursor; DEALLOCATE cust_cursor;

-- 10) Insert ~20 reviews for a chosen product (the first product of seller)
DECLARE @targetProductId BIGINT = (SELECT TOP 1 id FROM products WHERE seller_id = @sellerId ORDER BY id);
IF @targetProductId IS NOT NULL AND OBJECT_ID('review') IS NOT NULL
BEGIN
    -- If the actual table name is 'review' or 'reviews', try both
END

-- Try both table names for compatibility
IF @targetProductId IS NOT NULL
BEGIN
    IF OBJECT_ID('reviews') IS NOT NULL
    BEGIN
        DECLARE @r INT = 1;
        WHILE @r <= 20
        BEGIN
            DECLARE @randUserId BIGINT = (SELECT TOP 1 id FROM users WHERE username LIKE 'customer_seed%' ORDER BY NEWID());
            INSERT INTO reviews (rating, comment, user_id, product_id, created_at, updated_at)
            VALUES (CAST((FLOOR(RAND(CHECKSUM(NEWID())) * 2) + 3) AS INT), CONCAT('Đánh giá mẫu số ', @r, ' cho sản phẩm ', @targetProductId), @randUserId, @targetProductId, GETDATE(), GETDATE());
            SET @r = @r + 1;
        END
    END
    ELSE IF OBJECT_ID('review') IS NOT NULL
    BEGIN
        DECLARE @r2 INT = 1;
        WHILE @r2 <= 20
        BEGIN
            DECLARE @randUserId2 BIGINT = (SELECT TOP 1 id FROM users WHERE username LIKE 'customer_seed%' ORDER BY NEWID());
            INSERT INTO review (rating, comment, user_id, product_id, created_at, updated_at)
            VALUES (CAST((FLOOR(RAND(CHECKSUM(NEWID())) * 2) + 3) AS INT), CONCAT('Đánh giá mẫu số ', @r2, ' cho sản phẩm ', @targetProductId), @randUserId2, @targetProductId, GETDATE(), GETDATE());
            SET @r2 = @r2 + 1;
        END
    END
END

-- 11) Create carts for some customers and add items
DECLARE @exampleCustId BIGINT = (SELECT TOP 1 id FROM users WHERE username LIKE 'customer_seed%');
IF @exampleCustId IS NOT NULL AND OBJECT_ID('carts') IS NOT NULL
BEGIN
    IF NOT EXISTS (SELECT 1 FROM carts WHERE user_id = @exampleCustId)
    BEGIN
        INSERT INTO carts (user_id, created_at, updated_at) VALUES (@exampleCustId, GETDATE(), GETDATE());
        DECLARE @cartId BIGINT = SCOPE_IDENTITY();
        DECLARE @sampleProductId BIGINT = (SELECT TOP 1 id FROM products ORDER BY NEWID());
        IF OBJECT_ID('cart_items') IS NOT NULL
        BEGIN
            INSERT INTO cart_items (cart_id, product_id, quantity, price_at_add_time, created_at, updated_at)
            VALUES (@cartId, @sampleProductId, 2, (SELECT price FROM products WHERE id = @sampleProductId), GETDATE(), GETDATE());
        END
    END
END

COMMIT TRANSACTION;

PRINT 'Seed script finished.';
GO
