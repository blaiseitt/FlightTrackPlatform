package com.flightplatform.ingestion.client;

import com.flightplatform.ingestion.domain.OpenSkyResponse;
import com.flightplatform.ingestion.token.OpenSkyTokenService;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@WireMockTest(httpPort = 9999)
class OpenSkyClientTest {

    private OpenSkyClient openSkyClient;

    @BeforeEach
    void setup() {
        OpenSkyTokenService tokenService = mock(OpenSkyTokenService.class);
        when(tokenService.getToken()).thenReturn("test-token");
        WebClient webClient = WebClient.builder().build();

        openSkyClient = new OpenSkyClient(tokenService, webClient,
                "http://localhost:9999", 0.0, 0.0, 0.0, 0.0);
    }

    @Test
    void shouldReturnEmptyOnRateLimit() {
        stubFor(get(anyUrl())
                .willReturn(aResponse().withStatus(429)));

        Optional<OpenSkyResponse> result = openSkyClient.fetchStates();

        assertThat(result).isEmpty();
    }

    @Test
    void shouldHandleServerError() {
        stubFor(get(anyUrl())
                .willReturn(aResponse().withStatus(500)));

        assertThatCode(() -> openSkyClient.fetchStates())
            .doesNotThrowAnyException();
    }

    @Test
    void shouldReturnFlightsOnSuccess() {
        stubFor(get(urlPathEqualTo("/states/all"))
                .withQueryParam("lamin", equalTo("0.0"))
                .withQueryParam("lamax", equalTo("0.0"))
                .withQueryParam("lomin", equalTo("0.0"))
                .withQueryParam("lomax", equalTo("0.0"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("opensky-response.json")));

        Optional<OpenSkyResponse> result = openSkyClient.fetchStates();

        assertThat(result).isPresent();
        assertThat(result.get().getStates()).hasSize(80);
    }
}
