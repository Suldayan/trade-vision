package com.example.spring_backend.common;

import jakarta.annotation.Nonnull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class GlobalCorsConfig {
    private final static String BACKTEST_MAPPING = "/api/backtest/execute";
    private final static String STRATEGY_MAPPING = "/api/strategies/all";
    private final static String HEALTH_MAPPING = "/health/ping";

    @Value("${frontend.url}")
    private String frontendUrl;

    @Value("${health.url}")
    private String healthUrl;

    @Value("${cors.allowed.origins}")
    private String[] allowedOrigins;

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {

            @Override
            public void addCorsMappings(@Nonnull CorsRegistry registry) {
                registry.addMapping(BACKTEST_MAPPING)
                        .allowedOrigins(allowedOrigins)
                        .allowedMethods("POST", "OPTIONS")
                        .allowCredentials(true)
                        .allowedHeaders(
                                "Content-Type",
                                "Accept",
                                "Origin",
                                "X-Requested-With"
                        )
                        .maxAge(3600);

                registry.addMapping(STRATEGY_MAPPING)
                        .allowedOrigins(allowedOrigins)
                        .allowedMethods("GET", "OPTIONS")
                        .allowCredentials(true)
                        .allowedHeaders(
                                "Content-Type",
                                "Accept",
                                "Origin",
                                "X-Requested-With"
                        )
                        .maxAge(3600);

                registry.addMapping(HEALTH_MAPPING)
                        .allowedOrigins(healthUrl)
                        .allowedMethods("GET", "OPTIONS")
                        .allowedHeaders(
                                "Content-Type",
                                "Accept",
                                "Origin"
                        )
                        .maxAge(3600);
            }
        };
    }
}