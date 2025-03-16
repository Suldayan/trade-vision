package com.example.trade_vision_backend.ingestion.internal.infrastructure.repository;

import com.example.trade_vision_backend.ingestion.market.RawMarketModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface IngestionRepository extends JpaRepository<RawMarketModel, UUID> {
}
