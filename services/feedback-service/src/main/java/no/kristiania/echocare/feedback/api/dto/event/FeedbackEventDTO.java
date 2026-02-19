package no.kristiania.echocare.feedback.api.dto.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event DTO for Feedback-related events published to RabbitMQ
 * Used for feedback.submitted events
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

