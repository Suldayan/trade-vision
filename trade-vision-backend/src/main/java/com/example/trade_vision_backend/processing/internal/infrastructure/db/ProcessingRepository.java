package com.example.trade_vision_backend.processing.internal.infrastructure.db;

import com.example.trade_vision_backend.processing.ProcessedMarketModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface ProcessingRepository extends JpaRepository<ProcessedMarketModel, UUID> {
    List<ProcessedMarketModel> findAllByTimestampBetween(
            ZonedDateTime startTimestamp,
            ZonedDateTime endTimestamp);

    List<ProcessedMarketModel> findByBaseIdAndTimestampBetween(
            String baseId,
            ZonedDateTime startTimestamp,
            ZonedDateTime endTimestamp
    );

    List<ProcessedMarketModel> findByQuoteIdAndTimestampBetween(
            String quoteId,
            ZonedDateTime startTimestamp,
            ZonedDateTime endTimestamp
    );

    List<ProcessedMarketModel> findByExchangeIdAndTimestampBetween(
            String exchangeId,
            ZonedDateTime startTimestamp,
            ZonedDateTime endTimestamp
    );
}