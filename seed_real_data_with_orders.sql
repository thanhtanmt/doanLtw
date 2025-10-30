-- =============================================
-- SEED REAL DATA WITH VARIANTS, IMAGES, ORDERS
-- SQL Server (T-SQL)
-- Note: Uses real product image URLs (Unsplash/Pexels - permissive for demos)
-- Run safely multiple times (idempotent inserts via IF NOT EXISTS)
-- =============================================

USE ClothesShop;
GO

SET NOCOUNT ON;
BEGIN TRANSACTION;

-- 1) ROLES ------------------------------------------------------------
IF NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ROLE_USER') INSERT INTO roles(name) VALUES('ROLE_USER');
IF NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ROLE_ADMIN') INSERT INTO roles(name) VALUES('ROLE_ADMIN');
IF NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ROLE_SELLER') INSERT INTO roles(name) VALUES('ROLE_SELLER');
IF NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ROLE_SHIPPER') INSERT INTO roles(name) VALUES('ROLE_SHIPPER');

-- 2) USERS ------------------------------------------------------------
DECLARE @pwd NVARCHAR(255) = '$2a$10$ES1ol7mk0TeA1zMpIQ5nFe7WF6Vm97PeCYA4DvKAPUbu3S4tp5y0O'; -- 123123

IF NOT EXISTS (SELECT 1 FROM users WHERE username='admin')
INSERT INTO users(username,password,email,first_name,last_name,phone,enabled,email_verified,avatar_url,created_at,updated_at)
VALUES('admin',@pwd,'admin@clothesshop.vn',N'Quản Trị',N'Viên','0901234567',1,1,'https://images.unsplash.com/photo-1531123897727-8f129e1688ce?auto=format&fit=crop&w=200&q=60',GETDATE(),GETDATE());

IF NOT EXISTS (SELECT 1 FROM users WHERE username='fashionstore')
INSERT INTO users(username,password,email,first_name,last_name,phone,enabled,email_verified,avatar_url,created_at,updated_at)
VALUES('fashionstore',@pwd,'contact@fashionstore.vn','Fashion','Store','0912345678',1,1,'https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=200&q=60',GETDATE(),GETDATE());

IF NOT EXISTS (SELECT 1 FROM users WHERE username='trendyshop')
INSERT INTO users(username,password,email,first_name,last_name,phone,enabled,email_verified,avatar_url,created_at,updated_at)
VALUES('trendyshop',@pwd,'info@trendyshop.vn','Trendy','Shop','0923456789',1,1,'https://images.unsplash.com/photo-1547425260-76bcadfb4f2c?auto=format&fit=crop&w=200&q=60',GETDATE(),GETDATE());

IF NOT EXISTS (SELECT 1 FROM users WHERE username='shipper_anhtuan')
INSERT INTO users(username,password,email,first_name,last_name,phone,enabled,email_verified,avatar_url,created_at,updated_at)
VALUES('shipper_anhtuan',@pwd,'anhtuan.ship@clothesshop.vn',N'Anh',N'Tuấn','0934567890',1,1,'https://images.unsplash.com/photo-1552374196-c4e7ffc6e126?auto=format&fit=crop&w=200&q=60',GETDATE(),GETDATE());

IF NOT EXISTS (SELECT 1 FROM users WHERE username='shipper_thuylinh')
INSERT INTO users(username,password,email,first_name,last_name,phone,enabled,email_verified,avatar_url,created_at,updated_at)
VALUES('shipper_thuylinh',@pwd,'thuylinh.ship@clothesshop.vn',N'Thùy',N'Linh','0945678901',1,1,'https://images.unsplash.com/photo-1547425260-76bcadfb4f2c?auto=format&fit=crop&w=200&q=60',GETDATE(),GETDATE());

IF NOT EXISTS (SELECT 1 FROM users WHERE username='nguyenvana')
INSERT INTO users(username,password,email,first_name,last_name,phone,enabled,email_verified,avatar_url,created_at,updated_at)
VALUES('nguyenvana',@pwd,'nguyenvana@gmail.com',N'Nguyễn',N'Văn A','0956789012',1,1,'https://images.unsplash.com/photo-1502685104226-ee32379fefbe?auto=format&fit=crop&w=200&q=60',GETDATE(),GETDATE());

IF NOT EXISTS (SELECT 1 FROM users WHERE username='tranthib')
INSERT INTO users(username,password,email,first_name,last_name,phone,enabled,email_verified,avatar_url,created_at,updated_at)
VALUES('tranthib',@pwd,'tranthib@gmail.com',N'Trần',N'Thị B','0967890123',1,1,'https://images.unsplash.com/photo-1524504388940-b1c1722653e1?auto=format&fit=crop&w=200&q=60',GETDATE(),GETDATE());

-- 3) USER ROLES -------------------------------------------------------
DECLARE @idAdmin BIGINT=(SELECT id FROM users WHERE username='admin');
DECLARE @idSeller1 BIGINT=(SELECT id FROM users WHERE username='fashionstore');
DECLARE @idSeller2 BIGINT=(SELECT id FROM users WHERE username='trendyshop');
DECLARE @idSh1 BIGINT=(SELECT id FROM users WHERE username='shipper_anhtuan');
DECLARE @idSh2 BIGINT=(SELECT id FROM users WHERE username='shipper_thuylinh');
DECLARE @idU1 BIGINT=(SELECT id FROM users WHERE username='nguyenvana');
DECLARE @idU2 BIGINT=(SELECT id FROM users WHERE username='tranthib');

