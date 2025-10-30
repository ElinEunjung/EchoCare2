package no.kristiania.echocare.profile.domain.value;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.util.Collections;
import java.util.List;


@Embeddable
@NoArgsConstructor(force = true)
public class MusicPreference {
    @Getter
    @Column(name = "era", nullable = false)
    private final String era; // "1964-1975"
    @Column(name = "favorite_artists", nullable = false)
    private final String favoriteArtists;
    @Column(name = "favorite_genres", nullable = false) //e.g. "R&B, Ballads, Country"
    private final String favoriteGenres;


    public MusicPreference(String era,
                           List<String> favoriteArtists,
                           List<String> favoriteGenres) {
        this.era = era;
        this.favoriteArtists = String.join(",", favoriteArtists);
        this.favoriteGenres = String.join(",", favoriteGenres);
    }

    public List<String> getFavoriteArtists() {
        return List.of(favoriteArtists.split(","));
    }

    public List<String> getFavoriteGenres() {
        return List.of(favoriteGenres.split(","));
    }
}

//TODO: add runtime validation for era field (pattern check YYYY-YYYY)