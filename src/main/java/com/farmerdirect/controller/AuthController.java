package com.farmerdirect.controller;
import com.farmerdirect.security.JwtService;
import java.sql.Statement;
import java.util.*;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final JdbcTemplate db;
    private final PasswordEncoder enc;
    private final JwtService jwt;
    public AuthController(JdbcTemplate db, PasswordEncoder enc, JwtService jwt) { this.db = db; this.enc = enc; this.jwt = jwt; }

    private long insert(String sql, Object... args) {
        GeneratedKeyHolder kh = new GeneratedKeyHolder();
        db.update(c -> {
            var ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < args.length; i++) ps.setObject(i + 1, args[i]);
            return ps;
        }, kh);
        return kh.getKey().longValue();
    }
    private Map<String, Object> session(String email, String role, long pid, String name) {
        return Map.of("token", jwt.generate(email, role, pid), "role", role, "profileId", pid, "name", name == null ? "" : name);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> b) {
        String email = b.getOrDefault("email", "").trim().toLowerCase(), pw = b.getOrDefault("password", "");
        String name = b.getOrDefault("name", "").trim(), role = b.getOrDefault("role", "customer").toLowerCase();
        if (email.isEmpty() || pw.length() < 4 || name.isEmpty())
            return ResponseEntity.badRequest().body(Map.of("error", "Name, email and a password (min 4 chars) are required"));
        if (db.queryForObject("SELECT COUNT(*) FROM users WHERE email=?", Integer.class, email) > 0)
            return ResponseEntity.status(409).body(Map.of("error", "Email already registered"));
        long pid;
        if (role.equals("farmer")) pid = insert("INSERT INTO farmers(name,phone,location) VALUES(?,?,?)", name, b.get("phone"), b.get("location"));
        else if (role.equals("customer")) pid = insert("INSERT INTO customers(name,phone,address) VALUES(?,?,?)", name, b.get("phone"), b.get("address"));
        else return ResponseEntity.badRequest().body(Map.of("error", "Role must be farmer or customer"));
        db.update("INSERT INTO users(email,password_hash,user_role,profile_id,name) VALUES(?,?,?,?,?)", email, enc.encode(pw), role, pid, name);
        return ResponseEntity.ok(session(email, role, pid, name));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> b) {
        List<Map<String, Object>> rows = db.queryForList("SELECT * FROM users WHERE email=?", b.getOrDefault("email", "").trim().toLowerCase());
        String pw = b.getOrDefault("password", "");
        if (rows.isEmpty() || !enc.matches(pw, (String) rows.get(0).get("PASSWORD_HASH")))
            return ResponseEntity.status(401).body(Map.of("error", "Invalid email or password"));
        Map<String, Object> u = rows.get(0);
        return ResponseEntity.ok(session((String) u.get("EMAIL"), (String) u.get("USER_ROLE"), ((Number) u.get("PROFILE_ID")).longValue(), (String) u.get("NAME")));
    }
}
