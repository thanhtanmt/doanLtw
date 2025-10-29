-- Migration script to update Voucher table schema
USE clothesshop;
GO

PRINT 'Starting Voucher table migration...';

-- Drop FK constraint from orders table
IF EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FKrx5vk9ur428660yp19hw98nr2')
BEGIN
    PRINT 'Dropping FK constraint from orders table...';
    ALTER TABLE orders DROP CONSTRAINT FKrx5vk9ur428660yp19hw98nr2;
END

-- Drop junction tables
IF EXISTS (SELECT * FROM sys.tables WHERE name = 'product_voucher')
BEGIN
    PRINT 'Dropping product_voucher junction table...';
    DROP TABLE product_voucher;
END

IF EXISTS (SELECT * FROM sys.tables WHERE name = 'voucher_product')
BEGIN
    PRINT 'Dropping voucher_product junction table...';
    DROP TABLE voucher_product;
END

-- Now drop the voucher table
IF EXISTS (SELECT * FROM sys.tables WHERE name = 'voucher')
BEGIN
    PRINT 'Dropping old voucher table...';
    DROP TABLE voucher;
    PRINT 'Old voucher table dropped.';
END

-- Let Hibernate create the new table structure and constraints
PRINT 'New table will be created by Hibernate on next startup.';
PRINT 'Run the seed_data_with_variants.sql voucher section after startup.';

GO
