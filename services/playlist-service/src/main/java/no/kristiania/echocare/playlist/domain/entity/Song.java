package no.kristiania.echocare.playlist.domain.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Table(name = "songs")
@Data
public class Song {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "artist", nullable = false)
    private String artist;

    @Column(name = "genre", nullable = false)
    private String genre;

    @Column(name = "bpm")
    private Integer bpm; // beats per minute, optional but can be useful for matching energy levels

    @Column(name = "release_year", nullable = false)
    private Integer releaseYear;

     public Song(String title, String artist, String genre, Integer releaseYear) {
        this.title = title;
        this.artist = artist;
        this.genre = genre;
        this.releaseYear = releaseYear;
    }

     public Song() {
    }
}
