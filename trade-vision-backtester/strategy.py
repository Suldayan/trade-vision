import numpy as np
import indicator as Indicator

class Strategy:
    """Base strategy class that all strategies should inherit from."""
    def __init__(self):
        self.indicator = Indicator()
    
    def generate_signals(self, data):
        """Generate trading signals based on market data.
        Should be implemented by each strategy subclass."""
        self.data = data
        raise NotImplementedError("Strategy subclass must implement generate_signals method")

class MovingAverageCrossover(Strategy):
    def __init__(self, short_window=50, long_window=200):
        super().__init__()
        self.short_window = short_window
        self.long_window = long_window
    
    def generate_signals(self, data):
        prices = data['close'].values
        
        short_ma = self.indicator.sma(prices, self.short_window)
        long_ma = self.indicator.sma(prices, self.long_window)
        
        signals = np.zeros(len(prices))
        
        for i in range(self.long_window, len(prices)):
            if short_ma[i-1] < long_ma[i-1] and short_ma[i] > long_ma[i]:
                signals[i] = 1
            elif short_ma[i-1] > long_ma[i-1] and short_ma[i] < long_ma[i]:
                signals[i] = -1
        
        return signals

class RSIStrategy(Strategy):
    def __init__(self, window=14, oversold=30, overbought=70):
        super().__init__()
        self.window = window
        self.oversold = oversold
        self.overbought = overbought
    
    def generate_signals(self, data):
        prices = data['close'].values
        
        rsi_values = self.indicator.rsi(prices, self.window)
        
        signals = np.zeros(len(prices))
        
        for i in range(self.window + 1, len(prices)):
            if rsi_values[i-1] < self.oversold and rsi_values[i] >= self.oversold:
                signals[i] = 1
            elif rsi_values[i-1] > self.overbought and rsi_values[i] <= self.overbought:
                signals[i] = -1
        
        return signals

class MACDStrategy(Strategy):
    def __init__(self, fast=12, slow=26, signal=9):
        super().__init__()
        self.fast = fast
        self.slow = slow
        self.signal = signal
    
    def generate_signals(self, data):
        prices = data['close'].values

        macd_line, signal_line, histogram = self.indicator.macd(
            prices, self.fast, self.slow, self.signal
        )

        signals = np.zeros(len(prices))

        for i in range(self.slow + self.signal, len(prices)):
            if macd_line[i-1] < signal_line[i-1] and macd_line[i] > signal_line[i]:
                if histogram[i] > histogram[i-1]:  # Confirming increasing momentum
                    signals[i] = 1
            elif macd_line[i-1] > signal_line[i-1] and macd_line[i] < signal_line[i]:
                if histogram[i] < histogram[i-1]:  # Confirming decreasing momentum
                    signals[i] = -1
        
        return signals

class BollingerBandStrategy(Strategy):
    def __init__(self, window=20, num_std=2):
        super().__init__()
        self.window = window
        self.num_std = num_std
    
    def generate_signals(self, data):
        prices = data['close'].values

        upper, middle, lower = self.indicator.bollinger_bands(
            prices, self.window, self.num_std
        )

        signals = np.zeros(len(prices))
        
        for i in range(self.window, len(prices)):
            if prices[i] > middle[i] and prices[i-1] < lower[i-1] and prices[i] > lower[i]:
                signals[i] = 1  
            elif prices[i] < middle[i] and prices[i-1] > upper[i-1] and prices[i] < upper[i]:
                signals[i] = -1  
        
        return signals
    
class CompositeStrategy(Strategy):
    def __init__(self, rsi_window=14, macd_fast=12, macd_slow=26, macd_signal=9):
        super().__init__()
        self.rsi_window = rsi_window
        self.macd_fast = macd_fast
        self.macd_slow = macd_slow
        self.macd_signal = macd_signal
    
    def generate_signals(self, data):
        prices = data['close'].values

        rsi_values = self.indicator.rsi(prices, self.rsi_window)
        macd_line, signal_line, histogram = self.indicator.macd(
            prices, self.macd_fast, self.macd_slow, self.macd_signal
        )
        
        signals = np.zeros(len(prices))
        
        for i in range(self.macd_slow + self.macd_signal, len(prices)):
            if (rsi_values[i] < 40 and 
                macd_line[i-1] < signal_line[i-1] and 
                macd_line[i] > signal_line[i]):
                signals[i] = 1
            elif (rsi_values[i] > 60 and 
                  macd_line[i-1] > signal_line[i-1] and 
                  macd_line[i] < signal_line[i]):
                signals[i] = -1
        
        return signals