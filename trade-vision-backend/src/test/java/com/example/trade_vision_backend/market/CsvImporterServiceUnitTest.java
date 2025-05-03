package com.example.trade_vision_backend.market;

import com.example.trade_vision_backend.market.internal.CsvImporterServiceImpl;
import com.example.trade_vision_backend.market.internal.DataExtractor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CSV Importer Service Tests")
class CsvImporterServiceUnitTest {

    @Mock
    private DataExtractor dataExtractor;

    @InjectMocks
    private CsvImporterServiceImpl csvImporterService;

    @Nested
    @DisplayName("CSV Data Import Tests")
    class CsvDataImportTests {
        @Test
        @DisplayName("Should successfully import valid CSV data")
        void shouldImportValidCsvData() {
            String csvContent = """
                    timestamp,open,high,low,close,adjusted_close,volume,dividend_amount,split_coefficient
                    2023-01-01,100.0,105.0,95.0,102.0,102.0,1000,0.0,1.0
                    2023-01-02,102.0,110.0,100.0,108.0,108.0,2000,0.0,1.0
                    2023-01-03,108.0,115.0,105.0,112.0,112.0,1500,0.5,1.0
                    """;
            InputStream inputStream = createInputStream(csvContent);

            MarketData result = csvImporterService.importCsvFromStream(inputStream);

            List<MarketDataPoint> dataPoints = result.getDataPoints();
            assertEquals(3, dataPoints.size());

            MarketDataPoint firstPoint = dataPoints.getFirst();
            assertEquals(LocalDate.of(2023, 1, 1).atStartOfDay(), firstPoint.timestamp());
            assertEquals(100.0, firstPoint.open());
            assertEquals(105.0, firstPoint.high());
            assertEquals(95.0, firstPoint.low());
            assertEquals(102.0, firstPoint.close());
            assertEquals(102.0, firstPoint.adjustedClose());
            assertEquals(1000L, firstPoint.volume());
            assertEquals(0.0, firstPoint.dividendAmount());
            assertEquals(1.0, firstPoint.splitCoefficient());

            MarketDataPoint lastPoint = dataPoints.get(2);
            assertEquals(LocalDate.of(2023, 1, 3).atStartOfDay(), lastPoint.timestamp());
            assertEquals(108.0, lastPoint.open());
            assertEquals(115.0, lastPoint.high());
            assertEquals(105.0, lastPoint.low());
            assertEquals(112.0, lastPoint.close());
            assertEquals(112.0, lastPoint.adjustedClose());
            assertEquals(1500L, lastPoint.volume());
            assertEquals(0.5, lastPoint.dividendAmount());
            assertEquals(1.0, lastPoint.splitCoefficient());
        }

        @Test
        @DisplayName("Should handle case-insensitive CSV headers")
        void shouldHandleCaseInsensitiveHeaders() {
            String csvContent = """
                    Timestamp,Open,High,Low,Close,Adjusted_Close,Volume,Dividend_Amount,Split_Coefficient
                    2023-01-01,100.0,105.0,95.0,102.0,102.0,1000,0.0,1.0
                    2023-01-02,102.0,110.0,100.0,108.0,108.0,2000,0.0,1.0
                    """;
            InputStream inputStream = createInputStream(csvContent);

            MarketData result = csvImporterService.importCsvFromStream(inputStream);

            List<MarketDataPoint> dataPoints = result.getDataPoints();

            assertEquals(2, dataPoints.size());
            assertEquals(100.0, dataPoints.get(0).open());
            assertEquals(110.0, dataPoints.get(1).high());
        }

        @Test
        @DisplayName("Should skip empty lines in CSV")
        void shouldSkipEmptyLines() {
            String csvContent = """
                    timestamp,open,high,low,close,adjusted_close,volume,dividend_amount,split_coefficient
                    2023-01-01,100.0,105.0,95.0,102.0,102.0,1000,0.0,1.0
                    
                    2023-01-02,102.0,110.0,100.0,108.0,108.0,2000,0.0,1.0
                    """;
            InputStream inputStream = createInputStream(csvContent);

            MarketData result = csvImporterService.importCsvFromStream(inputStream);

            List<MarketDataPoint> dataPoints = result.getDataPoints();
            assertEquals(2, dataPoints.size());
        }
    }

