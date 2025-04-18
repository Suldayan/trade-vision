package com.example.trade_vision_backend.backtester.internal;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Builder
public class ConditionConfig {
    private String type;
    private final Map<String, Object> parameters = new HashMap<>();
}