package no.kristiania.echocare.playlist.api.dto.request;

import java.util.List;
import java.util.UUID;

/**
 * Request for generating a playlist based on patient profile and care need
 */
public record GeneratePlaylistRequest(
        UUID patientId,
        String careNeed, // stress_relief, activity_support, calming_agitation, ease_depression, ease_anxiety
        String era,
        String dementiaStage,
        List<String>favoriteArtists
) {
}
