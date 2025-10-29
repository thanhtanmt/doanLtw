-- =============================================
-- XÓA TOÀN BỘ CÁC BẢNG (CẢ CẤU TRÚC)
-- SQL Server (T-SQL)
-- Cẩn thận: Script này sẽ xóa hoàn toàn các bảng!
-- =============================================

USE ClothesShop;
GO

SET NOCOUNT ON;

PRINT '========================================';
PRINT 'WARNING: DROPPING ALL TABLES!';
PRINT 'This will remove table structure!';
PRINT '========================================';

-- Tắt kiểm tra Foreign Key
EXEC sp_MSforeachtable 'ALTER TABLE ? NOCHECK CONSTRAINT ALL';

-- Drop các bảng theo thứ tự (từ bảng con đến bảng cha)
PRINT 'Dropping tables...';

-- 1. Drop Cart Items
IF OBJECT_ID('dbo.cart_item', 'U') IS NOT NULL
BEGIN
    DROP TABLE cart_item;
    PRINT '- Dropped cart_item';
END

-- 2. Drop Order Items
IF OBJECT_ID('dbo.order_item', 'U') IS NOT NULL
BEGIN
    DROP TABLE order_item;
    PRINT '- Dropped order_item';
END

-- 3. Drop Order Details
IF OBJECT_ID('dbo.order_details', 'U') IS NOT NULL
BEGIN
    DROP TABLE order_details;
    PRINT '- Dropped order_details';
END

-- 4. Drop Orders
IF OBJECT_ID('dbo.orders', 'U') IS NOT NULL
BEGIN
    DROP TABLE orders;
    PRINT '- Dropped orders';
END

IF OBJECT_ID('dbo.order', 'U') IS NOT NULL
BEGIN
    DROP TABLE [order];
    PRINT '- Dropped order';
END

-- 5. Drop Reviews
IF OBJECT_ID('dbo.review', 'U') IS NOT NULL
BEGIN
    DROP TABLE review;
    PRINT '- Dropped review';
END

-- 6. Drop Favorites
IF OBJECT_ID('dbo.favorite', 'U') IS NOT NULL
BEGIN
    DROP TABLE favorite;
    PRINT '- Dropped favorite';
END

-- 7. Drop Product Images
IF OBJECT_ID('dbo.product_image', 'U') IS NOT NULL
BEGIN
    DROP TABLE product_image;
    PRINT '- Dropped product_image';
END

-- 8. Drop Product Variants
IF OBJECT_ID('dbo.product_variant', 'U') IS NOT NULL
BEGIN
    DROP TABLE product_variant;
    PRINT '- Dropped product_variant';
END

-- 9. Drop Product Voucher
IF OBJECT_ID('dbo.product_voucher', 'U') IS NOT NULL
BEGIN
    DROP TABLE product_voucher;
    PRINT '- Dropped product_voucher';
END

-- 10. Drop Products
IF OBJECT_ID('dbo.product', 'U') IS NOT NULL
BEGIN
    DROP TABLE product;
    PRINT '- Dropped product';
END

-- 11. Drop Vouchers
IF OBJECT_ID('dbo.voucher', 'U') IS NOT NULL
BEGIN
    DROP TABLE voucher;
    PRINT '- Dropped voucher';
END

-- 12. Drop Categories
IF OBJECT_ID('dbo.category', 'U') IS NOT NULL
BEGIN
    DROP TABLE category;
    PRINT '- Dropped category';
END

-- 13. Drop Carts
IF OBJECT_ID('dbo.cart', 'U') IS NOT NULL
BEGIN
    DROP TABLE cart;
    PRINT '- Dropped cart';
END

-- 14. Drop User Roles
IF OBJECT_ID('dbo.user_roles', 'U') IS NOT NULL
BEGIN
    DROP TABLE user_roles;
    PRINT '- Dropped user_roles';
END

-- 15. Drop Users
IF OBJECT_ID('dbo.users', 'U') IS NOT NULL
BEGIN
    DROP TABLE users;
    PRINT '- Dropped users';
END

-- 16. Drop Roles
IF OBJECT_ID('dbo.roles', 'U') IS NOT NULL
BEGIN
    DROP TABLE roles;
    PRINT '- Dropped roles';
END

PRINT '';
PRINT '========================================';
PRINT 'ALL TABLES DROPPED SUCCESSFULLY!';
PRINT 'Database is now empty.';
PRINT 'Run your JPA application to recreate tables.';
PRINT '========================================';
GO
