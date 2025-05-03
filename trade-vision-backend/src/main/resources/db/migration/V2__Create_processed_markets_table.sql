CREATE TABLE IF NOT EXISTS processed_markets (
    id UUID PRIMARY KEY,
    base_id VARCHAR(255) NOT NULL,
    quote_id VARCHAR(255) NOT NULL,
    exchange_id VARCHAR(255) NOT NULL,
    price_usd DECIMAL(19, 8) NOT NULL,
    updated BIGINT NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT uk_processed_markets_identifiers UNIQUE (base_id, quote_id, exchange_id)
);

CREATE INDEX idx_processed_markets_identifiers ON processed_markets (base_id, quote_id, exchange_id);

CREATE INDEX idx_processed_markets_timestamp ON processed_markets (timestamp);