use criterion::{black_box, criterion_group, criterion_main, Criterion, Throughput};
use engine::{calc_sma, calc_ema, calc_macd, calc_bollinger};

fn generate_ohlcv_prices(size: usize) -> Vec<f64> {
    let mut prices = Vec::with_capacity(size);
    let mut price = 100.0;
    let mut rng_state = 12345u64;
    
    for _ in 0..size {
        rng_state = rng_state.wrapping_mul(1664525).wrapping_add(1013904223);
        let change = ((rng_state % 200) as f64 - 100.0) / 100.0;
        price = (price + change).max(1.0);
        prices.push(price);
    }
    
    prices
}

fn bench_sma(c: &mut Criterion) {
    let prices = generate_ohlcv_prices(100_000);
    let mut out = vec![0.0; prices.len()];
    
    let mut group = c.benchmark_group("sma");
    group.throughput(Throughput::Elements(prices.len() as u64));
    group.bench_function("100k_window20", |b| {
        b.iter(|| {
            calc_sma(black_box(&prices), black_box(20), black_box(&mut out));
        });
    });
    group.finish();
}

fn bench_ema(c: &mut Criterion) {
    let prices = generate_ohlcv_prices(100_000);
    let mut out = vec![0.0; prices.len()];
    
    let mut group = c.benchmark_group("ema");
    group.throughput(Throughput::Elements(prices.len() as u64));
    group.bench_function("100k_window12", |b| {
        b.iter(|| {
            calc_ema(black_box(&prices), black_box(12), black_box(&mut out));
        });
    });
    group.finish();
}

fn bench_macd(c: &mut Criterion) {
    let prices = generate_ohlcv_prices(100_000);
    let mut macd = vec![0.0; prices.len()];
    let mut signal = vec![0.0; prices.len()];
    let mut hist = vec![0.0; prices.len()];
    
    let mut group = c.benchmark_group("macd");
    group.throughput(Throughput::Elements(prices.len() as u64));
    group.bench_function("100k_standard", |b| {
        b.iter(|| {
            calc_macd(
                black_box(&prices),
                black_box(12),
                black_box(26),
                black_box(9),
                black_box(&mut macd),
                black_box(&mut signal),
                black_box(&mut hist),
            );
        });
    });
    group.finish();
}

fn bench_bollinger(c: &mut Criterion) {
    let prices = generate_ohlcv_prices(100_000);
    let mut upper = vec![0.0; prices.len()];
    let mut mid = vec![0.0; prices.len()];
    let mut lower = vec![0.0; prices.len()];
    let mut std_dev = vec![0.0; prices.len()];
    
    let mut group = c.benchmark_group("bollinger");
    group.throughput(Throughput::Elements(prices.len() as u64));
    group.bench_function("100k_window20", |b| {
        b.iter(|| {
            calc_bollinger(
                black_box(&prices),
                black_box(20),
                black_box(2.0),
                black_box(&mut upper),
                black_box(&mut mid),
                black_box(&mut lower),
                black_box(&mut std_dev),
            );
        });
    });
    group.finish();
}

criterion_group!(
    benches,
    bench_sma,
    bench_ema,
    bench_macd,
    bench_bollinger,
);

criterion_main!(benches);