DECLARE @rAdmin BIGINT=(SELECT id FROM roles WHERE name='ROLE_ADMIN');
DECLARE @rSeller BIGINT=(SELECT id FROM roles WHERE name='ROLE_SELLER');
DECLARE @rShipper BIGINT=(SELECT id FROM roles WHERE name='ROLE_SHIPPER');
DECLARE @rUser BIGINT=(SELECT id FROM roles WHERE name='ROLE_USER');

IF NOT EXISTS (SELECT 1 FROM user_roles WHERE user_id=@idAdmin AND role_id=@rAdmin) INSERT INTO user_roles(user_id,role_id) VALUES(@idAdmin,@rAdmin);
IF NOT EXISTS (SELECT 1 FROM user_roles WHERE user_id=@idSeller1 AND role_id=@rSeller) INSERT INTO user_roles(user_id,role_id) VALUES(@idSeller1,@rSeller);
IF NOT EXISTS (SELECT 1 FROM user_roles WHERE user_id=@idSeller2 AND role_id=@rSeller) INSERT INTO user_roles(user_id,role_id) VALUES(@idSeller2,@rSeller);
IF NOT EXISTS (SELECT 1 FROM user_roles WHERE user_id=@idSh1 AND role_id=@rShipper) INSERT INTO user_roles(user_id,role_id) VALUES(@idSh1,@rShipper);
IF NOT EXISTS (SELECT 1 FROM user_roles WHERE user_id=@idSh2 AND role_id=@rShipper) INSERT INTO user_roles(user_id,role_id) VALUES(@idSh2,@rShipper);
IF NOT EXISTS (SELECT 1 FROM user_roles WHERE user_id=@idU1 AND role_id=@rUser) INSERT INTO user_roles(user_id,role_id) VALUES(@idU1,@rUser);
IF NOT EXISTS (SELECT 1 FROM user_roles WHERE user_id=@idU2 AND role_id=@rUser) INSERT INTO user_roles(user_id,role_id) VALUES(@idU2,@rUser);

-- 4) CATEGORIES -------------------------------------------------------
IF NOT EXISTS (SELECT 1 FROM category WHERE name=N'Thời trang Nam')
INSERT INTO category(name,description,parent_id,created_at,updated_at) VALUES(N'Thời trang Nam',N'Sản phẩm cho nam',NULL,GETDATE(),GETDATE());
DECLARE @catNam BIGINT=(SELECT id FROM category WHERE name=N'Thời trang Nam');
IF NOT EXISTS (SELECT 1 FROM category WHERE name=N'Thời trang Nữ')
INSERT INTO category(name,description,parent_id,created_at,updated_at) VALUES(N'Thời trang Nữ',N'Sản phẩm cho nữ',NULL,GETDATE(),GETDATE());
DECLARE @catNu BIGINT=(SELECT id FROM category WHERE name=N'Thời trang Nữ');

-- Children
IF NOT EXISTS (SELECT 1 FROM category WHERE name=N'Áo thun nam') INSERT INTO category(name,description,parent_id,created_at,updated_at) VALUES(N'Áo thun nam',N'Basic/Oversize',@catNam,GETDATE(),GETDATE());
IF NOT EXISTS (SELECT 1 FROM category WHERE name=N'Áo sơ mi nam') INSERT INTO category(name,description,parent_id,created_at,updated_at) VALUES(N'Áo sơ mi nam',N'Office/Casual',@catNam,GETDATE(),GETDATE());
IF NOT EXISTS (SELECT 1 FROM category WHERE name=N'Quần jean nam') INSERT INTO category(name,description,parent_id,created_at,updated_at) VALUES(N'Quần jean nam',N'Slim/Straight',@catNam,GETDATE(),GETDATE());
IF NOT EXISTS (SELECT 1 FROM category WHERE name=N'Áo khoác unisex') INSERT INTO category(name,description,parent_id,created_at,updated_at) VALUES(N'Áo khoác unisex',N'Hoodie/Jacket',@catNam,GETDATE(),GETDATE());

IF NOT EXISTS (SELECT 1 FROM category WHERE name=N'Áo thun nữ') INSERT INTO category(name,description,parent_id,created_at,updated_at) VALUES(N'Áo thun nữ',N'Basic/Croptop',@catNu,GETDATE(),GETDATE());
IF NOT EXISTS (SELECT 1 FROM category WHERE name=N'Quần jean nữ') INSERT INTO category(name,description,parent_id,created_at,updated_at) VALUES(N'Quần jean nữ',N'Skinny/Baggy',@catNu,GETDATE(),GETDATE());
IF NOT EXISTS (SELECT 1 FROM category WHERE name=N'Váy đầm') INSERT INTO category(name,description,parent_id,created_at,updated_at) VALUES(N'Váy đầm',N'Dạ hội/Dạo phố',@catNu,GETDATE(),GETDATE());

DECLARE @catAoThunNam BIGINT=(SELECT id FROM category WHERE name=N'Áo thun nam');
DECLARE @catAoSoMiNam BIGINT=(SELECT id FROM category WHERE name=N'Áo sơ mi nam');
DECLARE @catQuanJeanNam BIGINT=(SELECT id FROM category WHERE name=N'Quần jean nam');
DECLARE @catAoKhoacUnisex BIGINT=(SELECT id FROM category WHERE name=N'Áo khoác unisex');
DECLARE @catAoThunNu BIGINT=(SELECT id FROM category WHERE name=N'Áo thun nữ');
DECLARE @catQuanJeanNu BIGINT=(SELECT id FROM category WHERE name=N'Quần jean nữ');
DECLARE @catVayDam BIGINT=(SELECT id FROM category WHERE name=N'Váy đầm');

