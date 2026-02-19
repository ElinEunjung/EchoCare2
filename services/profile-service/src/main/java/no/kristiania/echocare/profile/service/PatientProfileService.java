package no.kristiania.echocare.profile.service;


import lombok.RequiredArgsConstructor;

import no.kristiania.echocare.profile.repository.PatientProfileRepo;
import org.springframework.stereotype.Service;



@Service
@RequiredArgsConstructor
public class PatientProfileService {
    private final PatientProfileRepo patientProfileRepo;


}


