CREATE TABLE IF NOT EXISTS raw_markets (
    id UUID PRIMARY KEY,
    exchange_id VARCHAR(255) NOT NULL,
    base_id VARCHAR(255) NOT NULL,
    quote_id VARCHAR(255) NOT NULL,
    base_symbol VARCHAR(255),
    quote_symbol VARCHAR(255),
    rank INTEGER,
    price_quote DECIMAL(19, 8) NOT NULL,
    price_usd DECIMAL(19, 8) NOT NULL,
    volume_usd_24hr DECIMAL(19, 8) NOT NULL,
    percent_exchange_volume DECIMAL(10, 8) NOT NULL,
    trades_count_24hr INTEGER,
    updated BIGINT NOT NULL,
    timestamp BIGINT,
    version INTEGER,
    CONSTRAINT uk_raw_markets_identifiers UNIQUE (base_id, quote_id, exchange_id)
);