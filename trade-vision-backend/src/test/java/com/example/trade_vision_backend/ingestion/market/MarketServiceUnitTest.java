package com.example.trade_vision_backend.ingestion.market;

import com.example.trade_vision_backend.ingestion.market.application.MarketServiceImpl;
import com.example.trade_vision_backend.ingestion.market.domain.client.MarketClient;
import com.example.trade_vision_backend.ingestion.market.domain.dto.MarketWrapperDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MarketServiceUnitTest {
    @Mock
    private MarketClient marketClient;

    @InjectMocks
    private MarketServiceImpl marketService;

    private static final Long TIMESTAMP = 123456789L;

    @Test
    public void getMarketsData_ReturnsValidMarketWrapperDTO() {
        MarketWrapperDTO wrapperDTO = new MarketWrapperDTO(
                createValidMarketDTOs(),
                TIMESTAMP
        );

        when(marketClient.getMarkets()).thenReturn(wrapperDTO);

        MarketWrapperDTO result = assertDoesNotThrow(() ->
                marketClient.getMarkets());

        assertNotNull(result);
        assertFalse(result.markets().isEmpty());
        assertEquals(100, result.markets().size());
    }

    @Test
    public void getMarketsData_ThrowsRestClientExceptionOnEmptyData() {
        MarketWrapperDTO wrapperDTO = new MarketWrapperDTO(
                Collections.emptySet(),
                TIMESTAMP
        );

        when(marketClient.getMarkets()).thenReturn(wrapperDTO);

        RestClientException exception = assertThrows(RestClientException.class,
                () -> marketService.getMarketsData());

        assertTrue(exception.getMessage().contains("Client failed to fetch market wrapper data"));
    }

    @Test
    public void getMarketsData_ThrowsRestClientExceptionOnInvalidDataSize() {
        MarketWrapperDTO wrapperDTO = new MarketWrapperDTO(
                Set.of(new RawMarketDTO(
                        "binance",
                        1,
                        "BTC",
                        "bitcoin",
                        "USDT",
                        "tether",
                        new BigDecimal("65000.00"),
                        new BigDecimal("65000.00"),
                        new BigDecimal("1500000000.00"),
                        new BigDecimal("5.25"),
                        1200,
                        1696252800000L,
                        null
                )),
                TIMESTAMP
        );

        when(marketClient.getMarkets()).thenReturn(wrapperDTO);

        RestClientException exception = assertThrows(RestClientException.class,
                () -> marketService.getMarketsData());

        assertTrue(exception.getMessage().contains("Client failed to fetch market wrapper data"));
    }

    @Test
    public void getMarketsData_ThrowsRestClientExceptionOnNullData() {
        MarketWrapperDTO wrapperDTO = new MarketWrapperDTO(null, TIMESTAMP);

        when(marketClient.getMarkets()).thenReturn(wrapperDTO);

        RestClientException exception = assertThrows(RestClientException.class,
                () -> marketService.getMarketsData());

        assertTrue(exception.getMessage().contains("An unexpected error occurred when fetching market data"));
    }

    @Test
    public void convertWrapperDataToRecord_ReturnsValidRawMarketDTOSet() {
        MarketWrapperDTO wrapperDTO = new MarketWrapperDTO(createValidMarketDTOs(), TIMESTAMP);

        Set<RawMarketDTO> result = assertDoesNotThrow(
                () -> marketService.convertWrapperDataToRecord(wrapperDTO));

        RawMarketDTO marketDTO = result.iterator().next();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(wrapperDTO.timestamp(), marketDTO.timestamp());
        assertEquals(100, result.size());
    }

    @Test
    public void rawMarketDTOToModel_ConvertsDTOToModelSuccessfully() {
        Set<RawMarketDTO> marketDTOS = createValidMarketDTOs();

        List<RawMarketModel> result = assertDoesNotThrow(
                () -> marketService.rawMarketDTOToModel(marketDTOS));

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(100, result.size());

    }

    private static Set<RawMarketDTO> createValidMarketDTOs() {
        Set<RawMarketDTO> set = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            set.add(new RawMarketDTO(
                    "binance",
                    i + 1,
                    "BTC",
                    "bitcoin",
                    "USDT",
                    "tether",
                    new BigDecimal("65000.00").add(new BigDecimal(i)),
                    new BigDecimal("65000.00"),
                    new BigDecimal("1500000000.00"),
                    new BigDecimal("5.25"),
                    1200 + i,
                    1696252800000L + i,
                    null
            ));
        }

        return set;
    }
}
