package no.kristiania.echocare.profile.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.kristiania.echocare.profile.api.dto.requests.CreatePatientProfileRequest;

import java.util.*;

@Entity
@Table(name = "patient_profiles")
@Data
@NoArgsConstructor
public class PatientProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String patientName;

    @Column(nullable = false)
    private String era;  // Format: "1965-1975". Validation can be added in service layer to ensure correct format.

    @Column(name = "dementia_stage")
    private String dementiaStage; // e.g., "mild", "moderate", "severe"

    @ManyToOne
    @JoinColumn(name = "caregiver_id")  // Made nullable for MVP demo
    private Caregiver caregiver;

    @ElementCollection
    @CollectionTable(name = "favorite_artists", joinColumns = @JoinColumn(name = "patient_profile_id"))
    @Column(name = "artist")
    private List<String> favoriteArtists = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "symptoms", joinColumns = @JoinColumn(name = "patient_profile_id"))
    @Column(name = "symptom", nullable = false)
    private List<String> symptoms = new ArrayList<>();

    /**
     * Constructor to convert from CreatePatientProfileRequest
     * Note: Caregiver must be set separately after fetching from repository
     */
    public PatientProfile(CreatePatientProfileRequest request) {
        this.patientName = request.patientName();
        this.era = request.era();
        this.dementiaStage = request.dementiaStage();
        this.symptoms = request.symptoms();

        // Initialize favoriteArtists from request
        if (request.favoriteArtists() != null) {
            this.favoriteArtists = new ArrayList<>(request.favoriteArtists());
        }

    }
}
