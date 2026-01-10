package com.example.spring_backend.common;

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