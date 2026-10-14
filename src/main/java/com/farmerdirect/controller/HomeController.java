package com.farmerdirect.controller;

import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public Map<String, Object> root() {
        return Map.of(
            "status", "UP",
            "service", "Farmer Direct Market API",
            "version", "1.0.0",
            "endpoints", List.of("/api/products", "/api/auth/login", "/api/auth/register", "/api/orders")
        );
    }

    @GetMapping("/api")
    public Map<String, Object> apiRoot() {
        return Map.of(
            "status", "UP",
            "service", "Farmer Direct Market API"
        );
    }
}
