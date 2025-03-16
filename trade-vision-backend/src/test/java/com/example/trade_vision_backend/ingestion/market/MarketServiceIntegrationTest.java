package com.example.trade_vision_backend.ingestion.market;

import com.example.trade_vision_backend.ingestion.market.domain.dto.MarketWrapperDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class MarketServiceIntegrationTest {

    @Autowired
    private MarketService marketService;

    @Test
    public void getMarketsData_SuccessfullyReturnsMarketWrapperDTO() {
        MarketWrapperDTO result = assertDoesNotThrow(
                () -> marketService.getMarketsData()
        );

        Set<RawMarketDTO> resultSet = result.markets();

        assertNotNull(result);
        assertFalse(resultSet.isEmpty());
        assertEquals(100, resultSet.size());
    }

    @Test
    public void convertWrapperDataToRecord_SuccessfullyConvertsWrapperSetToRecordSet() {
        MarketWrapperDTO wrapperDTO = assertDoesNotThrow(
                () -> marketService.getMarketsData()
        );

        Set<RawMarketDTO> result = assertDoesNotThrow(
                () -> marketService.convertWrapperDataToRecord(wrapperDTO)
        );

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(100, result.size());
    }

    @Test
    public void  rawMarketDTOToModel_SuccessfullyConvertsDTOSetToModelList() {
        MarketWrapperDTO wrapperDTO = assertDoesNotThrow(
                () -> marketService.getMarketsData()
        );
        Set<RawMarketDTO> dtoSet = assertDoesNotThrow(
                () -> marketService.convertWrapperDataToRecord(wrapperDTO)
        );
        List<RawMarketModel> result = assertDoesNotThrow(
                () -> marketService.rawMarketDTOToModel(dtoSet)
        );

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(100, result.size());
    }
}
