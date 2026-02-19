package no.kristiania.echocare.feedback.domain.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;
@Entity
@Table(name = "feedback_entries")
@Data
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

        public Feedback(UUID id, UUID playlistId, UUID songId, UUID patientProfileId, Boolean liked, Integer rating, String situation) {
            this.id = id;
            this.playlistId = playlistId;
            this.songId = songId;
            this.patientProfileId = patientProfileId;
            this.liked = liked;
            this.rating = rating;
            this.situation = situation;
        }
}
