package com.example.trade_vision_backend.market.internal;

import com.example.trade_vision_backend.market.MarketDataPoint;
import com.example.trade_vision_backend.market.CsvImporterService;
import com.example.trade_vision_backend.market.MarketData;
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
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class CsvImporterServiceImpl implements CsvImporterService {
    private final DataExtractor dataExtractor;

    private static class Headers {
        public static final String TIMESTAMP = "timestamp";
        public static final String OPEN = "open";
        public static final String HIGH = "high";
        public static final String LOW = "low";
        public static final String CLOSE = "close";
        public static final String ADJUSTED_CLOSE = "adjusted_close";
        public static final String VOLUME = "volume";
        public static final String DIVIDEND_AMOUNT = "dividend_amount";
        public static final String SPLIT_COEFFICIENT = "split_coefficient";
    }

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

    private static class ImportStats {
        int processedRows = 0;
        int skippedRows = 0;
        int invalidNumberRows = 0;
        int invalidDateRows = 0;
        int dataOutOfRangeRows = 0;
        int nonChronologicalRows = 0;
        List<String> errors = new ArrayList<>();

        void logResults() {
            log.info("CSV Import Complete: Processed {} rows, Skipped {} rows", processedRows, skippedRows);
            if (skippedRows > 0) {
                log.info("  - Invalid number format: {}", invalidNumberRows);
                log.info("  - Invalid date format: {}", invalidDateRows);
                log.info("  - Data out of valid range: {}", dataOutOfRangeRows);
                log.info("  - Non-chronological timestamps: {}", nonChronologicalRows);
            }
            if (!errors.isEmpty() && errors.size() <= 10) {
                log.info("Sample errors:");
                errors.forEach(e -> log.info("  - {}", e));
            } else if (!errors.isEmpty()) {
                log.info("Sample errors (first 10 of {}):", errors.size());
                errors.subList(0, 10).forEach(e -> log.info("  - {}", e));
            }
        }

        void addError(int recordNumber, String message) {
            if (errors.size() < 20) {
                errors.add(String.format("Line %d: %s", recordNumber, message));
            }
        }
    }

    @Nonnull
    @Override
    public MarketData importCsvFromStream(@Nonnull InputStream stream) {
        List<MarketDataPoint> allDataPoints = new ArrayList<>();
        ImportStats stats = new ImportStats();
        boolean isReverseChronological = false;
        boolean orderDetermined = false;
        LocalDateTime firstTimestamp = null;
        LocalDateTime secondTimestamp = null;

        try (BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(BOMInputStream.builder()
                        .setInputStream(stream)
                        .get(), StandardCharsets.UTF_8));
             CSVParser parser = CSVFormat.DEFAULT.builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setIgnoreHeaderCase(true)
                     .setIgnoreEmptyLines(true)
                     .setAllowMissingColumnNames(false)
                     .get()
                     .parse(bufferedReader)) {

            Map<String, Integer> headerMap = parser.getHeaderMap();
            validateHeaders(headerMap);

            for (CSVRecord record : parser) {
                stats.processedRows++;
                try {
                    LocalDateTime timestamp;
                    try {
                        timestamp = parseTimestamp(record.get(Headers.TIMESTAMP));
                    } catch (Exception e) {
                        stats.invalidDateRows++;
                        stats.addError((int) record.getRecordNumber(), "Invalid timestamp: " + e.getMessage());
                        stats.skippedRows++;
                        continue;
                    }

                    if (firstTimestamp == null) {
                        firstTimestamp = timestamp;
                    } else if (secondTimestamp == null) {
                        secondTimestamp = timestamp;
                        if (secondTimestamp.isBefore(firstTimestamp)) {
                            isReverseChronological = true;
                            log.info("Detected reverse chronological data (newest first). Will sort automatically.");
                        }
                        orderDetermined = true;
                    }

                    double open, high, low, close;
                    try {
                        open = parseDouble(record.get(Headers.OPEN));
                        high = parseDouble(record.get(Headers.HIGH));
                        low = parseDouble(record.get(Headers.LOW));
                        close = parseDouble(record.get(Headers.CLOSE));
                    } catch (NumberFormatException e) {
                        stats.invalidNumberRows++;
                        stats.addError((int) record.getRecordNumber(), "Invalid required numeric value: " + e.getMessage());
                        stats.skippedRows++;
                        continue;
                    }

                    if (high < low || open < low || open > high || close < low || close > high) {
                        stats.dataOutOfRangeRows++;
                        stats.addError((int) record.getRecordNumber(),
                                String.format("Price integrity check failed: OHLC values inconsistent (O: %.2f, H: %.2f, L: %.2f, C: %.2f)",
                                        open, high, low, close));
                        stats.skippedRows++;
                        continue;
                    }

                    double adjustedClose = safeParseDouble(record, Headers.ADJUSTED_CLOSE, close);
                    long volume = safeParseLong(record);
                    double dividendAmount = safeParseDouble(record, Headers.DIVIDEND_AMOUNT, 0.0);
                    double splitCoefficient = safeParseDouble(record, Headers.SPLIT_COEFFICIENT, 1.0);

                    if (volume < 0) {
                        stats.dataOutOfRangeRows++;
                        stats.addError((int) record.getRecordNumber(), "Volume cannot be negative: " + volume);
                        stats.skippedRows++;
                        continue;
                    }

                    // If we get here, the data point is valid
                    MarketDataPoint dataPoint = MarketDataPoint.builder()
                            .timestamp(timestamp)
                            .open(open)
                            .high(high)
                            .low(low)
                            .close(close)
                            .adjustedClose(adjustedClose)
                            .volume(volume)
                            .dividendAmount(dividendAmount)
                            .splitCoefficient(splitCoefficient)
                            .build();

                    allDataPoints.add(dataPoint);

                } catch (Exception e) {
                    stats.skippedRows++;
                    stats.addError((int) record.getRecordNumber(), "Unexpected error: " + e.getMessage());
                    log.warn("Error processing record at line {}, skipping: {}",
                            record.getRecordNumber(), e.getMessage());
                }
            }

            if (!orderDetermined && !allDataPoints.isEmpty()) {
                log.info("Could not determine chronological order of data. Assuming standard chronological order.");
            }

            MarketData marketData = new MarketData();
            if (isReverseChronological) {
                allDataPoints.sort(Comparator.comparing(MarketDataPoint::timestamp));
            }

            marketData.addDataPoints(allDataPoints);

            stats.logResults();

            int totalDataPoints = allDataPoints.size();
            if (totalDataPoints == 0) {
                throw new IllegalArgumentException("No valid market data points were found in the CSV file");
            }

            log.info("Successfully loaded {} market data points for backtesting", totalDataPoints);
            return marketData;
        } catch (IOException e) {
            throw new RuntimeException("Error importing CSV data for backtesting", e);
        }
    }

    private void validateHeaders(Map<String, Integer> headerMap) {
        List<String> requiredHeaders = Arrays.asList(
                Headers.TIMESTAMP,
                Headers.OPEN,
                Headers.HIGH,
                Headers.LOW,
                Headers.CLOSE
        );

        List<String> missingHeaders = new ArrayList<>();
        for (String header : requiredHeaders) {
            if (!headerMap.containsKey(header.toLowerCase())) {
                missingHeaders.add(header);
            }
        }

        List<String> foundOptionalHeaders = getOptionalHeaders(headerMap, missingHeaders);
        if (!foundOptionalHeaders.isEmpty()) {
            log.info("Found optional headers: {}", foundOptionalHeaders);
        } else {
            log.info("No optional headers found. Using default values for optional fields.");
        }
    }

    @Nonnull
    private static List<String> getOptionalHeaders(
            @Nonnull Map<String, Integer> headerMap,
            @Nonnull List<String> missingHeaders) {
        if (!missingHeaders.isEmpty()) {
            throw new IllegalArgumentException("CSV is missing required headers: " + missingHeaders);
        }

        List<String> optionalHeaders = Arrays.asList(
                Headers.ADJUSTED_CLOSE,
                Headers.VOLUME,
                Headers.DIVIDEND_AMOUNT,
                Headers.SPLIT_COEFFICIENT
        );

        List<String> foundOptionalHeaders = new ArrayList<>();
        for (String header : optionalHeaders) {
            if (headerMap.containsKey(header.toLowerCase())) {
                foundOptionalHeaders.add(header);
            }
        }
        return foundOptionalHeaders;
    }

    @Override
    public double[] getDataPoints(
            @Nonnull String instance,
            @Nonnull MarketData data) {
        dataExtractor.extractData(data);
        return switch (instance.toLowerCase()) {
            case "open" -> dataExtractor.getOpen();
            case "close" -> dataExtractor.getClose();
            case "high" -> dataExtractor.getHigh();
            case "low" -> dataExtractor.getLow();
            case "volume" -> dataExtractor.getVolume();
            default -> throw new IllegalArgumentException("Unknown instance type: " + instance);
        };
    }

    @Nonnull
    private LocalDateTime parseTimestamp(@Nonnull String timestamp) {
        if (timestamp.trim().isEmpty()) {
            throw new IllegalArgumentException("Timestamp cannot be empty");
        }

        // Try datetime formats first
        for (DateTimeFormatter formatter : DATETIME_FORMATTERS) {
            try {
                return LocalDateTime.parse(timestamp, formatter);
            } catch (DateTimeParseException ignored) {
                // Try next format
            }
        }

        // Try date formats (convert to LocalDateTime)
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                return LocalDate.parse(timestamp, formatter).atStartOfDay();
            } catch (DateTimeParseException ignored) {
                // Try next format
            }
        }

        // Fallback to the original approach
        try {
            if (timestamp.contains("T")) {
                return LocalDateTime.parse(timestamp);
            } else if (timestamp.contains(" ")) {
                return LocalDateTime.parse(timestamp.replace(" ", "T"));
            } else {
                return LocalDate.parse(timestamp).atStartOfDay();
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to parse timestamp: " + timestamp);
        }
    }

    private double parseDouble(@Nonnull String value) {
        if (value.trim().isEmpty()) {
            throw new NumberFormatException("Value cannot be empty");
        }
        return Double.parseDouble(value);
    }

    private double safeParseDouble(
            @Nonnull CSVRecord record,
            @Nonnull String header,
            double defaultValue) {
        try {
            if (!record.isMapped(header)) return defaultValue;

            String value = record.get(header);
            if (value == null || value.trim().isEmpty()) {
                return defaultValue;
            }
            return Double.parseDouble(value);
        } catch (Exception e) {
            log.debug("Failed to parse double for {}: {}", header, e.getMessage());
            return defaultValue;
        }
    }

    private long safeParseLong(
            @Nonnull CSVRecord record) {
        try {
            if (!record.isMapped(Headers.VOLUME)) {
                return 0L;
            }
            String value = record.get(Headers.VOLUME);
            if (value == null || value.trim().isEmpty()) {
                return 0L;
            }
            return Long.parseLong(value);
        } catch (Exception e) {
            log.debug("Failed to parse long for {}: {}", Headers.VOLUME, e.getMessage());
            return 0L;
        }
    }
}