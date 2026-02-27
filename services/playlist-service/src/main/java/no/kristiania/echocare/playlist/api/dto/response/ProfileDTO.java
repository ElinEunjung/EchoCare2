package no.kristiania.echocare.playlist.api.dto.response;

import java.util.List;
import java.util.UUID;

// Used for:
// Rest calls from Profile Service → Playlist Service
// Matches PatientProfileResponse from profile-service
public record ProfileDTO (
        UUID id,
        String patientName,
        String era,
        List<String> favoriteArtists,
        List<String> symptoms,
        String dementiaStage
) {}
