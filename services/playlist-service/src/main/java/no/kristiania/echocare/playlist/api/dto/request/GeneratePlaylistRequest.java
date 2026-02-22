package no.kristiania.echocare.playlist.api.dto.request;

import java.util.List;
import java.util.UUID;

// Used for:
// Frontend/API → Playlist Service (incoming request)

public record GeneratePlaylistRequest(
        UUID patientId,
        String careNeed, // stress_relief, activity_support, calming_agitation, ease_depression, ease_anxiety
        String era,
        String dementiaStage,
        List<String>favoriteArtists
) {
}
