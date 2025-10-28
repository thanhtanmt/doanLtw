-- =============================================
-- Script cập nhật password hash cho tất cả user
-- Password: 123456
-- BCrypt Hash: $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi
-- =============================================

USE ClothesShop;
GO

-- Cập nhật password cho tất cả user thành "123456"
UPDATE users SET password = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi' 
WHERE username IN ('admin', 'seller1', 'shipper1', 'shipper2', 'customer1', 'customer2', 'customer3');

-- Kiểm tra kết quả
SELECT username, password FROM users;

