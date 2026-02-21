package no.kristiania.echocare.playlist.api.dto;

import java.util.UUID;

public record SongDTO(
        UUID id,
        String title,
        String artist,
        String genre,
        int bpm,
        String eraTag,
        int energy  // 1-5 scale for energy level
) {
}
