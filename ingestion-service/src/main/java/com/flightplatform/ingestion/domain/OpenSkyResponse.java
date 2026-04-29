package com.flightplatform.ingestion.domain;

import lombok.Data;

import java.util.List;

/**
 * Raw response shape from OpenSky /api/states/all
 * Example response:
        * {
        *   "time": 1698234567,
        *   "states": [
        *     ["3c4b26", "DLH123 ", "Germany", 1698234560, 1698234565,
        *      13.4050, 52.5200, 10668.0, false, 245.3, 90.0, 0.0,
        *      null, 10820.0, "1234", false, 0],
        *     ...
        *   ]
        * }
        *
        * Index reference:
        *  0  icao24          String
 *  1  callsign        String (may have trailing spaces)
 *  2  origin_country  String
 *  3  time_position   Long (unix timestamp of last position update)
 *  4  last_contact    Long (unix timestamp of last ANY message)
 *  5  longitude       Double
 *  6  latitude        Double
 *  7  baro_altitude   Double (metres)
 *  8  on_ground       Boolean
 *  9  velocity        Double (m/s)
 *  10 true_track      Double (degrees, clockwise from north)
 *  11 vertical_rate   Double (m/s)
 *  12 sensors         int[] (sensor IDs, usually null)
        *  13 geo_altitude    Double (metres)
 *  14 squawk          String
 *  15 spi             Boolean
 *  16 position_source int (0=ADS-B, 1=ASTERIX, 2=MLAT, 3=FLARM)
 */
@Data
public class OpenSkyResponse {

    private Long time;
    private List<List<Object>> states;
}
