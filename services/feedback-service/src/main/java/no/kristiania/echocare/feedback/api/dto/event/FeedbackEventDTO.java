package no.kristiania.echocare.feedback.api.dto.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.kristiania.echocare.feedback.domain.entity.Feedback;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event DTO for Feedback-related events published to RabbitMQ
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FeedbackEventDTO {

    private UUID feedbackId;
    private UUID playlistId;
    private UUID patientProfileId;
    private Boolean liked;
    private String careNeed;
    private String dementiaStage;
    private String eventType;
    private LocalDateTime createdAt;

    public FeedbackEventDTO(Feedback feedback) {
        this.feedbackId = feedback.getId();
        this.playlistId = feedback.getPlaylistId();
        this.patientProfileId = feedback.getPatientProfileId();
        this.liked = feedback.getLiked();
        this.careNeed = feedback.getCareNeed();
        this.dementiaStage = feedback.getDementiaStage();
        this.eventType = "SUBMITTED";
        this.createdAt = feedback.getCreatedAt();
    }
}


