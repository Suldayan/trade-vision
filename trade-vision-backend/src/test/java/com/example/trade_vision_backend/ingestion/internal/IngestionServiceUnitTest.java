package com.example.trade_vision_backend.ingestion.internal;

import com.example.trade_vision_backend.ingestion.IngestionManagement;
import com.example.trade_vision_backend.ingestion.internal.infrastructure.service.IngestionServiceImpl;
import com.example.trade_vision_backend.ingestion.internal.infrastructure.repository.IngestionRepository;
import com.example.trade_vision_backend.ingestion.market.MarketService;
import com.example.trade_vision_backend.ingestion.market.RawMarketModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class IngestionServiceUnitTest {

    @Mock
    private MarketService marketService;

    @Mock
    private IngestionRepository ingestionRepository;

    @Mock
    private IngestionManagement ingestionManagement;

    @InjectMocks
    private IngestionServiceImpl ingestionService;

    @Test
    void saveMarketData_UpdatesAndSavesExistingDataSuccessfully() {
        List<RawMarketModel> updatedModels = createUpdatedValidMarketModelList();

        when(ingestionRepository.saveAll(any())).thenReturn(updatedModels);
        when(ingestionRepository.findAll()).thenReturn(updatedModels);

        assertDoesNotThrow(() -> ingestionService.saveMarketData(updatedModels));

        List<RawMarketModel> savedData = ingestionRepository.findAll();

        assertFalse(savedData.isEmpty());
        assertEquals(100, savedData.size());
        assertEquals(updatedModels, savedData);
    }

    private static List<RawMarketModel> createUpdatedValidMarketModelList() {
        List<RawMarketModel> list = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            list.add(new RawMarketModel(
                    UUID.randomUUID(),
                    "binance",
                    i + 1,
                    "BTC",
                    "bitcoin",
                    "USDT",
                    "tether",
                    new BigDecimal("25000.00").add(new BigDecimal(i)),
                    new BigDecimal("45000.00"),
                    new BigDecimal("9500000000.00"),
                    new BigDecimal("10.25"),
                    3200 + i,
                    1732252800000L + i
            ));
        }

        return list;
    }
}
