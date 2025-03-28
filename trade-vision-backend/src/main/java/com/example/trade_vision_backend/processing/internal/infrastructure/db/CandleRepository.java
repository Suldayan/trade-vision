package com.example.trade_vision_backend.processing.internal.infrastructure.db;

import com.example.trade_vision_backend.processing.ProcessedCandleModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface CandleRepository extends JpaRepository<ProcessedCandleModel, UUID> {

    @Query("SELECT m FROM ProcessedCandleModel m WHERE m.baseId = :base AND m.quoteId = :quote " +
            "AND m.exchangeId = :exchange AND m.timeRange BETWEEN :startTime AND :endTime")
    List<ProcessedCandleModel> findMarketPairWithinTimeRange(
            @Param("base") String baseId,
            @Param("quote") String quoteId,
            @Param("exchange") String exchangeId,
            @Param("startTime") ZonedDateTime startTime, // will always be from now till
            @Param("endTime") ZonedDateTime endTime
    );
}
