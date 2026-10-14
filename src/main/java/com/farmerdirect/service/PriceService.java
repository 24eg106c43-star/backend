package com.farmerdirect.service;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
/** Simple ML: Linear Regression (least squares) over historical market prices. */
@Service
public class PriceService {
    private final JdbcTemplate jdbc;
    public PriceService(JdbcTemplate jdbc) { this.jdbc = jdbc; }
    public Double recommend(String crop) {
        List<Double> p = jdbc.queryForList(
            "SELECT market_price FROM market_prices WHERE LOWER(crop_name)=LOWER(?) ORDER BY price_date", Double.class, crop);
        int n = p.size();
        if (n < 2) return null;
        double sx = 0, sy = 0, sxy = 0, sxx = 0;
        for (int i = 0; i < n; i++) { sx += i; sy += p.get(i); sxy += i * p.get(i); sxx += (double) i * i; }
        double slope = (n * sxy - sx * sy) / (n * sxx - sx * sx);
        double intercept = (sy - slope * sx) / n;
        return Math.round((intercept + slope * n) * 100.0) / 100.0;   // next period
    }
}
