package no.kristiania.echocare.profile.service;

import lombok.RequiredArgsConstructor;
import no.kristiania.echocare.profile.api.dto.response.CaregiverResponse;
import no.kristiania.echocare.profile.domain.entity.Caregiver;
import no.kristiania.echocare.profile.repository.CaregiverRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CaregiverService {

    private final CaregiverRepository caregiverRepository;

    /**
     * Get all caregivers
     */
    public List<CaregiverResponse> getAllCaregivers() {
        return caregiverRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Get a caregiver by ID
     */
    public CaregiverResponse getCaregiverById(UUID id) {
        Caregiver caregiver = caregiverRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Caregiver not found with id: " + id));
        return mapToResponse(caregiver);
    }

    private CaregiverResponse mapToResponse(Caregiver caregiver) {
        return new CaregiverResponse(
                caregiver.getId(),
                caregiver.getUsername(),
                caregiver.getEmail()
        );
    }
}

