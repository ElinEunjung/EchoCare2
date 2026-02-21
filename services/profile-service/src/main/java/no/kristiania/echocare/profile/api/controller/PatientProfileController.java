package no.kristiania.echocare.profile.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import no.kristiania.echocare.profile.api.dto.requests.CreatePatientProfileRequest;
import no.kristiania.echocare.profile.api.dto.response.PatientProfileResponse;
import no.kristiania.echocare.profile.domain.entity.PatientProfile;
import no.kristiania.echocare.profile.service.PatientProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
public class PatientProfileController {

    private final PatientProfileService profileService;

    /**
     * User story 1: Register patient profile
     * "As a caregiver, I register my mother, set her era, favorite artists, and symptoms"
     */
    @PostMapping
    public ResponseEntity<PatientProfileResponse> createProfile(
            @Valid @RequestBody CreatePatientProfileRequest request
    ) {
        PatientProfile profile = profileService.createProfile(request);
        PatientProfileResponse response = mapToResponse(profile);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get profile by ID - Used by Playlist Service (synchronous call)
     */
    @GetMapping("/{profileId}")
    public ResponseEntity<PatientProfileResponse> getProfile(@PathVariable UUID profileId) {
        PatientProfile profile = profileService.getProfileById(profileId);
        PatientProfileResponse response = mapToResponse(profile);
        return ResponseEntity.ok(response);
    }

    private PatientProfileResponse mapToResponse(PatientProfile profile) {
        List<String> favoriteArtists = profile.getFavoriteArtists() != null
                ? profile.getFavoriteArtists()
                : List.of();

        String symptoms = profile.getSymptoms() != null
                ? String.join(", ", profile.getSymptoms())
                : "";

        return new PatientProfileResponse(
                profile.getId(),
                profile.getPatientName(),
                profile.getEra(),
                favoriteArtists,
                symptoms,
                profile.getDementiaStage()
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<PatientProfileResponse> updateProfile(
            @PathVariable UUID id,
            @Valid @RequestBody CreatePatientProfileRequest request) {
        PatientProfile updatedProfile = profileService.updateProfile(id, request);
        PatientProfileResponse response = mapToResponse(updatedProfile);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Profile Service is running");
    }
}
