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

        String user = configuredUsername;
        String pass = configuredPassword;
        String envUser = System.getenv("SPRING_DATASOURCE_USERNAME");
        if (envUser != null && !envUser.isBlank()) user = envUser;
        String envPass = System.getenv("SPRING_DATASOURCE_PASSWORD");
        if (envPass != null && !envPass.isBlank()) pass = envPass;

        String jdbcUrl = rawUrl != null ? rawUrl.trim() : "";
        String driver = configuredDriver;

        // 1. If H2 in-memory
        if (jdbcUrl.startsWith("jdbc:h2:")) {
            HikariDataSource ds = new HikariDataSource();
            ds.setJdbcUrl(jdbcUrl);
            ds.setUsername(user);
            ds.setPassword(pass);
            ds.setDriverClassName("org.h2.Driver");
            return ds;
        }

        // 2. Normalize PostgreSQL URL (handles Aiven, Render, raw host:port, postgres://, etc.)
        if (!jdbcUrl.isEmpty()) {
            String working = jdbcUrl;

            // Strip "jdbc:" prefix temporarily for uniform URI parsing
            if (working.startsWith("jdbc:")) {
                working = working.substring(5);
            }

            // Ensure a scheme is present for java.net.URI
            if (!working.contains("://")) {
                working = "postgres://" + working;
            } else if (working.startsWith("//")) {
                working = "postgres:" + working;
            }

            try {
                // Use http scheme so java.net.URI correctly parses host, port, userInfo, path, query
                String httpStr = working.replaceFirst("^[a-zA-Z0-9_-]+://", "http://");
                URI uri = new URI(httpStr);

                String userInfo = uri.getUserInfo();
                if (userInfo != null && userInfo.contains(":")) {
                    String[] parts = userInfo.split(":", 2);
                    user = parts[0];
                    pass = parts[1];
                } else if (userInfo != null && !userInfo.isBlank()) {
                    user = userInfo;
                }

                String host = uri.getHost();
                int port = uri.getPort() == -1 ? 5432 : uri.getPort();
                String path = uri.getPath();
                if (path == null || path.isBlank() || path.equals("/")) {
                    path = "/defaultdb";
                }

                String query = uri.getQuery();
                if (query != null) {
                    query = query.replaceAll("(?i)ssl-mode=REQUIRED", "sslmode=require")
                                 .replaceAll("(?i)ssl-mode=require", "sslmode=require")
                                 .replaceAll("(?i)sslmode=REQUIRED", "sslmode=require");
                }

                // If host is Aiven cloud and no sslmode specified, default to sslmode=require
                if (host != null && host.contains("aivencloud.com")) {
                    if (query == null || !query.contains("sslmode")) {
                        query = (query == null || query.isBlank()) ? "sslmode=require" : (query + "&sslmode=require");
                    }
                }

                String queryPart = (query != null && !query.isBlank()) ? ("?" + query) : "";
                jdbcUrl = "jdbc:postgresql://" + host + ":" + port + path + queryPart;
                driver = "org.postgresql.Driver";
            } catch (Exception e) {
                // Manual fallback
                String clean = jdbcUrl.replaceFirst("^(jdbc:)?postgres(ql)?://", "");
                if (clean.contains("@")) {
                    String[] parts = clean.split("@", 2);
                    if (parts[0].contains(":")) {
                        String[] up = parts[0].split(":", 2);
                        user = up[0];
                        pass = up[1];
                    }
                    clean = parts[1];
                }
                clean = clean.replaceAll("(?i)ssl-mode=REQUIRED", "sslmode=require")
                             .replaceAll("(?i)ssl-mode=require", "sslmode=require")
                             .replaceAll("(?i)sslmode=REQUIRED", "sslmode=require");
                jdbcUrl = "jdbc:postgresql://" + clean;
                driver = "org.postgresql.Driver";
            }
        }

        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(jdbcUrl);
        if (user != null && !user.isBlank()) ds.setUsername(user);
        if (pass != null && !pass.isBlank()) ds.setPassword(pass);
        if (driver != null && !driver.isBlank()) {
            ds.setDriverClassName(driver);
        } else {
            ds.setDriverClassName("org.postgresql.Driver");
        }
        return ds;
    }
}
