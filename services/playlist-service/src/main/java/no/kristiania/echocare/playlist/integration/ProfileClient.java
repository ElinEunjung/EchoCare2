package no.kristiania.echocare.playlist.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.UUID;

@Component
public class ProfileClient {

    private final RestClient rest;

    public ProfileClient(@Value("${profile.service.url:http://localhost:8081}") String profileServiceUrl) {
        this.rest = RestClient.builder()
                .baseUrl(profileServiceUrl)
                .build();
    }

    public Map<String, Object> getProfile(UUID id) {
        return rest.get()
                .uri("/api/profiles/{id}", id)
                .retrieve()
                .body(Map.class);
    }
}
