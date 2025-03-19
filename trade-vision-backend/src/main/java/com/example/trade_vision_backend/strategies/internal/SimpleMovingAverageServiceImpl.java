package com.example.trade_vision_backend.strategies.internal;

import com.example.trade_vision_backend.processing.ProcessingDataService;
import com.example.trade_vision_backend.strategies.SimpleMovingAverageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
@RequiredArgsConstructor
public class SimpleMovingAverageServiceImpl implements SimpleMovingAverageService {
    private final ProcessingDataService processingDataService;

    @Override
    public BigDecimal sumClosingPrice(Integer timePeriod) {
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
