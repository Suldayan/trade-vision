package com.example.trade_vision_backend.ingestion.market;

import com.example.trade_vision_backend.ingestion.market.domain.dto.MarketWrapperDTO;

import java.util.List;
import java.util.Set;

public interface MarketService {
    MarketWrapperDTO getMarketsData();
    Set<RawMarketDTO> convertWrapperDataToRecord(MarketWrapperDTO wrapper);
    List<RawMarketModel> rawMarketDTOToModel(Set<RawMarketDTO> marketDTOS);
}
