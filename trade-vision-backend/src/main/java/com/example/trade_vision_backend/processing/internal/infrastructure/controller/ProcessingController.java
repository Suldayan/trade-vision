package com.example.trade_vision_backend.processing.internal.infrastructure.controller;

import com.example.trade_vision_backend.processing.ProcessedMarketModel;
import com.example.trade_vision_backend.processing.internal.infrastructure.db.ProcessingRepository;
import com.example.trade_vision_backend.processing.internal.infrastructure.service.ProcessingService;
import jakarta.annotation.Nonnull;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping(
        value = "api/v1/processing",
        produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
@RequiredArgsConstructor
public class ProcessingController {
    private final ProcessingRepository repository;
    private final ProcessingService processingService;

    @GetMapping("/all")
    ResponseEntity<List<ProcessedMarketModel>> fetchAllMarketModelsByTimeRange(
            @RequestParam @Valid @Nonnull Long startDate,
            @RequestParam @Valid @Nonnull Long endDate) throws IllegalArgumentException {

        processingService.validateTimestamps(startDate, endDate);
        ZonedDateTime zonedStartDate = processingService.convertLongToZonedDateTime(startDate);
        ZonedDateTime zonedEndDate = processingService.convertLongToZonedDateTime(endDate);

        log.info("Fetching market models between {} and {}", zonedStartDate, zonedEndDate);
        List<ProcessedMarketModel> marketModels = repository.findAllByTimestampBetween(zonedStartDate, zonedEndDate);
        if (marketModels.isEmpty()) {
            log.info("Market Models don't exist within: {} - {}, returning empty data",startDate, endDate);
            return ResponseEntity.ok(Collections.emptyList());
        }
        List<ProcessedMarketModel> sortedMarketModels = processingService.sortMarketModelsByTimestamp(marketModels);

        return ResponseEntity.ok(sortedMarketModels);
    }
}