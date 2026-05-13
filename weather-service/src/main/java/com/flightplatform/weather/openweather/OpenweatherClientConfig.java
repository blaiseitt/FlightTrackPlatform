package com.flightplatform.weather.openweather;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class OpenweatherClientConfig {

    @Bean
    public WebClient openweatherClient() {
        return WebClient.builder()
                .build();
    }
}
