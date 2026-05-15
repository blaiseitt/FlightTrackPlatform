package com.flightplatform.weather.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenWeatherResponse(
        List<WeatherCondition> weather,
        CurrentConditions main,
        int visibility,
        Wind wind,
        Clouds clouds
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record WeatherCondition(String main, String description) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CurrentConditions(
            double temp,
            @JsonProperty("feels_like") double feelsLike
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Wind(double speed, int deg) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Clouds(int all) {}
}
