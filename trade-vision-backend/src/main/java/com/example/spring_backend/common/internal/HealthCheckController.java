package com.example.spring_backend.common.internal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/health")
public class HealthCheckController {

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        log.debug("Health check ping received");
        return ResponseEntity.ok("Server is up and running!");
    }
}
