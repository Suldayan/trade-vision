package com.example.trade_vision_backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    private final Environment env;

    public CorsConfig(Environment env) {
        this.env = env;
    }

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // Get allowed origins from application properties, or use a default for development
        List<String> allowedOrigins = getAllowedOrigins();
        allowedOrigins.forEach(config::addAllowedOrigin);

        // Standard HTTP methods needed for a REST API
        config.addAllowedMethod("GET");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("DELETE");
        config.addAllowedMethod("OPTIONS");

        // Allow common headers
        config.addAllowedHeader("Authorization");
        config.addAllowedHeader("Content-Type");
        config.addAllowedHeader("Accept");

        // Allow credentials if needed (cookies, auth headers)
        config.setAllowCredentials(true);

        // How long the browser should cache the CORS response (in seconds)
        config.setMaxAge(3600L);

        // Apply this configuration to all API endpoints
        source.registerCorsConfiguration("/api/**", config);
        return new CorsFilter(source);
    }

    private List<String> getAllowedOrigins() {
        // Check if we're in a production profile
        boolean isProd = Arrays.asList(env.getActiveProfiles()).contains("prod");

        if (isProd) {
            // In production, only allow specific origins
            return Arrays.asList(
                    "https://your-production-domain.com",
                    "https://api.your-production-domain.com"
            );
        } else {
            // In development, allow localhost with common ports
            return Arrays.asList(
                    "http://localhost:5173",
                    "http://localhost:3000",
                    "http://localhost:8000"
            );
        }
    }
}