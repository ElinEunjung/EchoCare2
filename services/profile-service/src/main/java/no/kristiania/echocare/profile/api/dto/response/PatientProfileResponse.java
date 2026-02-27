package no.kristiania.echocare.profile.api.dto.response;

import java.util.List;
import java.util.UUID;

public record PatientProfileResponse(
        UUID id,
        String patientName,
        String era,  // Combined format: "1965-1975"
        List<String> favoriteArtists,
        List<String> symptoms, 
        String dementiaStage
){}
