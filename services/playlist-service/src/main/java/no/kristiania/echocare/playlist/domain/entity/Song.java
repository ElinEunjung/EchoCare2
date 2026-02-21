package no.kristiania.echocare.playlist.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "songs")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class Song {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "artist", nullable = false)
    private String artist;

    @Column(name = "release_year", nullable = false)
    private Integer releaseYear;

    // Audio features for filtering
    private Double bpm; // beats per minute (60-180), useful for matching energy levels
    private Double energy; // 1-10 scale for energy level, useful for matching care needs
}
