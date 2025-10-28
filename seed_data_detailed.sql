-- =============================================
-- SEED DATA CHI TIẾT CHO CLOTHESSHOP
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
-- 2. USERS - Chi tiết thực tế
-- Password: 123132 (đã hash BCrypt)
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
    VALUES ('shipper_anhtuan', @hashedPwd, 'anhtuan.ship@clothesshop.vn', 'Anh', N'Tuấn', '0934567890', 1, 1, 'https://i.pravatar.cc/150?img=33', GETDATE(), GETDATE());

IF NOT EXISTS (SELECT 1 FROM users WHERE username = 'shipper_thuylinh')
    INSERT INTO users (username, password, email, first_name, last_name, phone, enabled, email_verified, avatar_url, created_at, updated_at)
    VALUES ('shipper_thuylinh', @hashedPwd, 'thuylinh.ship@clothesshop.vn', N'Thùy', N'Linh', '0945678901', 1, 1, 'https://i.pravatar.cc/150?img=44', GETDATE(), GETDATE());

-- Customers (30 người)
IF NOT EXISTS (SELECT 1 FROM users WHERE username = 'nguyenvana')
    INSERT INTO users (username, password, email, first_name, last_name, phone, enabled, email_verified, avatar_url, created_at, updated_at)
    VALUES ('nguyenvana', @hashedPwd, 'nguyenvana@gmail.com', N'Nguyễn', N'Văn A', '0956789012', 1, 1, 'https://i.pravatar.cc/150?img=13', GETDATE(), GETDATE());

IF NOT EXISTS (SELECT 1 FROM users WHERE username = 'tranthib')
    INSERT INTO users (username, password, email, first_name, last_name, phone, enabled, email_verified, avatar_url, created_at, updated_at)
    VALUES ('tranthib', @hashedPwd, 'tranthib@gmail.com', N'Trần', N'Thị B', '0967890123', 1, 1, 'https://i.pravatar.cc/150?img=24', GETDATE(), GETDATE());

IF NOT EXISTS (SELECT 1 FROM users WHERE username = 'levanc')
    INSERT INTO users (username, password, email, first_name, last_name, phone, enabled, email_verified, avatar_url, created_at, updated_at)
    VALUES ('levanc', @hashedPwd, 'levanc@gmail.com', N'Lê', N'Văn C', '0978901234', 1, 1, 'https://i.pravatar.cc/150?img=14', GETDATE(), GETDATE());

IF NOT EXISTS (SELECT 1 FROM users WHERE username = 'phamthid')
    INSERT INTO users (username, password, email, first_name, last_name, phone, enabled, email_verified, avatar_url, created_at, updated_at)
    VALUES ('phamthid', @hashedPwd, 'phamthid@gmail.com', N'Phạm', N'Thị D', '0989012345', 1, 1, 'https://i.pravatar.cc/150?img=25', GETDATE(), GETDATE());

IF NOT EXISTS (SELECT 1 FROM users WHERE username = 'hoangvane')
    INSERT INTO users (username, password, email, first_name, last_name, phone, enabled, email_verified, avatar_url, created_at, updated_at)
    VALUES ('hoangvane', @hashedPwd, 'hoangvane@gmail.com', N'Hoàng', N'Văn E', '0990123456', 1, 1, 'https://i.pravatar.cc/150?img=15', GETDATE(), GETDATE());

-- Thêm 25 customers nữa
DECLARE @i INT = 6;
WHILE @i <= 30
BEGIN
    DECLARE @username NVARCHAR(50) = CONCAT('customer', @i);
    DECLARE @email NVARCHAR(100) = CONCAT('customer', @i, '@gmail.com');
    DECLARE @firstName NVARCHAR(50) = CONCAT(N'Khách', @i);
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

-- Assign roles
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

-- Assign ROLE_USER to all customers
DECLARE @customerId BIGINT;
DECLARE customer_cursor CURSOR FOR 
SELECT u.id 
FROM users u
WHERE u.username LIKE 'customer%' OR u.username IN ('nguyenvana', N'tranthib', 'levanc', N'phamthid', 'hoangvane');

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
-- 4. CATEGORIES (table name: category)
-- =============================================
PRINT 'Inserting Categories...';

