use ta::indicators::{
    BollingerBands, 
    ExponentialMovingAverage, 
    MovingAverageConvergenceDivergence as Macd,
    SimpleMovingAverage
};
use ta::Next; 
use crate::utils::fill_nan;

pub fn calc_sma(prices: &[f64], window: usize, out: &mut [f64]) {
    debug_assert_eq!(out.len(), prices.len());
    debug_assert!(window > 0);
    debug_assert!(!prices.is_empty());

    let mut sma = SimpleMovingAverage::new(window).unwrap();
    
    // Fills the output buffer while iterating input once
    for (i, &price) in prices.iter().enumerate() {
        let val = sma.next(price);
        out[i] = val;
    }
    
    // Overwrite the warmup period with NaN to match Java
    fill_nan(out, window - 1);
}

pub fn calc_ema(prices: &[f64], window: usize, out: &mut [f64]) {
    debug_assert_eq!(out.len(), prices.len());
    debug_assert!(window > 0);
    debug_assert!(!prices.is_empty());

    let mut ema = ExponentialMovingAverage::new(window).unwrap();

    for (i, &price) in prices.iter().enumerate() {
        out[i] = ema.next(price);
    }

    fill_nan(out, window - 1);
}

pub fn calc_macd(
    prices: &[f64], 
    fast: usize, slow: usize, signal: usize,
    out_macd: &mut [f64], out_signal: &mut [f64], out_hist: &mut [f64]
) {
    debug_assert_eq!(out_macd.len(), prices.len()); 
    debug_assert_eq!(out_signal.len(), prices.len()); 
    debug_assert_eq!(out_hist.len(), prices.len());

    let mut macd = Macd::new(fast, slow, signal).unwrap();

    for (i, &price) in prices.iter().enumerate() {
        let result = macd.next(price);
        out_macd[i] = result.macd;
        out_signal[i] = result.signal;
        out_hist[i] = result.histogram;
    }
    
    // MACD needs a longer warmup (usually slow period)
    let warmup = slow - 1; 
    fill_nan(out_macd, warmup);
    fill_nan(out_signal, warmup + signal); // Signal line lags further
    fill_nan(out_hist, warmup + signal);
}

pub fn calc_bollinger(
    prices: &[f64], window: usize, num_std: f64,
    out_upper: &mut [f64], out_mid: &mut [f64], 
    out_lower: &mut [f64], out_std_dev: &mut [f64]
) {
    debug_assert_eq!(out_upper.len(), prices.len());
    debug_assert_eq!(out_mid.len(), prices.len());
    debug_assert_eq!(out_lower.len(), prices.len());
    debug_assert_eq!(out_std_dev.len(), prices.len());

    let mut bb = BollingerBands::new(window, num_std).unwrap();

    for (i, &price) in prices.iter().enumerate() {
        let result = bb.next(price);
        out_upper[i] = result.upper;
        out_mid[i] = result.average;
        out_lower[i] = result.lower;
        
        if num_std != 0.0 {
            out_std_dev[i] = (result.upper - result.average) / num_std;
        } else {
            out_std_dev[i] = 0.0;
        }
    }
    
    fill_nan(out_upper, window - 1);
    fill_nan(out_mid, window - 1);
    fill_nan(out_lower, window - 1);
    fill_nan(out_std_dev, window - 1);
}