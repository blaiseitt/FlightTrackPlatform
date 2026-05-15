package com.flightplatform.weather.client;

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

    public Optional<OpenWeatherResponse> fetchForCoordinates(double lat, double lon) {
        try {
            return Optional.ofNullable(
                    webClient.get()
                            .uri(buildUri(lat, lon))
                            .retrieve()
                            .bodyToMono(OpenWeatherResponse.class)
                            .block()
            );
        } catch (Exception e) {
            log.warn("OpenWeather fetch failed for coordinates= lat={} lon={}: {}",
                    lat, lon, e.getMessage());
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
