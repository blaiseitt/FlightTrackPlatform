CREATE TABLE airline (
    id SERIAL PRIMARY KEY,
    icao_code VARCHAR(4) UNIQUE NOT NULL,
    iata_code VARCHAR(3),
    name TEXT NOT NULL,
    country TEXT
);

CREATE INDEX idx_airline_icao ON airline(icao_code);