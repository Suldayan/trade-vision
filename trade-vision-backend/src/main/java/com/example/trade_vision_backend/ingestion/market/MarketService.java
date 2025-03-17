package com.example.trade_vision_backend.ingestion.market;

import com.example.trade_vision_backend.ingestion.market.domain.dto.MarketWrapperDTO;

import java.util.List;

public interface MarketService {
    MarketWrapperDTO getMarketsData();
    List<RawMarketDTO> convertWrapperDataToRecord(MarketWrapperDTO wrapper);
    List<RawMarketModel> rawMarketDTOToModel(List<RawMarketDTO> marketDTOS);
}
