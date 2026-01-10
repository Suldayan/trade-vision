package com.example.spring_backend.market;

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

    public void addDataPoints(List<MarketDataPoint> points) {
        dataPoints.addAll(points);
    }

    public double[] close() {
        double[] close = new double[dataPoints.size()];
        for (int i = 0; i < dataPoints.size(); i++) {
            close[i] = dataPoints.get(i).close();
        }
        return close;
    }

    public double[] open() {
        double[] open = new double[dataPoints.size()];
        for (int i = 0; i < dataPoints.size(); i++) {
            open[i] = dataPoints.get(i).open();
        }
        return open;
    }

    public double[] high() {
        double[] high = new double[dataPoints.size()];
        for (int i = 0; i < dataPoints.size(); i++) {
            high[i] = dataPoints.get(i).high();
        }
        return high;
    }

    public double[] low() {
        double[] low = new double[dataPoints.size()];
        for (int i = 0; i < dataPoints.size(); i++) {
            low[i] = dataPoints.get(i).low();
        }
        return low;
    }

    public double[] volume() {
        double[] volume = new double[dataPoints.size()];
        for (int i = 0; i < dataPoints.size(); i++) {
            volume[i] = dataPoints.get(i).volume();
        }
        return volume;
    }
}

