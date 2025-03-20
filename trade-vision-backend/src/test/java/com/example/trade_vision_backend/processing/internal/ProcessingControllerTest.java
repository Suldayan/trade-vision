package com.example.trade_vision_backend.processing.internal;

import com.example.trade_vision_backend.processing.ProcessedMarketDTO;
import com.example.trade_vision_backend.processing.ProcessedMarketModel;
import com.example.trade_vision_backend.processing.ProcessingDataService;
import com.example.trade_vision_backend.processing.internal.infrastructure.controller.ProcessingController;
import com.example.trade_vision_backend.processing.internal.infrastructure.db.ProcessingRepository;
import com.example.trade_vision_backend.processing.internal.infrastructure.service.ProcessingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProcessingControllerTest {
    @Mock
    private ProcessingDataService processingDataService;

    @InjectMocks
    private ProcessingController processingController;

    private MockMvc mvc;

    private JacksonTester<List<ProcessedMarketDTO>> jsonMarketModel;

    private final ZonedDateTime TIMESTAMP = ZonedDateTime.now().minusMonths(6);

    private final List<ProcessedMarketDTO> batch = new ArrayList<>();

    private static final String BASE_URL = "/api/v1/processing";

    @BeforeEach
    public void setup() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        JacksonTester.initFields(this, objectMapper);
        mvc = MockMvcBuilders.standaloneSetup(processingController)
                .build();

        for (int i = 0; i < 100; i++) {
            ProcessedMarketDTO model = ProcessedMarketDTO.builder()
                    .baseId("BTC")
                    .priceUsd(new BigDecimal("45000.50"))
                    .updated(System.currentTimeMillis())
                    .exchangeId("Binance")
                    .quoteId("USDT")
                    .timestamp(TIMESTAMP)
                    .createdAt(Instant.now())
                    .build();
            batch.add(model);
        }
    }

    @Test
    public void canRetrieveByTimestampWhenExistsOnAllEndpoint() throws Exception {
        // Seconds to subtract is equal to a year
        long zonedStartDate = Instant.now().minusSeconds(31536000).toEpochMilli();
        long zonedEndDate = Instant.now().toEpochMilli();

        when(processingDataService.fetchAllMarketModelsByTimeRange(zonedStartDate, zonedEndDate))
                .thenReturn(batch);

        MockHttpServletResponse response = mvc.perform(
                        MockMvcRequestBuilders.get(BASE_URL + "/all")
                                .param("startDate", String.valueOf(zonedStartDate))
                                .param("endDate", String.valueOf(zonedEndDate))
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        String responseContent = response.getContentAsString();

        assertEquals(response.getStatus(), HttpStatus.OK.value());
        assertNotNull(responseContent);
        assertThat(responseContent).isEqualTo(
                jsonMarketModel.write(batch).getJson()
        );
    }

    @Transactional
    @Test
    public void returnsEmptySetOnNonExistingTimestampedDataOnAllEndpoint() throws Exception {
        long startDateMillis = Instant.now().minusSeconds(1000).toEpochMilli();
        long endDateMillis = Instant.now().toEpochMilli();

        MockHttpServletResponse response = mvc.perform(
                        MockMvcRequestBuilders.get(BASE_URL + "/all")
                                .param("startDate", String.valueOf(startDateMillis))
                                .param("endDate", String.valueOf(endDateMillis))
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        String responseContent = response.getContentAsString();

        assertEquals(response.getStatus(), HttpStatus.OK.value());
        assertNotNull(responseContent);
        assertThat(responseContent).isEqualTo(
                jsonMarketModel.write(Collections.emptyList()).getJson()
        );
    }
}

