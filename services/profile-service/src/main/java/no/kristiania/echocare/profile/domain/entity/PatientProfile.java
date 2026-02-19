package no.kristiania.echocare.profile.domain.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.*;

@Entity
@Table(name = "patient_profiles")
@Data
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

    @OneToMany(mappedBy = "patientProfile", cascade = CascadeType.ALL)
    private List<MusicPreference> musicPreferences;

    @ElementCollection
    @CollectionTable(name = "symptoms", joinColumns = @JoinColumn(name = "patient_profile_id"))
    @Column(name = "symptom", nullable = false)
    private List<String> symptoms;

}
