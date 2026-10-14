CREATE TABLE IF NOT EXISTS farmers (
  farmer_id SERIAL PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  phone VARCHAR(20),
  location VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS customers (
  customer_id SERIAL PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  phone VARCHAR(20),
  address VARCHAR(200)
);

CREATE TABLE IF NOT EXISTS products (
  product_id SERIAL PRIMARY KEY,
  farmer_id INT NOT NULL,
  crop_name VARCHAR(80) NOT NULL,
  category VARCHAR(50),
  quantity DOUBLE PRECISION NOT NULL,
  price DOUBLE PRECISION NOT NULL,
  quality VARCHAR(30),
  harvest_date DATE,
  FOREIGN KEY (farmer_id) REFERENCES farmers(farmer_id)
);

CREATE TABLE IF NOT EXISTS orders (
  order_id SERIAL PRIMARY KEY,
  customer_id INT NOT NULL,
  product_id INT NOT NULL,
  quantity DOUBLE PRECISION NOT NULL,
  total_amount DOUBLE PRECISION NOT NULL,
  order_status VARCHAR(20) DEFAULT 'PLACED',
  FOREIGN KEY (customer_id) REFERENCES customers(customer_id),
  FOREIGN KEY (product_id) REFERENCES products(product_id)
);

CREATE TABLE IF NOT EXISTS market_prices (
  price_id SERIAL PRIMARY KEY,
  crop_name VARCHAR(80) NOT NULL,
  location VARCHAR(100),
  price_date DATE NOT NULL,
  market_price DOUBLE PRECISION NOT NULL
);

CREATE TABLE IF NOT EXISTS users (
  user_id SERIAL PRIMARY KEY,
  email VARCHAR(150) NOT NULL UNIQUE,
  password_hash VARCHAR(100) NOT NULL,
  user_role VARCHAR(20) NOT NULL,
  profile_id INT DEFAULT 0,
  name VARCHAR(100)
);
