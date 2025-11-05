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
