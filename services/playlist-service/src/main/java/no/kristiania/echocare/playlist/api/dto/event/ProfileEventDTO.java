package no.kristiania.echocare.playlist.api.dto.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Event DTO for receiving Profile-related events from RabbitMQ
 * Consumed from profile.created and profile.updated events
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfileEventDTO {

    private UUID profileId;

    private String patientName;

    private String era;

    private String dementiaStage;

    private List<String> favoriteArtists;

    private List<String> symptoms;

    private String eventType; // "CREATED" or "UPDATED"

    private LocalDateTime timestamp;

}

