-- =============================================
-- Test password hash verification
-- =============================================

USE ClothesShop;
GO

-- Test 1: Kiểm tra user shipper1
SELECT username, password FROM users WHERE username = 'shipper1';

-- Test 2: Tạo user test với password "123456" 
-- Hash này được tạo bằng BCrypt cho password "123456"
INSERT INTO users (username, password, email, first_name, last_name, phone, enabled, email_verified) 
VALUES ('testuser', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'test@test.com', N'Test', N'User', '0123456789', 1, 1);

-- Test 3: Thêm role cho test user
INSERT INTO user_roles (user_id, role_id) VALUES 
((SELECT id FROM users WHERE username = 'testuser'), 1);

-- Test 4: Kiểm tra tất cả user
SELECT u.username, u.password, r.name as role_name
FROM users u
LEFT JOIN user_roles ur ON u.id = ur.user_id
LEFT JOIN roles r ON ur.role_id = r.id
ORDER BY u.username;

