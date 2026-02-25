package no.kristiania.echocare.playlist.integration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.kristiania.echocare.playlist.api.dto.event.ProfileEventDTO;
import no.kristiania.echocare.playlist.service.ProfileCacheService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Consumer for Profile-related events from RabbitMQ.
 * Caches profile data for playlist generation.
 *
 * This consumer is only active when RabbitMQ is enabled.
 * When disabled, the service falls back to synchronous REST calls.
 */
@Component
@ConditionalOnProperty(name = "spring.rabbitmq.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class ProfileEventConsumer {

    private final ProfileCacheService profileCacheService;

    @RabbitListener(queues = "${rabbitmq.queue.profile-events}")
    public void handleProfileEvent(ProfileEventDTO event) {
        log.info("Received {} event for profile: {}", event.getEventType(), event.getProfileId());

        try {
            // Always cache the profile (for both CREATED and UPDATED)
            profileCacheService.cacheProfileFromEvent(event);

            // Handle event-specific logic
            if ("CREATED".equals(event.getEventType())) {
                handleProfileCreated(event);
            } else if ("UPDATED".equals(event.getEventType())) {
                handleProfileUpdated(event);
            }
        } catch (Exception e) {
            log.error("Failed to process profile event: {}", event, e);
        }
    }

    private void handleProfileCreated(ProfileEventDTO event) {
        // TODO: Generate initial playlists for new patient
        log.debug("Profile created - ready for playlist generation: {}", event.getProfileId());
    }

    private void handleProfileUpdated(ProfileEventDTO event) {
        // TODO: Update/regenerate existing playlists if preferences changed
        log.debug("Profile updated - may need playlist regeneration: {}", event.getProfileId());
    }
}
