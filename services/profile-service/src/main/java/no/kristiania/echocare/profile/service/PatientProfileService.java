package no.kristiania.echocare.profile.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import no.kristiania.echocare.profile.api.dto.requests.CreatePatientProfileRequest;
import no.kristiania.echocare.profile.domain.entity.PatientProfile;
import no.kristiania.echocare.profile.repository.PatientProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public PatientProfile createProfile(CreatePatientProfileRequest request) {
        PatientProfile profile = new PatientProfile(request);
        PatientProfile savedProfile = patientProfileRepository.save(profile);

        // Publish event to RabbitMQ for playlist-service
        eventPublisher.publishProfileCreated(savedProfile);

        return savedProfile;
    }

    public PatientProfile getProfileById(UUID id) {
        return patientProfileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Profile not found with id: " + id));
    }

    @Transactional
    public PatientProfile updateProfile(UUID id, @Valid CreatePatientProfileRequest request) {
        PatientProfile profile = getProfileById(id);

        // Update with new data from request
        profile.setPatientName(request.patientName());
        profile.setEra(request.era());
        profile.setDementiaStage(request.dementiaStage());
        profile.setSymptoms(request.symptoms());
        profile.setFavoriteArtists(request.favoriteArtists());

        PatientProfile savedProfile = patientProfileRepository.save(profile);

        // Publish update event
        eventPublisher.publishProfileUpdated(savedProfile);

        return savedProfile;
    }
}
