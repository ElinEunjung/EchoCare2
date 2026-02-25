package no.kristiania.echocare.playlist.api.dto.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event DTO for receiving Feedback-related events from RabbitMQ
 * Consumed from feedback.submited events
 *
 * Feedback contains only the caregiver's response (liked/disliked).
 * Profile context (careNeed, dementiaStage) is fetched separately via
 * ProfileServiceClient when needed for analytics or recommendations.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FeedbackEventDTO {

    private UUID feedbackId;
    private UUID playlistId;
    private UUID patientProfileId;
    private Boolean liked;
    private String eventType; // "SUBMITTED"
    private LocalDateTime createdAt;
}

