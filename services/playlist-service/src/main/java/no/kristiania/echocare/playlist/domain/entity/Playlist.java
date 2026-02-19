package no.kristiania.echocare.playlist.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "playlists")
@Data
@NoArgsConstructor
public class Playlist {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "patient_profile_id", nullable = false)
    private UUID patientProfileId;

    @Column(name = "situation", nullable = false)
    private String situation; // reduce stress, support activity, calm agitation, ease depression, ease anxiety


    @Column(name = "dementia_stage")
    private String dementiaStage; // mild, moderate, severe

    @ManyToMany
    @JoinTable(
            name = "playlist_songs",
            joinColumns = @JoinColumn(name = "playlist_id"),
            inverseJoinColumns = @JoinColumn(name = "song_id")
    )
    private List<Song> songs;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();


     public Playlist(UUID id, UUID patientProfileId, String situation, String dementiaStage, List songs) {
        this.id = id;
        this.patientProfileId = patientProfileId;
        this.situation = situation;
        this.dementiaStage = dementiaStage;
        this.songs = songs;
    }
}
