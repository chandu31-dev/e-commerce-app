-- Pre-create users table so Hibernate can add FKs reliably in H2

-- Drop if exists (safe for repeated runs)
DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS cart_items;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS users;

-- Users
CREATE TABLE users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  email VARCHAR(255) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  role VARCHAR(50) NOT NULL
);

-- Products
CREATE TABLE products (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  description VARCHAR(1000),
  category VARCHAR(255) NOT NULL,
  price DECIMAL(10,2) NOT NULL,
  image_url VARCHAR(255),
  stock INTEGER NOT NULL
);

-- Orders
CREATE TABLE orders (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  order_date TIMESTAMP NOT NULL,
  total_price DECIMAL(10,2) NOT NULL,
  user_id BIGINT NOT NULL,
  CONSTRAINT FK_orders_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Order items
CREATE TABLE order_items (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  price DECIMAL(10,2) NOT NULL,
  quantity INTEGER NOT NULL,
  order_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  CONSTRAINT FK_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id),
  CONSTRAINT FK_order_items_product FOREIGN KEY (product_id) REFERENCES products(id)
);

-- Cart items
CREATE TABLE cart_items (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  quantity INTEGER NOT NULL,
  product_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  CONSTRAINT FK_cart_items_product FOREIGN KEY (product_id) REFERENCES products(id),
  CONSTRAINT FK_cart_items_user FOREIGN KEY (user_id) REFERENCES users(id)
);
