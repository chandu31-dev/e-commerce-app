-- Schema initialization: only create tables if they don't exist so a file-based H2
-- database retains data across restarts. Avoid DROP TABLE on startup.

-- Users
CREATE TABLE IF NOT EXISTS users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  email VARCHAR(255) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  role VARCHAR(50) NOT NULL,
  verified BOOLEAN NOT NULL DEFAULT FALSE,
  enabled BOOLEAN NOT NULL DEFAULT FALSE
);

-- Products
CREATE TABLE IF NOT EXISTS products (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  description VARCHAR(1000),
  category VARCHAR(255) NOT NULL,
  price DECIMAL(10,2) NOT NULL,
  image_url VARCHAR(255),
  stock INTEGER NOT NULL
);

-- Orders
CREATE TABLE IF NOT EXISTS orders (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  order_date TIMESTAMP NOT NULL,
  total_price DECIMAL(10,2) NOT NULL,
  user_id BIGINT NOT NULL,
  delivery_address VARCHAR(1000),
  delivery_latitude DOUBLE,
  delivery_longitude DOUBLE,
  delivery_phone VARCHAR(50),
  status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
  CONSTRAINT FK_orders_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Order items
CREATE TABLE IF NOT EXISTS order_items (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  price DECIMAL(10,2) NOT NULL,
  quantity INTEGER NOT NULL,
  order_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  CONSTRAINT FK_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id),
  CONSTRAINT FK_order_items_product FOREIGN KEY (product_id) REFERENCES products(id)
);

-- Cart items
CREATE TABLE IF NOT EXISTS cart_items (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  quantity INTEGER NOT NULL,
  product_id BIGINT NOT NULL,
  user_id BIGINT,
  session_id VARCHAR(255),
  CONSTRAINT FK_cart_items_product FOREIGN KEY (product_id) REFERENCES products(id),
  CONSTRAINT FK_cart_items_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Verification tokens
CREATE TABLE IF NOT EXISTS verification_tokens (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  token VARCHAR(255) NOT NULL UNIQUE,
  user_id BIGINT NOT NULL,
  expiry_date TIMESTAMP NOT NULL,
  CONSTRAINT FK_ver_tokens_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Password reset tokens
CREATE TABLE IF NOT EXISTS password_reset_tokens (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  token VARCHAR(255) NOT NULL UNIQUE,
  user_id BIGINT NOT NULL,
  expiry_date TIMESTAMP NOT NULL,
  CONSTRAINT FK_reset_tokens_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Vendors (for vendor POV features)
CREATE TABLE IF NOT EXISTS vendors (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL UNIQUE,
  shop_name VARCHAR(255) NOT NULL UNIQUE,
  description VARCHAR(1000),
  banner VARCHAR(500),
  logo VARCHAR(500),
  contact_email VARCHAR(255) NOT NULL,
  phone_number VARCHAR(50),
  address VARCHAR(500),
  approved BOOLEAN NOT NULL DEFAULT FALSE,
  total_sales DECIMAL(18,2) NOT NULL DEFAULT 0,
  total_orders INTEGER NOT NULL DEFAULT 0,
  rating DECIMAL(5,2) NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  CONSTRAINT FK_vendors_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Vendor products linking vendors to base products
CREATE TABLE IF NOT EXISTS vendor_products (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  vendor_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  vendor_price DECIMAL(10,2) NOT NULL,
  stock INTEGER NOT NULL DEFAULT 0,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  CONSTRAINT FK_vendor_products_vendor FOREIGN KEY (vendor_id) REFERENCES vendors(id),
  CONSTRAINT FK_vendor_products_product FOREIGN KEY (product_id) REFERENCES products(id)
);

-- User saved addresses
CREATE TABLE IF NOT EXISTS addresses (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  label VARCHAR(255),
  address VARCHAR(1000) NOT NULL,
  latitude DOUBLE,
  longitude DOUBLE,
  phone VARCHAR(50),
  is_default BOOLEAN NOT NULL DEFAULT FALSE,
  CONSTRAINT FK_addresses_user FOREIGN KEY (user_id) REFERENCES users(id)
);
-- Wishlist items
CREATE TABLE IF NOT EXISTS wishlist_items (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  added_at TIMESTAMP NOT NULL,
  CONSTRAINT FK_wishlist_items_user FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT FK_wishlist_items_product FOREIGN KEY (product_id) REFERENCES products(id)
);

-- Shipments
CREATE TABLE IF NOT EXISTS shipments (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  order_id BIGINT NOT NULL,
  courier VARCHAR(255),
  tracking_number VARCHAR(255),
  status VARCHAR(50),
  shipped_at TIMESTAMP,
  CONSTRAINT FK_shipments_order FOREIGN KEY (order_id) REFERENCES orders(id)
);

-- Reviews
CREATE TABLE IF NOT EXISTS reviews (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  rating INT,
  comment VARCHAR(1000),
  created_at TIMESTAMP NOT NULL,
  verified_purchase BOOLEAN NOT NULL DEFAULT FALSE,
  moderation_status VARCHAR(50),
  moderated_at TIMESTAMP,
  moderation_note VARCHAR(500),
  CONSTRAINT FK_reviews_user FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT FK_reviews_product FOREIGN KEY (product_id) REFERENCES products(id)
);

-- Coupons/Promotions
CREATE TABLE IF NOT EXISTS coupons (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  code VARCHAR(100) NOT NULL UNIQUE,
  discount_percent DECIMAL(5,2),
  fixed_amount DECIMAL(10,2),
  max_uses INT,
  used_count INT DEFAULT 0,
  per_user_limit INT,
  min_order_amount DECIMAL(10,2),
  stackable BOOLEAN DEFAULT FALSE,
  valid_from TIMESTAMP,
  valid_until TIMESTAMP,
  CONSTRAINT UK_coupons_code UNIQUE (code)
);

-- Subscription plans
CREATE TABLE IF NOT EXISTS subscriptions (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  plan_type VARCHAR(100),
  status VARCHAR(50),
  started_at TIMESTAMP,
  next_billing_date TIMESTAMP,
  amount DECIMAL(10,2),
  CONSTRAINT FK_subscriptions_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Payouts
CREATE TABLE IF NOT EXISTS payouts (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  vendor_id BIGINT NOT NULL,
  amount DECIMAL(18,2),
  status VARCHAR(50),
  created_at TIMESTAMP,
  CONSTRAINT FK_payouts_vendor FOREIGN KEY (vendor_id) REFERENCES vendors(id)
);

-- Messages
CREATE TABLE IF NOT EXISTS messages (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  sender_id BIGINT NOT NULL,
  receiver_id BIGINT NOT NULL,
  content VARCHAR(1000),
  created_at TIMESTAMP,
  CONSTRAINT FK_messages_sender FOREIGN KEY (sender_id) REFERENCES users(id),
  CONSTRAINT FK_messages_receiver FOREIGN KEY (receiver_id) REFERENCES users(id)
);

-- Payments
CREATE TABLE IF NOT EXISTS payments (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  order_id BIGINT NOT NULL,
  amount DECIMAL(10,2),
  status VARCHAR(50),
  transaction_id VARCHAR(255),
  CONSTRAINT FK_payments_order FOREIGN KEY (order_id) REFERENCES orders(id)
);