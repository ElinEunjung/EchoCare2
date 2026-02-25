package no.kristiania.echocare.profile.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import no.kristiania.echocare.profile.api.dto.requests.CreatePatientProfileRequest;
import no.kristiania.echocare.profile.api.dto.response.PatientProfileResponse;
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
     * Get all patient profiles
     */
    @GetMapping
    public ResponseEntity<List<PatientProfileResponse>> getAllProfiles() {
        List<PatientProfileResponse> profiles = profileService.getAllProfiles();
        return ResponseEntity.ok(profiles);
    }

    /**
     * User story 1: Register patient profile
     * "As a caregiver, I register my mother, set her era, favorite artists, and symptoms"
     */
    @PostMapping
    public ResponseEntity<PatientProfileResponse> createProfile(
            @Valid @RequestBody CreatePatientProfileRequest request
    ) {
        PatientProfileResponse response = profileService.createProfile(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get profile by ID - Used by Playlist Service (synchronous call)
     */
    @GetMapping("/{profileId}")
    public ResponseEntity<PatientProfileResponse> getProfile(@PathVariable UUID profileId) {
        PatientProfileResponse response = profileService.getProfileById(profileId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PatientProfileResponse> updateProfile(
            @PathVariable UUID id,
            @Valid @RequestBody CreatePatientProfileRequest request) {
        PatientProfileResponse response = profileService.updateProfile(id, request);
        return ResponseEntity.ok(response);
    }
}
