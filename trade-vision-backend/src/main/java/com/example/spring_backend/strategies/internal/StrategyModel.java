package com.example.spring_backend.strategies.internal;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.Map;
import java.util.List;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StrategyModel {
    private String key;
    private String name;
    private String description;
    private Map<String, ParameterDefinition> parameters;

    @Setter
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ParameterDefinition {
        private String type;
        private Object defaultValue;
        private String label;
        private Double step;
        private List<ParameterOption> options;

        public ParameterDefinition(String type, Object defaultValue, String label) {
            this.type = type;
            this.defaultValue = defaultValue;
            this.label = label;
        }

        public ParameterDefinition(String type, Object defaultValue, String label, Double step) {
            this.type = type;
            this.defaultValue = defaultValue;
            this.label = label;
            this.step = step;
        }

        public ParameterDefinition(String type, Object defaultValue, String label, List<ParameterOption> options) {
            this.type = type;
            this.defaultValue = defaultValue;
            this.label = label;
            this.options = options;
        }
    }

    @Setter
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ParameterOption {
        private String value;
        private String label;
    }
}