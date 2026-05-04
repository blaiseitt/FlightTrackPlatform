CREATE TEMP TABLE airline_import (
    icao_code VARCHAR(4),
    iata_code VARCHAR(3),
    name      TEXT,
    country   TEXT
);

COPY airline_import (icao_code, iata_code, name, country)
FROM '/docker-entrypoint-initdb.d/airlines_clean.csv'
DELIMITER ','
CSV HEADER;

INSERT INTO airline (icao_code, iata_code, name, country)
SELECT icao_code,
       NULLIF(iata_code, ''),
       name,
       NULLIF(country, '')
FROM airline_import
ON CONFLICT (icao_code) DO NOTHING;

DROP TABLE airline_import;