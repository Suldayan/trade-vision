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

    // Format detection fields
    private DateTimeFormatter detectedDateTimeFormatter = null;
    private DateTimeFormatter detectedDateFormatter = null;
    private boolean isDateOnly = false;

    private static class DataArrays {
        final LocalDateTime[] timestamps;
        final double[] opens;
        final double[] highs;
        final double[] lows;
        final double[] closes;
        final double[] adjustedCloses;
        final long[] volumes;
        final double[] dividendAmounts;
        final double[] splitCoefficients;
        int currentIndex = 0;

        DataArrays(int capacity) {
            timestamps = new LocalDateTime[capacity];
            opens = new double[capacity];
            highs = new double[capacity];
            lows = new double[capacity];
            closes = new double[capacity];
            adjustedCloses = new double[capacity];
            volumes = new long[capacity];
            dividendAmounts = new double[capacity];
            splitCoefficients = new double[capacity];
        }

        void addDataPoint(LocalDateTime timestamp, double open, double high, double low,
                          double close, double adjustedClose, long volume,
                          double dividendAmount, double splitCoefficient) {
            timestamps[currentIndex] = timestamp;
            opens[currentIndex] = open;
            highs[currentIndex] = high;
            lows[currentIndex] = low;
            closes[currentIndex] = close;
            adjustedCloses[currentIndex] = adjustedClose;
            volumes[currentIndex] = volume;
            dividendAmounts[currentIndex] = dividendAmount;
            splitCoefficients[currentIndex] = splitCoefficient;
            currentIndex++;
        }

        List<MarketDataPoint> toDataPoints() {
            List<MarketDataPoint> dataPoints = new ArrayList<>(currentIndex);
            for (int i = 0; i < currentIndex; i++) {
                MarketDataPoint dataPoint = new MarketDataPoint(
                        timestamps[i],
                        opens[i],
                        highs[i],
                        lows[i],
                        closes[i],
                        adjustedCloses[i],
                        volumes[i],
                        dividendAmounts[i],
                        splitCoefficients[i]
                );
                dataPoints.add(dataPoint);
            }
            return dataPoints;
        }
    }

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

        void addError(int recordNumber, @Nonnull String message) {
            if (errors.size() < 20) {
                errors.add(String.format("Line %d: %s", recordNumber, message));
            }
        }
    }

    @Nonnull
    @Override
    public MarketData importCsvFromStream(@Nonnull InputStream stream) throws IOException {
        ImportStats stats = new ImportStats();
        boolean isReverseChronological = false;
        boolean orderDetermined = false;
        LocalDateTime firstTimestamp = null;
        LocalDateTime secondTimestamp = null;

        // Reset format detection for each import
        detectedDateTimeFormatter = null;
        detectedDateFormatter = null;
        isDateOnly = false;

        // First pass: count rows for pre-sizing
        int estimatedRows = estimateRowCount(stream);
        DataArrays dataArrays = new DataArrays(estimatedRows);

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

                // Fast-path validation - check for obviously invalid records first
                if (record.size() < 5) {
                    stats.skippedRows++;
                    continue;
                }

                LocalDateTime timestamp;
                double open, high, low, close;

                try {
                    // Parse all required fields first - fail fast if any are invalid
                    String timestampStr = record.get(Headers.TIMESTAMP);
                    String openStr = record.get(Headers.OPEN);
                    String highStr = record.get(Headers.HIGH);
                    String lowStr = record.get(Headers.LOW);
                    String closeStr = record.get(Headers.CLOSE);

                    // Quick empty check - batch validation
                    if (timestampStr.isEmpty() || openStr.isEmpty() || highStr.isEmpty() ||
                            lowStr.isEmpty() || closeStr.isEmpty()) {
                        stats.skippedRows++;
                        continue;
                    }

                    timestamp = parseTimestamp(timestampStr);
                    open = parseDouble(openStr);
                    high = parseDouble(highStr);
                    low = parseDouble(lowStr);
                    close = parseDouble(closeStr);

                    if (high < low || open < low || open > high || close < low || close > high) {
                        stats.dataOutOfRangeRows++;
                        stats.skippedRows++;
                        continue;
                    }

                } catch (Exception e) {
                    if (e instanceof DateTimeParseException) {
                        stats.invalidDateRows++;
                    } else if (e instanceof NumberFormatException) {
                        stats.invalidNumberRows++;
                    }
                    stats.addError((int) record.getRecordNumber(), "Parsing failed: " + e.getMessage());
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

                double adjustedClose = safeParseDouble(record, Headers.ADJUSTED_CLOSE, close);
                long volume = safeParseLong(record);
                double dividendAmount = safeParseDouble(record, Headers.DIVIDEND_AMOUNT, 0.0);
                double splitCoefficient = safeParseDouble(record, Headers.SPLIT_COEFFICIENT, 1.0);

                if (volume < 0) {
                    stats.dataOutOfRangeRows++;
                    stats.skippedRows++;
                    continue;
                }

                dataArrays.addDataPoint(timestamp, open, high, low, close,
                        adjustedClose, volume, dividendAmount, splitCoefficient);
            }

            if (!orderDetermined && dataArrays.currentIndex > 0) {
                log.info("Could not determine chronological order of data. Assuming standard chronological order.");
            }

            List<MarketDataPoint> allDataPoints = dataArrays.toDataPoints();
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
            throw e;
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                throw new IllegalArgumentException(e);
            }
            throw new RuntimeException("Unexpected error during CSV import", e);
        }
    }

    private int estimateRowCount(InputStream stream) throws IOException {
        if (!stream.markSupported()) {
            return 10000; // Default estimate if we can't count
        }

        stream.mark(Integer.MAX_VALUE);
        int lineCount = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            while (reader.readLine() != null) {
                lineCount++;
            }
        }
        stream.reset();
        return Math.max(lineCount - 1, 100);
    }

    private double parseDouble(@Nonnull String value) {
        if (value.isEmpty()) {
            throw new NumberFormatException("Value cannot be empty");
        }
        return Double.parseDouble(value);
    }

    private void validateHeaders(@Nonnull Map<String, Integer> headerMap) {
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
        if (timestamp.isEmpty()) {
            throw new IllegalArgumentException("Timestamp cannot be empty");
        }

        // If we haven't detected the format yet, try to detect it
        if (detectedDateTimeFormatter == null && detectedDateFormatter == null) {
            detectDateFormat(timestamp);
        }

        // Use the detected formatter
        try {
            if (isDateOnly) {
                return LocalDate.parse(timestamp, detectedDateFormatter).atStartOfDay();
            } else {
                if (detectedDateTimeFormatter == null) {
                    throw new IllegalArgumentException("Date time formatter has been passed as null");
                }
                return LocalDateTime.parse(timestamp, detectedDateTimeFormatter);
            }
        } catch (DateTimeParseException e) {
            // Fallback only if detected format fails
            return fallbackTimestampParse(timestamp);
        }
    }

    private void detectDateFormat(@Nonnull String timestamp) {
        // Try datetime formats first
        for (DateTimeFormatter formatter : DATETIME_FORMATTERS) {
            try {
                LocalDateTime.parse(timestamp, formatter);
                detectedDateTimeFormatter = formatter;
                isDateOnly = false;
                log.debug("Detected datetime format: {}", formatter);
                return;
            } catch (DateTimeParseException ignored) {
                // Try next format
            }
        }

        // Try date formats
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                LocalDate.parse(timestamp, formatter);
                detectedDateFormatter = formatter;
                isDateOnly = true;
                log.debug("Detected date format: {}", formatter);
                return;
            } catch (DateTimeParseException ignored) {
                // Try next format
            }
        }

        // If we get here, use fallback approach
        log.warn("Could not detect date format, using fallback parsing");
    }

    private LocalDateTime fallbackTimestampParse(@Nonnull String timestamp) {
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

    private double safeParseDouble(
            @Nonnull CSVRecord record,
            @Nonnull String header,
            double defaultValue) {
        try {
            if (!record.isMapped(header)) return defaultValue;

            Map<String, Integer> headerMap = record.getParser().getHeaderMap();
            Integer headerIndex = headerMap.get(header.toLowerCase());
            if (headerIndex == null || headerIndex >= record.size()) {
                return defaultValue;
            }

            String value = record.get(header);
            if (value == null || value.isEmpty()) {
                return defaultValue;
            }
            return Double.parseDouble(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private long safeParseLong(
            @Nonnull CSVRecord record) {
        try {
            if (!record.isMapped(Headers.VOLUME)) {
                return 0L;
            }

            // Additional bounds check to prevent index out of bounds
            Map<String, Integer> headerMap = record.getParser().getHeaderMap();
            Integer headerIndex = headerMap.get(Headers.VOLUME.toLowerCase());
            if (headerIndex == null || headerIndex >= record.size()) {
                return 0L;
            }

            String value = record.get(Headers.VOLUME);
            if (value == null || value.isEmpty()) {
                return 0L;
            }
            return Long.parseLong(value);
        } catch (Exception e) {
            return 0L;
        }
    }
}