package com.example.trade_vision_backend.indicators;

import com.example.trade_vision_backend.data.MarketData;
import com.example.trade_vision_backend.data.MarketDataPoint;
import jakarta.annotation.Nonnull;
import lombok.Getter;

import java.util.List;

@Getter
public class DataExtractor {
    private double[] open;
    private double[] high;
    private double[] low;
    private double[] close;
    private double[] volume;

    public void extractData(@Nonnull MarketData data) {
        List<MarketDataPoint> points = data.getDataPoints();
        int size = points.size();

        open = new double[size];
        high = new double[size];
        low = new double[size];
        close = new double[size];
        volume = new double[size];

        for (int i = 0; i < size; i++) {
            MarketDataPoint point = points.get(i);
            open[i] = point.open();
            high[i] = point.high();
            low[i] = point.low();
            close[i] = point.close();
            volume[i] = point.volume();
        }
    }

    public int getSize() { return close.length; }
}