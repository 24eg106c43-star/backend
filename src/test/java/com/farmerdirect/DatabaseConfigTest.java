package com.farmerdirect;

import org.junit.jupiter.api.Test;
import org.postgresql.Driver;
import java.net.URI;
import static org.junit.jupiter.api.Assertions.*;

public class DatabaseConfigTest {

    private String normalize(String rawUrl) {
        String working = rawUrl.trim();
        if (working.startsWith("jdbc:h2:")) return working;
        if (working.startsWith("jdbc:")) working = working.substring(5);
        if (!working.contains("://")) {
            working = "postgres://" + working;
        } else if (working.startsWith("//")) {
            working = "postgres:" + working;
        }

        try {
            String httpStr = working.replaceFirst("^[a-zA-Z0-9_-]+://", "http://");
            URI uri = new URI(httpStr);
            String host = uri.getHost();
            int port = uri.getPort() == -1 ? 5432 : uri.getPort();
            String path = uri.getPath();
            if (path == null || path.isBlank() || path.equals("/")) path = "/defaultdb";
            String query = uri.getQuery();
            if (query != null) {
                query = query.replaceAll("(?i)ssl-mode=REQUIRED", "sslmode=require")
                             .replaceAll("(?i)ssl-mode=require", "sslmode=require")
                             .replaceAll("(?i)sslmode=REQUIRED", "sslmode=require");
            }
            if (host != null && host.contains("aivencloud.com")) {
                if (query == null || !query.contains("sslmode")) {
                    query = (query == null || query.isBlank()) ? "sslmode=require" : (query + "&sslmode=require");
                }
            }
            String queryPart = (query != null && !query.isBlank()) ? ("?" + query) : "";
            return "jdbc:postgresql://" + host + ":" + port + path + queryPart;
        } catch (Exception e) {
            return "jdbc:postgresql://" + rawUrl;
        }
    }

    @Test
    public void testAivenRawUrl() {
        String raw = "farmerdb-anurag-b36c.b.aivencloud.com:19377/defaultdb?ssl-mode=REQUIRED";
        String normalized = normalize(raw);
        assertEquals("jdbc:postgresql://farmerdb-anurag-b36c.b.aivencloud.com:19377/defaultdb?sslmode=require", normalized);
        assertTrue(new Driver().acceptsURL(normalized));
    }

    @Test
    public void testAivenFullUri() {
        String raw = "postgres://avnadmin:pass123@farmerdb-anurag-b36c.b.aivencloud.com:19377/defaultdb?ssl-mode=REQUIRED";
        String normalized = normalize(raw);
        assertEquals("jdbc:postgresql://farmerdb-anurag-b36c.b.aivencloud.com:19377/defaultdb?sslmode=require", normalized);
        assertTrue(new Driver().acceptsURL(normalized));
    }

    @Test
    public void testRenderUrl() {
        String raw = "jdbc:postgresql://dpg-db7qjonavr4c7397evfg-a:5432/project_ugco";
        String normalized = normalize(raw);
        assertEquals("jdbc:postgresql://dpg-db7qjonavr4c7397evfg-a:5432/project_ugco", normalized);
        assertTrue(new Driver().acceptsURL(normalized));
    }
}
