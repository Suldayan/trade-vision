package com.example.trade_vision_backend.processing.internal.infrastructure.service;

import com.example.trade_vision_backend.processing.*;
import com.example.trade_vision_backend.processing.internal.infrastructure.db.CandleRepository;
import com.example.trade_vision_backend.processing.internal.infrastructure.db.ProcessingRepository;
import com.example.trade_vision_backend.processing.internal.infrastructure.mapper.CandleMapper;
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
    private final CandleMapper candleMapper;
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
    public List<CandleDTO> fetchAllCandlePairsWithinTimeRange(
            @Nonnull String baseId,
            @Nonnull String quoteId,
            @Nonnull String exchangeId,
            @Nonnull ZonedDateTime endTime) {
        List<CandleModel> candleModels = candleRepository.findMarketPairWithinTimeRange(
                baseId,
                quoteId,
                exchangeId,
                ZonedDateTime.now(),
                endTime
        );

        return candleMapper.INSTANCE.entityListToDTOList(candleModels);
    }

    @Nonnull
    private List<ProcessedMarketDTO> convertToDto(List<ProcessedMarketModel> marketModels) {
        return mapper.INSTANCE.entityListToDtoList(marketModels);
    }
}
