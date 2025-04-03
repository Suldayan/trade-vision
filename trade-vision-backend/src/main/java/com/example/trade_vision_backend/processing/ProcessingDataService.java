package com.example.trade_vision_backend.processing;

import java.time.ZonedDateTime;
import java.util.List;

public interface ProcessingDataService {
    List<ProcessedMarketDTO> fetchAllMarketModelsByTimeRange(Long startDate, Long endDate) throws IllegalArgumentException;
    List<CandleDTO> fetchAllCandlePairsWithinTimeRange(String baseId, String quoteId, String exchangeId, int period);
}
