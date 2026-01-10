package com.example.spring_backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.modulith.core.ApplicationModules;

@SpringBootTest
class SpringBackendApplicationTests {

	@Test
	void contextLoads() {
		ApplicationModules modules = ApplicationModules.of(SpringBackendApplication.class);
		modules.forEach(System.out::println);
		modules.verify();
	}
}
