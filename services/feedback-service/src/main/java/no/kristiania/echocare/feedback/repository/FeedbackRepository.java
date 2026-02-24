package no.kristiania.echocare.feedback.repository;

import no.kristiania.echocare.feedback.domain.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, UUID> {
    List<Feedback> findByPatientProfileId(UUID patientProfileId);
}

