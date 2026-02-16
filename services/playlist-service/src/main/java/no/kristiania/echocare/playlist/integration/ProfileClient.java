package no.kristiania.echocare.playlist.integration;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.UUID;

@Component
public class ProfileClient {

    private final RestClient rest = RestClient.builder()
            .baseUrl("http://profile-service:8081") // adjust for local/dev
            .build();

    public Map<String, Object> getProfile(UUID id) {
        return rest.get()
                .uri("/profiles/{id}", id)
                .retrieve()
                .body(Map.class);
    }
}
