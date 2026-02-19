package no.kristiania.echocare.playlist.api.dto.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event DTO for receiving Feedback-related events from RabbitMQ
 * Consumed from feedback.submitted events
 */
@Data
@AllArgsConstructor
@NoArgsConstructor

public class FeedbackEventDTO {

    private UUID feedbackId;

    private UUID playlistId;

    private UUID songId;

    private UUID patientProfileId;

    private Boolean liked;

    private Integer rating;

    private String situation;

    private String eventType; // "SUBMITTED"

    private LocalDateTime timestamp;
}

