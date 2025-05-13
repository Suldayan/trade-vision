package com.example.trade_vision_backend.backtester.internal;

import jakarta.annotation.Nonnull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class GlobalCorsConfig {

    @Value("${frontend.url}")
    private String frontendUrl;

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(@Nonnull CorsRegistry registry) {
                registry.addMapping("/api/v1/strategy/**")
                        .allowedOrigins(frontendUrl)
                        .allowedMethods("POST", "OPTIONS")
                        .allowCredentials(true)
                        .allowedHeaders(
                                "Content-Type",
                                "Accept",
                                "Origin",
                                "X-Requested-With"
                        );
            }
        };
    }
}
