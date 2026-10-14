INSERT INTO farmers(name,phone,location) VALUES ('Ramesh','9876500001','Kurnool');
INSERT INTO customers(name,phone,address) VALUES ('Anita','9876500002','Hyderabad');
INSERT INTO products(farmer_id,crop_name,category,quantity,price,quality,harvest_date) VALUES
 (1,'Tomato','Vegetable',200,24,'A',CURRENT_DATE),(1,'Onion','Vegetable',500,30,'B',CURRENT_DATE),(1,'Groundnut','Oilseed',300,70,'A',CURRENT_DATE);
INSERT INTO market_prices(crop_name,location,price_date,market_price) VALUES
 ('Tomato','Kurnool','2026-04-01',18),('Tomato','Kurnool','2026-05-01',20),('Tomato','Kurnool','2026-06-01',22),('Tomato','Kurnool','2026-07-01',23),('Tomato','Kurnool','2026-08-01',26),
 ('Onion','Kurnool','2026-04-01',26),('Onion','Kurnool','2026-05-01',28),('Onion','Kurnool','2026-06-01',27),('Onion','Kurnool','2026-07-01',31),('Onion','Kurnool','2026-08-01',33),
 ('Groundnut','Kurnool','2026-04-01',64),('Groundnut','Kurnool','2026-05-01',66),('Groundnut','Kurnool','2026-06-01',65),('Groundnut','Kurnool','2026-07-01',68),('Groundnut','Kurnool','2026-08-01',71);
