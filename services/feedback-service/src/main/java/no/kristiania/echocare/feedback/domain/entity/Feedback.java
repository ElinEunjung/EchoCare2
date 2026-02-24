package no.kristiania.echocare.feedback.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.kristiania.echocare.feedback.api.dto.request.SubmitFeedbackRequest;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "feedback")
@Data
@NoArgsConstructor
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "patient_profile_id", nullable = false)
    private UUID patientProfileId;

    @Column(name = "playlist_id", nullable = false)
    private UUID playlistId;

    private String dementiaStage; // mild/moderate/severe/null

    private String careNeed;

    private Boolean liked; // true/false/null

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }



    // Constructor to convert from CreateFeedbackRequest
    public Feedback(SubmitFeedbackRequest request) {
        this.playlistId = request.playlistId();
        this.patientProfileId = request.patientProfileId();
        this.liked = request.liked();
        this.dementiaStage = request.dementiaStage();
        this.careNeed = request.careNeed();
    }
}