-- 5) PRODUCTS ---------------------------------------------------------
-- 7 real products; sellers distributed
IF NOT EXISTS (SELECT 1 FROM product WHERE name=N'Áo Thun Nam Basic Cotton')
INSERT INTO product(name,brand,gender,description,detail,specification,material,active,category_id,seller_id,created_at,updated_at)
VALUES(N'Áo Thun Nam Basic Cotton',N'Basic Tee Co',N'Nam',N'Áo thun basic chất cotton 100%, thoáng mát.',
N'<ul><li>Cổ tròn, form regular</li><li>Vải cotton 100%</li><li>Dễ phối đồ</li></ul>',
N'<table><tr><td>Chất liệu</td><td>Cotton 100%</td></tr><tr><td>Xuất xứ</td><td>Việt Nam</td></tr></table>',
N'Cotton 100%',1,@catAoThunNam,@idSeller1,GETDATE(),GETDATE());

IF NOT EXISTS (SELECT 1 FROM product WHERE name=N'Áo Thun Unisex Oversize')
INSERT INTO product(name,brand,gender,description,detail,specification,material,active,category_id,seller_id,created_at,updated_at)
VALUES(N'Áo Thun Unisex Oversize',N'StreetWear VN',N'Unisex',N'Áo thun oversize phong cách streetwear.',
N'<p>Form rộng, thoải mái.</p>',N'<table><tr><td>Form</td><td>Oversize</td></tr></table>',N'Cotton 2 chiều',1,@catAoThunNam,@idSeller2,GETDATE(),GETDATE());

IF NOT EXISTS (SELECT 1 FROM product WHERE name=N'Quần Jean Nam Slim Fit')
INSERT INTO product(name,brand,gender,description,detail,specification,material,active,category_id,seller_id,created_at,updated_at)
VALUES(N'Quần Jean Nam Slim Fit',N'DenimLab',N'Nam',N'Quần jean slim fit co giãn.',N'<p>5 túi, denim co giãn.</p>',N'<table><tr><td>Form</td><td>Slim</td></tr></table>',N'Denim',1,@catQuanJeanNam,@idSeller1,GETDATE(),GETDATE());

IF NOT EXISTS (SELECT 1 FROM product WHERE name=N'Quần Jean Nữ Skinny')
INSERT INTO product(name,brand,gender,description,detail,specification,material,active,category_id,seller_id,created_at,updated_at)
VALUES(N'Quần Jean Nữ Skinny',N'DenimLab',N'Nữ',N'Jeans skinny tôn dáng.',N'<p>Co giãn 4 chiều.</p>',N'<table><tr><td>Form</td><td>Skinny</td></tr></table>',N'Denim',1,@catQuanJeanNu,@idSeller2,GETDATE(),GETDATE());

IF NOT EXISTS (SELECT 1 FROM product WHERE name=N'Áo Sơ Mi Nam Trắng')
INSERT INTO product(name,brand,gender,description,detail,specification,material,active,category_id,seller_id,created_at,updated_at)
VALUES(N'Áo Sơ Mi Nam Trắng',N'OfficeLine',N'Nam',N'Sơ mi trắng công sở, ít nhăn.',N'<p>Form slim, tay dài.</p>',N'<table><tr><td>Chất liệu</td><td>Kate</td></tr></table>',N'Kate',1,@catAoSoMiNam,@idSeller1,GETDATE(),GETDATE());

IF NOT EXISTS (SELECT 1 FROM product WHERE name=N'Đầm Hoa Nữ Midi')
INSERT INTO product(name,brand,gender,description,detail,specification,material,active,category_id,seller_id,created_at,updated_at)
VALUES(N'Đầm Hoa Nữ Midi',N'BellaWear',N'Nữ',N'Đầm hoa midi dịu dàng.',N'<p>Phù hợp dạo phố.</p>',N'<table><tr><td>Form</td><td>Midi</td></tr></table>',N'Polyester',1,@catVayDam,@idSeller2,GETDATE(),GETDATE());

IF NOT EXISTS (SELECT 1 FROM product WHERE name=N'Áo Khoác Hoodie Unisex')
INSERT INTO product(name,brand,gender,description,detail,specification,material,active,category_id,seller_id,created_at,updated_at)
VALUES(N'Áo Khoác Hoodie Unisex',N'Warm&Co',N'Unisex',N'Hoodie nỉ ấm, mềm.',N'<p>Có túi kangaroo.</p>',N'<table><tr><td>Chất liệu</td><td>Nỉ bông</td></tr></table>',N'Fleece',1,@catAoKhoacUnisex,@idSeller2,GETDATE(),GETDATE());

-- 6) VARIANTS --------------------------------------------------------
DECLARE @p1 BIGINT=(SELECT id FROM product WHERE name=N'Áo Thun Nam Basic Cotton');
DECLARE @p2 BIGINT=(SELECT id FROM product WHERE name=N'Áo Thun Unisex Oversize');
DECLARE @p3 BIGINT=(SELECT id FROM product WHERE name=N'Quần Jean Nam Slim Fit');
DECLARE @p4 BIGINT=(SELECT id FROM product WHERE name=N'Quần Jean Nữ Skinny');
DECLARE @p5 BIGINT=(SELECT id FROM product WHERE name=N'Áo Sơ Mi Nam Trắng');
DECLARE @p6 BIGINT=(SELECT id FROM product WHERE name=N'Đầm Hoa Nữ Midi');
DECLARE @p7 BIGINT=(SELECT id FROM product WHERE name=N'Áo Khoác Hoodie Unisex');

