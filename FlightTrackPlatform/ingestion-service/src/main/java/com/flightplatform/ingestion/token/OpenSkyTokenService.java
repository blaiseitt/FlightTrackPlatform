package com.flightplatform.ingestion.token;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.util.Map;

@Service
@Slf4j
public class OpenSkyTokenService {

    private final WebClient webClient;
    private final String tokenUrl;
    private final String clientId;
    private final String clientSecret;

    private String cachedToken;
    private Instant tokenExpiresAt = Instant.MIN;

    public OpenSkyTokenService(
            @Value("${opensky.token-url}") String tokenUrl,
            @Value("${opensky.client-id}") String clientId,
            @Value("${opensky.client-secret}") String clientSecret) {
        this.webClient = WebClient.builder().build();
        this.tokenUrl = tokenUrl;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public String getToken() {
        if (isDemoMode()) {
            log.debug("Running in demo mode - using anonymous OpenSky access");
            return null;
        }

        if (cachedToken == null || isExpiringSoon()) {
            refreshToken();
        }
        return cachedToken;
    }

    public boolean isDemoMode() {
        return "demo".equals(clientId) || "demo".equals(clientSecret);
    }

    private boolean isExpiringSoon() {
        return Instant.now().isAfter(tokenExpiresAt.minusSeconds(60));
    }

    @SuppressWarnings("unchecked")
    private void refreshToken() {
        log.info("Refreshing OpenSky OAuth2 token");
        try {
            Map<String, Object> response = webClient.post()
                    .uri(tokenUrl)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData("grant_type", "client_credentials")
                            .with("client_id", clientId)
                            .with("client_secret", clientSecret))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null) {
                throw new RuntimeException("Empty response from token endpoint");
            }

            this.cachedToken = (String) response.get("access_token");
            int expiresIn = ((Number) response.get("expires_in")).intValue();
            this.tokenExpiresAt = Instant.now().plusSeconds(expiresIn);

            log.info("OpenSky token refreshed successfully, valid for {}s", expiresIn);
        } catch (Exception e) {
            log.error("Failed to refresh OpenSky token: {}", e.getMessage());
        }
    }
}
