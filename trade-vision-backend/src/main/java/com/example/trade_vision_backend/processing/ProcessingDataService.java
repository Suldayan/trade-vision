package com.example.trade_vision_backend.processing;

import java.util.List;

public interface ProcessingDataService {
    List<ProcessedMarketDTO> fetchAllMarketModelsByTimeRange(Long startDate, Long endDate) throws IllegalArgumentException;
    List<ProcessedMarketDTO> fetchModelByBaseIdAndTimeRange(Long startDate, Long endDate, String id) throws IllegalArgumentException;
    List<ProcessedMarketDTO> fetchModelByQuoteIdAndTimeRange(Long startDate, Long endDate, String id) throws IllegalArgumentException;
    List<ProcessedMarketDTO> fetchModelByExchangeIdAndTimeRange(Long startDate, Long endDate, String id) throws IllegalArgumentException;

}
