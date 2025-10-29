-- =============================================
-- SEED DATA MỚI VỚI PRODUCT VARIANTS
-- SQL Server (T-SQL)
-- =============================================

USE ClothesShop;
GO

SET NOCOUNT ON;
BEGIN TRANSACTION;

-- =============================================
-- 1. ROLES
-- =============================================
PRINT 'Inserting Roles...';

IF NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ROLE_USER')
    INSERT INTO roles (name) VALUES ('ROLE_USER');
IF NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ROLE_ADMIN')
    INSERT INTO roles (name) VALUES ('ROLE_ADMIN');
IF NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ROLE_SELLER')
    INSERT INTO roles (name) VALUES ('ROLE_SELLER');
IF NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ROLE_SHIPPER')
    INSERT INTO roles (name) VALUES ('ROLE_SHIPPER');

-- =============================================
-- 2. USERS
-- Password: 123456 (BCrypt hash)
-- =============================================
PRINT 'Inserting Users...';

DECLARE @hashedPwd NVARCHAR(255) = '$2a$10$ES1ol7mk0TeA1zMpIQ5nFe7WF6Vm97PeCYA4DvKAPUbu3S4tp5y0O';

-- Admin
IF NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin')
    INSERT INTO users (username, password, email, first_name, last_name, phone, enabled, email_verified, avatar_url, created_at, updated_at)
    VALUES ('admin', @hashedPwd, 'admin@clothesshop.vn', N'Quản Trị', N'Viên', '0901234567', 1, 1, 'https://i.pravatar.cc/150?img=1', GETDATE(), GETDATE());

-- Sellers
IF NOT EXISTS (SELECT 1 FROM users WHERE username = 'fashionstore')
    INSERT INTO users (username, password, email, first_name, last_name, phone, enabled, email_verified, avatar_url, created_at, updated_at)
    VALUES ('fashionstore', @hashedPwd, 'contact@fashionstore.vn', N'Fashion', 'Store', '0912345678', 1, 1, 'https://i.pravatar.cc/150?img=11', GETDATE(), GETDATE());

IF NOT EXISTS (SELECT 1 FROM users WHERE username = 'trendyshop')
    INSERT INTO users (username, password, email, first_name, last_name, phone, enabled, email_verified, avatar_url, created_at, updated_at)
    VALUES ('trendyshop', @hashedPwd, 'info@trendyshop.vn', 'Trendy', 'Shop', '0923456789', 1, 1, 'https://i.pravatar.cc/150?img=12', GETDATE(), GETDATE());

-- Shippers
IF NOT EXISTS (SELECT 1 FROM users WHERE username = 'shipper_anhtuan')
    INSERT INTO users (username, password, email, first_name, last_name, phone, enabled, email_verified, avatar_url, created_at, updated_at)
    VALUES ('shipper_anhtuan', @hashedPwd, 'anhtuan.ship@clothesshop.vn', N'Anh', N'Tuấn', '0934567890', 1, 1, 'https://i.pravatar.cc/150?img=33', GETDATE(), GETDATE());

IF NOT EXISTS (SELECT 1 FROM users WHERE username = 'shipper_thuylinh')
    INSERT INTO users (username, password, email, first_name, last_name, phone, enabled, email_verified, avatar_url, created_at, updated_at)
    VALUES ('shipper_thuylinh', @hashedPwd, 'thuylinh.ship@clothesshop.vn', N'Thùy', N'Linh', '0945678901', 1, 1, 'https://i.pravatar.cc/150?img=44', GETDATE(), GETDATE());

-- Customers
IF NOT EXISTS (SELECT 1 FROM users WHERE username = 'nguyenvana')
    INSERT INTO users (username, password, email, first_name, last_name, phone, enabled, email_verified, avatar_url, created_at, updated_at)
    VALUES ('nguyenvana', @hashedPwd, 'nguyenvana@gmail.com', N'Nguyễn', N'Văn A', '0956789012', 1, 1, 'https://i.pravatar.cc/150?img=13', GETDATE(), GETDATE());

IF NOT EXISTS (SELECT 1 FROM users WHERE username = 'tranthib')
    INSERT INTO users (username, password, email, first_name, last_name, phone, enabled, email_verified, avatar_url, created_at, updated_at)
    VALUES ('tranthib', @hashedPwd, 'tranthib@gmail.com', N'Trần', N'Thị B', '0967890123', 1, 1, 'https://i.pravatar.cc/150?img=24', GETDATE(), GETDATE());

