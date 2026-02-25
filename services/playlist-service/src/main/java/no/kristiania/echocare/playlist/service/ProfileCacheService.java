package no.kristiania.echocare.playlist.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.kristiania.echocare.playlist.api.dto.event.ProfileEventDTO;
import no.kristiania.echocare.playlist.api.dto.response.ProfileDTO;
import no.kristiania.echocare.playlist.integration.ProfileServiceClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cache for patient profiles. Stores profiles from RabbitMQ events
 * and serves them during playlist generation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileCacheService {

    private final ProfileServiceClient profileServiceClient;
    private final Map<UUID, ProfileDTO> cache = new ConcurrentHashMap<>();

    public ProfileDTO getProfile(UUID profileId) {
        ProfileDTO cached = cache.get(profileId);

        // Fast! No network call
        if (cached != null) {
            log.info("Profile cache hit: {}", profileId);
            return cached;
        }

        // If cache misses, fetch from Profile Service (network call)
        log.warn("Profile cache miss: {} - fetching from service", profileId);
        ProfileDTO profile = profileServiceClient.getProfile(profileId);

        if (profile != null) {
            cache.put(profile.id(), profile);
        }

        return profile;
    }

    public void cacheProfileFromEvent(ProfileEventDTO event) {
        log.info("Caching profile: {} ({})", event.getProfileId(), event.getEventType());

        ProfileDTO profile = new ProfileDTO(
            event.getProfileId(),
            event.getPatientName(),
            event.getEra(),
            event.getFavoriteArtists() != null ? event.getFavoriteArtists() : List.of(),
            event.getSymptoms() != null ? String.join(", ", event.getSymptoms()) : "",
            event.getDementiaStage()
        );

        cache.put(event.getProfileId(), profile);
        log.debug("Cache size: {}", cache.size());
    }

    /**
     * Clear all cached profiles (useful for testing synchronous REST calls)
     */
    public void clearCache() {
        int size = cache.size();
        cache.clear();
        log.info("Profile cache cleared - removed {} entries", size);
    }

    /**
     * Get cache statistics
     */
    public int getCacheSize() {
        return cache.size();
    }
}

