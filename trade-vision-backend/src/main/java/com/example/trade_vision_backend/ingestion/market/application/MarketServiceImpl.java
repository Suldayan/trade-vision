package com.example.trade_vision_backend.ingestion.market.application;

import com.example.trade_vision_backend.ingestion.market.MarketService;
import com.example.trade_vision_backend.ingestion.market.domain.client.MarketClient;
import com.example.trade_vision_backend.ingestion.market.domain.dto.MarketWrapperDTO;
import com.example.trade_vision_backend.ingestion.market.RawMarketDTO;
import com.example.trade_vision_backend.ingestion.market.domain.mapper.RawMarketMapper;
import com.example.trade_vision_backend.ingestion.market.RawMarketModel;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class MarketServiceImpl implements MarketService {
    private final MarketClient marketClient;
    private final RawMarketMapper mapper;

    private static final Integer EXPECTED_MARKET_SIZE = 100;

    @Nonnull
    @Override
    public MarketWrapperDTO getMarketsData() {
        try {
            MarketWrapperDTO marketHolder = marketClient.getMarkets();
            validateMarketWrapper(marketHolder);
            log.info("Successfully fetched and validated all markets");
            return marketHolder;
        } catch (RestClientResponseException ex) {
            throw ex;
        } catch (RestClientException ex) {
            throw new RestClientException("Client failed to fetch market wrapper data:", ex);
        } catch (Exception ex) {
            throw new RestClientException("An unexpected error occurred when fetching market data");
        }
    }

    @Nonnull
    @Override
    public Set<RawMarketDTO> convertWrapperDataToRecord(@Nonnull MarketWrapperDTO marketWrapper) {
        Set<RawMarketDTO> marketSet = marketWrapper.markets()
                .stream()
                .filter(Objects::nonNull)
                .map(field -> RawMarketDTO.builder()
                        .exchangeId(field.exchangeId())
                        .rank(field.rank())
                        .baseSymbol(field.baseSymbol())
                        .baseId(field.baseId())
                        .quoteSymbol(field.quoteSymbol())
                        .quoteId(field.quoteId())
                        .priceQuote(field.priceQuote())
                        .priceUsd(field.priceUsd())
                        .volumeUsd24Hr(field.volumeUsd24Hr())
                        .percentExchangeVolume(field.percentExchangeVolume())
                        .tradesCount24Hr(field.tradesCount24Hr())
                        .updated(field.updated())
                        .timestamp(marketWrapper.timestamp())
                        .build())
                .collect(Collectors.toSet());
        if (marketSet.isEmpty()) {
            log.warn("Market set returned as empty. Endpoint might be returning incomplete data");
            throw new RestClientException("Market set fetched but is empty");
        }
        if (marketSet.size() != 100) {
            throw new RestClientException("Market set fetched but set size is not 100");
        }
        log.info("Converted wrapper data to record set of size: {}", marketSet.size());
        return marketSet;
    }

    @Override
    public List<RawMarketModel> rawMarketDTOToModel(Set<RawMarketDTO> marketDTOS) {
        return mapper.INSTANCE.dtoSetToEntitySet(marketDTOS);
    }

    private void validateMarketWrapper(@Nonnull MarketWrapperDTO marketHolder) {
        if (marketHolder.markets().stream().anyMatch(Objects::isNull)) {
            throw new RestClientException("An object in the market set has returned as null");
        }
        if (marketHolder.markets().isEmpty()) {
            throw new RestClientException("Market set from wrapper returned empty");
        }
        if (marketHolder.markets().size() != EXPECTED_MARKET_SIZE) {
            throw new RestClientException("Market set from wrapper is not equal to 100");
        }
        if (marketHolder.timestamp() < 0) {
            throw new RestClientException("Market data contains an invalid timestamp");
        }
    }
}
