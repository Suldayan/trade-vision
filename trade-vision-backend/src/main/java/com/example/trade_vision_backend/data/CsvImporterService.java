package com.example.trade_vision_backend.data;

import java.io.IOException;
import java.io.InputStream;

public interface CsvImporterService {
    MarketData importCsvFromStream(InputStream stream) throws IOException;

}
