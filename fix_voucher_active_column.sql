-- Fix the active column in voucher table
-- This script adds the active column with a default value to allow adding it to a non-empty table

-- Check if the column already exists, if not add it
IF NOT EXISTS (SELECT * FROM sys.columns 
               WHERE object_id = OBJECT_ID(N'[dbo].[voucher]') 
               AND name = 'active')
BEGIN
    -- Add the column with a default value
    ALTER TABLE voucher ADD active bit NOT NULL DEFAULT 1;
    
    -- Optionally, you can update specific records after adding the column
    -- UPDATE voucher SET active = 1 WHERE <some condition>;
    
    PRINT 'Column active added successfully to voucher table';
END
ELSE
BEGIN
    PRINT 'Column active already exists in voucher table';
END
