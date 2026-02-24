package no.kristiania.echocare.playlist.api.dto.response;

import no.kristiania.echocare.playlist.api.dto.SongDTO;

import java.util.List;
import java.util.UUID;

public record PlaylistResponse(
        UUID patientId,
        UUID playlistId,
        String careNeed,
        String era,
        String dementiaStage,
        List<SongDTO> songs,
        String message // e.g., "10 calming songs selected for stress relief in mild stage"
) {
}
