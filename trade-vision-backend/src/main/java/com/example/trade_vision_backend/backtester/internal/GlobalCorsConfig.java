package com.example.trade_vision_backend.backtester.internal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class GlobalCorsConfig {

    @Value("${frontend.url:http://localhost:5173}")
    private String frontendUrl;

    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/v1/strategy/backtest")
                        .allowedOrigins(frontendUrl)
                        .allowedMethods("POST")
                        .allowedHeaders("*");
            }
        };
    }
}
