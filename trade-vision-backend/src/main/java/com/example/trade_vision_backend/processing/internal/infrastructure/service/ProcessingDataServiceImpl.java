package com.example.trade_vision_backend.processing.internal.infrastructure.service;

import com.example.trade_vision_backend.processing.ProcessedMarketDTO;
import com.example.trade_vision_backend.processing.ProcessedMarketModel;
import com.example.trade_vision_backend.processing.ProcessingDataService;
import com.example.trade_vision_backend.processing.internal.infrastructure.db.ProcessingRepository;
import com.example.trade_vision_backend.processing.internal.infrastructure.mapper.ProcessingMapper;
import jakarta.annotation.Nonnull;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProcessingDataServiceImpl implements ProcessingDataService {
    private final ProcessingRepository repository;
    private final ProcessingMapper mapper;

    @Nonnull
    @Override
    public List<ProcessedMarketDTO> fetchAllMarketModelsByTimeRange(
            @Valid @Nonnull Long startDate,
            @Valid @Nonnull Long endDate) throws IllegalArgumentException {

        validateTimestamps(startDate, endDate);
        ZonedDateTime zonedStartDate = convertLongToZonedDateTime(startDate);
        ZonedDateTime zonedEndDate = convertLongToZonedDateTime(endDate);

        log.info("Fetching market models between {} and {}", zonedStartDate, zonedEndDate);
        List<ProcessedMarketModel> marketModels = repository.findAllByTimestampBetween(zonedStartDate, zonedEndDate);
        if (isEmpty(marketModels)) {
            log.info("Market Models don't exist within: {} - {}, returning empty data",startDate, endDate);
            return Collections.emptyList();
        }

        List<ProcessedMarketModel> sortedModels = sortMarketModelsByTimestamp(marketModels);
        return convertToDto(sortedModels);
    }

    @Nonnull
    @Override
    public List<ProcessedMarketDTO> fetchModelByBaseIdAndTimeRange(
            @Valid @Nonnull Long startDate,
            @Valid @Nonnull Long endDate,
            @Valid @Nonnull String id) throws IllegalArgumentException {

        validateTimestamps(startDate, endDate);
        ZonedDateTime start = convertLongToZonedDateTime(startDate);
        ZonedDateTime end = convertLongToZonedDateTime(endDate);

        log.debug("Fetching base market models between {} and {} with id: {}",
                start, end, id);
        List<ProcessedMarketModel> marketModels = repository.findByBaseIdAndTimestampBetween(id, start, end);
        if (isEmpty(marketModels)) {
            return Collections.emptyList();
        }

        List<ProcessedMarketModel> sortedModels = sortMarketModelsByTimestamp(marketModels);
        return convertToDto(sortedModels);
    }

    @Nonnull
    @Override
    public List<ProcessedMarketDTO> fetchModelByQuoteIdAndTimeRange(
            @Valid @Nonnull Long startDate,
            @Valid @Nonnull Long endDate,
            @Valid @Nonnull String id) throws IllegalArgumentException {

        validateTimestamps(startDate, endDate);
        ZonedDateTime start = convertLongToZonedDateTime(startDate);
        ZonedDateTime end = convertLongToZonedDateTime(endDate);

        log.debug("Fetching quote market models between {} and {} with id: {}",
                start, end, id);
        List<ProcessedMarketModel> marketModels = repository.findByQuoteIdAndTimestampBetween(id, start, end);
        if (isEmpty(marketModels)) {
            return Collections.emptyList();
        }

        List<ProcessedMarketModel> sortedModels = sortMarketModelsByTimestamp(marketModels);
        return convertToDto(sortedModels);
    }

    @Nonnull
    @Override
    public List<ProcessedMarketDTO> fetchModelByExchangeIdAndTimeRange(
            @Valid @Nonnull Long startDate,
            @Valid @Nonnull Long endDate,
            @Valid @Nonnull String id) throws IllegalArgumentException {

        validateTimestamps(startDate, endDate);
        ZonedDateTime start = convertLongToZonedDateTime(startDate);
        ZonedDateTime end = convertLongToZonedDateTime(endDate);

        log.debug("Fetching exchange market models between {} and {} with id: {}",
                start, end, id);
        List<ProcessedMarketModel> marketModels = repository.findByExchangeIdAndTimestampBetween(id, start, end);
        if (isEmpty(marketModels)) {
            return Collections.emptyList();
        }

        List<ProcessedMarketModel> sortedModels = sortMarketModelsByTimestamp(marketModels);
        return convertToDto(sortedModels);
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

    @Nonnull
    private List<ProcessedMarketDTO> convertToDto(List<ProcessedMarketModel> marketModels) {
        return mapper.INSTANCE.entityListToDtoList(marketModels);
    }
}
