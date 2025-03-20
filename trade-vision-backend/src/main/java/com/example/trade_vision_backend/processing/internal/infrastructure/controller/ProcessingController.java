package com.example.trade_vision_backend.processing.internal.infrastructure.controller;

import com.example.trade_vision_backend.processing.ProcessedMarketModel;
import com.example.trade_vision_backend.processing.internal.infrastructure.db.ProcessingRepository;
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

    @GetMapping("/all")
    ResponseEntity<List<ProcessedMarketModel>> fetchAllMarketModelsByTimeRange(
            @RequestParam @Valid @Nonnull Long startDate,
            @RequestParam @Valid @Nonnull Long endDate) throws IllegalArgumentException {

        validateTimestamps(startDate, endDate);
        ZonedDateTime zonedStartDate = convertLongToZonedDateTime(startDate);
        ZonedDateTime zonedEndDate = convertLongToZonedDateTime(endDate);

        log.info("Fetching market models between {} and {}", zonedStartDate, zonedEndDate);
        List<ProcessedMarketModel> marketModels = repository.findAllByTimestampBetween(zonedStartDate, zonedEndDate);
        if (isEmpty(marketModels)) {
            log.info("Market Models don't exist within: {} - {}, returning empty data",startDate, endDate);
            return ResponseEntity.ok(Collections.emptyList());
        }
        List<ProcessedMarketModel> sortedMarketModels = sortMarketModelsByTimestamp(marketModels);

        return ResponseEntity.ok(sortedMarketModels);
    }

    private boolean isEmpty(@Nonnull List<ProcessedMarketModel> marketModels) {
        return marketModels.isEmpty();
    }

    private void validateTimestamps(@Nonnull Long start, @Nonnull Long end) throws IllegalArgumentException {
        if (start >= end) {
            throw new IllegalArgumentException("Start date must be before end date");
        }
    }

    @Nonnull
    private List<ProcessedMarketModel> sortMarketModelsByTimestamp(@Nonnull List<ProcessedMarketModel> marketModels) {
        return marketModels.stream()
                .sorted(Comparator.comparing(ProcessedMarketModel::getTimestamp))
                .collect(Collectors.toCollection(LinkedList::new));
    }

    @Nonnull
    private ZonedDateTime convertLongToZonedDateTime(@Nonnull Long date) {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(date), ZoneOffset.UTC);
    }
}