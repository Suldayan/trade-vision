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
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
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
    ResponseEntity<Set<ProcessedMarketModel>> fetchAllMarketModelsByTimeRange(
            @RequestParam @Valid @Nonnull Long startDate,
            @RequestParam @Valid @Nonnull Long endDate) throws IllegalArgumentException {

        validateTimestamps(startDate, endDate);
        ZonedDateTime zonedStartDate = convertLongToZonedDateTime(startDate);
        ZonedDateTime zonedEndDate = convertLongToZonedDateTime(endDate);

        log.info("Fetching market models between {} and {}", zonedStartDate, zonedEndDate);
        Set<ProcessedMarketModel> marketModels = repository.findAllByTimestampBetween(zonedStartDate, zonedEndDate);
        if (isEmpty(marketModels)) {
            log.info("Market Models don't exist within: {} - {}, returning empty data",startDate, endDate);
            return ResponseEntity.ok(Collections.emptySet());
        }
        Set<ProcessedMarketModel> sortedMarketModels = sortMarketModelsByTimestamp(marketModels);

        return ResponseEntity.ok(sortedMarketModels);
    }

    @GetMapping("/base/{id}")
    ResponseEntity<Set<ProcessedMarketModel>> fetchModelByBaseIdAndTimeRange(
            @RequestParam @Valid @Nonnull Long startDate,
            @RequestParam @Valid @Nonnull Long endDate,
            @PathVariable @Valid @Nonnull String id) throws IllegalArgumentException {

        validateTimestamps(startDate, endDate);
        ZonedDateTime start = convertLongToZonedDateTime(startDate);
        ZonedDateTime end = convertLongToZonedDateTime(endDate);

        log.debug("Fetching base market models between {} and {} with id: {}",
                start, end, id);
        Set<ProcessedMarketModel> marketModels = repository.findByBaseIdAndTimestampBetween(id, start, end);
        if (isEmpty(marketModels)) {
            return ResponseEntity.ok(Collections.emptySet());
        }
        Set<ProcessedMarketModel> sortedMarketModels = sortMarketModelsByTimestamp(marketModels);

        return ResponseEntity.ok(sortedMarketModels);
    }

    @GetMapping("/quote/{id}")
    ResponseEntity<Set<ProcessedMarketModel>> fetchModelByQuoteIdAndTimeRange(
            @RequestParam @Valid @Nonnull Long startDate,
            @RequestParam @Valid @Nonnull Long endDate,
            @PathVariable @Valid @Nonnull String id) throws IllegalArgumentException {

        validateTimestamps(startDate, endDate);
        ZonedDateTime start = convertLongToZonedDateTime(startDate);
        ZonedDateTime end = convertLongToZonedDateTime(endDate);

        log.debug("Fetching quote market models between {} and {} with id: {}",
                start, end, id);
        Set<ProcessedMarketModel> marketModels = repository.findByQuoteIdAndTimestampBetween(id, start, end);
        if (isEmpty(marketModels)) {
            return ResponseEntity.ok(Collections.emptySet());
        }
        Set<ProcessedMarketModel> sortedMarketModels = sortMarketModelsByTimestamp(marketModels);

        return ResponseEntity.ok(sortedMarketModels);
    }

    @GetMapping("/exchange/{id}")
    ResponseEntity<Set<ProcessedMarketModel>> fetchModelByExchangeIdAndTimeRange(
            @RequestParam @Valid @Nonnull Long startDate,
            @RequestParam @Valid @Nonnull Long endDate,
            @PathVariable @Valid @Nonnull String id) throws IllegalArgumentException {

        validateTimestamps(startDate, endDate);
        ZonedDateTime start = convertLongToZonedDateTime(startDate);
        ZonedDateTime end = convertLongToZonedDateTime(endDate);

        log.debug("Fetching exchange market models between {} and {} with id: {}",
                start, end, id);
        Set<ProcessedMarketModel> marketModels = repository.findByExchangeIdAndTimestampBetween(id, start, end);
        if (isEmpty(marketModels)) {
            return ResponseEntity.ok(Collections.emptySet());
        }
        Set<ProcessedMarketModel> sortedMarketModels = sortMarketModelsByTimestamp(marketModels);

        return ResponseEntity.ok(sortedMarketModels);
    }

    private boolean isEmpty(@Nonnull Set<ProcessedMarketModel> marketModels) {
        return marketModels.isEmpty();
    }

    private void validateTimestamps(@Nonnull Long start, @Nonnull Long end) throws IllegalArgumentException {
        if (start >= end) {
            throw new IllegalArgumentException("Start date must be before end date");
        }
    }

    @Nonnull
    private Set<ProcessedMarketModel> sortMarketModelsByTimestamp(@Nonnull Set<ProcessedMarketModel> marketModels) {
        return marketModels.stream()
                .sorted(Comparator.comparing(ProcessedMarketModel::getTimestamp))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Nonnull
    private ZonedDateTime convertLongToZonedDateTime(@Nonnull Long date) {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(date), ZoneOffset.UTC);
    }
}