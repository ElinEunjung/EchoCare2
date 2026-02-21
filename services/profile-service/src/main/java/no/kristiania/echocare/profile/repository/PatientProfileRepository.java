package no.kristiania.echocare.profile.repository;

import no.kristiania.echocare.profile.domain.entity.PatientProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PatientProfileRepository extends JpaRepository<PatientProfile, UUID> {
    List<PatientProfile> findByCaregiverId(UUID caregiverId);
}
