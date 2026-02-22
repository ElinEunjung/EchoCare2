package no.kristiania.echocare.profile.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import no.kristiania.echocare.profile.api.dto.requests.CreatePatientProfileRequest;
import no.kristiania.echocare.profile.api.dto.response.PatientProfileResponse;
import no.kristiania.echocare.profile.domain.entity.PatientProfile;
import no.kristiania.echocare.profile.integration.ProfileEventPublisher;
import no.kristiania.echocare.profile.repository.PatientProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PatientProfileService {
    private final PatientProfileRepository patientProfileRepository;
    private final ProfileEventPublisher eventPublisher;

    /**
     * User story 1: Create patient profile and publish event to RabbitMQ
     */
    @Transactional
    public PatientProfileResponse createProfile(CreatePatientProfileRequest request) {
        PatientProfile profile = new PatientProfile(request);
        PatientProfile savedProfile = patientProfileRepository.save(profile);

        // Publish event to RabbitMQ for playlist-service
        eventPublisher.publishProfileCreated(savedProfile);

        return mapToResponse(savedProfile);
    }

    public PatientProfileResponse getProfileById(UUID id) {
        PatientProfile profile = patientProfileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Profile not found with id: " + id));
        return mapToResponse(profile);
    }

    /**
     * Get all patient profiles
     */
    public List<PatientProfileResponse> getAllProfiles() {
        return patientProfileRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public PatientProfileResponse updateProfile(UUID id, @Valid CreatePatientProfileRequest request) {
        PatientProfile profile = patientProfileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Profile not found with id: " + id));

        // Update with new data from request
        profile.setPatientName(request.patientName());
        profile.setEra(request.era());
        profile.setDementiaStage(request.dementiaStage());
        profile.setSymptoms(request.symptoms());
        profile.setFavoriteArtists(request.favoriteArtists());

        PatientProfile savedProfile = patientProfileRepository.save(profile);

        // Publish update event
        eventPublisher.publishProfileUpdated(savedProfile);

        return mapToResponse(savedProfile);
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
}
