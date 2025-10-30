-- Add address column to users table
-- Run this SQL script to add the address field to existing users table

USE clothesshop;

-- Add address column if it doesn't exist
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'users') AND name = 'address')
BEGIN
    ALTER TABLE users ADD address NVARCHAR(500) NULL;
    PRINT 'Address column added successfully';
END
ELSE
BEGIN
    PRINT 'Address column already exists';
END

GO