    @Nested
    @DisplayName("Date Format Handling Tests")
    class DateFormatTests {
        @Test
        @DisplayName("Should parse various date formats correctly")
        void shouldParseDifferentDateFormats() {
            String csvContent = """
                    timestamp,open,high,low,close,adjusted_close,volume,dividend_amount,split_coefficient
                    2023-01-01,100.0,105.0,95.0,102.0,102.0,1000,0.0,1.0
                    01/02/2023,102.0,110.0,100.0,108.0,108.0,2000,0.0,1.0
                    2023/01/03,108.0,115.0,105.0,112.0,112.0,1500,0.5,1.0
                    2023-01-04T10:30:00,110.0,118.0,108.0,115.0,115.0,2500,0.0,1.0
                    2023-01-05 14:45:30,115.0,120.0,112.0,118.0,118.0,3000,0.0,1.0
                    """;
            InputStream inputStream = createInputStream(csvContent);

            MarketData result = csvImporterService.importCsvFromStream(inputStream);
            List<MarketDataPoint> dataPoints = result.getDataPoints();

            assertEquals(5, dataPoints.size());
            assertEquals(LocalDate.of(2023, 1, 1).atStartOfDay(), dataPoints.get(0).timestamp());
            assertEquals(LocalDate.of(2023, 1, 2).atStartOfDay(), dataPoints.get(1).timestamp());
            assertEquals(LocalDate.of(2023, 1, 3).atStartOfDay(), dataPoints.get(2).timestamp());
            assertEquals(LocalDateTime.of(2023, 1, 4, 10, 30, 0), dataPoints.get(3).timestamp());
            assertEquals(LocalDateTime.of(2023, 1, 5, 14, 45, 30), dataPoints.get(4).timestamp());
        }
    }

    @Nested
    @DisplayName("Missing Column Handling Tests")
    class MissingColumnTests {
        @Test
        @DisplayName("Should use default values for missing optional columns")
        void shouldUseDefaultValuesForMissingOptionalColumns() {
            String csvContent = """
                    timestamp,open,high,low,close
                    2023-01-01,100.0,105.0,95.0,102.0
                    2023-01-02,102.0,110.0,100.0,108.0
                    """;
            InputStream inputStream = createInputStream(csvContent);
            MarketData result = csvImporterService.importCsvFromStream(inputStream);

            List<MarketDataPoint> dataPoints = result.getDataPoints();
            assertEquals(2, dataPoints.size());

            MarketDataPoint point = dataPoints.getFirst();
            assertEquals(102.0, point.adjustedClose());
            assertEquals(0L, point.volume());
            assertEquals(0.0, point.dividendAmount());
            assertEquals(1.0, point.splitCoefficient());
        }

        @Test
        @DisplayName("Should throw exception when required column is missing")
        void shouldThrowExceptionWhenRequiredColumnIsMissing() {
            String csvContent = """
                    timestamp,open,low,close
                    2023-01-01,100.0,95.0,102.0
                    """;
            InputStream inputStream = createInputStream(csvContent);
            Exception exception = assertThrows(IllegalArgumentException.class,
                    () -> csvImporterService.importCsvFromStream(inputStream));

            assertTrue(exception.getMessage().contains("missing required headers"));
            assertTrue(exception.getMessage().contains("high"));
        }
    }

    @Nested
    @DisplayName("Invalid Data Handling Tests")
    class InvalidDataTests {
        @Test
        @DisplayName("Should skip rows with invalid number format")
        void shouldSkipRowsWithInvalidNumberFormat() {
            String csvContent = """
                    timestamp,open,high,low,close,adjusted_close,volume,dividend_amount,split_coefficient
                    2023-01-01,100.0,105.0,95.0,102.0,102.0,1000,0.0,1.0
                    2023-01-02,InvalidNumber,110.0,100.0,108.0,108.0,2000,0.0,1.0
                    2023-01-03,108.0,115.0,105.0,112.0,112.0,1500,0.5,1.0
                    """;
            InputStream inputStream = createInputStream(csvContent);

            MarketData result = csvImporterService.importCsvFromStream(inputStream);
            List<MarketDataPoint> dataPoints = result.getDataPoints();

            assertEquals(2, dataPoints.size());
            assertEquals(LocalDate.of(2023, 1, 1).atStartOfDay(), dataPoints.get(0).timestamp());
            assertEquals(LocalDate.of(2023, 1, 3).atStartOfDay(), dataPoints.get(1).timestamp());
        }

