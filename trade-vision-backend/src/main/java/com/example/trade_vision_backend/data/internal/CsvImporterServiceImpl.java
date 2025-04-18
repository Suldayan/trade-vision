package com.example.trade_vision_backend.data.internal;

import com.example.trade_vision_backend.data.MarketDataPoint;
import com.example.trade_vision_backend.data.CsvImporterService;
import com.example.trade_vision_backend.data.MarketData;
import com.example.trade_vision_backend.indicators.DataExtractor;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.input.BOMInputStream;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CsvImporterServiceImpl implements CsvImporterService {
    private final DataExtractor dataExtractor;

    private static final List<DateTimeFormatter> DATE_FORMATTERS = Arrays.asList(
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd"),
            DateTimeFormatter.ISO_DATE
    );

    private static final List<DateTimeFormatter> DATETIME_FORMATTERS = Arrays.asList(
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
            DateTimeFormatter.ISO_DATE_TIME
    );

    @Nonnull
    @Override
    public MarketData importCsvFromStream(@Nonnull InputStream stream) {
        MarketData marketData = new MarketData();

        try (BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(BOMInputStream.builder()
                        .setInputStream(stream)
                        .get(), StandardCharsets.UTF_8));
             CSVParser parser = CSVFormat.DEFAULT.builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .get()
                     .parse(bufferedReader)) {

            int recordCount = 0;
            for (CSVRecord record : parser) {
                try {
                    MarketDataPoint dataPoint = MarketDataPoint.builder()
                            .timestamp(parseTimestamp(record.get(Headers.TIMESTAMP)))
                            .open(parseDoubleOrDefault(record.get(Headers.OPEN), 0.0))
                            .high(parseDoubleOrDefault(record.get(Headers.HIGH), 0.0))
                            .low(parseDoubleOrDefault(record.get(Headers.LOW), 0.0))
                            .close(parseDoubleOrDefault(record.get(Headers.CLOSE), 0.0))
                            .adjustedClose(parseDoubleOrDefault(record.get(Headers.ADJUSTED_CLOSE), 0.0))
                            .volume(parseLongOrDefault(record.get(Headers.VOLUME)))
                            .dividendAmount(parseDoubleOrDefault(record.get(Headers.DIVIDEND_AMOUNT), 0.0))
                            .splitCoefficient(parseDoubleOrDefault(record.get(Headers.SPLIT_COEFFICIENT), 1.0))
                            .build();

                    marketData.addDataPoint(dataPoint);
                    recordCount++;

                    if (recordCount % 100000 == 0) {
                        log.info("Loaded {} market data points so far", recordCount);
                    }
                } catch (Exception e) {
                    log.warn("Error parsing record at line {}, skipping: {}",
                            record.getRecordNumber(), e.getMessage());
                }
            }

            log.info("Successfully loaded {} market data points for backtesting", recordCount);
            return marketData;
        } catch (IOException e) {
            throw new RuntimeException("Error importing CSV data for backtesting", e);
        }
    }

    @Override
    public double[] getDataPoints(
            @Nonnull String instance,
            @Nonnull MarketData data) {
        dataExtractor.extractData(data);
        return switch (instance) {
            case "open" -> dataExtractor.getOpen();
            case "close" -> dataExtractor.getClose();
            case "high" -> dataExtractor.getHigh();
            case "low" -> dataExtractor.getLow();
            default -> throw new IllegalArgumentException("Unknown instance type: " + instance);
        };
    }

    private LocalDateTime parseTimestamp(String timestamp) {
        try {
            if (timestamp.contains("T")) {
                return LocalDateTime.parse(timestamp);
            } else if (timestamp.contains(" ")) {
                return LocalDateTime.parse(timestamp.replace(" ", "T"));
            } else {
                return LocalDate.parse(timestamp).atStartOfDay();
            }
        } catch (Exception e) {
            log.warn("Failed to parse timestamp: {}", timestamp);
            throw e;
        }
    }

    private Double parseDoubleOrDefault(String value, Double defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            log.warn("Failed to parse double value: {}", value);
            return defaultValue;
        }
    }

    private Long parseLongOrDefault(String value) {
        if (value == null || value.trim().isEmpty()) {
            return 0L;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            log.warn("Failed to parse long value: {}", value);
            return 0L;
        }
    }
}
