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
    //TODO update with JPQL
    List<ProcessedMarketModel> findAllByTimestampBetween(
            ZonedDateTime startTimestamp,
            ZonedDateTime endTimestamp);
}