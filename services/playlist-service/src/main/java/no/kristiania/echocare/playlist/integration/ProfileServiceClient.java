package no.kristiania.echocare.playlist.integration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.kristiania.echocare.playlist.api.dto.response.ProfileDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProfileServiceClient {

    private final RestTemplate restTemplate;

    @Value("${services.profile.url:http://localhost:8081}")
    private String profileServiceUrl;

    public ProfileDTO getProfile(UUID profileId) {
        String url = profileServiceUrl + "/api/profiles/" + profileId;

        log.info("Fetching profile data from Profile Service for profileId={}", profileId);

        try {
            ProfileDTO profile = restTemplate.getForObject(url, ProfileDTO.class);
            log.info("Received profile data: {}", profile);
            return profile;
        } catch (HttpClientErrorException e) {
            log.error("Profile Service returned error status={} for profileId={}: {}",
                    e.getStatusCode().value(), profileId, e.getResponseBodyAsString());
            throw new RuntimeException("Failed to fetch profile data - Status: " + e.getStatusCode().value(), e);
        } catch (HttpServerErrorException e) {
            log.error("Profile Service server error status={} for profileId={}: {}",
                    e.getStatusCode().value(), profileId, e.getResponseBodyAsString());
            throw new RuntimeException("Profile Service server error - Status: " + e.getStatusCode().value(), e);
        } catch (Exception e) {
            log.error("Failed to fetch profile from {}: {}", url, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch profile data: " + e.getMessage(), e);
        }
    }
}
