package no.kristiania.echocare.feedback.api.dto.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.kristiania.echocare.feedback.domain.entity.Feedback;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event DTO for Feedback-related events published to RabbitMQ
 *
 * Feedback is immutable once submitted - represents the patient's authentic
 * response at that moment in time. This preserves historical accuracy for
 * analytics and pattern detection.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FeedbackEventDTO {

    private UUID feedbackId;
    private UUID playlistId;
    private UUID patientProfileId;
    private Boolean liked;
    private String eventType;
    private LocalDateTime createdAt;

    public FeedbackEventDTO(Feedback feedback) {
        this.feedbackId = feedback.getId();
        this.playlistId = feedback.getPlaylistId();
        this.patientProfileId = feedback.getPatientProfileId();
        this.liked = feedback.getLiked();
        this.eventType = "SUBMITTED";
        this.createdAt = feedback.getCreatedAt();
    }
}
