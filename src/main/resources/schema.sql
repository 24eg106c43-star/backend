CREATE TABLE IF NOT EXISTS farmers (
  farmer_id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL, phone VARCHAR(20), location VARCHAR(100));
CREATE TABLE IF NOT EXISTS customers (
  customer_id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL, phone VARCHAR(20), address VARCHAR(200));
CREATE TABLE IF NOT EXISTS products (
  product_id INT AUTO_INCREMENT PRIMARY KEY,
  farmer_id INT NOT NULL, crop_name VARCHAR(80) NOT NULL, category VARCHAR(50),
  quantity DOUBLE NOT NULL, price DOUBLE NOT NULL, quality VARCHAR(30), harvest_date DATE,
  FOREIGN KEY (farmer_id) REFERENCES farmers(farmer_id));
CREATE TABLE IF NOT EXISTS orders (
  order_id INT AUTO_INCREMENT PRIMARY KEY,
  customer_id INT NOT NULL, product_id INT NOT NULL, quantity DOUBLE NOT NULL,
  total_amount DOUBLE NOT NULL, order_status VARCHAR(20) DEFAULT 'PLACED',
  FOREIGN KEY (customer_id) REFERENCES customers(customer_id),
  FOREIGN KEY (product_id) REFERENCES products(product_id));
CREATE TABLE IF NOT EXISTS market_prices (
  price_id INT AUTO_INCREMENT PRIMARY KEY,
  crop_name VARCHAR(80) NOT NULL, location VARCHAR(100), price_date DATE NOT NULL, market_price DOUBLE NOT NULL);
