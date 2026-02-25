package no.kristiania.echocare.playlist.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "playlists")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Playlist {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "patient_profile_id", nullable = false)
    private UUID patientProfileId;

    @Column(name = "care_need", nullable = false)
    private String careNeed; // stress_relief, activity_support, calming_agitation,easing_depression, reducing_anxiety

    @Column
    private String era; // 1960-1970

    @Column(name = "dementia_stage")
    private String dementiaStage; // mild, moderate, severe

    @ManyToMany
    @JoinTable(
            name = "playlist_songs",
            joinColumns = @JoinColumn(name = "playlist_id"),
            inverseJoinColumns = @JoinColumn(name = "song_id")
    )
    private List<Song> songs = new ArrayList<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

}
