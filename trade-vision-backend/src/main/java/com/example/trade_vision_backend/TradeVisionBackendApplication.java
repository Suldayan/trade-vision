package com.example.trade_vision_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.modulith.Modulith;

@Modulith
@SpringBootApplication(scanBasePackages = "com.example.trade_vision_backend")
@ConfigurationPropertiesScan
public class TradeVisionBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(TradeVisionBackendApplication.class, args);
	}

}
