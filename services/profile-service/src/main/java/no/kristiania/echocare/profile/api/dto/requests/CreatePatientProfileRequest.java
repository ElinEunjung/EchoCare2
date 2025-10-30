package no.kristiania.echocare.profile.api.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import no.kristiania.echocare.profile.domain.value.DementiaStage;

import java.util.List;


public record CreatePatientProfileRequest(

        @NotBlank String patientName,
        @NotBlank String era,                 // "1965-1975"
        @NotEmpty List<String> favoriteArtists,     // "ABBA", "The Beatles"
        @NotEmpty List<String> favoriteGenres,      // "Pop", "Rock"
        @NotBlank String symptoms,            // "anxiety, agitation"
        @NotNull DementiaStage stage

) {
}

// @NotBlank annotation ensures Spring to rejects bad payloads