-- Initial schema migration for Catchy

CREATE TABLE IF NOT EXISTS users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  email VARCHAR(255) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  role VARCHAR(50) NOT NULL,
  verified BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS products (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  description VARCHAR(1000),
  category VARCHAR(255) NOT NULL,
  price DECIMAL(10,2) NOT NULL,
  image_url VARCHAR(255),
  stock INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS orders (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  order_date TIMESTAMP NOT NULL,
  total_price DECIMAL(10,2) NOT NULL,
  discount_amount DECIMAL(10,2),
  coupon_code VARCHAR(255),
  user_id BIGINT NOT NULL,
  status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
  CONSTRAINT FK_orders_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS order_items (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  price DECIMAL(10,2) NOT NULL,
  quantity INTEGER NOT NULL,
  order_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  CONSTRAINT FK_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id),
  CONSTRAINT FK_order_items_product FOREIGN KEY (product_id) REFERENCES products(id)
);

CREATE TABLE IF NOT EXISTS cart_items (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  quantity INTEGER NOT NULL,
  product_id BIGINT NOT NULL,
  user_id BIGINT,
  session_id VARCHAR(255),
  CONSTRAINT FK_cart_items_product FOREIGN KEY (product_id) REFERENCES products(id),
  CONSTRAINT FK_cart_items_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS verification_tokens (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  token VARCHAR(255) NOT NULL UNIQUE,
  user_id BIGINT NOT NULL,
  expiry_date TIMESTAMP NOT NULL,
  CONSTRAINT FK_ver_tokens_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS password_reset_tokens (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  token VARCHAR(255) NOT NULL UNIQUE,
  user_id BIGINT NOT NULL,
  expiry_date TIMESTAMP NOT NULL,
  CONSTRAINT FK_reset_tokens_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Payment ledger (internal)
CREATE TABLE IF NOT EXISTS payment_ledger (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  order_id BIGINT,
  user_id BIGINT,
  amount DECIMAL(10,2),
  currency VARCHAR(10),
  method VARCHAR(50),
  status VARCHAR(50),
  provider_reference VARCHAR(255),
  provider_payload TEXT,
  notes VARCHAR(1000),
  created_at TIMESTAMP,
  updated_at TIMESTAMP
);

-- Payment audit entries
CREATE TABLE IF NOT EXISTS payment_audit (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  payment_id BIGINT NOT NULL,
  action VARCHAR(255),
  actor VARCHAR(255),
  notes TEXT,
  created_at TIMESTAMP
);
