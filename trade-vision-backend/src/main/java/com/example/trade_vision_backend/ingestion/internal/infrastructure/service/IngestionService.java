package com.example.trade_vision_backend.ingestion.internal.infrastructure.service;

import com.example.trade_vision_backend.ingestion.internal.infrastructure.exception.IngestionException;
import com.example.trade_vision_backend.ingestion.market.RawMarketDTO;
import com.example.trade_vision_backend.ingestion.market.RawMarketModel;

import java.util.List;
import java.util.Set;

public interface IngestionService {
    void sendEvent(Set<RawMarketDTO> marketDTOS);
    void executeIngestion();
    void saveMarketData(List<RawMarketModel> latestFetchedData) throws IngestionException;
}
