package com.example.trade_vision_backend.strategies;

import com.example.trade_vision_backend.backtester.StrategyEvent;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class StrategyManagement {

    @ApplicationModuleListener
    public void executeStrategy(@Nonnull StrategyEvent event) {

    }
}
