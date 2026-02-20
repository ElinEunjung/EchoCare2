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

    @Column(name = "patient_name", nullable = false)
    private String patientName;

    @Column(name = "birth_year", nullable = false)
    private Integer birthYear;

    @Column(name = "era_start", nullable = false) // e.g., 1965
    private Integer eraStart;

    @Column(name = "era_end", nullable = false) // e.g., 1975
    private Integer eraEnd;

    @Enumerated(EnumType.STRING)
    @Column(name = "dementia_stage", nullable = false)
    private DementiaStage dementiaStage;

    @ManyToOne
    @JoinColumn(name = "caregiver_id", nullable = false)
    private Caregiver caregiver;

    @OneToMany(mappedBy = "patientProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MusicPreference> musicPreferences = new ArrayList<>();

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
        this.birthYear = request.birthYear();

        // Parse era string "1965-1975" into eraStart and eraEnd
        String[] eraParts = request.era().split("-");
        this.eraStart = Integer.parseInt(eraParts[0].trim());
        this.eraEnd = Integer.parseInt(eraParts[1].trim());

        this.dementiaStage = request.stage();
        this.symptoms = new ArrayList<>(request.symptoms());

        // Note: caregiver and musicPreferences need to be set in the service layer
    }

}