-- P1 sizes S-XXL price 150k
IF @p1 IS NOT NULL BEGIN
    IF NOT EXISTS(SELECT 1 FROM product_variant WHERE product_id=@p1 AND sku='AT001-S') INSERT INTO product_variant(product_id,size,price,quantity,sku,available,created_at,updated_at) VALUES(@p1,'S',150000,40,'AT001-S',1,GETDATE(),GETDATE());
    IF NOT EXISTS(SELECT 1 FROM product_variant WHERE product_id=@p1 AND sku='AT001-M') INSERT INTO product_variant(product_id,size,price,quantity,sku,available,created_at,updated_at) VALUES(@p1,'M',150000,60,'AT001-M',1,GETDATE(),GETDATE());
    IF NOT EXISTS(SELECT 1 FROM product_variant WHERE product_id=@p1 AND sku='AT001-L') INSERT INTO product_variant(product_id,size,price,quantity,sku,available,created_at,updated_at) VALUES(@p1,'L',150000,70,'AT001-L',1,GETDATE(),GETDATE());
    IF NOT EXISTS(SELECT 1 FROM product_variant WHERE product_id=@p1 AND sku='AT001-XL') INSERT INTO product_variant(product_id,size,price,quantity,sku,available,created_at,updated_at) VALUES(@p1,'XL',150000,50,'AT001-XL',1,GETDATE(),GETDATE());
    IF NOT EXISTS(SELECT 1 FROM product_variant WHERE product_id=@p1 AND sku='AT001-XXL') INSERT INTO product_variant(product_id,size,price,quantity,sku,available,created_at,updated_at) VALUES(@p1,'XXL',150000,30,'AT001-XXL',1,GETDATE(),GETDATE());
END
-- P2 L-XXL 180k
IF @p2 IS NOT NULL BEGIN
    IF NOT EXISTS(SELECT 1 FROM product_variant WHERE product_id=@p2 AND sku='AT002-L') INSERT INTO product_variant(product_id,size,price,quantity,sku,available,created_at,updated_at) VALUES(@p2,'L',180000,45,'AT002-L',1,GETDATE(),GETDATE());
    IF NOT EXISTS(SELECT 1 FROM product_variant WHERE product_id=@p2 AND sku='AT002-XL') INSERT INTO product_variant(product_id,size,price,quantity,sku,available,created_at,updated_at) VALUES(@p2,'XL',180000,50,'AT002-XL',1,GETDATE(),GETDATE());
    IF NOT EXISTS(SELECT 1 FROM product_variant WHERE product_id=@p2 AND sku='AT002-XXL') INSERT INTO product_variant(product_id,size,price,quantity,sku,available,created_at,updated_at) VALUES(@p2,'XXL',180000,35,'AT002-XXL',1,GETDATE(),GETDATE());
END
-- P3 29-34 350k
IF @p3 IS NOT NULL BEGIN
    DECLARE @s INT=29;
    WHILE @s<=34 BEGIN
        IF NOT EXISTS(SELECT 1 FROM product_variant WHERE product_id=@p3 AND size=CAST(@s AS NVARCHAR(10)))
            INSERT INTO product_variant(product_id,size,price,quantity,sku,available,created_at,updated_at)
            VALUES(@p3,CAST(@s AS NVARCHAR(10)),350000,25,CONCAT('QJ001-',@s),1,GETDATE(),GETDATE());
        SET @s=@s+1;
    END
END
-- P4 26-31 320k
IF @p4 IS NOT NULL BEGIN
    SET @s=26; WHILE @s<=31 BEGIN
        IF NOT EXISTS(SELECT 1 FROM product_variant WHERE product_id=@p4 AND size=CAST(@s AS NVARCHAR(10)))
            INSERT INTO product_variant(product_id,size,price,quantity,sku,available,created_at,updated_at)
            VALUES(@p4,CAST(@s AS NVARCHAR(10)),320000,20,CONCAT('QJN001-',@s),1,GETDATE(),GETDATE());
        SET @s=@s+1;
    END
END
-- P5 38-42 250k
IF @p5 IS NOT NULL BEGIN
    SET @s=38; WHILE @s<=42 BEGIN
        IF NOT EXISTS(SELECT 1 FROM product_variant WHERE product_id=@p5 AND size=CAST(@s AS NVARCHAR(10)))
            INSERT INTO product_variant(product_id,size,price,quantity,sku,available,created_at,updated_at)
            VALUES(@p5,CAST(@s AS NVARCHAR(10)),250000,30,CONCAT('SM001-',@s),1,GETDATE(),GETDATE());
        SET @s=@s+1;
    END
END
-- P6 dress sizes S-L 420k
IF @p6 IS NOT NULL BEGIN
    IF NOT EXISTS(SELECT 1 FROM product_variant WHERE product_id=@p6 AND sku='DM001-S') INSERT INTO product_variant(product_id,size,price,quantity,sku,available,created_at,updated_at) VALUES(@p6,'S',420000,20,'DM001-S',1,GETDATE(),GETDATE());
    IF NOT EXISTS(SELECT 1 FROM product_variant WHERE product_id=@p6 AND sku='DM001-M') INSERT INTO product_variant(product_id,size,price,quantity,sku,available,created_at,updated_at) VALUES(@p6,'M',420000,30,'DM001-M',1,GETDATE(),GETDATE());
    IF NOT EXISTS(SELECT 1 FROM product_variant WHERE product_id=@p6 AND sku='DM001-L') INSERT INTO product_variant(product_id,size,price,quantity,sku,available,created_at,updated_at) VALUES(@p6,'L',420000,15,'DM001-L',1,GETDATE(),GETDATE());
