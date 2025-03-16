package com.example.trade_vision_backend.ingestion;

import com.example.trade_vision_backend.ingestion.market.RawMarketDTO;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Transactional
@Service
@Slf4j
@RequiredArgsConstructor
public class IngestionManagement {
    private final ApplicationEventPublisher eventPublisher;
    private final static Integer EXPECTED_MARKET_COUNT = 100;

    public void processFieldsForEvent(@Nonnull Set<RawMarketDTO> data) {
        if (data.isEmpty()) {
            throw new IllegalArgumentException("Data set is empty");
        }

        RawMarketDTO firstItem = data.iterator().next();
        Long timestamp = firstItem.timestamp();

        if (timestamp == null) {
            throw new IllegalArgumentException("Timestamp cannot be null");
        }

        int size = data.size();
        validateFields(size, timestamp, data);
        complete(size, timestamp);
    }

    public void complete(int size, @Nonnull Long timestamp) {
        IngestionCompleted event = new IngestionCompleted(
                UUID.randomUUID(),
                size,
                timestamp
        );
        eventPublisher.publishEvent(event);
        log.info("Ingestion event sent with size: {} and timestamp: {}", size, timestamp);
    }

    private void validateFields(int size, @Nonnull Long timestamp, @Nonnull Set<RawMarketDTO> data) {
        if (size != EXPECTED_MARKET_COUNT) {
            throw new IllegalArgumentException(
                    String.format("Expected %d markets, but received %d", EXPECTED_MARKET_COUNT, size)
            );
        }
        if (timestamp < 0) {
            throw new IllegalArgumentException("Invalid negative timestamp: " + timestamp);
        }

        Set<String> invalidDataIds = new HashSet<>();

        for (RawMarketDTO dto : data) {
            if (!timestamp.equals(dto.timestamp())) {
                invalidDataIds.add(dto.baseId());
            }
        }
        if (!invalidDataIds.isEmpty()) {
            log.error("Invalid timestamped data has been found for these data ids: {}", invalidDataIds);
            throw new IllegalArgumentException(
                    "Given timestamp does not match the timestamp of " + invalidDataIds.size() + " data models"
            );
        }
    }
}
