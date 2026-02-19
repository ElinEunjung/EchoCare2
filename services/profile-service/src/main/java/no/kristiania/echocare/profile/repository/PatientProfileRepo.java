package no.kristiania.echocare.profile.repository;

import no.kristiania.echocare.profile.domain.entity.PatientProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PatientProfileRepo extends JpaRepository<PatientProfile, UUID> {

}