-- Thêm 20 customers nữa
DECLARE @i INT = 3;
WHILE @i <= 20
BEGIN
    DECLARE @username NVARCHAR(50) = CONCAT('customer', @i);
    DECLARE @email NVARCHAR(100) = CONCAT('customer', @i, '@gmail.com');
    DECLARE @firstName NVARCHAR(50) = CONCAT(N'Khách ', @i);
    DECLARE @phone NVARCHAR(20) = CONCAT('09', RIGHT('00000000' + CAST((10000000 + @i) AS VARCHAR), 8));
    DECLARE @avatarNum INT = (@i % 70) + 1;
    
    IF NOT EXISTS (SELECT 1 FROM users WHERE username = @username)
        INSERT INTO users (username, password, email, first_name, last_name, phone, enabled, email_verified, avatar_url, created_at, updated_at)
        VALUES (@username, @hashedPwd, @email, @firstName, N'Hàng', @phone, 1, 1, CONCAT('https://i.pravatar.cc/150?img=', @avatarNum), GETDATE(), GETDATE());
    
    SET @i = @i + 1;
END

-- =============================================
-- 3. USER_ROLES
-- =============================================
PRINT 'Assigning Roles...';

DECLARE @adminId BIGINT = (SELECT id FROM users WHERE username = 'admin');
DECLARE @sellerId1 BIGINT = (SELECT id FROM users WHERE username = 'fashionstore');
DECLARE @sellerId2 BIGINT = (SELECT id FROM users WHERE username = 'trendyshop');
DECLARE @shipperId1 BIGINT = (SELECT id FROM users WHERE username = 'shipper_anhtuan');
DECLARE @shipperId2 BIGINT = (SELECT id FROM users WHERE username = 'shipper_thuylinh');

DECLARE @roleAdmin BIGINT = (SELECT id FROM roles WHERE name = 'ROLE_ADMIN');
DECLARE @roleSeller BIGINT = (SELECT id FROM roles WHERE name = 'ROLE_SELLER');
DECLARE @roleShipper BIGINT = (SELECT id FROM roles WHERE name = 'ROLE_SHIPPER');
DECLARE @roleUser BIGINT = (SELECT id FROM roles WHERE name = 'ROLE_USER');

IF NOT EXISTS (SELECT 1 FROM user_roles WHERE user_id = @adminId AND role_id = @roleAdmin)
    INSERT INTO user_roles (user_id, role_id) VALUES (@adminId, @roleAdmin);
IF NOT EXISTS (SELECT 1 FROM user_roles WHERE user_id = @sellerId1 AND role_id = @roleSeller)
    INSERT INTO user_roles (user_id, role_id) VALUES (@sellerId1, @roleSeller);
IF NOT EXISTS (SELECT 1 FROM user_roles WHERE user_id = @sellerId2 AND role_id = @roleSeller)
    INSERT INTO user_roles (user_id, role_id) VALUES (@sellerId2, @roleSeller);
IF NOT EXISTS (SELECT 1 FROM user_roles WHERE user_id = @shipperId1 AND role_id = @roleShipper)
    INSERT INTO user_roles (user_id, role_id) VALUES (@shipperId1, @roleShipper);
IF NOT EXISTS (SELECT 1 FROM user_roles WHERE user_id = @shipperId2 AND role_id = @roleShipper)
    INSERT INTO user_roles (user_id, role_id) VALUES (@shipperId2, @roleShipper);

-- Assign ROLE_USER to customers
DECLARE @customerId BIGINT;
DECLARE customer_cursor CURSOR FOR 
SELECT id FROM users WHERE username LIKE 'customer%' OR username IN ('nguyenvana', N'tranthib');

OPEN customer_cursor;
FETCH NEXT FROM customer_cursor INTO @customerId;
WHILE @@FETCH_STATUS = 0
BEGIN
    IF NOT EXISTS (SELECT 1 FROM user_roles WHERE user_id = @customerId AND role_id = @roleUser)
        INSERT INTO user_roles (user_id, role_id) VALUES (@customerId, @roleUser);
    FETCH NEXT FROM customer_cursor INTO @customerId;
