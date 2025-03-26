package com.example.trade_vision_backend.processing.internal.infrastructure.db;

import com.example.trade_vision_backend.processing.ProcessedCandleModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CandleRepository extends JpaRepository<ProcessedCandleModel, UUID> {
}
