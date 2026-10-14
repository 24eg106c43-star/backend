package com.farmerdirect.controller;
import com.farmerdirect.model.*;
import com.farmerdirect.service.PriceService;
import java.sql.Statement;
import java.util.*;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api", "", "/api/api"})
public class ApiController {
    private final JdbcTemplate db;
    private final PriceService prices;
    public ApiController(JdbcTemplate db, PriceService prices) { this.db = db; this.prices = prices; }

    private static Object getCol(Map<String, Object> row, String col) {
        if (row == null) return null;
        if (row.containsKey(col)) return row.get(col);
        if (row.containsKey(col.toUpperCase())) return row.get(col.toUpperCase());
        if (row.containsKey(col.toLowerCase())) return row.get(col.toLowerCase());
        for (Map.Entry<String, Object> e : row.entrySet()) {
            if (e.getKey().equalsIgnoreCase(col)) return e.getValue();
        }
        return null;
    }

    private long insert(String sql, Object... args) {
        GeneratedKeyHolder kh = new GeneratedKeyHolder();
        db.update(c -> {
            var ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < args.length; i++) ps.setObject(i + 1, args[i]);
            return ps;
        }, kh);
        try {
            if (kh.getKey() != null) return kh.getKey().longValue();
        } catch (Exception ignored) { }
        if (kh.getKeys() != null) {
            for (Map.Entry<String, Object> e : kh.getKeys().entrySet()) {
                if (e.getKey().toLowerCase().endsWith("id") && e.getValue() instanceof Number n) {
                    return n.longValue();
                }
            }
            for (Object v : kh.getKeys().values()) {
                if (v instanceof Number n) return n.longValue();
            }
        }
        throw new IllegalStateException("Could not retrieve generated key");
    }
    private int count(String sql, Object... a) { return db.queryForObject(sql, Integer.class, a); }
    private static double num(Object o) { return Double.parseDouble(String.valueOf(o)); }

    @PostMapping("/farmers")
    public Map<String, Object> addFarmer(@RequestBody Map<String, String> b) {
        return Map.of("id", insert("INSERT INTO farmers(name,phone,location) VALUES(?,?,?)", b.get("name"), b.get("phone"), b.get("location")));
    }
    @PostMapping("/customers")
    public Map<String, Object> addCustomer(@RequestBody Map<String, String> b) {
        return Map.of("id", insert("INSERT INTO customers(name,phone,address) VALUES(?,?,?)", b.get("name"), b.get("phone"), b.get("address")));
    }
    @PostMapping("/products")
    public Map<String, Object> addProduct(@RequestBody Map<String, Object> b) {
        long id = insert("INSERT INTO products(farmer_id,crop_name,category,quantity,price,quality,harvest_date) VALUES(?,?,?,?,?,?,?)",
            (int) num(b.get("farmerId")), b.get("cropName"), b.get("category"), num(b.get("quantity")), num(b.get("price")),
            b.get("quality"), java.sql.Date.valueOf(String.valueOf(b.get("harvestDate"))));
        return Map.of("id", id);
    }
    @GetMapping("/products")
    public List<Map<String, Object>> products(@RequestParam(defaultValue = "") String q) {
        return db.queryForList("SELECT p.*, f.name AS farmer_name, f.location FROM products p JOIN farmers f ON f.farmer_id=p.farmer_id " +
            "WHERE p.quantity>0 AND (LOWER(p.crop_name) LIKE ? OR LOWER(p.category) LIKE ?) ORDER BY p.crop_name",
            "%" + q.toLowerCase() + "%", "%" + q.toLowerCase() + "%");
    }
    @GetMapping("/price-suggestion")
    public Map<String, Object> suggest(@RequestParam String crop) {
        Double p = prices.recommend(crop);
        Map<String, Object> m = new HashMap<>(); m.put("crop", crop); m.put("recommendedPrice", p); return m;
    }
    @PostMapping("/orders") @Transactional
    public ResponseEntity<?> order(@RequestBody Map<String, Object> b) {
        int pid = (int) num(b.get("productId")), cid = (int) num(b.get("customerId")); double qty = num(b.get("quantity"));
        List<Map<String, Object>> rows = db.queryForList("SELECT quantity, price FROM products WHERE product_id=?", pid);
        if (rows.isEmpty()) return ResponseEntity.badRequest().body(Map.of("error", "Product not found"));
        if (count("SELECT COUNT(*) FROM customers WHERE customer_id=?", cid) == 0) return ResponseEntity.badRequest().body(Map.of("error", "Unknown customer id"));
        double stock = num(getCol(rows.get(0), "quantity")), price = num(getCol(rows.get(0), "price"));
        if (qty <= 0 || qty > stock) return ResponseEntity.badRequest().body(Map.of("error", "Only " + stock + " available"));
        double total = qty * price;
        long id = insert("INSERT INTO orders(customer_id,product_id,quantity,total_amount,order_status) VALUES(?,?,?,?, 'PLACED')", cid, pid, qty, total);
        db.update("UPDATE products SET quantity=quantity-? WHERE product_id=?", qty, pid);
        return ResponseEntity.ok(Map.of("orderId", id, "total", total));
    }
    @GetMapping("/orders")
    public List<Map<String, Object>> orders(@RequestParam(required = false) Integer customerId, @RequestParam(required = false) Integer farmerId) {
        String sql = "SELECT o.*, p.crop_name, p.farmer_id FROM orders o JOIN products p ON p.product_id=o.product_id WHERE 1=1";
        List<Object> a = new ArrayList<>();
        if (customerId != null) { sql += " AND o.customer_id=?"; a.add(customerId); }
        if (farmerId != null) { sql += " AND p.farmer_id=?"; a.add(farmerId); }
        return db.queryForList(sql + " ORDER BY o.order_id DESC", a.toArray());
    }
    @PutMapping("/orders/{id}/status")
    public Map<String, Object> status(@PathVariable int id, @RequestParam String status) {
        db.update("UPDATE orders SET order_status=? WHERE order_id=?", status.toUpperCase(), id);
        return Map.of("ok", true);
    }
    @GetMapping("/dashboard/{role}/{id}")
    public Map<String, String> dashboard(@PathVariable String role, @PathVariable int id) {
        User u;
        switch (role) {
            case "farmer" -> u = new Farmer(id, db.queryForObject("SELECT name FROM farmers WHERE farmer_id=?", String.class, id),
                count("SELECT COUNT(*) FROM products WHERE farmer_id=?", id),
                count("SELECT COUNT(*) FROM orders o JOIN products p ON p.product_id=o.product_id WHERE p.farmer_id=?", id));
            case "customer" -> u = new Customer(id, db.queryForObject("SELECT name FROM customers WHERE customer_id=?", String.class, id),
                count("SELECT COUNT(*) FROM orders WHERE customer_id=?", id));
            default -> u = new Admin(count("SELECT COUNT(*) FROM farmers"), count("SELECT COUNT(*) FROM customers"),
                count("SELECT COUNT(*) FROM products"), count("SELECT COUNT(*) FROM orders"));
        }
        return Map.of("dashboard", u.dashboard());   // polymorphic call
    }
}
