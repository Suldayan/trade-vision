package com.example.trade_vision_backend.strategies.internal;

import com.example.trade_vision_backend.processing.ProcessedMarketDTO;
import com.example.trade_vision_backend.processing.ProcessingDataService;
import com.example.trade_vision_backend.strategies.SimpleMovingAverageService;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class SimpleMovingAverageServiceImpl implements SimpleMovingAverageService {
    private final ProcessingDataService processingDataService;

    @Override
    public BigDecimal sumClosingPrice(
            @Nonnull Long startDate,
            @Nonnull Long endDate
    ) {
        List<ProcessedMarketDTO> models = processingDataService.fetchAllMarketModelsByTimeRange(startDate, endDate);
        return null;
    }

    @Override
    public Integer getTotalNumberOfPeriods() {
        return 0;
    }

    @Override
    public BigDecimal calculateAverage() {
        return null;
    }
}
