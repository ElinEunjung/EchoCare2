package no.kristiania.echocare.profile.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.kristiania.echocare.profile.api.dto.event.ProfileEventDTO;
import no.kristiania.echocare.profile.domain.entity.PatientProfile;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Publisher service for Profile-related events
 *
 * This service is responsible for publishing profile events to RabbitMQ
 * when profiles are created or updated. Other services (like playlist-service)
 * can subscribe to these events to react accordingly.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ProfileEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.profile}")
    private String profileExchange;

    @Value("${rabbitmq.routing-key.profile-created}")
    private String profileCreatedRoutingKey;

    @Value("${rabbitmq.routing-key.profile-updated}")
    private String profileUpdatedRoutingKey;

    public void publishProfileCreated(PatientProfile profile) {
        try {
            ProfileEventDTO event = buildProfileEvent(profile, "CREATED");

            rabbitTemplate.convertAndSend(
                profileExchange,
                profileCreatedRoutingKey,
                event
            );

            log.info("Published profile created event: profileId={}, patientName={}, caregiverId={}",
                event.getProfileId(), event.getPatientName(), event.getCaregiverId());
        } catch (Exception e) {
            log.error("Failed to publish profile created event for profileId={}", profile.getId(), e);
        }
    }

    public void publishProfileUpdated(PatientProfile profile) {
        try {
            ProfileEventDTO event = buildProfileEvent(profile, "UPDATED");

            rabbitTemplate.convertAndSend(
                profileExchange,
                profileUpdatedRoutingKey,
                event
            );

            log.info("Published profile updated event: profileId={}, patientName={}, caregiverId={}",
                event.getProfileId(), event.getPatientName(), event.getCaregiverId());
        } catch (Exception e) {
            log.error("Failed to publish profile updated event for profileId={}", profile.getId(), e);
        }
    }

    private ProfileEventDTO buildProfileEvent(PatientProfile profile, String eventType) {
        ProfileEventDTO event = new ProfileEventDTO();
        event.setProfileId(profile.getId());
        event.setPatientName(profile.getPatientName());
        event.setBirthYear(profile.getBirthYear());
        event.setEraStart(profile.getEraStart());
        event.setEraEnd(profile.getEraEnd());
        event.setDementiaStage(profile.getDementiaStage());
        event.setCaregiverId(profile.getCaregiver() != null ? profile.getCaregiver().getId() : null);

        // Map music preferences to DTOs
        if (profile.getMusicPreferences() != null) {
            event.setMusicPreferences(
                profile.getMusicPreferences().stream()
                    .map(pref -> new ProfileEventDTO.MusicPreferenceEventDTO(
                        pref.getId(),
                        pref.getArtist(),
                        pref.getGenre(),
                        pref.getPreferenceLevel()
                    ))
                    .toList()
            );
        }

        // Map symptoms (already a List<String>)
        event.setSymptoms(profile.getSymptoms());

        event.setEventType(eventType);
        event.setTimestamp(LocalDateTime.now());

        return event;
    }
}


