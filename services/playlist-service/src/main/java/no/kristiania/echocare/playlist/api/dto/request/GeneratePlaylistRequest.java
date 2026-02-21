package no.kristiania.echocare.playlist.api.dto.request;

import no.kristiania.echocare.playlist.domain.entity.CareNeed;

import java.util.UUID;

/**
 * Request for generating a playlist based on patient profile and care need
 */
public record GeneratePlaylistRequest(
        UUID patientId,
        CareNeed careNeed,
        String dementiaStage
) {
}
