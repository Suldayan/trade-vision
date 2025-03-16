package com.example.trade_vision_backend.ingestion;

import com.example.trade_vision_backend.ingestion.internal.infrastructure.service.IngestionService;
import com.example.trade_vision_backend.ingestion.internal.infrastructure.repository.IngestionRepository;
import com.example.trade_vision_backend.ingestion.market.MarketService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.modulith.test.Scenario;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;

@ApplicationModuleTest(ApplicationModuleTest.BootstrapMode.DIRECT_DEPENDENCIES)
@ActiveProfiles("test")
public class IngestionIntegrationTests {

    @MockitoBean
    private MarketService marketService;

    @MockitoBean
    private IngestionRepository ingestionRepository;

    @MockitoBean
    private IngestionManagement ingestionManagement;

    @Autowired
    private IngestionService ingestionService;

    @Test
    public void eventReceivedThroughPublishing(Scenario scenario) {
        UUID testID = UUID.randomUUID();

        scenario.publish(
                new IngestionCompleted(
                        testID,
                        100,
                        Instant.now(),
                        123456789L
                )
        ).andWaitForEventOfType(IngestionCompleted.class)
                .matching(event -> event.id().equals(testID) && event.marketCount() == 100)
                .toArriveAndVerify(event -> {
                    assertNotNull(event);
                    assertEquals(testID, event.id());
                    assertEquals(123456789L, event.ingestedTimestamp());
                    assertEquals(100, event.marketCount());
                });
    }

}