END
-- P7 hoodie sizes M-XL 390k
IF @p7 IS NOT NULL BEGIN
    IF NOT EXISTS(SELECT 1 FROM product_variant WHERE product_id=@p7 AND sku='HD001-M') INSERT INTO product_variant(product_id,size,price,quantity,sku,available,created_at,updated_at) VALUES(@p7,'M',390000,25,'HD001-M',1,GETDATE(),GETDATE());
    IF NOT EXISTS(SELECT 1 FROM product_variant WHERE product_id=@p7 AND sku='HD001-L') INSERT INTO product_variant(product_id,size,price,quantity,sku,available,created_at,updated_at) VALUES(@p7,'L',390000,30,'HD001-L',1,GETDATE(),GETDATE());
    IF NOT EXISTS(SELECT 1 FROM product_variant WHERE product_id=@p7 AND sku='HD001-XL') INSERT INTO product_variant(product_id,size,price,quantity,sku,available,created_at,updated_at) VALUES(@p7,'XL',390000,20,'HD001-XL',1,GETDATE(),GETDATE());
END

-- 7) PRODUCT IMAGES (Real URLs) --------------------------------------
-- Helper proc: insert if not exists by position
DECLARE @img TABLE(pid BIGINT, url NVARCHAR(500), pos INT, primaryFlag BIT);

-- P1 images (basic tee)
INSERT INTO @img VALUES
(@p1,'https://images.unsplash.com/photo-1512436991641-6745cdb1723f?auto=format&fit=crop&w=900&q=80',0,1),
(@p1,'https://images.unsplash.com/photo-1512436991641-6745cdb1723f?auto=format&fit=crop&w=700&q=60',1,0),
(@p1,'https://images.unsplash.com/photo-1520974735194-06f4bba1f2e4?auto=format&fit=crop&w=900&q=80',2,0),
(@p1,'https://images.unsplash.com/photo-1523381210434-271e8be1f52b?auto=format&fit=crop&w=900&q=80',3,0);

-- P2 images (oversize tee)
INSERT INTO @img VALUES
(@p2,'https://images.unsplash.com/photo-1519741497674-611481863552?auto=format&fit=crop&w=900&q=80',0,1),
(@p2,'https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?auto=format&fit=crop&w=900&q=80',1,0),
(@p2,'https://images.unsplash.com/photo-1516826957135-700dedea6986?auto=format&fit=crop&w=900&q=80',2,0);

-- P3 images (men jeans)
INSERT INTO @img VALUES
(@p3,'https://images.unsplash.com/photo-1514996937319-344454492b37?auto=format&fit=crop&w=900&q=80',0,1),
(@p3,'https://images.unsplash.com/photo-1495121605193-b116b5b09e10?auto=format&fit=crop&w=900&q=80',1,0),
(@p3,'https://images.unsplash.com/photo-1516822003754-cca485356ecb?auto=format&fit=crop&w=900&q=80',2,0);

-- P4 images (women jeans)
INSERT INTO @img VALUES
(@p4,'https://images.unsplash.com/photo-1520974735194-06f4bba1f2e4?auto=format&fit=crop&w=900&q=80',0,1),
(@p4,'https://images.unsplash.com/photo-1503342394124-48075f1a57c1?auto=format&fit=crop&w=900&q=80',1,0);

-- P5 images (white shirt)
INSERT INTO @img VALUES
(@p5,'https://images.unsplash.com/photo-1516826957135-700dedea6986?auto=format&fit=crop&w=900&q=80',0,1),
(@p5,'https://images.unsplash.com/photo-1548883354-aa03f121c571?auto=format&fit=crop&w=900&q=80',1,0);

-- P6 images (dress)
INSERT INTO @img VALUES
(@p6,'https://images.unsplash.com/photo-1512436991641-6745cdb1723f?auto=format&fit=crop&w=900&q=80',0,1),
(@p6,'https://images.unsplash.com/photo-1519741497674-611481863552?auto=format&fit=crop&w=900&q=80',1,0);

-- P7 images (hoodie)
INSERT INTO @img VALUES
(@p7,'https://images.unsplash.com/photo-1490481651871-ab68de25d43d?auto=format&fit=crop&w=900&q=80',0,1),
(@p7,'https://images.unsplash.com/photo-1512436991641-6745cdb1723f?auto=format&fit=crop&w=900&q=80',1,0);

DECLARE @pid BIGINT,@url NVARCHAR(500),@pos INT,@isP BIT;
DECLARE c CURSOR FOR SELECT pid,url,pos,primaryFlag FROM @img;
OPEN c; FETCH NEXT FROM c INTO @pid,@url,@pos,@isP;
WHILE @@FETCH_STATUS=0 BEGIN
    IF @pid IS NOT NULL AND NOT EXISTS(SELECT 1 FROM product_image WHERE product_id=@pid AND position=@pos)
        INSERT INTO product_image(product_id,url,position,is_primary) VALUES(@pid,@url,@pos,@isP);
    FETCH NEXT FROM c INTO @pid,@url,@pos,@isP;
END
CLOSE c; DEALLOCATE c;

