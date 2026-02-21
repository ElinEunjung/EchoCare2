package no.kristiania.echocare.playlist.api.dto.response;

import no.kristiania.echocare.playlist.api.dto.SongDTO;

import java.util.List;
import java.util.UUID;

public record PlaylistResponse(
        UUID patientId,
        List<SongDTO> tracks
) {
}
