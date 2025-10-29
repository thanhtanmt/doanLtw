-- =============================================
-- CREATE WALLET TABLES FOR PAYMENT SYSTEM
-- =============================================

-- Kiểm tra và tạo bảng wallet
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'wallet')
BEGIN
    CREATE TABLE wallet (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        user_id BIGINT NOT NULL UNIQUE,
        balance DECIMAL(15,2) NOT NULL DEFAULT 0,
        created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
        updated_at DATETIME2 NOT NULL DEFAULT GETDATE(),
        CONSTRAINT FK_wallet_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
    );
    
    CREATE INDEX idx_wallet_user_id ON wallet(user_id);
    
    PRINT 'Bảng wallet đã được tạo thành công';
END
ELSE
BEGIN
    PRINT 'Bảng wallet đã tồn tại';
END
GO

-- Kiểm tra và tạo bảng wallet_transaction
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'wallet_transaction')
BEGIN
    CREATE TABLE wallet_transaction (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        wallet_id BIGINT NOT NULL,
        type NVARCHAR(20) NOT NULL CHECK (type IN ('DEPOSIT', 'PAYMENT', 'REFUND')),
        amount DECIMAL(15,2) NOT NULL,
        balance_before DECIMAL(15,2) NOT NULL,
        balance_after DECIMAL(15,2) NOT NULL,
        description NVARCHAR(500),
        order_id BIGINT,
        created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
        CONSTRAINT FK_wallet_transaction_wallet FOREIGN KEY (wallet_id) REFERENCES wallet(id) ON DELETE CASCADE,
        CONSTRAINT FK_wallet_transaction_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE SET NULL
    );
    
    CREATE INDEX idx_wallet_transaction_wallet_id ON wallet_transaction(wallet_id);
    CREATE INDEX idx_wallet_transaction_created_at ON wallet_transaction(created_at);
    CREATE INDEX idx_wallet_transaction_order_id ON wallet_transaction(order_id);
    
    PRINT 'Bảng wallet_transaction đã được tạo thành công';
END
ELSE
BEGIN
    PRINT 'Bảng wallet_transaction đã tồn tại';
END
GO

-- Tạo ví mặc định cho tất cả users hiện có với số dư 0
INSERT INTO wallet (user_id, balance, created_at, updated_at)
SELECT u.id, 0, GETDATE(), GETDATE()
FROM users u
WHERE NOT EXISTS (SELECT 1 FROM wallet w WHERE w.user_id = u.id);

PRINT 'Đã tạo ví mặc định cho tất cả users';
GO

-- Cập nhật một số tài khoản test với số dư để demo
UPDATE wallet
SET balance = 5000000
WHERE user_id IN (
    SELECT id FROM users WHERE username IN ('nguyenvana', 'tranthib', 'customer1', 'customer2', 'customer3')
);

PRINT 'Đã cập nhật số dư demo cho một số tài khoản test';
GO
