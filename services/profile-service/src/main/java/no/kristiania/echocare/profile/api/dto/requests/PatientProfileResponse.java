package no.kristiania.echocare.profile.api.dto.requests;

import no.kristiania.echocare.profile.domain.value.DementiaStage;

import java.util.List;
import java.util.UUID;

public record PatientProfileResponse(
        UUID id,
        String patientName,
        String era,
        List<String> favoriteArtists,
        List<String> favoriteGenres,
        String symptoms,
        DementiaStage stage
){}
