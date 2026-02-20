package no.kristiania.echocare.profile.api.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import no.kristiania.echocare.profile.domain.entity.DementiaStage;

import java.util.List;
import java.util.UUID;


public record CreatePatientProfileRequest(

        @NotBlank String patientName,
        @NotNull Integer birthYear,
        @NotBlank String era,                       // "1965-1975"
        @NotEmpty List<String> favoriteArtists,     // "ABBA", "The Beatles"
        @NotEmpty List<String> favoriteGenres,      // "Pop", "Rock"
        @NotEmpty List<String> symptoms,            // ["anxiety", "agitation"]
        @NotNull DementiaStage stage,
        @NotNull UUID caregiverId

) {

}
