import pytest
import numpy as np
from indicator import Indicator

class TestIndicator:
    def setup_method(self):
        self.indicator = Indicator()
        self.prices = np.array([10.0, 11.0, 12.0, 13.0, 14.0, 15.0, 16.0, 15.0, 14.0, 13.0, 12.0, 11.0, 10.0, 
                              11.0, 12.0, 13.0, 14.0, 15.0, 16.0, 17.0, 18.0, 19.0, 20.0, 21.0, 22.0])
        self.high = np.array([11.0, 12.0, 13.0, 14.0, 15.0, 16.0, 17.0, 16.0, 15.0, 14.0, 13.0, 12.0, 11.0, 
                            12.0, 13.0, 14.0, 15.0, 16.0, 17.0, 18.0, 19.0, 20.0, 21.0, 22.0, 23.0])
        self.low = np.array([9.0, 10.0, 11.0, 12.0, 13.0, 14.0, 15.0, 14.0, 13.0, 12.0, 11.0, 10.0, 9.0, 
                           10.0, 11.0, 12.0, 13.0, 14.0, 15.0, 16.0, 17.0, 18.0, 19.0, 20.0, 21.0])
        self.close = self.prices  

    def test_sma(self):
        """Test Simple Moving Average calculation"""
        result = self.indicator.sma(self.prices, 3)
        
        # First two values should be NaN
        assert np.isnan(result[0])
        assert np.isnan(result[1])
        
        # Check a few specific values
        assert result[2] == pytest.approx((10.0 + 11.0 + 12.0) / 3)
        assert result[10] == pytest.approx((12.0 + 13.0 + 14.0) / 3)
        
        # Check lengths match
        assert len(result) == len(self.prices)

    def test_ema(self):
        """Test Exponential Moving Average calculation"""
        window = 3
        result = self.indicator.ema(self.prices, window)
        
        # Check first value is the first price
        assert result[0] == self.prices[0]
        
        # Test EMA calculation for a few points
        alpha = 2 / (window + 1)
        manual_ema = self.prices[0]
        for i in range(1, 5):
            manual_ema = alpha * self.prices[i] + (1 - alpha) * manual_ema
            assert result[i] == pytest.approx(manual_ema)
        
        # Check length
        assert len(result) == len(self.prices)

    def test_rsi(self):
        """Test Relative Strength Index calculation"""
        window = 14
        result = self.indicator.rsi(self.prices, window)
        
        # RSI should be between 0 and 100
        valid_results = result[~np.isnan(result)]
        assert all(0 <= val <= 100 for val in valid_results)
        
        # With our sample data, we expect certain RSI values
        # For this specific price pattern (up, then down, then up)
        # Length check
        assert len(result) == len(self.prices) - 1  # Due to diff() reducing length by 1

    def test_macd(self):
        """Test MACD calculation"""
        macd_line, signal_line, histogram = self.indicator.macd(self.prices)
        
        # Check lengths
        assert len(macd_line) == len(self.prices)
        assert len(signal_line) == len(self.prices)
        assert len(histogram) == len(self.prices)
        
        # Check histogram calculation
        for i in range(len(histogram)):
            assert histogram[i] == pytest.approx(macd_line[i] - signal_line[i])

    def test_atr(self):
        """Test Average True Range calculation"""
        window = 14
        result = self.indicator.atr(self.high, self.low, self.close, window)
        
        # ATR should be positive
        valid_results = result[~np.isnan(result)]
        assert all(val >= 0 for val in valid_results)
        
        # Check expected NaN values at beginning
        assert np.isnan(result[0])  # First value is NaN because of prev_close[0]
        
        # Check length
        assert len(result) == len(self.prices)

    def test_bollinger_bands(self):
        """Test Bollinger Bands calculation"""
        window = 5
        num_std = 2
        upper, middle, lower = self.indicator.bollinger_bands(self.prices, window, num_std)
        
        # Check lengths
        assert len(upper) == len(self.prices)
        assert len(middle) == len(self.prices)
        assert len(lower) == len(self.prices)
        
        # First window-1 values should be NaN
        for i in range(window - 1):
            assert np.isnan(upper[i])
            assert np.isnan(middle[i])
            assert np.isnan(lower[i])
        
        # For valid values, upper should be higher than middle, which should be higher than lower
        for i in range(window - 1, len(upper)):
            assert upper[i] > middle[i]
            assert middle[i] > lower[i]
            
        # Middle band should be equal to SMA
        sma_values = self.indicator.sma(self.prices, window)
        for i in range(len(middle)):
            if not np.isnan(middle[i]):
                assert middle[i] == pytest.approx(sma_values[i])
        
        # Bands should be 2*std_dev away from middle
        valid_indices = range(window - 1, len(upper))
        for i in valid_indices:
            window_segment = self.prices[i - window + 1:i + 1]
            std_dev = np.std(window_segment)
            assert upper[i] == pytest.approx(middle[i] + num_std * std_dev)
            assert lower[i] == pytest.approx(middle[i] - num_std * std_dev)