IF NOT EXISTS (SELECT 1 FROM category WHERE name = N'Áo thun nam')
    INSERT INTO category (name, description, created_at, updated_at)
    VALUES (N'Áo thun nam', N'Áo thun nam basic, form rộng, oversize', GETDATE(), GETDATE());

IF NOT EXISTS (SELECT 1 FROM category WHERE name = N'Áo thun nữ')
    INSERT INTO category (name, description, created_at, updated_at)
    VALUES (N'Áo thun nữ', N'Áo thun nữ, croptop, baby tee', GETDATE(), GETDATE());

IF NOT EXISTS (SELECT 1 FROM category WHERE name = N'Quần jean nam')
    INSERT INTO category (name, description, created_at, updated_at)
    VALUES (N'Quần jean nam', N'Quần jean nam baggy, slim fit, straight', GETDATE(), GETDATE());

IF NOT EXISTS (SELECT 1 FROM category WHERE name = N'Quần jean nữ')
    INSERT INTO category (name, description, created_at, updated_at)
    VALUES (N'Quần jean nữ', N'Quần jean nữ skinny, rách, ống loe', GETDATE(), GETDATE());

IF NOT EXISTS (SELECT 1 FROM category WHERE name = N'Áo sơ mi')
    INSERT INTO category (name, description, created_at, updated_at)
    VALUES (N'Áo sơ mi', N'Áo sơ mi nam nữ công sở, casual', GETDATE(), GETDATE());

IF NOT EXISTS (SELECT 1 FROM category WHERE name = N'Váy đầm')
    INSERT INTO category (name, description, created_at, updated_at)
    VALUES (N'Váy đầm', N'Váy đầm nữ dự tiệc, công sở, dạo phố', GETDATE(), GETDATE());

IF NOT EXISTS (SELECT 1 FROM category WHERE name = N'Áo khoác')
    INSERT INTO category (name, description, created_at, updated_at)
    VALUES (N'Áo khoác', N'Áo khoác jacket, hoodie, blazer', GETDATE(), GETDATE());

IF NOT EXISTS (SELECT 1 FROM category WHERE name = N'Giày dép')
    INSERT INTO category (name, description, created_at, updated_at)
    VALUES (N'Giày dép', N'Giày sneaker, sandal, boot', GETDATE(), GETDATE());

IF NOT EXISTS (SELECT 1 FROM category WHERE name = N'Phụ kiện')
    INSERT INTO category (name, description, created_at, updated_at)
    VALUES (N'Phụ kiện', N'Túi xách, ví, thắt lưng, mũN', GETDATE(), GETDATE());

-- =============================================
-- 5. PRODUCTS - 100 sản phẩm chi tiết (table name: product)
-- =============================================
PRINT 'Inserting Products...';

DECLARE @catAoThunNam BIGINT = (SELECT id FROM category WHERE name = N'Áo thun nam');
DECLARE @catAoThunNu BIGINT = (SELECT id FROM category WHERE name = N'Áo thun nữ');
DECLARE @catQuanJeanNam BIGINT = (SELECT id FROM category WHERE name = N'Quần jean nam');
DECLARE @catQuanJeanNu BIGINT = (SELECT id FROM category WHERE name = N'Quần jean nữ');
DECLARE @catAoSoMi BIGINT = (SELECT id FROM category WHERE name = N'Áo sơ mi');
DECLARE @catVayDam BIGINT = (SELECT id FROM category WHERE name = N'Váy đầm');
DECLARE @catAoKhoac BIGINT = (SELECT id FROM category WHERE name = N'Áo khoác');
DECLARE @catGiayDep BIGINT = (SELECT id FROM category WHERE name = N'Giày dép');
DECLARE @catPhuKien BIGINT = (SELECT id FROM category WHERE name = N'Phụ kiện');

