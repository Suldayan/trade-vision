package com.example.spring_backend.market;

import jakarta.annotation.Nonnull;

import java.io.IOException;
import java.io.InputStream;

public interface CsvImporterService {
    MarketData importCsvFromStream(InputStream stream) throws IOException;
    double[] getDataPoints(
            @Nonnull String instance,
            @Nonnull MarketData data);
}