-- 8) REVIEWS (with images) -------------------------------------------
DECLARE @anyProd BIGINT=(SELECT TOP 1 id FROM product ORDER BY id);
IF @anyProd IS NOT NULL BEGIN
    DECLARE @rvUser BIGINT=(SELECT id FROM users WHERE username='nguyenvana');
    IF NOT EXISTS(SELECT 1 FROM review WHERE user_id=@rvUser AND product_id=@anyProd)
    BEGIN
        INSERT INTO review(rating,comment,user_id,product_id,created_at,updated_at)
        VALUES(5,N'Sản phẩm đúng mô tả, chất lượng tốt!',@rvUser,@anyProd,DATEADD(day,-2,GETDATE()),GETDATE());
        DECLARE @rvId BIGINT=SCOPE_IDENTITY();
        INSERT INTO review_images(review_id,image_url) VALUES
        (@rvId,'https://images.unsplash.com/photo-1520974735194-06f4bba1f2e4?auto=format&fit=crop&w=900&q=80');
    END
END

-- Additional sample reviews across products (idempotent)
DECLARE @uA BIGINT=@idU1; -- nguyenvana
DECLARE @uB BIGINT=@idU2; -- tranthib

-- P1: Áo Thun Nam Basic Cotton
IF @p1 IS NOT NULL BEGIN
    IF NOT EXISTS(SELECT 1 FROM review WHERE user_id=@uA AND product_id=@p1)
    BEGIN
        INSERT INTO review(rating,comment,user_id,product_id,created_at,updated_at)
        VALUES(5,N'Áo thun mềm, thấm hút tốt, form chuẩn. Rất đáng tiền!',@uA,@p1,DATEADD(day,-7,GETDATE()),GETDATE());
        DECLARE @rvP1A BIGINT=SCOPE_IDENTITY();
        INSERT INTO review_images(review_id,image_url) VALUES
        (@rvP1A,'https://images.unsplash.com/photo-1523381210434-271e8be1f52b?auto=format&fit=crop&w=900&q=80');
    END
    IF NOT EXISTS(SELECT 1 FROM review WHERE user_id=@uB AND product_id=@p1)
    BEGIN
        INSERT INTO review(rating,comment,user_id,product_id,created_at,updated_at)
        VALUES(4,N'Chất vải ổn, đường may đẹp. Mình sẽ mua thêm size khác.',@uB,@p1,DATEADD(day,-6,GETDATE()),GETDATE());
        DECLARE @rvP1B BIGINT=SCOPE_IDENTITY();
        INSERT INTO review_images(review_id,image_url) VALUES
        (@rvP1B,'https://images.unsplash.com/photo-1512436991641-6745cdb1723f?auto=format&fit=crop&w=900&q=80');
    END
END

-- P2: Áo Thun Unisex Oversize
IF @p2 IS NOT NULL BEGIN
    IF NOT EXISTS(SELECT 1 FROM review WHERE user_id=@uB AND product_id=@p2)
    BEGIN
        INSERT INTO review(rating,comment,user_id,product_id,created_at,updated_at)
        VALUES(5,N'Form oversize đúng ý, mặc thoải mái và không bị phai màu.',@uB,@p2,DATEADD(day,-8,GETDATE()),GETDATE());
        DECLARE @rvP2B BIGINT=SCOPE_IDENTITY();
        INSERT INTO review_images(review_id,image_url) VALUES
        (@rvP2B,'https://images.unsplash.com/photo-1519741497674-611481863552?auto=format&fit=crop&w=900&q=80');
    END
END

-- P3: Quần Jean Nam Slim Fit
IF @p3 IS NOT NULL BEGIN
    IF NOT EXISTS(SELECT 1 FROM review WHERE user_id=@uA AND product_id=@p3)
    BEGIN
        INSERT INTO review(rating,comment,user_id,product_id,created_at,updated_at)
        VALUES(4,N'Jean co giãn vừa đủ, mặc đi làm cả ngày vẫn thoải mái.',@uA,@p3,DATEADD(day,-10,GETDATE()),GETDATE());
        DECLARE @rvP3A BIGINT=SCOPE_IDENTITY();
        INSERT INTO review_images(review_id,image_url) VALUES
        (@rvP3A,'https://images.unsplash.com/photo-1514996937319-344454492b37?auto=format&fit=crop&w=900&q=80');
    END
END

-- P4: Quần Jean Nữ Skinny
IF @p4 IS NOT NULL BEGIN
    IF NOT EXISTS(SELECT 1 FROM review WHERE user_id=@uB AND product_id=@p4)
    BEGIN
        INSERT INTO review(rating,comment,user_id,product_id,created_at,updated_at)
        VALUES(5,N'Form ôm tôn dáng, co giãn tốt. Màu lên rất đẹp!',@uB,@p4,DATEADD(day,-9,GETDATE()),GETDATE());
        DECLARE @rvP4B BIGINT=SCOPE_IDENTITY();
        INSERT INTO review_images(review_id,image_url) VALUES
        (@rvP4B,'https://images.unsplash.com/photo-1503342394124-48075f1a57c1?auto=format&fit=crop&w=900&q=80');
    END
END

-- P5: Áo Sơ Mi Nam Trắng
IF @p5 IS NOT NULL BEGIN
    IF NOT EXISTS(SELECT 1 FROM review WHERE user_id=@uA AND product_id=@p5)
    BEGIN
        INSERT INTO review(rating,comment,user_id,product_id,created_at,updated_at)
        VALUES(4,N'Sơ mi ít nhăn, phù hợp công sở. Size đúng mô tả.',@uA,@p5,DATEADD(day,-12,GETDATE()),GETDATE());
        DECLARE @rvP5A BIGINT=SCOPE_IDENTITY();
        INSERT INTO review_images(review_id,image_url) VALUES
        (@rvP5A,'https://images.unsplash.com/photo-1548883354-aa03f121c571?auto=format&fit=crop&w=900&q=80');
    END
END

