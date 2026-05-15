package com.flightplatform.weather.openweather;

import com.flightplatform.weather.client.OpenWeatherClient;
import com.flightplatform.weather.client.OpenWeatherResponse;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import com.flightplatform.weather.service.BoundingBoxGrid.BoundingBox;

import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

@WireMockTest(httpPort = 9999)
class OpenWeatherClientTest {

    private OpenWeatherClient openWeatherClient;

    @BeforeEach
    void setup() {
        WebClient webClient = WebClient.builder().build();

        openWeatherClient = new OpenWeatherClient(webClient,
                "http://localhost:9999", "test-api-key");
    }

    @Test
    void shouldReturnEmptyOnRateLimit() {
        stubFor(get(urlPathEqualTo("/weather"))
                .willReturn(aResponse()
                        .withStatus(429)));

        BoundingBox cell = new BoundingBox("PL_03_05", 51.0, 53.0, 20.0, 22.0, 52.0, 21.0);
        Optional<OpenWeatherResponse> result = openWeatherClient.fetchForCoordinates(cell.centerLat(), cell.centerLon());

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnWeatherForCell() {
        stubFor(get(urlPathEqualTo("/weather"))
                .withQueryParam("lat", equalTo("51.76"))
                .withQueryParam("lon", equalTo("19.46"))
                .withQueryParam("appid", equalTo("test-api-key"))
                .withQueryParam("units", equalTo("metric"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("openweather-response.json")));

        //only central la lo matters for this test
        BoundingBox cell = new BoundingBox("PL_03_05", 51.0, 53.0, 20.0, 22.0, 51.76, 19.46);
        Optional<OpenWeatherResponse> result = openWeatherClient.fetchForCoordinates(cell.centerLat(), cell.centerLon());

        assertThat(result).isPresent();
        assertThat(result.get().main().temp()).isEqualTo(13.08);
        assertThat(result.get().main().feelsLike()).isEqualTo(11.88);
        assertThat(result.get().wind().speed()).isEqualTo(6.71);
        assertThat(result.get().wind().deg()).isEqualTo(223);
        assertThat(result.get().visibility()).isEqualTo(10000);
        assertThat(result.get().weather()).hasSize(1);
        assertThat(result.get().weather().get(0).main()).isEqualTo("Clouds");
        assertThat(result.get().weather().get(0).description()).isEqualTo("scattered clouds");
    }

    //TODO add timeout both in test and in client
    //TODO test for server error

}
