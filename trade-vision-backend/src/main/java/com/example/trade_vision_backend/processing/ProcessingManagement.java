package com.example.trade_vision_backend.processing;

import com.example.trade_vision_backend.ingestion.IngestionCompleted;
import com.example.trade_vision_backend.ingestion.IngestionDataService;
import com.example.trade_vision_backend.ingestion.ProcessableMarketDTO;
import com.example.trade_vision_backend.ingestion.market.RawMarketModel;
import com.example.trade_vision_backend.processing.internal.infrastructure.exception.ProcessingException;
import com.example.trade_vision_backend.processing.internal.infrastructure.mapper.ProcessingMapper;
import com.example.trade_vision_backend.processing.internal.infrastructure.service.ProcessingService;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Transactional
@Service
@Slf4j
@RequiredArgsConstructor
public class ProcessingManagement {
    private final IngestionDataService ingestionDataService;
    private final ProcessingService processingService;
    private final ProcessingMapper mapper;

    private static final Integer EXPECTED_SIZE = 100;

    @ApplicationModuleListener
    public void activateProcessing(@Nonnull IngestionCompleted ingestionCompleted) throws ProcessingException {
        List<ProcessableMarketDTO> unprocessedData = ingestionDataService.getAllData();
        final Long timestamp = ingestionCompleted.ingestedTimestamp();;
        validateMarkets(unprocessedData);
        log.info("Processing has been triggered for data of timestamp: {}", timestamp);
        Set<ProcessableMarketDTO> unprocessedSet = mapper.INSTANCE.listToSet(unprocessedData);
        processingService.executeProcessing(unprocessedSet, timestamp);
    }

    private void validateMarkets(@Nonnull List<ProcessableMarketDTO> unprocessedData) {
        if (unprocessedData.isEmpty()) {
            throw new IllegalArgumentException("Expected data to be in the repository but nothing is available");
        }
        if (unprocessedData.size() != EXPECTED_SIZE) {
            log.error("Data size returned as {}/100", unprocessedData.size());
            throw new IllegalArgumentException("Data is not of valid size");
        }
    }
}
