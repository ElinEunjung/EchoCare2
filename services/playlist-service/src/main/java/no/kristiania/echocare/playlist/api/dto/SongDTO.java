package no.kristiania.echocare.playlist.api.dto;

import java.util.UUID;

public record SongDTO(
        UUID id,
        String title,
        String artist,
        Integer releaseYear, Double bpm,
        Double energy  // 1-5 scale for energy level
) {
}