END
CLOSE customer_cursor;
DEALLOCATE customer_cursor;

-- =============================================
-- =============================================
-- 4. CATEGORIES (PHÂN CẤP)
-- =============================================
PRINT 'Inserting Categories...';

-- Parent: Thời trang Nam
IF NOT EXISTS (SELECT 1 FROM category WHERE name = N'Thời trang Nam')
    INSERT INTO category (name, description, parent_id, created_at, updated_at)
    VALUES (N'Thời trang Nam', N'Tất cả sản phẩm dành cho nam giới', NULL, GETDATE(), GETDATE());

DECLARE @parentNam BIGINT = (SELECT id FROM category WHERE name = N'Thời trang Nam');

-- Parent: Thời trang Nữ
IF NOT EXISTS (SELECT 1 FROM category WHERE name = N'Thời trang Nữ')
    INSERT INTO category (name, description, parent_id, created_at, updated_at)
    VALUES (N'Thời trang Nữ', N'Tất cả sản phẩm dành cho nữ giới', NULL, GETDATE(), GETDATE());

DECLARE @parentNu BIGINT = (SELECT id FROM category WHERE name = N'Thời trang Nữ');

-- Children - Nam
IF NOT EXISTS (SELECT 1 FROM category WHERE name = N'Áo thun nam')
    INSERT INTO category (name, description, parent_id, created_at, updated_at)
    VALUES (N'Áo thun nam', N'Áo thun nam basic, oversize, form rộng', @parentNam, GETDATE(), GETDATE());

IF NOT EXISTS (SELECT 1 FROM category WHERE name = N'Quần jean nam')
    INSERT INTO category (name, description, parent_id, created_at, updated_at)
    VALUES (N'Quần jean nam', N'Quần jean nam slim fit, baggy, straight', @parentNam, GETDATE(), GETDATE());

IF NOT EXISTS (SELECT 1 FROM category WHERE name = N'Áo sơ mi nam')
    INSERT INTO category (name, description, parent_id, created_at, updated_at)
    VALUES (N'Áo sơ mi nam', N'Áo sơ mi nam công sở, casual', @parentNam, GETDATE(), GETDATE());

IF NOT EXISTS (SELECT 1 FROM category WHERE name = N'Áo khoác nam')
    INSERT INTO category (name, description, parent_id, created_at, updated_at)
    VALUES (N'Áo khoác nam', N'Áo khoác jacket, hoodie, blazer nam', @parentNam, GETDATE(), GETDATE());

IF NOT EXISTS (SELECT 1 FROM category WHERE name = N'Quần tây nam')
    INSERT INTO category (name, description, parent_id, created_at, updated_at)
    VALUES (N'Quần tây nam', N'Quần tây công sở, quần kaki nam', @parentNam, GETDATE(), GETDATE());

-- Children - Nữ
IF NOT EXISTS (SELECT 1 FROM category WHERE name = N'Áo thun nữ')
    INSERT INTO category (name, description, parent_id, created_at, updated_at)
    VALUES (N'Áo thun nữ', N'Áo thun nữ basic, croptop, form rộng', @parentNu, GETDATE(), GETDATE());

IF NOT EXISTS (SELECT 1 FROM category WHERE name = N'Quần jean nữ')
    INSERT INTO category (name, description, parent_id, created_at, updated_at)
    VALUES (N'Quần jean nữ', N'Quần jean nữ skinny, bootcut, baggy', @parentNu, GETDATE(), GETDATE());

IF NOT EXISTS (SELECT 1 FROM category WHERE name = N'Váy đầm')
    INSERT INTO category (name, description, parent_id, created_at, updated_at)
    VALUES (N'Váy đầm', N'Váy đầm dự tiệc, công sở, dạo phố', @parentNu, GETDATE(), GETDATE());

IF NOT EXISTS (SELECT 1 FROM category WHERE name = N'Áo sơ mi nữ')
    INSERT INTO category (name, description, parent_id, created_at, updated_at)
    VALUES (N'Áo sơ mi nữ', N'Áo sơ mi nữ công sở, kiểu vintage', @parentNu, GETDATE(), GETDATE());

