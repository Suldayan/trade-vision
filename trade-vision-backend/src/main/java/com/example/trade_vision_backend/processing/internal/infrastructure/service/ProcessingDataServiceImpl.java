package com.example.trade_vision_backend.processing.internal.infrastructure.service;

import com.example.trade_vision_backend.processing.ProcessedMarketDTO;
import com.example.trade_vision_backend.processing.ProcessedMarketModel;
import com.example.trade_vision_backend.processing.ProcessingDataService;
import com.example.trade_vision_backend.processing.internal.infrastructure.db.CandleRepository;
import com.example.trade_vision_backend.processing.internal.infrastructure.db.ProcessingRepository;
import com.example.trade_vision_backend.processing.internal.infrastructure.mapper.ProcessingMapper;
import jakarta.annotation.Nonnull;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProcessingDataServiceImpl implements ProcessingDataService {
    private final ProcessingRepository repository;
    private final CandleRepository candleRepository;
    private final ProcessingMapper mapper;
    private final ProcessingService processingService;

    @Nonnull
    @Override
    public List<ProcessedMarketDTO> fetchAllMarketModelsByTimeRange(
            @Valid @Nonnull Long startDate,
            @Valid @Nonnull Long endDate) throws IllegalArgumentException {

        processingService.validateTimestamps(startDate, endDate);
        ZonedDateTime zonedStartDate = processingService.convertLongToZonedDateTime(startDate);
        ZonedDateTime zonedEndDate = processingService.convertLongToZonedDateTime(endDate);

        log.info("Fetching market models between {} and {}", zonedStartDate, zonedEndDate);
        List<ProcessedMarketModel> marketModels = repository.findAllByTimestampBetween(zonedStartDate, zonedEndDate);
        if (marketModels.isEmpty()) {
            log.info("Market Models don't exist within: {} - {}, returning empty data",startDate, endDate);
            return Collections.emptyList();
        }

        List<ProcessedMarketModel> sortedModels = processingService.sortMarketModelsByTimestamp(marketModels);
        return convertToDto(sortedModels);
    }

    @Nonnull
    @Override
    public List<ProcessedMarketDTO> fetchModelByBaseIdAndTimeRange(
            @Valid @Nonnull Long startDate,
            @Valid @Nonnull Long endDate,
            @Valid @Nonnull String id) throws IllegalArgumentException {

        processingService.validateTimestamps(startDate, endDate);
        ZonedDateTime start = processingService.convertLongToZonedDateTime(startDate);
        ZonedDateTime end = processingService.convertLongToZonedDateTime(endDate);

        log.debug("Fetching base market models between {} and {} with id: {}",
                start, end, id);
        List<ProcessedMarketModel> marketModels = repository.findByBaseIdAndTimestampBetween(id, start, end);
        if (marketModels.isEmpty()) {
            return Collections.emptyList();
        }

        List<ProcessedMarketModel> sortedModels = processingService.sortMarketModelsByTimestamp(marketModels);
        return convertToDto(sortedModels);
    }

    @Nonnull
    @Override
    public List<ProcessedMarketDTO> fetchModelByQuoteIdAndTimeRange(
            @Valid @Nonnull Long startDate,
            @Valid @Nonnull Long endDate,
            @Valid @Nonnull String id) throws IllegalArgumentException {

        processingService.validateTimestamps(startDate, endDate);
        ZonedDateTime start = processingService.convertLongToZonedDateTime(startDate);
        ZonedDateTime end = processingService.convertLongToZonedDateTime(endDate);

        log.debug("Fetching quote market models between {} and {} with id: {}",
                start, end, id);
        List<ProcessedMarketModel> marketModels = repository.findByQuoteIdAndTimestampBetween(id, start, end);
        if (marketModels.isEmpty()) {
            return Collections.emptyList();
        }

        List<ProcessedMarketModel> sortedModels = processingService.sortMarketModelsByTimestamp(marketModels);
        return convertToDto(sortedModels);
    }

    @Nonnull
    @Override
    public List<ProcessedMarketDTO> fetchModelByExchangeIdAndTimeRange(
            @Valid @Nonnull Long startDate,
            @Valid @Nonnull Long endDate,
            @Valid @Nonnull String id) throws IllegalArgumentException {

        processingService.validateTimestamps(startDate, endDate);
        ZonedDateTime start = processingService.convertLongToZonedDateTime(startDate);
        ZonedDateTime end = processingService.convertLongToZonedDateTime(endDate);

        log.debug("Fetching exchange market models between {} and {} with id: {}",
                start, end, id);
        List<ProcessedMarketModel> marketModels = repository.findByExchangeIdAndTimestampBetween(id, start, end);
        if (marketModels.isEmpty()) {
            return Collections.emptyList();
        }

        List<ProcessedMarketModel> sortedModels = processingService.sortMarketModelsByTimestamp(marketModels);
        return convertToDto(sortedModels);
    }

    @Nonnull
    private List<ProcessedMarketDTO> convertToDto(List<ProcessedMarketModel> marketModels) {
        return mapper.INSTANCE.entityListToDtoList(marketModels);
    }
}
