package com.example.trade_vision_backend.ingestion;

import org.jmolecules.event.types.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record IngestionCompleted(
        UUID id,
        int marketCount,
        Instant completedAt,
        Long ingestedTimestamp
) implements DomainEvent {
    public IngestionCompleted(UUID ingestionId, int marketCount, Long ingestedTimestamp) {
        this(
                ingestionId,
                marketCount,
                Instant.now(),
                ingestedTimestamp
        );
    }
}
