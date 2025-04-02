package com.example.trade_vision_backend.strategies;

import com.example.trade_vision_backend.backtester.BackTestEvent;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class StrategyManagement {
    private final EMAService emaService;

    private static final String EMA = "ema";

    @ApplicationModuleListener
    public void executeStrategy(@Nonnull BackTestEvent event) {
        final String strategy = event.strategy();
        final String baseId = event.baseId();
        final String quoteId = event.quoteId();
        final String exchangeId = event.exchangeId();
        final Long window = event.window();

        switch (strategy.toLowerCase()) {
            case EMA -> emaService.calculateEMA(
                    baseId,
                    quoteId,
                    exchangeId,
                    window
            );
        }
    }
}
