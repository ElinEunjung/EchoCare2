package no.kristiania.echocare.profile.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;


@Entity
@Table(name = "music_preferences")
@Data
public class MusicPreference {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_profile_id", nullable = false)
    private PatientProfile patientProfile;

    private String artist;

    private String genre;

    @Column(name = "preference_level", nullable = false)
    private Integer preferenceLevel = 0; // 1-5, where 5 is most preferred, default 0 (not specified), updated by feedback
}
