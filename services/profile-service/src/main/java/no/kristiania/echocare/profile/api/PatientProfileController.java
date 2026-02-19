package no.kristiania.echocare.profile.api;

import jakarta.validation.Valid;
import no.kristiania.echocare.profile.api.dto.requests.CreatePatientProfileRequest;
import no.kristiania.echocare.profile.api.dto.response.PatientProfileResponse;
import no.kristiania.echocare.profile.service.PatientProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID; // create unique combination of IDs or keys

@RestController
@RequestMapping("/profiles")
public class PatientProfileController {

    private final PatientProfileService service;

    public PatientProfileController(PatientProfileService service) {
        this.service = service;
    }

}

