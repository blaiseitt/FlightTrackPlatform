CREATE TABLE IF NOT EXISTS airports (
    id          SERIAL PRIMARY KEY,
    icao_code   VARCHAR(4)  NOT NULL UNIQUE,
    iata_code   VARCHAR(3),
    name        VARCHAR(100) NOT NULL,
    city        VARCHAR(100),
    latitude    DOUBLE PRECISION NOT NULL,
    longitude   DOUBLE PRECISION NOT NULL
);