-- Áo thun nam (20 sản phẩm)
IF NOT EXISTS (SELECT 1 FROM product WHERE name = N'Áo Thun Nam Basic Cotton Trắng')
    INSERT INTO product (name, description, detail, specification, price, quantity, gender, category_id, seller_id, created_at, updated_at)
    VALUES (
        N'Áo Thun Nam Basic Cotton Trắng',
        N'Áo thun nam basic màu trắng, chất liệu cotton 100% mềm mại, thoáng mát. Form regular fit phù hợp mọi dáng người.',
        N'<h3>Mô tả chi tiết</h3><p>Áo thun nam basic với thiết kế tối giản, dễ phối đồ. Chất liệu cotton 100% cao cấp, thấm hút mồ hôi tốt, không ra màu khi giặt. Đường may tỉ mỉ, form áo chuẩn Hàn Quốc.</p><ul><li>Thiết kế cổ tròn basic</li><li>Form regular fit thoải mái</li><li>Dễ phối với quần jean, kaki</li><li>Phù hợp mặc hàng ngày</li></ul>',
        N'<table><tr><td>Chất liệu</td><td>Cotton 100%</td></tr><tr><td>Màu sắc</td><td>Trắng</td></tr><tr><td>Size</td><td>M, L, XL, XXL</td></tr><tr><td>Xuất xứ</td><td>Việt Nam</td></tr></table>',
        150000, 100, 'Nam', @catAoThunNam, @sellerId1, GETDATE(), GETDATE()
    );

IF NOT EXISTS (SELECT 1 FROM product WHERE name = N'Áo Thun Nam Oversize Đen Form Rộng')
    INSERT INTO product (name, description, detail, specification, price, quantity, gender, category_id, seller_id, created_at, updated_at)
    VALUES (
        N'Áo Thun Nam Oversize Đen Form Rộng',
        N'Áo thun oversize form rộng màu đen, phong cách streetwear năng động. Chất vải cotton dày dặn, không xù lông.',
        N'<h3>Mô tả chi tiết</h3><p>Áo thun oversize form rộng theo phong cách Hàn Quốc, phù hợp với giới trẻ yêu thích phong cách streetwear. Chất vải cotton 2 chiều co giãn nhẹ, mặc thoải mái cả ngày.</p><ul><li>Form oversize rộng rãi</li><li>Phối được với nhiều style</li><li>Màu đen basic dễ mặc</li><li>Thích hợp dạo phố, đi chơi</li></ul>',
        N'<table><tr><td>Chất liệu</td><td>Cotton 2 chiều</td></tr><tr><td>Màu sắc</td><td>Đen</td></tr><tr><td>Size</td><td>L, XL, XXL</td></tr><tr><td>Form dáng</td><td>Oversize</td></tr></table>',
        180000, 80, 'Nam', @catAoThunNam, @sellerId1, GETDATE(), GETDATE()
    );

IF NOT EXISTS (SELECT 1 FROM product WHERE name = N'Áo Thun Nam Polo Trắng Phối Viền')
    INSERT INTO product (name, description, detail, specification, price, quantity, gender, category_id, seller_id, created_at, updated_at)
    VALUES (
        N'Áo Thun Nam Polo Trắng Phối Viền',
        N'Áo polo nam màu trắng phối viền đen, thiết kế lịch sự, sang trọng. Chất liệu pique cotton cao cấp.',
        N'<h3>Mô tả chi tiết</h3><p>Áo polo nam với chất liệu pique cotton cao cấp, bề mặt vải có kết cấu nổi đặc trưng. Thiết kế cổ bẻ lịch sự, phù hợp đi làm hoặc đi chơi. Phối viền tương phản tạo điểm nhấn.</p>',
        N'<table><tr><td>Chất liệu</td><td>Pique Cotton</td></tr><tr><td>Màu sắc</td><td>Trắng phối đen</td></tr><tr><td>Size</td><td>M, L, XL</td></tr></table>',
        220000, 60, 'Nam', @catAoThunNam, @sellerId2, GETDATE(), GETDATE()
    );

-- Thêm 17 áo thun nam nữa với tên khác nhau
DECLARE @productCount INT = 4;
WHILE @productCount <= 20
BEGIN
    DECLARE @prodName NVARCHAR(255) = CONCAT(N'Áo Thun Nam Style ', @productCount);
    IF NOT EXISTS (SELECT 1 FROM product WHERE name = @prodName)
        INSERT INTO product (name, description, detail, specification, price, quantity, gender, category_id, seller_id, created_at, updated_at)
        VALUES (
            @prodName,
            CONCAT(N'Áo thun nam chất lượng cao, thiết kế hiện đại, phù hợp mọi lứa tuổi. Mã sản phẩm: AT', @productCount),
            N'<h3>Thông tin sản phẩm</h3><p>Áo thun nam với chất liệu cotton cao cấp, form dáng chuẩn, dễ phối đồ.</p>',
            N'<table><tr><td>Chất liệu</td><td>Cotton</td></tr><tr><td>Size</td><td>M, L, XL</td></tr></table>',
            CAST((150000 + (@productCount * 5000)) AS DECIMAL(15,2)),
            50 + (@productCount * 2),
            'Nam',
            @catAoThunNam,
            CASE WHEN @productCount % 2 = 0 THEN @sellerId1 ELSE @sellerId2 END,
            GETDATE(),
            GETDATE()
        );
    SET @productCount = @productCount + 1;
