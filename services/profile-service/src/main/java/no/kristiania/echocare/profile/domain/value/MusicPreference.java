package no.kristiania.echocare.profile.domain.value;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class MusicPreference {
    @Column(name = "era", nullable = false)
    private String era; // "1964-1975"
    @Column(name = "favorite_artists", nullable = false)
    private String favoriteArtists;
    @Column(name = "favorite_genres", nullable = false) //e.g. "R&B, Ballads, Country"
    private String favoriteGenres;
}

//TODO: add runtime validation for era field (pattern check YYYY-YYYY)