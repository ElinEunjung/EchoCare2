package no.kristiania.echocare.profile.repository;

import no.kristiania.echocare.profile.domain.entity.PatientProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PatientProfileRepo extends JpaRepository<PatientProfile, UUID> {
}
