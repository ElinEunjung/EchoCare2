package no.kristiania.echocare.feedback.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.kristiania.echocare.feedback.api.dto.request.CreateFeedbackRequest;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "feedback_entries")
@Data
@NoArgsConstructor
public class Feedback {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "playlist_id", nullable = false)
    private UUID playlistId;

    @Column(name = "song_id", nullable = false)
    private UUID songId;

    @Column(name = "patient_profile_id", nullable = false)
    private UUID patientProfileId;

    private Boolean liked; // true for like, false for dislike

    private Integer rating; // 1-5 stars

    private String situation; // reduce stress, support activity, calm agitation, ease depression, ease anxiety

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // Constructor to convert from CreateFeedbackRequest
    public Feedback(CreateFeedbackRequest request) {
        this.playlistId = request.getPlaylistId();
        this.songId = request.getSongId();
        this.patientProfileId = request.getPatientProfileId();
        this.liked = request.getLiked();
        this.rating = request.getRating();
        this.situation = request.getSituation();
    }
}
