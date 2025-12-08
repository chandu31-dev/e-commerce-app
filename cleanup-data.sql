-- Delete all data cleanup script
-- This will delete all users, vendors, and products along with their related data

-- Disable foreign key checks temporarily
SET FOREIGN_KEY_CHECKS = 0;

-- Delete from all tables (order matters due to foreign keys)
DELETE FROM messages;
DELETE FROM payments;
DELETE FROM payouts;
DELETE FROM subscriptions;
DELETE FROM coupons;
DELETE FROM reviews;
DELETE FROM shipments;
DELETE FROM wishlist_items;
DELETE FROM addresses;
DELETE FROM order_items;
DELETE FROM orders;
DELETE FROM cart_items;
DELETE FROM vendor_products;
DELETE FROM vendors;
DELETE FROM password_reset_tokens;
DELETE FROM verification_tokens;
DELETE FROM products;
DELETE FROM users;

-- Reset auto-increment counters to 1
ALTER TABLE users AUTO_INCREMENT = 1;
ALTER TABLE products AUTO_INCREMENT = 1;
ALTER TABLE vendors AUTO_INCREMENT = 1;
ALTER TABLE orders AUTO_INCREMENT = 1;
ALTER TABLE order_items AUTO_INCREMENT = 1;
ALTER TABLE cart_items AUTO_INCREMENT = 1;
ALTER TABLE addresses AUTO_INCREMENT = 1;
ALTER TABLE wishlist_items AUTO_INCREMENT = 1;
ALTER TABLE reviews AUTO_INCREMENT = 1;
ALTER TABLE coupons AUTO_INCREMENT = 1;
ALTER TABLE subscriptions AUTO_INCREMENT = 1;
ALTER TABLE payouts AUTO_INCREMENT = 1;
ALTER TABLE messages AUTO_INCREMENT = 1;
ALTER TABLE payments AUTO_INCREMENT = 1;
ALTER TABLE shipments AUTO_INCREMENT = 1;
ALTER TABLE verification_tokens AUTO_INCREMENT = 1;
ALTER TABLE password_reset_tokens AUTO_INCREMENT = 1;
ALTER TABLE vendor_products AUTO_INCREMENT = 1;

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

-- Confirm deletion
SELECT 'All data deleted successfully' AS status;