IF NOT EXISTS (SELECT 1 FROM category WHERE name = N'Áo khoác nữ')
    INSERT INTO category (name, description, parent_id, created_at, updated_at)
    VALUES (N'Áo khoác nữ', N'Áo khoác blazer, cardigan, jacket nữ', @parentNu, GETDATE(), GETDATE());

IF NOT EXISTS (SELECT 1 FROM category WHERE name = N'Đầm dạ hội')
    INSERT INTO category (name, description, parent_id, created_at, updated_at)
    VALUES (N'Đầm dạ hội', N'Đầm dạ hội sang trọng, đầm dự tiệc', @parentNu, GETDATE(), GETDATE());

IF NOT EXISTS (SELECT 1 FROM category WHERE name = N'Chân váy')
    INSERT INTO category (name, description, parent_id, created_at, updated_at)
    VALUES (N'Chân váy', N'Chân váy ngắn, dài, xòe, bút chì', @parentNu, GETDATE(), GETDATE());

-- =============================================
-- 5. PRODUCTS (Không có price và quantity)
-- =============================================
PRINT 'Inserting Products...';

DECLARE @catAoThunNam BIGINT = (SELECT id FROM category WHERE name = N'Áo thun nam');
DECLARE @catQuanJeanNam BIGINT = (SELECT id FROM category WHERE name = N'Quần jean nam');
DECLARE @catAoSoMiNam BIGINT = (SELECT id FROM category WHERE name = N'Áo sơ mi nam');
DECLARE @catAoThunNu BIGINT = (SELECT id FROM category WHERE name = N'Áo thun nữ');
DECLARE @catQuanJeanNu BIGINT = (SELECT id FROM category WHERE name = N'Quần jean nữ');
DECLARE @catVayDam BIGINT = (SELECT id FROM category WHERE name = N'Váy đầm');

-- Product 1: Áo Thun Nam Basic
IF NOT EXISTS (SELECT 1 FROM product WHERE name = N'Áo Thun Nam Basic Cotton')
    INSERT INTO product (name, brand, gender, description, detail, specification, material, active, category_id, seller_id, created_at, updated_at)
    VALUES (
        N'Áo Thun Nam Basic Cotton',
        N'Fashion Store',
        N'Nam',
        N'Áo thun nam basic màu trơn, chất liệu cotton 100% mềm mại, thoáng mát. Form regular fit phù hợp mọi dáng người.',
        N'<h3>Mô tả chi tiết</h3><p>Áo thun nam basic với thiết kế tối giản, dễ phối đồ. Chất liệu cotton 100% cao cấp, thấm hút mồ hôi tốt, không ra màu khi giặt.</p><ul><li>Thiết kế cổ tròn basic</li><li>Form regular fit</li><li>Dễ phối với quần jean, kaki</li></ul>',
        N'<table><tr><td>Chất liệu</td><td>Cotton 100%</td></tr><tr><td>Form dáng</td><td>Regular Fit</td></tr><tr><td>Xuất xứ</td><td>Việt Nam</td></tr></table>',
        N'Cotton 100%',
        1,
        @catAoThunNam,
        @sellerId1,
        GETDATE(),
        GETDATE()
    );

-- Product 2: Áo Thun Oversize
IF NOT EXISTS (SELECT 1 FROM product WHERE name = N'Áo Thun Unisex Oversize')
    INSERT INTO product (name, brand, gender, description, detail, specification, material, active, category_id, seller_id, created_at, updated_at)
    VALUES (
        N'Áo Thun Unisex Oversize',
        N'Trendy Shop',
        N'Unisex',
        N'Áo thun oversize form rộng, phong cách streetwear. Chất vải cotton dày dặn, không xù lông.',
        N'<h3>Thông tin sản phẩm</h3><p>Áo thun oversize phù hợp cả nam và nữ, tạo phong cách năng động, trẻ trung.</p>',
        N'<table><tr><td>Chất liệu</td><td>Cotton 2 chiều</td></tr><tr><td>Form</td><td>Oversize</td></tr></table>',
        N'Cotton 2 chiều',
        1,
        @catAoThunNam,
        @sellerId2,
        GETDATE(),
        GETDATE()
    );