-- P6: Đầm Hoa Nữ Midi (liên quan đơn đã giao)
IF @p6 IS NOT NULL BEGIN
    IF NOT EXISTS(SELECT 1 FROM review WHERE user_id=@uB AND product_id=@p6)
    BEGIN
        INSERT INTO review(rating,comment,user_id,product_id,created_at,updated_at)
        VALUES(5,N'Đầm mặc rất xinh, lên form chuẩn. Giao nhanh.',@uB,@p6,DATEADD(day,-5,GETDATE()),GETDATE());
        DECLARE @rvP6B BIGINT=SCOPE_IDENTITY();
        INSERT INTO review_images(review_id,image_url) VALUES
        (@rvP6B,'https://images.unsplash.com/photo-1519741497674-611481863552?auto=format&fit=crop&w=900&q=80');
    END
END

-- P7: Áo Khoác Hoodie Unisex
IF @p7 IS NOT NULL BEGIN
    IF NOT EXISTS(SELECT 1 FROM review WHERE user_id=@uA AND product_id=@p7)
    BEGIN
        INSERT INTO review(rating,comment,user_id,product_id,created_at,updated_at)
        VALUES(5,N'Hoodie ấm, mịn. Màu sắc và form y như hình quảng cáo.',@uA,@p7,DATEADD(day,-11,GETDATE()),GETDATE());
        DECLARE @rvP7A BIGINT=SCOPE_IDENTITY();
        INSERT INTO review_images(review_id,image_url) VALUES
        (@rvP7A,'https://images.unsplash.com/photo-1490481651871-ab68de25d43d?auto=format&fit=crop&w=900&q=80');
    END
END

-- 9) VOUCHERS ---------------------------------------------------------
DECLARE @creator BIGINT=@idAdmin;
IF NOT EXISTS (SELECT 1 FROM voucher WHERE code='WELCOME10')
INSERT INTO voucher(code,name,description,discount_type,discount_value,max_discount,min_order_value,total_quantity,used_quantity,usage_limit,start_date,end_date,active,type,created_by,created_at,updated_at)
VALUES('WELCOME10',N'Mã chào mừng',N'Giảm 10%, tối đa 50K','PERCENTAGE',10,50000,0,100,0,1,CAST(GETDATE() AS DATE),DATEADD(month,3,CAST(GETDATE() AS DATE)),1,'ADMIN',@creator,GETDATE(),GETDATE());

-- 10) FAVORITES -------------------------------------------------------
IF NOT EXISTS (SELECT 1 FROM favorite)
BEGIN
    INSERT INTO favorite(user_id,product_id)
    SELECT @idU1,@p1 UNION ALL SELECT @idU1,@p3 UNION ALL SELECT @idU2,@p6;
END

-- 11) WALLETS ---------------------------------------------------------
IF NOT EXISTS (SELECT 1 FROM wallet WHERE user_id=@idU1) INSERT INTO wallet(user_id,balance,created_at,updated_at) VALUES(@idU1,500000,GETDATE(),GETDATE());
IF NOT EXISTS (SELECT 1 FROM wallet WHERE user_id=@idU2) INSERT INTO wallet(user_id,balance,created_at,updated_at) VALUES(@idU2,800000,GETDATE(),GETDATE());

-- 12) ORDERS + DETAILS ------------------------------------------------
DECLARE @wUser1 BIGINT=@idU1, @wUser2 BIGINT=@idU2;
DECLARE @orderId BIGINT, @voucherId BIGINT=(SELECT id FROM voucher WHERE code='WELCOME10');

-- Order 1 (WALLET, with voucher)
IF NOT EXISTS (SELECT 1 FROM orders WHERE order_code='ORD-10001')
BEGIN
    DECLARE @o1_pv1 BIGINT=(SELECT TOP 1 id FROM product_variant WHERE product_id=@p1 AND size='M');
    DECLARE @o1_pv2 BIGINT=(SELECT TOP 1 id FROM product_variant WHERE product_id=@p3 AND size='32');
    DECLARE @o1_price1 DECIMAL(15,2)=(SELECT price FROM product_variant WHERE id=@o1_pv1);
    DECLARE @o1_price2 DECIMAL(15,2)=(SELECT price FROM product_variant WHERE id=@o1_pv2);
    DECLARE @o1_qty1 INT=2, @o1_qty2 INT=1;
    DECLARE @o1_total DECIMAL(15,2)=(@o1_price1*@o1_qty1)+(@o1_price2*@o1_qty2);
    DECLARE @o1_discount DECIMAL(15,2)=(@o1_total*0.10);
    IF @o1_discount>50000 SET @o1_discount=50000;
    DECLARE @o1_pay DECIMAL(15,2)=@o1_total-@o1_discount;

    INSERT INTO orders(order_code,user_id,shipper_id,voucher_id,voucher_code,discount_amount,total_price,total_amount,cod_amount,status,failure_reason,address,shipping_address,shipping_phone,shipping_name,payment_method,delivered_date,assigned_at,delivered_at,delivery_notes,created_at,updated_at)
    VALUES('ORD-10001',@wUser1,NULL,@voucherId,'WELCOME10',@o1_discount,@o1_total,@o1_pay,NULL,'PENDING',NULL,
           N'123 Nguyễn Trãi, Q.5, TP.HCM',N'123 Nguyễn Trãi, Q.5, TP.HCM','0909000111',N'Nguyễn Văn A','WALLET',NULL,NULL,NULL,NULL,DATEADD(day,-1,GETDATE()),DATEADD(day,-1,GETDATE()));
    SET @orderId=SCOPE_IDENTITY();
    INSERT INTO order_details(order_id,product_id,variant_id,quantity,unit_price,total_price,size_at_order)
    VALUES(@orderId,@p1,@o1_pv1,@o1_qty1,@o1_price1,@o1_price1*@o1_qty1,'M'),
          (@orderId,@p3,@o1_pv2,@o1_qty2,@o1_price2,@o1_price2*@o1_qty2,'32');
