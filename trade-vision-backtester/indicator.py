import numpy as np

class Indicator:
    def __init__(self):
        pass
    
    def sma(self, prices, window):
        weights = np.ones(window) / window
        sma = np.convolve(prices, weights, mode='valid')
        return np.concatenate([np.full(window-1, np.nan), sma])
    
    def ema(self, prices, window):
        alpha = 2 / (window + 1)
        ema = np.zeros_like(prices)
        ema[0] = prices[0]
        for i in range(1, len(prices)):
            ema[i] = alpha * prices[i] + (1 - alpha) * ema[i-1]
        return ema
    
    def rsi(self, prices, window=14):
        deltas = np.diff(prices)
        gains = np.where(deltas > 0, deltas, 0)
        losses = np.where(deltas < 0, -deltas, 0)
        
        avg_gain = self.sma(gains, window) 
        avg_loss = self.sma(losses, window)
        
        rs = avg_gain / (avg_loss + 1e-10)
        return 100 - (100 / (1 + rs))
    
    def macd(self, prices, fast=12, slow=26, signal=9):
        ema_fast = self.ema(prices, fast)
        ema_slow = self.ema(prices, slow)
        macd_line = ema_fast - ema_slow
        signal_line = self.ema(macd_line, signal)
        histogram = macd_line - signal_line
        return macd_line, signal_line, histogram
    
    def atr(self, high, low, close, window=14):
        prev_close = np.roll(close, 1)
        prev_close[0] = np.nan  
        tr = np.maximum(
            high - low,
            np.maximum(
                np.abs(high - prev_close),
                np.abs(low - prev_close)
            )
        )
        return self.sma(tr, window)
    
    def bollinger_bands(self, prices, window=20, num_std=2):
        sma_values = self.sma(prices, window)
        rolling_std = np.lib.stride_tricks.sliding_window_view(prices, window).std(axis=1)
        rolling_std = np.concatenate([np.full(window-1, np.nan), rolling_std])
        upper = sma_values + (rolling_std * num_std)
        lower = sma_values - (rolling_std * num_std)
        return upper, sma_values, lower