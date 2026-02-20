package no.kristiania.echocare.playlist.api.dto;

public record TrackDto(
        String title,
        String artist,
        int bpm,
        String eraTag,
        int energy  // 1-5 scale for energy level
) {
}