END

-- Áo thun nữ (20 sản phẩm)
IF NOT EXISTS (SELECT 1 FROM product WHERE name = N'Áo Thun Nữ Baby Tee Trắng')
    INSERT INTO product (name, description, detail, specification, price, quantity, gender, category_id, seller_id, created_at, updated_at)
    VALUES (
        N'Áo Thun Nữ Baby Tee Trắng',
        N'Áo baby tee form ngắn ôm vừa vặn, chất cotton mềm mại. Phối được với quần jean, chân váy.',
        N'<h3>Mô tả</h3><p>Áo baby tee là item không thể thiếu trong tủ đồ của các cô gái. Form áo ôm vừa phải, tôn dáng mà vẫn thoải mái.</p>',
        N'<table><tr><td>Chất liệu</td><td>Cotton spandex</td></tr><tr><td>Màu</td><td>Trắng</td></tr><tr><td>Size</td><td>S, M, L</td></tr></table>',
        120000, 90, N'Nữ', @catAoThunNu, @sellerId1, GETDATE(), GETDATE()
    );

IF NOT EXISTS (SELECT 1 FROM product WHERE name = N'Áo Thun Nữ Croptop Đen')
    INSERT INTO product (name, description, detail, specification, price, quantity, gender, category_id, seller_id, created_at, updated_at)
    VALUES (
        N'Áo Thun Nữ Croptop Đen',
        N'Áo croptop form ngắn sexy, tôn dáng. Chất cotton co giãn 4 chiều, ôm body chuẩn.',
        N'<h3>Thông tin</h3><p>Áo croptop đen basic, dễ mix & match với nhiều trang phục. Form ôm nhẹ tôn dáng.</p>',
        N'<table><tr><td>Chất liệu</td><td>Cotton 4 chiều</td></tr><tr><td>Màu</td><td>Đen</td></tr><tr><td>Size</td><td>S, M, L</td></tr></table>',
        130000, 80, N'Nữ', @catAoThunNu, @sellerId2, GETDATE(), GETDATE()
    );

-- Thêm 18 áo thun nữ nữa
SET @productCount = 3;
WHILE @productCount <= 20
BEGIN
    SET @prodName = CONCAT(N'Áo Thun Nữ Style ', @productCount);
    IF NOT EXISTS (SELECT 1 FROM product WHERE name = @prodName)
        INSERT INTO product (name, description, detail, specification, price, quantity, gender, category_id, seller_id, created_at, updated_at)
        VALUES (
            @prodName,
            CONCAT(N'Áo thun nữ thời trang, chất lượng cao. Mã: ATNu', @productCount),
            N'<h3>Chi tiết</h3><p>Áo thun nữ thiết kế trẻ trung, năng động.</p>',
            N'<table><tr><td>Chất liệu</td><td>Cotton</td></tr><tr><td>Size</td><td>S, M, L</td></tr></table>',
            CAST((120000 + (@productCount * 3000)) AS DECIMAL(15,2)),
            40 + (@productCount * 2),
            N'Nữ',
            @catAoThunNu,
            CASE WHEN @productCount % 2 = 0 THEN @sellerId1 ELSE @sellerId2 END,
            GETDATE(),
            GETDATE()
        );
    SET @productCount = @productCount + 1;
END

-- Quần jean nam (15 sản phẩm)
IF NOT EXISTS (SELECT 1 FROM product WHERE name = N'Quần Jean Nam Slim Fit Xanh Đậm')
    INSERT INTO product (name, description, detail, specification, price, quantity, gender, category_id, seller_id, created_at, updated_at)
    VALUES (
        N'Quần Jean Nam Slim Fit Xanh Đậm',
        N'Quần jean nam form slim fit ôm vừa, chất denim cao cấp không ra màu. Thiết kế 5 túi cổ điển.',
        N'<h3>Chi tiết sản phẩm</h3><p>Quần jean nam slim fit với chất denim cotton co giãn, mặc thoải mái suốt cả ngày. Form dáng chuẩn, tôn dáng nam tính.</p>',
        N'<table><tr><td>Chất liệu</td><td>Denim cotton</td></tr><tr><td>Màu</td><td>Xanh đậm</td></tr><tr><td>Size</td><td>29, 30, 31, 32, 33</td></tr></table>',
        350000, 70, 'Nam', @catQuanJeanNam, @sellerId1, GETDATE(), GETDATE()
    );