-- Product 3: Quần Jean Nam
IF NOT EXISTS (SELECT 1 FROM product WHERE name = N'Quần Jean Nam Slim Fit')
    INSERT INTO product (name, brand, gender, description, detail, specification, material, active, category_id, seller_id, created_at, updated_at)
    VALUES (
        N'Quần Jean Nam Slim Fit',
        N'Fashion Store',
        N'Nam',
        N'Quần jean nam form slim fit ôm vừa, chất denim cao cấp. Thiết kế 5 túi cổ điển.',
        N'<h3>Chi tiết</h3><p>Quần jean nam với chất denim cotton co giãn, mặc thoải mái. Form dáng tôn dáng nam tính.</p>',
        N'<table><tr><td>Chất liệu</td><td>Denim cotton</td></tr><tr><td>Form</td><td>Slim Fit</td></tr></table>',
        N'Denim',
        1,
        @catQuanJeanNam,
        @sellerId1,
        GETDATE(),
        GETDATE()
    );

-- Product 4: Quần Jean Nữ
IF NOT EXISTS (SELECT 1 FROM product WHERE name = N'Quần Jean Nữ Skinny')
    INSERT INTO product (name, brand, gender, description, detail, specification, material, active, category_id, seller_id, created_at, updated_at)
    VALUES (
        N'Quần Jean Nữ Skinny',
        N'Trendy Shop',
        N'Nữ',
        N'Quần jean nữ skinny ôm sát, tôn dáng. Chất denim co giãn 4 chiều thoải mái.',
        N'<h3>Mô tả</h3><p>Quần jean skinny form chuẩn, tôn dáng. Chất co giãn 4 chiều giúp di chuyển dễ dàng.</p>',
        N'<table><tr><td>Chất liệu</td><td>Denim 4 chiều</td></tr><tr><td>Form</td><td>Skinny</td></tr></table>',
        N'Denim co giãn',
        1,
        @catQuanJeanNu,
        @sellerId2,
        GETDATE(),
        GETDATE()
    );

-- Product 5: Áo Sơ Mi Nam
IF NOT EXISTS (SELECT 1 FROM product WHERE name = N'Áo Sơ Mi Nam Trắng')
    INSERT INTO product (name, brand, gender, description, detail, specification, material, active, category_id, seller_id, created_at, updated_at)
    VALUES (
        N'Áo Sơ Mi Nam Trắng',
        N'Fashion Store',
        N'Nam',
        N'Áo sơ mi nam trắng công sở, chất liệu kate cao cấp, không nhăn.',
        N'<h3>Chi tiết</h3><p>Áo sơ mi trắng basic là item không thể thiếu cho quý ông công sở.</p>',
        N'<table><tr><td>Chất liệu</td><td>Kate</td></tr><tr><td>Kiểu dáng</td><td>Công sở</td></tr></table>',
        N'Kate',
        1,
        @catAoSoMiNam,
        @sellerId1,
        GETDATE(),
        GETDATE()
    );

-- =============================================
-- 6. PRODUCT_VARIANTS (Chỉ có Size, không có Color)
-- =============================================
PRINT 'Inserting Product Variants...';

DECLARE @productId BIGINT;

-- Variants cho Product 1: Áo Thun Nam Basic (S, M, L, XL, XXL)
SET @productId = (SELECT id FROM product WHERE name = N'Áo Thun Nam Basic Cotton');
IF @productId IS NOT NULL
BEGIN
    IF NOT EXISTS (SELECT 1 FROM product_variant WHERE product_id = @productId AND size = 'S')
        INSERT INTO product_variant (product_id, size, price, quantity, sku, available, created_at, updated_at)
        VALUES (@productId, 'S', 150000, 40, 'AT001-S', 1, GETDATE(), GETDATE());
    
    IF NOT EXISTS (SELECT 1 FROM product_variant WHERE product_id = @productId AND size = 'M')
        INSERT INTO product_variant (product_id, size, price, quantity, sku, available, created_at, updated_at)
        VALUES (@productId, 'M', 150000, 60, 'AT001-M', 1, GETDATE(), GETDATE());
    
    IF NOT EXISTS (SELECT 1 FROM product_variant WHERE product_id = @productId AND size = 'L')
        INSERT INTO product_variant (product_id, size, price, quantity, sku, available, created_at, updated_at)
        VALUES (@productId, 'L', 150000, 70, 'AT001-L', 1, GETDATE(), GETDATE());
    
    IF NOT EXISTS (SELECT 1 FROM product_variant WHERE product_id = @productId AND size = 'XL')
        INSERT INTO product_variant (product_id, size, price, quantity, sku, available, created_at, updated_at)
        VALUES (@productId, 'XL', 150000, 50, 'AT001-XL', 1, GETDATE(), GETDATE());
    
    IF NOT EXISTS (SELECT 1 FROM product_variant WHERE product_id = @productId AND size = 'XXL')
        INSERT INTO product_variant (product_id, size, price, quantity, sku, available, created_at, updated_at)
        VALUES (@productId, 'XXL', 150000, 30, 'AT001-XXL', 1, GETDATE(), GETDATE());
