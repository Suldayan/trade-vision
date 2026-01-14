pub fn fill_nan(buffer: &mut [f64], count: usize) {
    for i in 0..count.min(buffer.len()) {
        buffer[i] = f64::NAN;
    }
}