IF NOT EXISTS (SELECT 1 FROM product WHERE name = N'Quần Jean Nam Baggy Đen Trơn')
    INSERT INTO product (name, description, detail, specification, price, quantity, gender, category_id, seller_id, created_at, updated_at)
    VALUES (
        N'Quần Jean Nam Baggy Đen Trơn',
        N'Quần jean baggy form rộng theo phong cách streetwear. Chất denim dày dặn, màu đen basic.',
        N'<h3>Thông tin</h3><p>Quần jean baggy hot trend, phù hợp phong cách năng động, cá tính.</p>',
        N'<table><tr><td>Chất liệu</td><td>Denim</td></tr><tr><td>Màu</td><td>Đen</td></tr><tr><td>Size</td><td>29-34</td></tr></table>',
        380000, 60, 'Nam', @catQuanJeanNam, @sellerId2, GETDATE(), GETDATE()
    );

-- Thêm 13 quần jean nam nữa
SET @productCount = 3;
WHILE @productCount <= 15
BEGIN
    SET @prodName = CONCAT(N'Quần Jean Nam Style ', @productCount);
    IF NOT EXISTS (SELECT 1 FROM product WHERE name = @prodName)
        INSERT INTO product (name, description, detail, specification, price, quantity, gender, category_id, seller_id, created_at, updated_at)
        VALUES (
            @prodName,
            CONCAT(N'Quần jean nam chất lượng, form đẹp. Mã: QJ', @productCount),
            N'<h3>Chi tiết</h3><p>Quần jean nam thiết kế hiện đại.</p>',
            N'<table><tr><td>Chất liệu</td><td>Denim</td></tr><tr><td>Size</td><td>29-33</td></tr></table>',
            CAST((350000 + (@productCount * 10000)) AS DECIMAL(15,2)),
            50 + @productCount,
            'Nam',
            @catQuanJeanNam,
            CASE WHEN @productCount % 2 = 0 THEN @sellerId1 ELSE @sellerId2 END,
            GETDATE(),
            GETDATE()
        );
    SET @productCount = @productCount + 1;
END

-- Quần jean nữ (15 sản phẩm)
IF NOT EXISTS (SELECT 1 FROM product WHERE name = N'Quần Jean Nữ Skinny Xanh Nhạt')
    INSERT INTO product (name, description, detail, specification, price, quantity, gender, category_id, seller_id, created_at, updated_at)
    VALUES (
        N'Quần Jean Nữ Skinny Xanh Nhạt',
        N'Quần jean nữ skinny ôm sát, tôn dáng. Chất denim co giãn 4 chiều thoải mái.',
        N'<h3>Mô tả</h3><p>Quần jean skinny form chuẩn, tôn dáng tối đa. Chất liệu co giãn 4 chiều giúp di chuyển thoải mái.</p>',
        N'<table><tr><td>Chất liệu</td><td>Denim 4 chiều</td></tr><tr><td>Màu</td><td>Xanh nhạt</td></tr><tr><td>Size</td><td>26-30</td></tr></table>',
        320000, 75, N'Nữ', @catQuanJeanNu, @sellerId1, GETDATE(), GETDATE()
    );

-- Thêm 14 quần jean nữ
SET @productCount = 2;
WHILE @productCount <= 15
BEGIN
    SET @prodName = CONCAT(N'Quần Jean Nữ Style ', @productCount);
    IF NOT EXISTS (SELECT 1 FROM product WHERE name = @prodName)
        INSERT INTO product (name, description, detail, specification, price, quantity, gender, category_id, seller_id, created_at, updated_at)
        VALUES (
            @prodName,
            CONCAT(N'Quần jean nữ thời trang. Mã: QJNu', @productCount),
            N'<h3>Chi tiết</h3><p>Quần jean nữ đẹp, form chuẩn.</p>',
            N'<table><tr><td>Chất liệu</td><td>Denim</td></tr><tr><td>Size</td><td>26-30</td></tr></table>',
            CAST((300000 + (@productCount * 8000)) AS DECIMAL(15,2)),
            45 + @productCount,
            N'Nữ',
            @catQuanJeanNu,
            CASE WHEN @productCount % 2 = 0 THEN @sellerId1 ELSE @sellerId2 END,
            GETDATE(),
            GETDATE()
        );
    SET @productCount = @productCount + 1;
