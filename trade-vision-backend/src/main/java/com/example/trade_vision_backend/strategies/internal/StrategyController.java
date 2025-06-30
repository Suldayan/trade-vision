package com.example.trade_vision_backend.strategies.internal;

import com.example.trade_vision_backend.strategies.StrategyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/strategy")
public class StrategyController {
    private final StrategyService strategyService;

}
