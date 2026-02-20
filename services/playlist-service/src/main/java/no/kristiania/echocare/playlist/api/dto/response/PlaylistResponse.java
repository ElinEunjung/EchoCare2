package no.kristiania.echocare.playlist.api.dto.response;

import no.kristiania.echocare.playlist.api.dto.TrackDto;

import java.util.List;

public record PlaylistResponse(
        String strategy, // e.g. "ANXEITY_MODERATE_SLOW_START"
        List<TrackDto> tracks
) {
}
