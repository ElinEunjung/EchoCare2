package no.kristiania.echocare.profile.api.controller;

import lombok.RequiredArgsConstructor;
import no.kristiania.echocare.profile.api.dto.response.CaregiverResponse;
import no.kristiania.echocare.profile.service.CaregiverService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/caregivers")
@RequiredArgsConstructor
public class CaregiverController {

    private final CaregiverService caregiverService;

    /**
     * Get all caregivers
     * GET /api/caregivers
     */
    @GetMapping
    public ResponseEntity<List<CaregiverResponse>> getAllCaregivers() {
        List<CaregiverResponse> caregivers = caregiverService.getAllCaregivers();
        return ResponseEntity.ok(caregivers);
    }

    /**
     * Get a single caregiver by ID
     * GET /api/caregivers/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<CaregiverResponse> getCaregiverById(@PathVariable UUID id) {
        CaregiverResponse caregiver = caregiverService.getCaregiverById(id);
        return ResponseEntity.ok(caregiver);
    }
}

