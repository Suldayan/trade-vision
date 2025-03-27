package com.example.trade_vision_backend.processing;

import com.example.trade_vision_backend.ingestion.IngestionCompleted;
import com.example.trade_vision_backend.ingestion.IngestionDataService;
import com.example.trade_vision_backend.ingestion.ProcessableMarketDTO;
import com.example.trade_vision_backend.processing.internal.infrastructure.exception.ProcessingException;
import com.example.trade_vision_backend.processing.internal.infrastructure.service.ProcessingService;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@Service
@Slf4j
@RequiredArgsConstructor
public class ProcessingManagement {
    private final IngestionDataService ingestionDataService;
    private final ProcessingService processingService;

    @ApplicationModuleListener
    public void activateProcessing(@Nonnull IngestionCompleted ingestionCompleted) throws ProcessingException {
        List<ProcessableMarketDTO> unprocessedData = ingestionDataService.getAllData();
        final Long timestamp = ingestionCompleted.ingestedTimestamp();
        validateMarkets(unprocessedData);
        log.info("Processing has been triggered for data of timestamp: {}", timestamp);
        processingService.executeProcessing(unprocessedData, timestamp);
    }

    private void validateMarkets(@Nonnull List<ProcessableMarketDTO> unprocessedData) {
        if (unprocessedData.isEmpty()) {
            throw new IllegalArgumentException("Expected data to be in the repository but nothing is available");
        }
    }
}
