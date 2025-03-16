package com.example.trade_vision_backend.processing.internal;

import com.example.trade_vision_backend.ingestion.ProcessableMarketDTO;
import com.example.trade_vision_backend.ingestion.market.RawMarketModel;
import com.example.trade_vision_backend.processing.ProcessedMarketModel;
import com.example.trade_vision_backend.processing.internal.infrastructure.db.ProcessingRepository;
import com.example.trade_vision_backend.processing.internal.infrastructure.exception.ProcessingException;
import com.example.trade_vision_backend.processing.internal.infrastructure.service.ProcessingServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class ProcessingServiceUnitTest {

    @Mock
    private ProcessingRepository repository;

    @InjectMocks
    private ProcessingServiceImpl processingService;

    private static final Long MOCK_TIMESTAMP = 123456789L;

    @Test
    public void transformToMarketModel_SuccessfullyReturnsMarketModelSet() {
        Set<ProcessableMarketDTO> processableMarketDTOS = createValidMarketModels();

        List<ProcessedMarketModel> result = assertDoesNotThrow(
                () -> processingService.transformToMarketModel(processableMarketDTOS, MOCK_TIMESTAMP));

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(100, result.size());

        for (ProcessedMarketModel model : result) {
            assertNotNull(model.getTimestamp());
        }
    }

    @Transactional
    @Test
    public void executeProcessing_SuccessfullyExecutesFullProcessingFlow() {
        Set<ProcessableMarketDTO> validSet = createValidMarketModels();

        assertDoesNotThrow(
                () -> processingService.executeProcessing(validSet, MOCK_TIMESTAMP));
    }

    @Test
    public void saveProcessedData_SuccessfullySavesData() {
        List<ProcessedMarketModel> processedMarketModels = createValidMarketModelList();

        when(repository.saveAll(processedMarketModels)).thenReturn(processedMarketModels);
        when(repository.findAll()).thenReturn(processedMarketModels);

        assertDoesNotThrow(() -> processingService.saveProcessedData(processedMarketModels));

        verify(repository, times(1)).saveAll(processedMarketModels);

        List<ProcessedMarketModel> savedData = repository.findAll();

        assertFalse(savedData.isEmpty());
        assertEquals(100, savedData.size());
        assertEquals(processedMarketModels, savedData);
    }

    @Test
    public void executeProcessing_ThrowsProcessingExceptionOnEmptyData() {
        ProcessingException exception = assertThrows(
                ProcessingException.class, () -> processingService.executeProcessing(Collections.emptySet(), MOCK_TIMESTAMP));

        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("Failed to process due to invalid data"));
    }

    @Test
    public void executeProcessing_ThrowsProcessingExceptionOnInvalidDataSize() {
        Set<ProcessableMarketDTO> invalidSet = createInvalidMarketModels();

        ProcessingException exception = assertThrows(
                ProcessingException.class, () -> processingService.executeProcessing(invalidSet, MOCK_TIMESTAMP));

        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("Failed to process due to invalid data"));
    }

    private static Set<ProcessableMarketDTO> createValidMarketModels() {
        Set<ProcessableMarketDTO> set = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            set.add(new ProcessableMarketDTO(
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
                    123456789L
            ));
        }

        return set;
    }

    private static List<ProcessedMarketModel> createValidMarketModelList() {
        List<ProcessedMarketModel> list = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            list.add(new ProcessedMarketModel(
                    UUID.randomUUID(),
                    "binance",
                    "bitcoin",
                    "tether",
                    new BigDecimal("65000.00").add(new BigDecimal(i)),
                    1696252800000L + i,
                    ZonedDateTime.now(),
                    Instant.now()
            ));
        }

        return list;
    }

    private static Set<ProcessableMarketDTO> createInvalidMarketModels() {
        Set<ProcessableMarketDTO> set = new HashSet<>();
        for (int i = 0; i < 10; i++) {
            set.add(new ProcessableMarketDTO(
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
                    123456789L
            ));
        }

        return set;
    }
}