END

-- Các danh mục khác (30 sản phẩm còn lại)
-- Áo sơ mi, váy đầm, áo khoác, giày dép, phụ kiện
SET @productCount = 1;
DECLARE @categories TABLE (cat_id BIGINT, cat_name NVARCHAR(50), gender NVARCHAR(10));
INSERT INTO @categories VALUES (@catAoSoMi, N'Áo Sơ Mi', N'Unisex');
INSERT INTO @categories VALUES (@catVayDam, N'Váy Đầm', N'Nữ');
INSERT INTO @categories VALUES (@catAoKhoac, N'Áo Khoác', N'Unisex');
INSERT INTO @categories VALUES (@catGiayDep, N'Giày Dép', N'Unisex');
INSERT INTO @categories VALUES (@catPhuKien, N'Phụ Kiện', N'Unisex');

DECLARE @catId BIGINT, @catNameVar NVARCHAR(50), @genderVar NVARCHAR(10);
DECLARE cat_cursor CURSOR FOR SELECT cat_id, cat_name, gender FROM @categories;
OPEN cat_cursor;
FETCH NEXT FROM cat_cursor INTO @catId, @catNameVar, @genderVar;

WHILE @@FETCH_STATUS = 0
BEGIN
    SET @productCount = 1;
    WHILE @productCount <= 6
    BEGIN
        SET @prodName = CONCAT(@catNameVar, N' Item ', @productCount);
        IF NOT EXISTS (SELECT 1 FROM product WHERE name = @prodName)
            INSERT INTO product (name, description, detail, specification, price, quantity, gender, category_id, seller_id, created_at, updated_at)
            VALUES (
                @prodName,
                CONCAT(N'Sản phẩm ', @catNameVar, N' chất lượng cao, thiết kế đẹp. Mã: ', LEFT(@catNameVar, 3), @productCount),
                N'<h3>Chi tiết sản phẩm</h3><p>Sản phẩm chất lượng cao, kiểu dáng thời trang.</p>',
                N'<table><tr><td>Chất liệu</td><td>Cao cấp</td></tr><tr><td>Size</td><td>Đa dạng</td></tr></table>',
                CAST((200000 + (@productCount * 50000)) AS DECIMAL(15,2)),
                30 + (@productCount * 3),
                @genderVar,
                @catId,
                CASE WHEN @productCount % 2 = 0 THEN @sellerId1 ELSE @sellerId2 END,
                GETDATE(),
                GETDATE()
            );
        SET @productCount = @productCount + 1;
    END
    FETCH NEXT FROM cat_cursor INTO @catId, @catNameVar, @genderVar;
END

CLOSE cat_cursor;
DEALLOCATE cat_cursor;

PRINT 'Total products should be ~100';

-- =============================================
-- 6. product_image - 3-5 ảnh cho mỗi sản phẩm
-- =============================================
PRINT 'Inserting Product Images...';

-- Lấy danh sách product IDs
DECLARE @prodId BIGINT, @imgCount INT;
DECLARE prod_cursor CURSOR FOR SELECT id FROM product ORDER BY id;
OPEN prod_cursor;
FETCH NEXT FROM prod_cursor INTO @prodId;

WHILE @@FETCH_STATUS = 0
BEGIN
    -- Main image
    IF NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = @prodId AND position = 0)
        INSERT INTO product_image (product_id, url, position, is_primary, created_at, updated_at)
        VALUES (@prodId, CONCAT('https://picsum.photos/800/1000?random=', @prodId, '1'), 0, 1, GETDATE(), GETDATE());
    
    -- Additional images
    SET @imgCount = 1;
    WHILE @imgCount <= 4
    BEGIN
        IF NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = @prodId AND position = @imgCount)
            INSERT INTO product_image (product_id, url, position, is_primary, created_at, updated_at)
            VALUES (@prodId, CONCAT('https://picsum.photos/800/1000?random=', @prodId, (@imgCount + 1)), @imgCount, 0, GETDATE(), GETDATE());
        SET @imgCount = @imgCount + 1;
    END
    
    FETCH NEXT FROM prod_cursor INTO @prodId;