END

-- Order 2 (COD, no voucher)
IF NOT EXISTS (SELECT 1 FROM orders WHERE order_code='ORD-10002')
BEGIN
    DECLARE @o2_pv1 BIGINT=(SELECT TOP 1 id FROM product_variant WHERE product_id=@p5 AND size='40');
    DECLARE @o2_pv2 BIGINT=(SELECT TOP 1 id FROM product_variant WHERE product_id=@p2 AND size='XL');
    DECLARE @o2_price1 DECIMAL(15,2)=(SELECT price FROM product_variant WHERE id=@o2_pv1);
    DECLARE @o2_price2 DECIMAL(15,2)=(SELECT price FROM product_variant WHERE id=@o2_pv2);
    DECLARE @o2_qty1 INT=1, @o2_qty2 INT=1;
    DECLARE @o2_total DECIMAL(15,2)=(@o2_price1*@o2_qty1)+(@o2_price2*@o2_qty2);

    INSERT INTO orders(order_code,user_id,shipper_id,voucher_id,voucher_code,discount_amount,total_price,total_amount,cod_amount,status,failure_reason,address,shipping_address,shipping_phone,shipping_name,payment_method,delivered_date,assigned_at,delivered_at,delivery_notes,created_at,updated_at)
    VALUES('ORD-10002',@wUser2,NULL,NULL,NULL,0,@o2_total,@o2_total,@o2_total,'CONFIRMED',NULL,
           N'45 Hai Bà Trưng, Q.1, TP.HCM',N'45 Hai Bà Trưng, Q.1, TP.HCM','0909888777',N'Trần Thị B','COD',NULL,NULL,NULL,NULL,GETDATE(),GETDATE());
    SET @orderId=SCOPE_IDENTITY();
    INSERT INTO order_details(order_id,product_id,variant_id,quantity,unit_price,total_price,size_at_order)
    VALUES(@orderId,@p5,@o2_pv1,@o2_qty1,@o2_price1,@o2_price1*@o2_qty1,'40'),
          (@orderId,@p2,@o2_pv2,@o2_qty2,@o2_price2,@o2_price2*@o2_qty2,'XL');
END

-- Order 3 (DELIVERED)
IF NOT EXISTS (SELECT 1 FROM orders WHERE order_code='ORD-10003')
BEGIN
    DECLARE @o3_pv1 BIGINT=(SELECT TOP 1 id FROM product_variant WHERE product_id=@p6 AND size='M');
    DECLARE @o3_price1 DECIMAL(15,2)=(SELECT price FROM product_variant WHERE id=@o3_pv1);
    DECLARE @o3_qty1 INT=1;
    DECLARE @o3_total DECIMAL(15,2)=(@o3_price1*@o3_qty1);

    INSERT INTO orders(order_code,user_id,shipper_id,voucher_id,voucher_code,discount_amount,total_price,total_amount,cod_amount,status,failure_reason,address,shipping_address,shipping_phone,shipping_name,payment_method,delivered_date,assigned_at,delivered_at,delivery_notes,created_at,updated_at)
    VALUES('ORD-10003',@wUser1,@idSh1,NULL,NULL,0,@o3_total,@o3_total,@o3_total,'DELIVERED',NULL,
           N'123 Nguyễn Trãi, Q.5, TP.HCM',N'123 Nguyễn Trãi, Q.5, TP.HCM','0909000111',N'Nguyễn Văn A','COD',DATEADD(day,-5,GETDATE()),DATEADD(day,-6,GETDATE()),DATEADD(day,-5,GETDATE()),N'Giao nhanh',DATEADD(day,-7,GETDATE()),DATEADD(day,-5,GETDATE()));
    SET @orderId=SCOPE_IDENTITY();
    INSERT INTO order_details(order_id,product_id,variant_id,quantity,unit_price,total_price,size_at_order)
    VALUES(@orderId,@p6,@o3_pv1,@o3_qty1,@o3_price1,@o3_price1*@o3_qty1,'M');
END

-- 13) OPTIONAL: Wallet transactions reflecting Order 1
DECLARE @w1 BIGINT=(SELECT id FROM wallet WHERE user_id=@wUser1);
IF @w1 IS NOT NULL AND NOT EXISTS(SELECT 1 FROM wallet_transaction WHERE wallet_id=@w1 AND description LIKE 'Thanh toán ORD-10001%')
BEGIN
    DECLARE @ord1 BIGINT=(SELECT id FROM orders WHERE order_code='ORD-10001');
    DECLARE @amt DECIMAL(15,2)=(SELECT total_amount FROM orders WHERE id=@ord1);
    DECLARE @balBefore DECIMAL(15,2)=(SELECT balance FROM wallet WHERE id=@w1);
    UPDATE wallet SET balance=balance-@amt, updated_at=GETDATE() WHERE id=@w1;
    DECLARE @balAfter DECIMAL(15,2)=(SELECT balance FROM wallet WHERE id=@w1);
    INSERT INTO wallet_transaction(wallet_id,type,amount,balance_before,balance_after,description,order_id,created_at)
    VALUES(@w1,'PAYMENT',@amt,@balBefore,@balAfter,N'Thanh toán ORD-10001',@ord1,DATEADD(day,-1,GETDATE()));
END

COMMIT TRANSACTION;
GO
