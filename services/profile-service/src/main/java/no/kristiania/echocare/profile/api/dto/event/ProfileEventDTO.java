package no.kristiania.echocare.profile.api.dto.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

/** profile-service -> message broker -> playlist-service **/

public class ProfileEventDTO {

    private UUID profileId;

    private String patientName;

    private String era;

    private String dementiaStage;

    private List<String> favoriteArtists; // Comma-separated string of favorite artists

    private List<String> symptoms;

    private String eventType; // "CREATED" or "UPDATED"

    private LocalDateTime timestamp;

}