END

CLOSE prod_cursor;
DEALLOCATE prod_cursor;

-- =============================================
-- 7. REVIEWS - 20 reviews cho sản phẩm đầu tiên
-- =============================================
PRINT 'Inserting Reviews...';

DECLARE @firstProductId BIGINT = (SELECT TOP 1 id FROM product ORDER BY id);
DECLARE @reviewerUserId BIGINT;
DECLARE @reviewCount INT = 1;
DECLARE @ratings TABLE (rating INT, comment NVARCHAR(MAX));

INSERT INTO @ratings VALUES 
(5, N'Sản phẩm rất tốt, chất lượng vượt mong đợi. Sẽ ủng hộ shop lâu dài!'),
(5, N'Đóng gói cẩn thận, ship nhanh. Áo đẹp như hình, form chuẩn.'),
(4, N'Áo đẹp, chất vải tốt nhưng hơi mỏng. Nhìn chung ok.'),
(5, N'Rất hài lòng với sản phẩm. Giá cả hợp lý, chất lượng tốt.'),
(5, N'Mặc rất thoải mái, không bị ra màu khi giặt. Recommend!'),
(4, N'Đẹp, nhưng size hơi nhỏ. Nên order size lớn hơn 1 size.'),
(5, N'Áo đẹp lắm, giá hợp lý. Shop phục vụ tốt.'),
(5, N'Chất liệu cotton mềm mại, mặc rất mát. Sẽ mua thêm.'),
(3, N'Áo ok nhưng màu hơi khác hình. Vẫn chấp nhận được.'),
(5, N'Perfect! Exactly what I wanted. Fast shipping!'),
(4, N'Áo đẹp, form chuẩn. Trừ 1 sao vì ship hơi lâu.'),
(5, N'Chất lượng tuyệt vời, giá rẻ. Đáng đồng tiền!'),
(5, N'Mua lần 2 rồi, vẫn ưng như lần đầu. Shop uy tín.'),
(4, N'Áo đẹp, chất vải mềm. Nhưng mùi hơi nặng phải giặt trước.'),
(5, N'Cực kỳ hài lòng! Form đẹp, chất vải xịn. 5 sao!'),
(5, N'Shop tư vấn nhiệt tình, giao hàng nhanh. Sản phẩm ok.'),
(4, N'Áo đẹp, giá tốt. Nhưng size hơi chật, nên lưu ý.'),
(5, N'Chất lượng quá tốt so với giá tiền. Sẽ giới thiệu bạn bè.'),
(5, N'Mặc rất thoải mái, co giãn tốt. Rất hài lòng!'),
(5, N'Đẹp xuất sắc! Mua nhiều màu luôn. Thanks shop!');

DECLARE rating_cursor CURSOR FOR SELECT rating, comment FROM @ratings;
DECLARE @rating INT, @comment NVARCHAR(MAX);
OPEN rating_cursor;
FETCH NEXT FROM rating_cursor INTO @rating, @comment;

WHILE @@FETCH_STATUS = 0 AND @reviewCount <= 20
BEGIN
    -- Random user
    SET @reviewerUserId = (SELECT TOP 1 id FROM users WHERE username LIKE 'customer%' OR username IN ('nguyenvana', N'tranthib', 'levanc', N'phamthid', 'hoangvane') ORDER BY NEWID());
    
    IF NOT EXISTS (SELECT 1 FROM review WHERE user_id = @reviewerUserId AND product_id = @firstProductId)
    BEGIN
        INSERT INTO review (rating, comment, user_id, product_id, created_at, updated_at)
        VALUES (@rating, @comment, @reviewerUserId, @firstProductId, DATEADD(day, -@reviewCount, GETDATE()), GETDATE());
    END
    
    SET @reviewCount = @reviewCount + 1;
    FETCH NEXT FROM rating_cursor INTO @rating, @comment;
END

CLOSE rating_cursor;
DEALLOCATE rating_cursor;

-- =============================================
-- 8. VOUCHERS
-- =============================================
PRINT 'Inserting Vouchers...';

