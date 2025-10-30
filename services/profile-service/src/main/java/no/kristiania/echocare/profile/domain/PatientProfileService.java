package no.kristiania.echocare.profile.domain;

import lombok.RequiredArgsConstructor;
import no.kristiania.echocare.profile.api.dto.requests.CreatePatientProfileRequest;
import no.kristiania.echocare.profile.api.dto.requests.PatientProfileResponse;
import no.kristiania.echocare.profile.domain.entity.PatientProfile;
import no.kristiania.echocare.profile.domain.value.MusicPreference;
import no.kristiania.echocare.profile.repository.PatientProfileRepo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class PatientProfileService {

    private final PatientProfileRepo patientProfileRepo;

    public PatientProfileResponse create(CreatePatientProfileRequest req) {
        var profile = new PatientProfile();
        profile.setPatientName(req.patientName());
        profile.setMusicPreference(new MusicPreference(
                req.era(),
                req.favoriteArtists(),
                req.favoriteGenres()
        ));
        profile.setSymptoms(req.symptoms());
        profile.setStage(req.stage());

        var saved = patientProfileRepo.save(profile);

        return toResponse(saved);
    }

    public PatientProfileResponse get(UUID id) {
        var p = patientProfileRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Profile not found"));
        return toResponse(p);
    }

    private PatientProfileResponse toResponse(PatientProfile p) {
        return new PatientProfileResponse(
                p.getId(),
                p.getPatientName(),
                p.getMusicPreference().getEra(),
                p.getMusicPreference().getFavoriteArtists(),
                p.getMusicPreference().getFavoriteGenres(),
                p.getSymptoms(),
                p.getStage()
        );
    }

    public List<PatientProfileResponse> getAll() {
        return patientProfileRepo.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }
}


