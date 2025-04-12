package com.example.trade_vision_backend.data;

import com.example.trade_vision_backend.MarketDataPoint;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class MarketData {
    private final List<MarketDataPoint> dataPoints = new ArrayList<>();

    public void addDataPoint(MarketDataPoint point) {
        dataPoints.add(point);
    }
}

