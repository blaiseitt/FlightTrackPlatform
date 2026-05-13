package com.flightplatform.weather.openweather;

import com.flightplatform.weather.openweather.BoundingBoxGrid.BoundingBox;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Optional;

@Component
@Slf4j
public class OpenWeatherClient {

    private final WebClient webClient;
    private final String apiKey;
    private final String baseUrl;

    public OpenWeatherClient(WebClient openweatherClient,
                             @Value("${weather.openweather.base-url}") String baseUrl,
                             @Value("${weather.openweather.client-key}") String apiKey) {
        this.webClient = openweatherClient;
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
    }

    public Optional<OpenWeatherResponse> fetchForCell(BoundingBox cell) {
        try {
            return Optional.ofNullable(
                    webClient.get()
                            .uri(buildUri(cell.centerLat(), cell.centerLon()))
                            .retrieve()
                            .bodyToMono(OpenWeatherResponse.class)
                            .block()
            );
        } catch (Exception e) {
            log.warn("OpenWeather fetch failed for cell={} lat={} lon={}: {}",
                    cell.id(), cell.centerLat(), cell.centerLon(), e.getMessage());
            return Optional.empty();
        }
    }

    private String buildUri(double lat, double lon) {
        return String.format(
                "%s/weather?lat=%s&lon=%s&appid=%s&units=metric",
                baseUrl, lat, lon, apiKey
        );
    }
}
