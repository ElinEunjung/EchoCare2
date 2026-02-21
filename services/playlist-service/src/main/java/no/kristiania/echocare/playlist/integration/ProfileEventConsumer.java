package no.kristiania.echocare.playlist.integration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.kristiania.echocare.playlist.api.dto.event.ProfileEventDTO;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumer for Profile-related events from RabbitMQ
 * Listens to profile.created and profile.updated events
 *
 * This consumer enables the playlist service to react to profile changes
 * in an event-driven manner, allowing automatic playlist generation or updates
 * when patient profiles are created or modified.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ProfileEventConsumer {

    @RabbitListener(queues = "${rabbitmq.queue.profile-events}")
    public void handleProfileEvent(ProfileEventDTO event) {
        log.info("Received profile event: type={}, profileId={}", event.getEventType(), event.getProfileId());

        try {
            switch (event.getEventType()) {
                case "CREATED":
                    handleProfileCreated(event);
                    break;
                case "UPDATED":
                    handleProfileUpdated(event);
                    break;
                default:
                    log.warn("Unknown event type: {}", event.getEventType());
            }
        } catch (Exception e) {
            log.error("Failed to process profile event: {}", event, e);
        }
    }

    //TODO: Implement initial playlist generation for new patients

    private void handleProfileCreated(ProfileEventDTO event) {
        log.info("Processing CREATED event for profile: {}", event.getProfileId());
        log.debug("Profile details - Name: {}, Stage: {}, Era: {}",
            event.getPatientName(),
            event.getDementiaStage(),
            event.getEra());

        // TODO: Generate initial playlists for the new patient
        // This could include:
        // - Creating default playlists based on patient preferences
        // - Generating situation-specific playlists
        // Example: playlistService.generateInitialPlaylists(event);
    }


     //TODO: Implement playlist update logic based on profile changes

    private void handleProfileUpdated(ProfileEventDTO event) {
        log.info("Processing UPDATED event for profile: {}", event.getProfileId());

        // TODO: Update existing playlists based on profile changes
        // This could include:
        // - Regenerating playlists if preferences changed
        // - Adjusting playlist complexity based on dementia stage
        // Example: playlistService.updatePlaylistsForProfile(event);
    }
}

