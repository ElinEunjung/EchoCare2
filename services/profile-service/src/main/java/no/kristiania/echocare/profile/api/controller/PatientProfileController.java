package no.kristiania.echocare.profile.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import no.kristiania.echocare.profile.api.dto.requests.CreatePatientProfileRequest;
import no.kristiania.echocare.profile.api.dto.response.PatientProfileResponse;
import no.kristiania.echocare.profile.domain.entity.MusicPreference;
import no.kristiania.echocare.profile.domain.entity.PatientProfile;
import no.kristiania.echocare.profile.service.PatientProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for Patient Profile operations
 * Handles patient profile creation, retrieval, and updates
 */
@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
public class PatientProfileController {

    private final PatientProfileService profileService;

    @PostMapping
    public ResponseEntity<PatientProfileResponse> createProfile(@Valid @RequestBody CreatePatientProfileRequest request) {
        // Service layer handles both saving and event publishing
        PatientProfile profile = profileService.createProfile(request);

        PatientProfileResponse response = mapToResponse(profile);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PatientProfileResponse> getProfile(@PathVariable UUID id) {
        PatientProfile profile = profileService.getProfileById(id);
        PatientProfileResponse response = mapToResponse(profile);
        return ResponseEntity.ok(response);
    }

    /**
     * Map PatientProfile entity to PatientProfileResponse DTO
     */
    private PatientProfileResponse mapToResponse(PatientProfile profile) {
        String era = profile.getEraStart() + "-" + profile.getEraEnd();

        // Extract favorite artists and genres from music preferences
        List<String> favoriteArtists = profile.getMusicPreferences() != null
            ? profile.getMusicPreferences().stream()
                .map(MusicPreference::getArtist)
                .filter(artist -> artist != null && !artist.isBlank())
                .distinct()
                .toList()
            : List.of();

        List<String> favoriteGenres = profile.getMusicPreferences() != null
            ? profile.getMusicPreferences().stream()
                .map(MusicPreference::getGenre)
                .filter(genre -> genre != null && !genre.isBlank())
                .distinct()
                .toList()
            : List.of();

        return new PatientProfileResponse(
                profile.getId(),
                profile.getPatientName(),
                era,
                favoriteArtists,
                favoriteGenres,
                String.join(", ", profile.getSymptoms()),
                profile.getDementiaStage()
        );
    }
}

