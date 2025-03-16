package com.example.trade_vision_backend.strategies.ema;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
@RequiredArgsConstructor
public class SimpleMovingAverageServiceImpl implements SimpleMovingAverageService {
    

    @Override
    public BigDecimal sumClosingPrice() {
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
