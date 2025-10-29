-- Insert voucher seed data only
USE clothesshop;
GO

PRINT 'Inserting Vouchers...';

-- Get admin ID
DECLARE @adminId BIGINT = (SELECT TOP 1 id FROM users WHERE username = 'admin');

IF NOT EXISTS (SELECT 1 FROM voucher WHERE code = 'WELCOME10')
    INSERT INTO voucher (code, name, description, discount_type, discount_value, max_discount, min_order_value, total_quantity, used_quantity, usage_limit, start_date, end_date, is_active, type, created_by, created_at, updated_at)
    VALUES ('WELCOME10', N'Chào mừng khách hàng mới', N'Giảm 10% cho đơn hàng đầu tiên', 'PERCENTAGE', 10, 50000, 200000, 100, 0, 1, GETDATE(), DATEADD(month, 3, GETDATE()), 1, 'ADMIN', @adminId, GETDATE(), GETDATE());

IF NOT EXISTS (SELECT 1 FROM voucher WHERE code = 'SUMMER20')
    INSERT INTO voucher (code, name, description, discount_type, discount_value, max_discount, min_order_value, total_quantity, used_quantity, usage_limit, start_date, end_date, is_active, type, created_by, created_at, updated_at)
    VALUES ('SUMMER20', N'Sale Mùa Hè', N'Giảm 20% cho đơn hàng từ 500k', 'PERCENTAGE', 20, 100000, 500000, 50, 0, 2, GETDATE(), DATEADD(month, 2, GETDATE()), 1, 'ADMIN', @adminId, GETDATE(), GETDATE());

IF NOT EXISTS (SELECT 1 FROM voucher WHERE code = 'FLASH50K')
    INSERT INTO voucher (code, name, description, discount_type, discount_value, max_discount, min_order_value, total_quantity, used_quantity, usage_limit, start_date, end_date, is_active, type, created_by, created_at, updated_at)
    VALUES ('FLASH50K', N'Flash Sale 50K', N'Giảm cố định 50K cho mọi đơn hàng', 'FIXED_AMOUNT', 50000, NULL, 300000, 200, 0, 1, GETDATE(), DATEADD(day, 7, GETDATE()), 1, 'ADMIN', @adminId, GETDATE(), GETDATE());

IF NOT EXISTS (SELECT 1 FROM voucher WHERE code = 'VIP15')
    INSERT INTO voucher (code, name, description, discount_type, discount_value, max_discount, min_order_value, total_quantity, used_quantity, usage_limit, start_date, end_date, is_active, type, created_by, created_at, updated_at)
    VALUES ('VIP15', N'Ưu đãi VIP', N'Giảm 15% cho thành viên VIP', 'PERCENTAGE', 15, 200000, 1000000, 30, 0, 5, GETDATE(), DATEADD(month, 6, GETDATE()), 1, 'ADMIN', @adminId, GETDATE(), GETDATE());

PRINT 'Vouchers inserted successfully!';
SELECT * FROM voucher;

GO
