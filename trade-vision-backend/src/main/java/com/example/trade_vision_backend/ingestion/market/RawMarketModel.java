package com.example.trade_vision_backend.ingestion.market;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.*;

import java.math.BigDecimal;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "raw_markets")
@EqualsAndHashCode(of = {"baseId", "quoteId", "exchangeId"})
public class RawMarketModel {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "exchange_id", nullable = false)
    private String exchangeId;

    @Column(name = "rank")
    private Integer rank;

    @Column(name = "base_symbol")
    private String baseSymbol;

    @Column(name = "base_id", nullable = false)
    private String baseId;

    @Column(name = "quote_symbol")
    private String quoteSymbol;

    @Column(name = "quote_id", nullable = false)
    private String quoteId;

    @Column(name = "price_quote", nullable = false)
    private BigDecimal priceQuote;

    @Column(name = "price_usd", nullable = false)
    private BigDecimal priceUsd;

    @Column(name = "volume_usd_24hr", nullable = false)
    private BigDecimal volumeUsd24Hr;

    @Column(name = "percent_exchange_volume", nullable = false)
    private BigDecimal percentExchangeVolume;

    @Column(name = "trades_count_24hr")
    private Integer tradesCount24Hr;

    @Column(name = "updated", nullable = false)
    private Long updated;

    @Column(name = "timestamp")
    private Long timestamp;

    @Version
    @Column(name = "version")
    private Integer version;

    public RawMarketModel(UUID id, String exchangeId, Integer rank, String baseSymbol, String baseId,
                          String quoteSymbol, String quoteId, BigDecimal priceQuote, BigDecimal priceUsd,
                          BigDecimal volumeUsd24Hr, BigDecimal percentExchangeVolume, Integer tradesCount24Hr,
                          Long updated) {
        this.id = id;
        this.exchangeId = exchangeId;
        this.rank = rank;
        this.baseSymbol = baseSymbol;
        this.baseId = baseId;
        this.quoteSymbol = quoteSymbol;
        this.quoteId = quoteId;
        this.priceQuote = priceQuote;
        this.priceUsd = priceUsd;
        this.volumeUsd24Hr = volumeUsd24Hr;
        this.percentExchangeVolume = percentExchangeVolume;
        this.tradesCount24Hr = tradesCount24Hr;
        this.updated = updated;
    }
}

