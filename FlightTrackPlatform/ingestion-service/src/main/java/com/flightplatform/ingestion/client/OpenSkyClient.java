package com.flightplatform.ingestion.client;

import com.flightplatform.ingestion.domain.OpenSkyResponse;
import com.flightplatform.ingestion.token.OpenSkyTokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Component
@Slf4j
public class OpenSkyClient {

    private final WebClient webClient;
    private final OpenSkyTokenService tokenService;

    @Value("${opensky.base-url}")
    private String baseUrl;

    @Value("${opensky.bounding-box.lamin}")
    private double lamin;
    @Value("${opensky.bounding-box.lamax}")
    private double lamax;
    @Value("${opensky.bounding-box.lomin}")
    private double lomin;
    @Value("${opensky.bounding-box.lomax}")
    private double lomax;

    public OpenSkyClient(OpenSkyTokenService tokenService) {
        this.tokenService = tokenService;
        this.webClient = WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(5 * 1024 * 1024))
                .build();
    }

    public Optional<OpenSkyResponse> fetchStates() {
        try {
            String uri = buildUri();
            log.debug("Fetching OpenSky states from: {}", uri);

            var requestSpec = webClient.get().uri(uri);

            String token = tokenService.getToken();
            if (token != null) {
                requestSpec = requestSpec.header("Authorization", "Bearer " + token);
            } else {
                log.debug("No token available - using anonymous access (rate limits apply)");
            }

            OpenSkyResponse response = requestSpec
                    .retrieve()
                    .onStatus(status -> status.value() == 401, resp -> {
                        log.error("OpenSky 401 Unauthorized - check your client_id and client_secret");
                        return Mono.error(new RuntimeException("Unauthorized"));
                    })
                    .onStatus(status -> status.value() == 429, resp -> {
                        log.warn("OpenSky rate limit hit (429) - backing off until next scheduled poll");
                        return Mono.error(new RuntimeException("Rate limited"));
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, resp -> {
                        log.warn("OpenSky server error {} - will retry next poll", resp.statusCode());
                        return Mono.error(new RuntimeException("Server error: " + resp.statusCode()));
                    })
                    .bodyToMono(OpenSkyResponse.class)
                    .block();

            return Optional.ofNullable(response);

        } catch (WebClientResponseException e) {
            log.error("OpenSky HTTP error {}: {}", e.getStatusCode(), e.getMessage());
            return Optional.empty();
        } catch (Exception e) {
            log.error("Failed to fetch OpenSky states: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private String buildUri() {
        return String.format(
                "%s/states/all?lamin=%s&lamax=%s&lomin=%s&lomax=%s",
                baseUrl, lamin, lamax, lomin, lomax
        );
    }
}