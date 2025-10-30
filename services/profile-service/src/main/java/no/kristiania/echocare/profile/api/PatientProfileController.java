package no.kristiania.echocare.profile.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import no.kristiania.echocare.profile.api.dto.requests.CreatePatientProfileRequest;
import no.kristiania.echocare.profile.api.dto.requests.PatientProfileResponse;
import no.kristiania.echocare.profile.domain.PatientProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID; // create unique combination of IDs or keys

@RestController
@RequestMapping("/profiles")
@RequiredArgsConstructor
public class PatientProfileController {

    private final PatientProfileService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PatientProfileResponse create(@Valid @RequestBody CreatePatientProfileRequest req) {
        return service.create(req);
    }

    @GetMapping("/{id}")
    public PatientProfileResponse get(@PathVariable UUID id) {
        return service.get(id);
    }

    @GetMapping
    public List<PatientProfileResponse> getAll() {
        return service.getAll();
    }
}