END

-- Variants cho Product 2: Áo Oversize (L, XL, XXL)
SET @productId = (SELECT id FROM product WHERE name = N'Áo Thun Unisex Oversize');
IF @productId IS NOT NULL
BEGIN
    IF NOT EXISTS (SELECT 1 FROM product_variant WHERE product_id = @productId AND size = 'L')
        INSERT INTO product_variant (product_id, size, price, quantity, sku, available, created_at, updated_at)
        VALUES (@productId, 'L', 180000, 45, 'AT002-L', 1, GETDATE(), GETDATE());
    
    IF NOT EXISTS (SELECT 1 FROM product_variant WHERE product_id = @productId AND size = 'XL')
        INSERT INTO product_variant (product_id, size, price, quantity, sku, available, created_at, updated_at)
        VALUES (@productId, 'XL', 180000, 50, 'AT002-XL', 1, GETDATE(), GETDATE());
    
    IF NOT EXISTS (SELECT 1 FROM product_variant WHERE product_id = @productId AND size = 'XXL')
        INSERT INTO product_variant (product_id, size, price, quantity, sku, available, created_at, updated_at)
        VALUES (@productId, 'XXL', 180000, 35, 'AT002-XXL', 1, GETDATE(), GETDATE());
END

-- Variants cho Product 3: Quần Jean Nam (29, 30, 31, 32, 33, 34)
SET @productId = (SELECT id FROM product WHERE name = N'Quần Jean Nam Slim Fit');
IF @productId IS NOT NULL
BEGIN
    DECLARE @size INT = 29;
    WHILE @size <= 34
    BEGIN
        DECLARE @sizeStr NVARCHAR(10) = CAST(@size AS NVARCHAR(10));
        
        IF NOT EXISTS (SELECT 1 FROM product_variant WHERE product_id = @productId AND size = @sizeStr)
            INSERT INTO product_variant (product_id, size, price, quantity, sku, available, created_at, updated_at)
            VALUES (@productId, @sizeStr, 350000, 25, CONCAT('QJ001-', @sizeStr), 1, GETDATE(), GETDATE());
        
        SET @size = @size + 1;
    END
END

-- Variants cho Product 4: Quần Jean Nữ (26, 27, 28, 29, 30, 31)
SET @productId = (SELECT id FROM product WHERE name = N'Quần Jean Nữ Skinny');
IF @productId IS NOT NULL
BEGIN
    SET @size = 26;
    WHILE @size <= 31
    BEGIN
        SET @sizeStr = CAST(@size AS NVARCHAR(10));
        
        IF NOT EXISTS (SELECT 1 FROM product_variant WHERE product_id = @productId AND size = @sizeStr)
            INSERT INTO product_variant (product_id, size, price, quantity, sku, available, created_at, updated_at)
            VALUES (@productId, @sizeStr, 320000, 20, CONCAT('QJN001-', @sizeStr), 1, GETDATE(), GETDATE());
        
        SET @size = @size + 1;
    END
END

-- Variants cho Product 5: Áo Sơ Mi (38, 39, 40, 41, 42)
SET @productId = (SELECT id FROM product WHERE name = N'Áo Sơ Mi Nam Trắng');
IF @productId IS NOT NULL
BEGIN
    SET @size = 38;
    WHILE @size <= 42
    BEGIN
        SET @sizeStr = CAST(@size AS NVARCHAR(10));
        
        IF NOT EXISTS (SELECT 1 FROM product_variant WHERE product_id = @productId AND size = @sizeStr)
            INSERT INTO product_variant (product_id, size, price, quantity, sku, available, created_at, updated_at)
            VALUES (@productId, @sizeStr, 250000, 30, CONCAT('SM001-', @sizeStr), 1, GETDATE(), GETDATE());
        
        SET @size = @size + 1;
    END
