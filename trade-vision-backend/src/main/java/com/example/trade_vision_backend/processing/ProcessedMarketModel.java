package com.example.trade_vision_backend.processing;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "processed_markets")
@EqualsAndHashCode(of = {"baseId", "quoteId", "exchangeId"})
public class ProcessedMarketModel {
        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        @Column(name = "id")
        private UUID id;

        @Column(name = "base_id", nullable = false)
        private String baseId;

        @Column(name = "quote_id", nullable = false)
        private String quoteId;

        @Column(name = "exchange_id", nullable = false)
        private String exchangeId;

        @Column(name = "price_usd", nullable = false)
        private BigDecimal priceUsd;

        @Column(name = "updated", nullable = false)
        private Long updated;

        @Column(name = "timestamp", nullable = false)
        private ZonedDateTime timestamp;

        @Column(name = "created_at")
        private Instant createdAt;
}