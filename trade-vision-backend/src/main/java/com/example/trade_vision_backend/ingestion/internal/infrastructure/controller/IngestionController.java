package com.example.trade_vision_backend.ingestion.internal.infrastructure.controller;

import com.example.trade_vision_backend.ingestion.internal.infrastructure.service.IngestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
        value = "api/v1/ingestion",
        produces = MediaType.APPLICATION_JSON_VALUE
)
@Slf4j
@RequiredArgsConstructor
public class IngestionController {
    private final IngestionService ingestionService;

    // TODO: configure controller advice
    @GetMapping("/activate")
    public ResponseEntity<String> activateIngestionService() {
        ingestionService.executeIngestion();
        return ResponseEntity.ok("Successfully executed ingestion service");
    }
}
