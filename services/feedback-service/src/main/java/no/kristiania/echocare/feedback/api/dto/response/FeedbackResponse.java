package no.kristiania.echocare.feedback.api.dto.response;

import no.kristiania.echocare.feedback.domain.entity.Feedback;

import java.time.LocalDateTime;
import java.util.UUID;

public record FeedbackResponse(
        UUID feedbackId,
        UUID playlistId,
        UUID patientProfileId,
        Boolean liked,
        LocalDateTime createdAt
) {
    // Constructor to convert from Feedback entity
    public FeedbackResponse(Feedback feedback) {
        this(
            feedback.getId(),
            feedback.getPlaylistId(),
            feedback.getPatientProfileId(),
            feedback.getLiked(),
            feedback.getCreatedAt()
        );
    }
}
