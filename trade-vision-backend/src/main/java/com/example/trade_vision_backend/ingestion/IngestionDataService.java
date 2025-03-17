package com.example.trade_vision_backend.ingestion;

import java.util.List;

public interface IngestionDataService {
    List<ProcessableMarketDTO> getAllData();
}
