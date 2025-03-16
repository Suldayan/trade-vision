package com.example.trade_vision_backend.ingestion.market.domain.client;

import com.example.trade_vision_backend.ingestion.market.domain.dto.MarketWrapperDTO;
import org.springframework.web.service.annotation.GetExchange;

public interface MarketClient {
    @GetExchange(url = "/markets")
    MarketWrapperDTO getMarkets();
}