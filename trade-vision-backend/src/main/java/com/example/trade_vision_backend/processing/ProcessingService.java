package com.example.trade_vision_backend.processing;

import com.example.trade_vision_backend.ingestion.ProcessableMarketDTO;
import com.example.trade_vision_backend.processing.internal.infrastructure.exception.ProcessingException;

import java.time.ZonedDateTime;
import java.util.List;

public interface ProcessingService {
    List<ProcessedMarketModel> transformToMarketModel(List<ProcessableMarketDTO> processableMarketDTOS, Long timestamp) throws IllegalArgumentException;
    void executeProcessing(List<ProcessableMarketDTO> processableMarketDTOS, Long timestamp) throws ProcessingException;
    void saveProcessedData(List<ProcessedMarketModel> processedData) throws ProcessingException;
    List<ProcessedMarketModel> sortMarketModelsByTimestamp(List<ProcessedMarketModel> marketModels);
    ZonedDateTime convertLongToZonedDateTime(Long date);
    void validateTimestamps(Long start, Long end);
}
