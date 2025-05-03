package com.example.trade_vision_backend.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@Builder
public class ConditionConfig {
    private String type;
    private Map<String, Object> parameters;
}