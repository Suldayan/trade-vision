package com.example.trade_vision_backend.ingestion.market.domain.dto;

import com.example.trade_vision_backend.ingestion.market.RawMarketDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nonnull;
import lombok.Builder;

import java.util.Set;

@Builder
public record MarketWrapperDTO(
        @JsonProperty(value = "data") @Nonnull Set<RawMarketDTO> markets,
        @JsonProperty(value = "timestamp") @Nonnull Long timestamp)
{}