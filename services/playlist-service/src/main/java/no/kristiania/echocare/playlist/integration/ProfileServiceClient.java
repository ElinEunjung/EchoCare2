package no.kristiania.echocare.playlist.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.UUID;

@Component
public class ProfileServiceClient {

    private final RestClient rest;

    public ProfileServiceClient(@Value("${profile.service.url:http://localhost:8081}") String profileServiceUrl) {
        this.rest = RestClient.builder()
                .baseUrl(profileServiceUrl)
                .build();
    }

    public Map<String, Object> getProfile(UUID profileId) {
        return rest.get()
                .uri("/api/profiles/{profileId}", profileId)
                .retrieve()
                .body(Map.class);
    }
}
