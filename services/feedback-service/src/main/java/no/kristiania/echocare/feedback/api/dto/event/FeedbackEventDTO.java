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
    private UUID songId;
    private UUID patientProfileId;
    private Boolean liked;
    private Integer rating;
    private String situation;
    private String eventType;
    private LocalDateTime timestamp;

    public FeedbackEventDTO(Feedback feedback) {
        this.feedbackId = feedback.getId();
        this.playlistId = feedback.getPlaylistId();
        this.songId = feedback.getSongId();
        this.patientProfileId = feedback.getPatientProfileId();
        this.liked = feedback.getLiked();
        this.rating = feedback.getRating();
        this.situation = feedback.getSituation();
        this.eventType = "SUBMITTED";
        this.timestamp = feedback.getCreatedAt();
    }
}


