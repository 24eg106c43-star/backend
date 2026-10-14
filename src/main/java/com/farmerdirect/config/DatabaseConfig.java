package com.farmerdirect.config;

import com.zaxxer.hikari.HikariDataSource;
import java.net.URI;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class DatabaseConfig {

    @Value("${spring.datasource.url:}")
    private String configuredUrl;

    @Value("${spring.datasource.username:}")
    private String configuredUsername;

    @Value("${spring.datasource.password:}")
    private String configuredPassword;

    @Value("${spring.datasource.driver-class-name:}")
    private String configuredDriver;

    @Bean
    @Primary
    public DataSource dataSource() {
        String rawUrl = System.getenv("DATABASE_URL");
        if (rawUrl == null || rawUrl.isBlank()) {
            rawUrl = System.getenv("SPRING_DATASOURCE_URL");
        }
        if (rawUrl == null || rawUrl.isBlank()) {
            rawUrl = configuredUrl;
        }

        String jdbcUrl = rawUrl;
        String user = configuredUsername;
        String pass = configuredPassword;
        String driver = configuredDriver;

        // Automatically convert Render standard "postgresql://user:pass@host/db" to "jdbc:postgresql://..."
        if (rawUrl != null && (rawUrl.startsWith("postgres://") || rawUrl.startsWith("postgresql://"))) {
            try {
                String normalized = rawUrl.replaceFirst("^postgres(ql)?://", "http://");
                URI uri = new URI(normalized);
                String host = uri.getHost();
                int port = uri.getPort() == -1 ? 5432 : uri.getPort();
                String path = uri.getPath(); // includes leading '/'
                String userInfo = uri.getUserInfo();
                if (userInfo != null && userInfo.contains(":")) {
                    String[] parts = userInfo.split(":", 2);
                    user = parts[0];
                    pass = parts[1];
                }
                jdbcUrl = "jdbc:postgresql://" + host + ":" + port + path;
                driver = "org.postgresql.Driver";
            } catch (Exception ignored) {
                // fallback
            }
        }

        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(jdbcUrl);
        if (user != null && !user.isBlank()) ds.setUsername(user);
        if (pass != null && !pass.isBlank()) ds.setPassword(pass);
        if (driver != null && !driver.isBlank()) {
            ds.setDriverClassName(driver);
        } else if (jdbcUrl != null && jdbcUrl.startsWith("jdbc:postgresql:")) {
            ds.setDriverClassName("org.postgresql.Driver");
        } else if (jdbcUrl != null && jdbcUrl.startsWith("jdbc:h2:")) {
            ds.setDriverClassName("org.h2.Driver");
        }
        return ds;
    }
}
