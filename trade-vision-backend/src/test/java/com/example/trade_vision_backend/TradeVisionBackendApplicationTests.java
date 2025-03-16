package com.example.trade_vision_backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.modulith.core.ApplicationModules;

@SpringBootTest
class TradeVisionBackendApplicationTests {

	@Test
	void contextLoads() {
		ApplicationModules modules = ApplicationModules.of(TradeVisionBackendApplication.class);
		modules.forEach(System.out::println);
		modules.verify();
	}
}
