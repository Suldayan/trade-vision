package com.example.trade_vision_backend.data;

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

    public double[] getClose() {
        double[] close = new double[dataPoints.size()];
        for (int i = 0; i < dataPoints.size(); i++) {
            close[i] = dataPoints.get(i).close();
        }
        return close;
    }

    public double[] getOpen() {
        double[] open = new double[dataPoints.size()];
        for (int i = 0; i < dataPoints.size(); i++) {
            open[i] = dataPoints.get(i).open();
        }
        return open;
    }

    public double[] getHigh() {
        double[] high = new double[dataPoints.size()];
        for (int i = 0; i < dataPoints.size(); i++) {
            high[i] = dataPoints.get(i).high();
        }
        return high;
    }

    public double[] getLow() {
        double[] low = new double[dataPoints.size()];
        for (int i = 0; i < dataPoints.size(); i++) {
            low[i] = dataPoints.get(i).low();
        }
        return low;
    }

    public long[] getVolume() {
        long[] volume = new long[dataPoints.size()];
        for (int i = 0; i < dataPoints.size(); i++) {
            volume[i] = dataPoints.get(i).volume();
        }
        return volume;
    }
}