END

-- =============================================
-- 7. PRODUCT_IMAGES
-- =============================================
PRINT 'Inserting Product Images...';

DECLARE @prodId BIGINT, @imgCount INT;
DECLARE prod_cursor CURSOR FOR SELECT id FROM product;
OPEN prod_cursor;
FETCH NEXT FROM prod_cursor INTO @prodId;

WHILE @@FETCH_STATUS = 0
BEGIN
    -- Main image
    IF NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = @prodId AND position = 0)
        INSERT INTO product_image (product_id, url, position, is_primary)
        VALUES (@prodId, CONCAT('https://picsum.photos/800/1000?random=', @prodId, '1'), 0, 1);
    
    -- Additional images (4 ảnh phụ)
    SET @imgCount = 1;
    WHILE @imgCount <= 4
    BEGIN
        IF NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = @prodId AND position = @imgCount)
            INSERT INTO product_image (product_id, url, position, is_primary)
            VALUES (@prodId, CONCAT('https://picsum.photos/800/1000?random=', @prodId, (@imgCount + 1)), @imgCount, 0);
        SET @imgCount = @imgCount + 1;
    END
    
    FETCH NEXT FROM prod_cursor INTO @prodId;
END

CLOSE prod_cursor;
DEALLOCATE prod_cursor;

-- =============================================
-- 8. REVIEWS
-- =============================================
PRINT 'Inserting Reviews...';

DECLARE @firstProductId BIGINT = (SELECT TOP 1 id FROM product ORDER BY id);
DECLARE @reviewerUserId BIGINT;
DECLARE @reviewTexts TABLE (rating INT, comment NVARCHAR(MAX));

INSERT INTO @reviewTexts VALUES 
(5, N'Sản phẩm rất tốt, chất lượng vượt mong đợi!'),
(5, N'Đóng gói cẩn thận, ship nhanh. Áo đẹp như hình.'),
(4, N'Áo đẹp, chất vải tốt nhưng hơi mỏng.'),
(5, N'Rất hài lòng với sản phẩm. Giá cả hợp lý.'),
(5, N'Mặc rất thoải mái, không bị ra màu. Recommend!'),
(4, N'Đẹp, nhưng size hơi nhỏ. Nên order size lớn hơn.'),
(5, N'Perfect! Exactly what I wanted!'),
(5, N'Chất lượng tuyệt vời, giá rẻ. Đáng tiền!'),
(4, N'Áo đẹp, form chuẩn. Trừ 1 sao vì ship lâu.'),
(5, N'Cực kỳ hài lòng! 5 sao!');

DECLARE @rating INT, @comment NVARCHAR(MAX);
DECLARE @reviewCount INT = 1;
DECLARE review_cursor CURSOR FOR SELECT rating, comment FROM @reviewTexts;
OPEN review_cursor;
FETCH NEXT FROM review_cursor INTO @rating, @comment;

WHILE @@FETCH_STATUS = 0 AND @reviewCount <= 10
BEGIN
    SET @reviewerUserId = (SELECT TOP 1 id FROM users WHERE username LIKE 'customer%' OR username IN ('nguyenvana', N'tranthib') ORDER BY NEWID());
    
    IF NOT EXISTS (SELECT 1 FROM review WHERE user_id = @reviewerUserId AND product_id = @firstProductId)
        INSERT INTO review (rating, comment, user_id, product_id, created_at, updated_at)
        VALUES (@rating, @comment, @reviewerUserId, @firstProductId, DATEADD(day, -@reviewCount, GETDATE()), GETDATE());
    
    SET @reviewCount = @reviewCount + 1;
    FETCH NEXT FROM review_cursor INTO @rating, @comment;
END

CLOSE review_cursor;
DEALLOCATE review_cursor;

-- =============================================
-- 9. VOUCHERS
-- =============================================
PRINT 'Inserting Vouchers...';

IF NOT EXISTS (SELECT 1 FROM voucher WHERE code = 'WELCOME10')
    INSERT INTO voucher (code, discount_percent, max_discount, expiry_date, active, type, created_by, created_at, updated_at)
    VALUES ('WELCOME10', 10, 50000, DATEADD(month, 3, GETDATE()), 1, 'ADMIN', @adminId, GETDATE(), GETDATE());

