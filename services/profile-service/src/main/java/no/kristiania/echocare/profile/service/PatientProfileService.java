package no.kristiania.echocare.profile.service;
import lombok.RequiredArgsConstructor;
import no.kristiania.echocare.profile.api.dto.requests.CreatePatientProfileRequest;
import no.kristiania.echocare.profile.domain.entity.Caregiver;
import no.kristiania.echocare.profile.domain.entity.MusicPreference;
import no.kristiania.echocare.profile.domain.entity.PatientProfile;
import no.kristiania.echocare.profile.repository.CaregiverRepo;
import no.kristiania.echocare.profile.repository.PatientProfileRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
@Service
@RequiredArgsConstructor
public class PatientProfileService {
    private final PatientProfileRepo patientProfileRepo;
    private final CaregiverRepo caregiverRepo;
    private final ProfileEventPublisher eventPublisher;
    /**
     * Create a new patient profile and publish event to RabbitMQ
     */
    @Transactional
    public PatientProfile createProfile(CreatePatientProfileRequest request) {
        // Create profile from request
        PatientProfile profile = new PatientProfile(request);
        // Fetch and set caregiver
        Caregiver caregiver = caregiverRepo.findById(request.caregiverId())
                .orElseThrow(() -> new RuntimeException("Caregiver not found with id: " + request.caregiverId()));
        profile.setCaregiver(caregiver);
        // Create music preferences from favorite artists and genres
        List<MusicPreference> musicPreferences = new ArrayList<>();
        // Add artist preferences
        for (String artist : request.favoriteArtists()) {
            MusicPreference pref = new MusicPreference();
            pref.setArtist(artist);
            pref.setPatientProfile(profile);
            pref.setPreferenceLevel(5); // Default to highest preference
            musicPreferences.add(pref);
        }
        // Add genre preferences
        for (String genre : request.favoriteGenres()) {
            MusicPreference pref = new MusicPreference();
            pref.setGenre(genre);
            pref.setPatientProfile(profile);
            pref.setPreferenceLevel(5); // Default to highest preference
            musicPreferences.add(pref);
        }
        profile.setMusicPreferences(musicPreferences);
        // Save profile to database
        PatientProfile savedProfile = patientProfileRepo.save(profile);
        // Publish event to RabbitMQ (for playlist-service and other consumers)
        eventPublisher.publishProfileCreated(savedProfile);
        // Return the saved profile
        return savedProfile;
    }

    public PatientProfile getProfileById(UUID id) {
        return patientProfileRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Profile not found with id: " + id));
    }
}