IF NOT EXISTS (SELECT 1 FROM voucher WHERE code = 'WELCOME10')
    INSERT INTO voucher (code, discount_percent, max_discount, expiry_date, active, type, created_by, created_at, updated_at)
    VALUES ('WELCOME10', 10, 50000, DATEADD(month, 3, GETDATE()), 1, 'ADMIN', @adminId, GETDATE(), GETDATE());

IF NOT EXISTS (SELECT 1 FROM voucher WHERE code = 'SUMMER20')
    INSERT INTO voucher (code, discount_percent, max_discount, expiry_date, active, type, created_by, created_at, updated_at)
    VALUES ('SUMMER20', 20, 100000, DATEADD(month, 2, GETDATE()), 1, 'ADMIN', @adminId, GETDATE(), GETDATE());

IF NOT EXISTS (SELECT 1 FROM voucher WHERE code = 'FLASH50')
    INSERT INTO voucher (code, discount_percent, max_discount, expiry_date, active, type, created_by, created_at, updated_at)
    VALUES ('FLASH50', 50, 200000, DATEADD(day, 7, GETDATE()), 1, 'SELLER', @sellerId1, GETDATE(), GETDATE());

-- =============================================
-- 9. FAVORITES - Random favorites cho users
-- =============================================
PRINT 'Inserting Favorites...';

DECLARE @favUserId BIGINT, @favProdId BIGINT;
SET @i = 1;
WHILE @i <= 50
BEGIN
    SET @favUserId = (SELECT TOP 1 id FROM users WHERE username LIKE 'customer%' OR username IN ('nguyenvana', N'tranthib', 'levanc') ORDER BY NEWID());
    SET @favProdId = (SELECT TOP 1 id FROM product ORDER BY NEWID());
    
    IF NOT EXISTS (SELECT 1 FROM favorite WHERE user_id = @favUserId AND product_id = @favProdId)
        INSERT INTO favorite (user_id, product_id, created_at, updated_at)
        VALUES (@favUserId, @favProdId, GETDATE(), GETDATE());
    
    SET @i = @i + 1;
END

-- =============================================
-- 10. CARTS & CART_ITEMS
-- =============================================
PRINT 'Inserting Carts...';

DECLARE @cartUserId BIGINT, @cartId BIGINT;
SET @i = 1;
WHILE @i <= 10
BEGIN
    SET @cartUserId = (SELECT TOP 1 id FROM users WHERE username LIKE 'customer%' ORDER BY NEWID());
    
    IF NOT EXISTS (SELECT 1 FROM cart WHERE user_id = @cartUserId)
    BEGIN
        INSERT INTO cart (user_id, created_at, updated_at)
        VALUES (@cartUserId, GETDATE(), GETDATE());
        
        SET @cartId = SCOPE_IDENTITY();
        
        -- Add 1-3 items to cart
        DECLARE @itemCount INT = 1 + (ABS(CHECKSUM(NEWID())) % 3);
        DECLARE @itemIdx INT = 1;
        WHILE @itemIdx <= @itemCount
        BEGIN
            DECLARE @cartProdId BIGINT = (SELECT TOP 1 id FROM product ORDER BY NEWID());
            DECLARE @cartProdPrice DECIMAL(15,2) = (SELECT price FROM product WHERE id = @cartProdId);
            
            IF NOT EXISTS (SELECT 1 FROM cart_item WHERE cart_id = @cartId AND product_id = @cartProdId)
                INSERT INTO cart_item (cart_id, product_id, quantity, price_at_add_time)
                VALUES (@cartId, @cartProdId, 1 + (ABS(CHECKSUM(NEWID())) % 3), @cartProdPrice);
            
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
PRINT '- Users: ~35 (admin, 2 sellers, 2 shippers, ~30 customers)';
PRINT '- Categories: 9';
PRINT '- Products: ~100 with detailed info';
PRINT '- Product Images: ~500 (5 per product)';
PRINT '- Reviews: 20 for first product';
PRINT '- Vouchers: 3';
PRINT '- Favorites: 50';
PRINT '- Carts: 10 with items';
PRINT '========================================';
PRINT 'Login credentials (password: 123456):';
PRINT '- Admin: admin / 123456';
PRINT '- Seller: fashionstore / 123456';
PRINT '- Seller: trendyshop / 123456';
PRINT '- Shipper: shipper_anhtuan / 123456';
PRINT '- Customer: nguyenvana / 123456';
PRINT '========================================';
GO


