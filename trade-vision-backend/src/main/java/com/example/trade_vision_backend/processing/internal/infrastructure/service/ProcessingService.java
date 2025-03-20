package com.example.trade_vision_backend.processing.internal.infrastructure.service;

import com.example.trade_vision_backend.ingestion.ProcessableMarketDTO;
import com.example.trade_vision_backend.ingestion.market.RawMarketModel;
import com.example.trade_vision_backend.processing.ProcessedMarketModel;
import com.example.trade_vision_backend.processing.internal.infrastructure.exception.ProcessingException;
import jakarta.annotation.Nonnull;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

public interface ProcessingService {
    List<ProcessedMarketModel> transformToMarketModel(List<ProcessableMarketDTO> processableMarketDTOS, Long timestamp) throws IllegalArgumentException;
    void executeProcessing(List<ProcessableMarketDTO> processableMarketDTOS, Long timestamp) throws ProcessingException;
    void saveProcessedData(List<ProcessedMarketModel> processedData) throws ProcessingException;
    List<ProcessedMarketModel> sortMarketModelsByTimestamp(List<ProcessedMarketModel> marketModels);
    ZonedDateTime convertLongToZonedDateTime(Long date);
    void validateTimestamps(Long start, Long end);
}
