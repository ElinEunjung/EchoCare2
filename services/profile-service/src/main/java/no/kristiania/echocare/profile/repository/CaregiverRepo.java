package no.kristiania.echocare.profile.repository;

import no.kristiania.echocare.profile.domain.entity.Caregiver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CaregiverRepo extends JpaRepository<Caregiver, UUID> {
    @Query("SELECT c FROM Caregiver c WHERE c.username = :username")
    public List<Caregiver> findByUsername(String username);
}
