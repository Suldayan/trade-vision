package com.example.spring_backend.strategies.internal;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/strategies")
@RequiredArgsConstructor
public class StrategyController {
    private final StrategyManager strategyManager;

    @GetMapping("/all")
    public ResponseEntity<List<StrategyModel>> getAllStrategies() {
        List<StrategyModel> strategies = strategyManager.getAllStrategies();
        return ResponseEntity.ok(strategies);
    }

    @GetMapping("/{key}")
    public ResponseEntity<StrategyModel> getStrategy(@PathVariable String key) {
        StrategyModel strategy = strategyManager.getStrategy(key);
        if (strategy != null) {
            return ResponseEntity.ok(strategy);
        }
        return ResponseEntity.notFound().build();
    }
}