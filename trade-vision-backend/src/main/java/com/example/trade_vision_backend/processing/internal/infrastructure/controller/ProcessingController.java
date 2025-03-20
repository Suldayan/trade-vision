package com.example.trade_vision_backend.processing.internal.infrastructure.controller;

import com.example.trade_vision_backend.processing.ProcessedMarketDTO;
import com.example.trade_vision_backend.processing.ProcessingDataService;
import jakarta.annotation.Nonnull;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping(
        value = "api/v1/processing",
        produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
@RequiredArgsConstructor
public class ProcessingController {
    private final ProcessingDataService dataService;

    @GetMapping("/all")
    ResponseEntity<List<ProcessedMarketDTO>> fetchAllMarketModelsByTimeRange(
            @RequestParam @Valid @Nonnull Long startDate,
            @RequestParam @Valid @Nonnull Long endDate) throws IllegalArgumentException {

        List<ProcessedMarketDTO> sortedMarketModels = dataService.fetchAllMarketModelsByTimeRange(startDate, endDate);

        return ResponseEntity.ok(sortedMarketModels);
    }
}