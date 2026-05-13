package com.flightplatform.weather.openweather;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class BoundingBoxGrid {

    private static final int LON_COLS = 10;
    private static final int LAT_ROWS = 6;

    private final List<BoundingBox> cells;
    private final double lamin, lamax, lomin, lomax;

    public BoundingBoxGrid(@Value("${weather.opensky.bounding-box.lamin}") double lamin,
                           @Value("${weather.opensky.bounding-box.lamax}") double lamax,
                           @Value("${weather.opensky.bounding-box.lomin}") double lomin,
                           @Value("${weather.opensky.bounding-box.lomax}") double lomax) {
        this.lamin = lamin;
        this.lamax = lamax;
        this.lomin = lomin;
        this.lomax = lomax;
        this.cells = buildGrid();
    }

    private List<BoundingBox> buildGrid() {
        List<BoundingBox> grid = new ArrayList<>(LON_COLS * LAT_ROWS);

        double latStep = (lamax - lamin) / LAT_ROWS;
        double lonStep = (lomax - lomin) / LON_COLS;

        for (int row = 0; row < LAT_ROWS; row++) {
            for (int col = 0; col < LON_COLS; col++) {
                double minLat = lamin + row * latStep;
                double maxLat = minLat + latStep;
                double minLon = lomin + col * lonStep;
                double maxLon = minLon + lonStep;

                String bboxId = String.format("PL_%02d_%02d", row, col);

                grid.add(new BoundingBox(
                        bboxId,
                        minLat, maxLat,
                        minLon, maxLon,
                        (minLat + maxLat) / 2.0,
                        (minLon + maxLon) / 2.0
                ));
            }
        }
        return grid;
    }

    public List<BoundingBox> getCells() {
        return cells;
    }

    public record BoundingBox(
            String id,
            double minLat, double maxLat,
            double minLon, double maxLon,
            double centerLat, double centerLon
    ) {}
}
