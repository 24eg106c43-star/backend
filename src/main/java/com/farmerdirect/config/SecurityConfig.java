package com.farmerdirect.config;

import com.farmerdirect.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SecurityConfig implements WebMvcConfigurer {
    private final JwtService jwt;
    public SecurityConfig(JwtService jwt) { this.jwt = jwt; }

    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    /** Every /api/** call except /api/auth/** needs "Authorization: Bearer <token>". */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        HandlerInterceptor interceptor = new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
                if ("OPTIONS".equalsIgnoreCase(req.getMethod())) return true;
                String h = req.getHeader("Authorization");
                if (h != null && h.startsWith("Bearer ")) {
                    try {
                        jwt.parse(h.substring(7));
                        return true;
                    } catch (Exception ignored) { }
                }
                res.sendError(401, "Please login");
                return false;
            }
        };

        registry.addInterceptor(interceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/**");
    }

    /** Idempotent seed sample data (farmers, products, market prices) if empty. */
    @Bean CommandLineRunner seedSampleData(JdbcTemplate db) {
        return args -> {
            try {
                Integer count = db.queryForObject("SELECT COUNT(*) FROM farmers", Integer.class);
                if (count == null || count == 0) {
                    db.update("INSERT INTO farmers(name,phone,location) VALUES ('Ramesh','9876500001','Kurnool')");
                    db.update("INSERT INTO customers(name,phone,address) VALUES ('Anita','9876500002','Hyderabad')");
                    db.update("INSERT INTO products(farmer_id,crop_name,category,quantity,price,quality,harvest_date) VALUES " +
                        "(1,'Tomato','Vegetable',200,24,'A',CURRENT_DATE),(1,'Onion','Vegetable',500,30,'B',CURRENT_DATE),(1,'Groundnut','Oilseed',300,70,'A',CURRENT_DATE)");
                    db.update("INSERT INTO market_prices(crop_name,location,price_date,market_price) VALUES " +
                        "('Tomato','Kurnool','2026-04-01',18),('Tomato','Kurnool','2026-05-01',20),('Tomato','Kurnool','2026-06-01',22),('Tomato','Kurnool','2026-07-01',23),('Tomato','Kurnool','2026-08-01',26)," +
                        "('Onion','Kurnool','2026-04-01',26),('Onion','Kurnool','2026-05-01',28),('Onion','Kurnool','2026-06-01',27),('Onion','Kurnool','2026-07-01',31),('Onion','Kurnool','2026-08-01',33)," +
                        "('Groundnut','Kurnool','2026-04-01',64),('Groundnut','Kurnool','2026-05-01',66),('Groundnut','Kurnool','2026-06-01',65),('Groundnut','Kurnool','2026-07-01',68),('Groundnut','Kurnool','2026-08-01',71)");
                }
            } catch (Exception e) {
                System.err.println("Notice: Sample data seeding skipped: " + e.getMessage());
            }
        };
    }

    /** Demo accounts (password: demo123), linked to the sample farmer 1 / customer 1. */
    @Bean CommandLineRunner demoUsers(JdbcTemplate db, PasswordEncoder enc) {
        return args -> {
            try {
                String[][] demo = {{"farmer@demo.com", "Ramesh", "farmer", "1"}, {"customer@demo.com", "Anita", "customer", "1"}, {"admin@demo.com", "Admin", "admin", "0"}};
                for (String[] d : demo) {
                    Integer n = db.queryForObject("SELECT COUNT(*) FROM users WHERE email=?", Integer.class, d[0]);
                    if (n == 0) db.update("INSERT INTO users(email,password_hash,user_role,profile_id,name) VALUES(?,?,?,?,?)",
                            d[0], enc.encode("demo123"), d[2], Integer.parseInt(d[3]), d[1]);
                }
            } catch (Exception e) {
                System.err.println("Notice: Demo users seeding skipped: " + e.getMessage());
            }
        };
    }
}