        @Test
        @DisplayName("Should skip rows with data integrity violations")
        void shouldSkipRowsWithDataIntegrityViolations() {
            String csvContent = """
                    timestamp,open,high,low,close,adjusted_close,volume,dividend_amount,split_coefficient
                    2023-01-01,100.0,105.0,95.0,102.0,102.0,1000,0.0,1.0
                    2023-01-02,120.0,110.0,100.0,108.0,108.0,2000,0.0,1.0
                    2023-01-03,108.0,115.0,105.0,112.0,112.0,1500,0.5,1.0
                    """;
            InputStream inputStream = createInputStream(csvContent);
            MarketData result = csvImporterService.importCsvFromStream(inputStream);
            List<MarketDataPoint> dataPoints = result.getDataPoints();

            assertEquals(2, dataPoints.size());
            assertEquals(LocalDate.of(2023, 1, 1).atStartOfDay(), dataPoints.get(0).timestamp());
            assertEquals(LocalDate.of(2023, 1, 3).atStartOfDay(), dataPoints.get(1).timestamp());
        }

        @Test
        @DisplayName("Should sort non-chronological dates")
        void shouldSortNonChronologicalDates() {
            String csvContent = """
                    timestamp,open,high,low,close,adjusted_close,volume,dividend_amount,split_coefficient
                    2023-01-02,102.0,110.0,100.0,108.0,108.0,2000,0.0,1.0
                    2023-01-01,100.0,105.0,95.0,102.0,102.0,1000,0.0,1.0
                    2023-01-03,108.0,115.0,105.0,112.0,112.0,1500,0.5,1.0
                    """;
            InputStream inputStream = createInputStream(csvContent);
            MarketData result = csvImporterService.importCsvFromStream(inputStream);

            List<MarketDataPoint> dataPoints = result.getDataPoints();

            assertEquals(3, dataPoints.size());
            assertEquals(LocalDate.of(2023, 1, 1).atStartOfDay(), dataPoints.get(0).timestamp());
            assertEquals(LocalDate.of(2023, 1, 2).atStartOfDay(), dataPoints.get(1).timestamp());
            assertEquals(LocalDate.of(2023, 1, 3).atStartOfDay(), dataPoints.get(2).timestamp());
        }

        @Test
        @DisplayName("Should skip rows with negative volume")
        void shouldSkipRowsWithNegativeVolume() {
            String csvContent = """
                    timestamp,open,high,low,close,volume
                    2023-01-01,100.0,105.0,95.0,102.0,1000
                    2023-01-02,102.0,110.0,100.0,108.0,-100
                    2023-01-03,108.0,115.0,105.0,112.0,1500
                    """;
            InputStream inputStream = createInputStream(csvContent);

            MarketData result = csvImporterService.importCsvFromStream(inputStream);
            List<MarketDataPoint> dataPoints = result.getDataPoints();

            assertEquals(2, dataPoints.size());
            assertEquals(LocalDate.of(2023, 1, 1).atStartOfDay(), dataPoints.get(0).timestamp());
            assertEquals(LocalDate.of(2023, 1, 3).atStartOfDay(), dataPoints.get(1).timestamp());
        }

        @Test
        @DisplayName("Should throw exception when all rows are invalid")
        void shouldThrowExceptionWhenAllRowsAreInvalid() {
            String csvContent = """
                    timestamp,open,high,low,close
                    InvalidDate,100.0,105.0,95.0,102.0
                    2023-13-01,100.0,105.0,95.0,102.0
                    """;
            InputStream inputStream = createInputStream(csvContent);

            Exception exception = assertThrows(IllegalArgumentException.class,
                    () -> csvImporterService.importCsvFromStream(inputStream));

            assertTrue(exception.getMessage().contains("No valid market data points were found"));
        }
    }

    @Test
    @DisplayName("Should throw exception when accessing data points with invalid instance type")
    void shouldThrowExceptionWhenAccessingDataPointsWithInvalidInstanceType() {
        MarketData marketData = new MarketData();

        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> csvImporterService.getDataPoints("invalid", marketData));

        assertTrue(exception.getMessage().contains("Unknown instance type"));
    }

    private InputStream createInputStream(String csvContent) {
        return new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8));
    }
}