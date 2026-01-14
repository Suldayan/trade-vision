#[cfg(test)]
mod tests {
    use rust_native::{calc_sma, calc_ema, calc_macd, calc_bollinger};
    use approx::assert_relative_eq;

    fn sample_prices() -> Vec<f64> {
        vec![10.0, 11.0, 12.0, 11.5, 13.0, 14.0, 13.5, 15.0, 14.5, 16.0]
    }

    #[test]
    fn test_sma_basic() {
        let prices = sample_prices();
        let mut out = vec![0.0; prices.len()];
        
        calc_sma(&prices, 3, &mut out);
        
        // Warmup period should be NaN
        assert!(out[0].is_nan());
        assert!(out[1].is_nan());
        
        // First valid SMA: (10 + 11 + 12) / 3 = 11.0
        assert_relative_eq!(out[2], 11.0, epsilon = 1e-10);
        
        // Next: (11 + 12 + 11.5) / 3 = 11.5
        assert_relative_eq!(out[3], 11.5, epsilon = 1e-10);
    }

    #[test]
    fn test_sma_warmup() {
        let prices = sample_prices();
        let window = 5;
        let mut out = vec![0.0; prices.len()];
        
        calc_sma(&prices, window, &mut out);
        
        // First window-1 values should be NaN
        for i in 0..(window - 1) {
            assert!(out[i].is_nan(), "Index {} should be NaN", i);
        }
        
        // After warmup should be valid
        for i in (window - 1)..prices.len() {
            assert!(!out[i].is_nan(), "Index {} should not be NaN", i);
        }
    }

    #[test]
    fn test_ema_basic() {
        let prices = sample_prices();
        let mut out = vec![0.0; prices.len()];
        
        calc_ema(&prices, 3, &mut out);
        
        // Warmup period
        assert!(out[0].is_nan());
        assert!(out[1].is_nan());
        
        // After warmup, should have valid values
        assert!(!out[2].is_nan());
        assert!(out[2] > 0.0);
    }

    #[test]
    fn test_ema_flat_prices() {
        let prices = vec![50.0; 20];
        let mut out = vec![0.0; prices.len()];
        
        calc_ema(&prices, 5, &mut out);
        
        // After warmup, EMA should converge to flat value
        for i in 10..prices.len() {
            assert_relative_eq!(out[i], 50.0, epsilon = 0.1);
        }
    }

    #[test]
    fn test_macd_basic() {
        let prices = vec![100.0; 50].into_iter()
            .chain((0..50).map(|i| 100.0 + i as f64))
            .collect::<Vec<_>>();
        
        let mut macd = vec![0.0; prices.len()];
        let mut signal = vec![0.0; prices.len()];
        let mut hist = vec![0.0; prices.len()];
        
        calc_macd(&prices, 12, 26, 9, &mut macd, &mut signal, &mut hist);
        
        // Check warmup
        assert!(macd[0].is_nan());
        assert!(signal[0].is_nan());
        assert!(hist[0].is_nan());
        
        // After full warmup, should have valid values
        // MACD warmup: slow - 1 = 25
        // Signal warmup: (slow - 1) + signal = 25 + 9 = 34
        assert!(!macd[25].is_nan());
        assert!(!signal[34].is_nan());
        assert!(!hist[34].is_nan());
    }

    #[test]
    fn test_macd_histogram_relationship() {
        let prices = (0..50).map(|i| 100.0 + i as f64).collect::<Vec<_>>();
        let mut macd = vec![0.0; prices.len()];
        let mut signal = vec![0.0; prices.len()];
        let mut hist = vec![0.0; prices.len()];
        
        calc_macd(&prices, 12, 26, 9, &mut macd, &mut signal, &mut hist);
        
        // Histogram = MACD - Signal (after warmup)
        // Signal warmup is at index 34
        for i in 34..prices.len() {
            assert_relative_eq!(
                hist[i], 
                macd[i] - signal[i], 
                epsilon = 1e-10
            );
        }
    }

    #[test]
    fn test_bollinger_basic() {
        let prices = sample_prices();
        let mut upper = vec![0.0; prices.len()];
        let mut mid = vec![0.0; prices.len()];
        let mut lower = vec![0.0; prices.len()];
        let mut std_dev = vec![0.0; prices.len()];
        
        calc_bollinger(&prices, 5, 2.0, &mut upper, &mut mid, &mut lower, &mut std_dev);
        
        // Check warmup
        for i in 0..4 {
            assert!(upper[i].is_nan());
            assert!(mid[i].is_nan());
            assert!(lower[i].is_nan());
        }
        
        // After warmup, should have valid values
        assert!(!upper[4].is_nan());
        assert!(!mid[4].is_nan());
        assert!(!lower[4].is_nan());
    }

    #[test]
    fn test_bollinger_band_order() {
        let prices = sample_prices();
        let mut upper = vec![0.0; prices.len()];
        let mut mid = vec![0.0; prices.len()];
        let mut lower = vec![0.0; prices.len()];
        let mut std_dev = vec![0.0; prices.len()];
        
        calc_bollinger(&prices, 5, 2.0, &mut upper, &mut mid, &mut lower, &mut std_dev);
        
        // Upper >= Mid >= Lower (after warmup)
        for i in 4..prices.len() {
            assert!(upper[i] >= mid[i], "Upper should be >= mid at {}", i);
            assert!(mid[i] >= lower[i], "Mid should be >= lower at {}", i);
        }
    }

    #[test]
    fn test_bollinger_formula() {
        let prices = sample_prices();
        let num_std = 2.0;
        let mut upper = vec![0.0; prices.len()];
        let mut mid = vec![0.0; prices.len()];
        let mut lower = vec![0.0; prices.len()];
        let mut std_dev = vec![0.0; prices.len()];
        
        calc_bollinger(&prices, 5, num_std, &mut upper, &mut mid, &mut lower, &mut std_dev);
        
        // Verify formula: Upper = Mid + (num_std * std_dev)
        for i in 4..prices.len() {
            let expected_upper = mid[i] + (num_std * std_dev[i]);
            let expected_lower = mid[i] - (num_std * std_dev[i]);
            
            assert_relative_eq!(upper[i], expected_upper, epsilon = 1e-10);
            assert_relative_eq!(lower[i], expected_lower, epsilon = 1e-10);
        }
    }

    #[test]
    fn test_bollinger_flat_prices() {
        let prices = vec![75.0; 10];
        let mut upper = vec![0.0; prices.len()];
        let mut mid = vec![0.0; prices.len()];
        let mut lower = vec![0.0; prices.len()];
        let mut std_dev = vec![0.0; prices.len()];
        
        calc_bollinger(&prices, 5, 2.0, &mut upper, &mut mid, &mut lower, &mut std_dev);
        
        // With no volatility, bands should collapse to the price
        for i in 4..prices.len() {
            assert_relative_eq!(std_dev[i], 0.0, epsilon = 1e-10);
            assert_relative_eq!(upper[i], 75.0, epsilon = 1e-10);
            assert_relative_eq!(mid[i], 75.0, epsilon = 1e-10);
            assert_relative_eq!(lower[i], 75.0, epsilon = 1e-10);
        }
    }
}