IF NOT EXISTS (SELECT 1 FROM voucher WHERE code = 'SUMMER20')
    INSERT INTO voucher (code, discount_percent, max_discount, expiry_date, active, type, created_by, created_at, updated_at)
    VALUES ('SUMMER20', 20, 100000, DATEADD(month, 2, GETDATE()), 1, 'ADMIN', @adminId, GETDATE(), GETDATE());

-- =============================================
-- 10. FAVORITES
-- =============================================
PRINT 'Inserting Favorites...';

DECLARE @favUserId BIGINT, @favProdId BIGINT;
SET @i = 1;
WHILE @i <= 20
BEGIN
    SET @favUserId = (SELECT TOP 1 id FROM users WHERE username LIKE 'customer%' OR username IN ('nguyenvana', N'tranthib') ORDER BY NEWID());
    SET @favProdId = (SELECT TOP 1 id FROM product ORDER BY NEWID());
    
    IF NOT EXISTS (SELECT 1 FROM favorite WHERE user_id = @favUserId AND product_id = @favProdId)
        INSERT INTO favorite (user_id, product_id)
        VALUES (@favUserId, @favProdId);
    
    SET @i = @i + 1;
END

-- =============================================
-- 11. CARTS & CART_ITEMS (với variant_id)
-- =============================================
PRINT 'Inserting Carts with Variants...';

DECLARE @cartUserId BIGINT, @cartId BIGINT, @variantId BIGINT;
SET @i = 1;
WHILE @i <= 5
BEGIN
    SET @cartUserId = (SELECT TOP 1 id FROM users WHERE username LIKE 'customer%' ORDER BY NEWID());
    
    IF NOT EXISTS (SELECT 1 FROM cart WHERE user_id = @cartUserId)
    BEGIN
        INSERT INTO cart (user_id) VALUES (@cartUserId);
        SET @cartId = SCOPE_IDENTITY();
        
        -- Add 1-3 items với variant
        DECLARE @itemCount INT = 1 + (ABS(CHECKSUM(NEWID())) % 3);
        DECLARE @itemIdx INT = 1;
        WHILE @itemIdx <= @itemCount
        BEGIN
            SET @variantId = (SELECT TOP 1 id FROM product_variant WHERE quantity > 0 ORDER BY NEWID());
            
            IF @variantId IS NOT NULL
            BEGIN
                DECLARE @cartProdId BIGINT = (SELECT product_id FROM product_variant WHERE id = @variantId);
                DECLARE @cartPrice DECIMAL(15,2) = (SELECT price FROM product_variant WHERE id = @variantId);
                
                IF NOT EXISTS (SELECT 1 FROM cart_item WHERE cart_id = @cartId AND variant_id = @variantId)
                    INSERT INTO cart_item (cart_id, product_id, variant_id, quantity, price_at_add_time)
                    VALUES (@cartId, @cartProdId, @variantId, 1 + (ABS(CHECKSUM(NEWID())) % 2), @cartPrice);
            END
            
            SET @itemIdx = @itemIdx + 1;
        END
    END
    
    SET @i = @i + 1;
END

COMMIT TRANSACTION;

PRINT '========================================';
PRINT 'SEED DATA COMPLETED SUCCESSFULLY!';
PRINT '========================================';
PRINT 'Summary:';
PRINT '- Users: ~22';
PRINT '- Roles assigned';
PRINT '- Categories: 5';
PRINT '- Products: 5 (với nhiều variants)';
PRINT '- Product Variants: ~30 (chỉ có Size)';
PRINT '- Product Images: ~25';
PRINT '- Reviews: 10';
PRINT '- Vouchers: 2';
PRINT '- Favorites: 20';
PRINT '- Carts with Variants: 5';
PRINT '========================================';
PRINT 'Login credentials (password: 123456):';
PRINT '- Admin: admin / 123456';
PRINT '- Seller: fashionstore / 123456';
PRINT '- Customer: nguyenvana / 123456';
PRINT '========================================';
PRINT 'NOTES:';
PRINT '- Products NO price/quantity (in variants)';
PRINT '- Variants have ONLY SIZE (no color)';
PRINT '- SKU format: [ProductCode]-[Size]';
PRINT '- CartItem & OrderItem reference variant_id';
PRINT '========================================';
GO
