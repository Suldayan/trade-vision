package com.example.trade_vision_backend.strategies;

import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class StrategyManagement {

    @ApplicationModuleListener
    public void executeStrategy() {

    }
}
