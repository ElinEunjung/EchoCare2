package no.kristiania.echocare.profile.api.dto.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.kristiania.echocare.profile.domain.entity.DementiaStage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Event DTO for Profile-related events published to RabbitMQ
 * Used for profile.created and profile.updated events
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfileEventDTO {

    private UUID profileId;

    private String patientName;

    private Integer birthYear;

    private Integer eraStart;

    private Integer eraEnd;

    private DementiaStage dementiaStage;

    private UUID caregiverId;

    private List<MusicPreferenceEventDTO> musicPreferences;

    private List<String> symptoms;

    private String eventType; // "CREATED" or "UPDATED"

    private LocalDateTime timestamp;

    /**
     * Nested DTO for music preferences in events
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MusicPreferenceEventDTO {
        private UUID id;
        private String artist;
        private String genre;
        private Integer preferenceLevel;
    }
}

