package com.example.trade_vision_backend.ingestion;

import com.example.trade_vision_backend.ingestion.market.RawMarketModel;

import java.util.List;

public interface IngestionDataService {
    List<ProcessableMarketDTO> getAllData();
}
