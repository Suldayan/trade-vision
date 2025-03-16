package com.example.trade_vision_backend.ingestion.internal;

import com.example.trade_vision_backend.ingestion.internal.infrastructure.repository.IngestionRepository;
import com.example.trade_vision_backend.ingestion.internal.infrastructure.service.IngestionService;
import com.example.trade_vision_backend.ingestion.market.MarketService;
import com.example.trade_vision_backend.ingestion.market.RawMarketDTO;
import com.example.trade_vision_backend.ingestion.market.RawMarketModel;
import com.example.trade_vision_backend.ingestion.market.domain.dto.MarketWrapperDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class IngestionServiceIntegrationTest {

    @Autowired
    private IngestionService ingestionService;

    @Test
    public void executeIngestion_SuccessfullyExecutesEntireFlow() {
        assertDoesNotThrow(() -> ingestionService.executeIngestion());
    }
}
