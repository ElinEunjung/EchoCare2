package no.kristiania.echocare.profile.api.dto.requests;

import jakarta.validation.constraints.NotBlank;
import no.kristiania.echocare.profile.domain.value.DementiaStage;
import org.antlr.v4.runtime.misc.NotNull;


public record CreatePatientProfileRequest(

        @NotBlank String patientName,
        @NotBlank String era,                 // "1965-1975"
        @NotBlank String favoriteArtists,     // "ABBA, The Beatles"
        @NotBlank String favoriteGenres,      // "Pop, Rock"
        @NotBlank String symptoms,            // "anxiety, agitation"
        @NotNull DementiaStage stage

) {
}

// @NotBlank annotation ensures Spring to rejects bad payloads