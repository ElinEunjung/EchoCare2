package no.kristiania.echocare.profile.api.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreatePatientProfileRequest(
        @NotBlank String patientName,
        @NotNull String era,
        @NotNull String dementiaStage,
        @NotEmpty List<String> favoriteArtists,
        @NotEmpty List<String> symptoms

) {
}
