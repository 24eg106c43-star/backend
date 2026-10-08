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
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SecurityConfig implements WebMvcConfigurer {
    private final JwtService jwt;
    public SecurityConfig(JwtService jwt) { this.jwt = jwt; }

    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    /** Every /api/** call except /api/auth/** needs "Authorization: Bearer <token>". */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        HandlerInterceptor interceptor = new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
                if ("OPTIONS".equals(req.getMethod())) return true;
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

    /** Demo accounts (password: demo123), linked to the sample farmer 1 / customer 1. */
    @Bean CommandLineRunner demoUsers(JdbcTemplate db, PasswordEncoder enc) {
        return args -> {
            String[][] demo = {{"farmer@demo.com", "Ramesh", "farmer", "1"}, {"customer@demo.com", "Anita", "customer", "1"}, {"admin@demo.com", "Admin", "admin", "0"}};
            for (String[] d : demo) {
                Integer n = db.queryForObject("SELECT COUNT(*) FROM users WHERE email=?", Integer.class, d[0]);
                if (n == 0) db.update("INSERT INTO users(email,password_hash,user_role,profile_id,name) VALUES(?,?,?,?,?)",
                        d[0], enc.encode("demo123"), d[2], Integer.parseInt(d[3]), d[1]);
            }
        };
    }
}
