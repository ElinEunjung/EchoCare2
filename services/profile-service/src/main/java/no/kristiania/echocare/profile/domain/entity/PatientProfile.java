package no.kristiania.echocare.profile.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.kristiania.echocare.profile.domain.value.DementiaStage;
import no.kristiania.echocare.profile.domain.value.MusicPreference;
import no.kristiania.echocare.profile.domain.value.CareNeed;

import java.util.*;

@Entity
@Data
@NoArgsConstructor(force = true)
@Table(name = "patient_profiles")
public class PatientProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", name = "id", nullable = false, updatable = false)
    private UUID id;


    @Column(name = "patient_name", nullable = false)
    private String patientName;

    @Embedded
    private MusicPreference MusicPreference;

    @Column(name = "symptoms", nullable = false)
    private String symptoms;

    @Enumerated(EnumType.STRING)
    @Column(name = "stage", nullable = false)
    private DementiaStage stage;
}

//TODO: Include disliked songs, caregiver ID in the Entity later
//@ElementCollection
//@CollectionTable(
//        name = "patient_disliked_songs",
//        joinColumns = @JoinColumn(name = "patient_id")
//)
//@Column(name = "disliked_song")
//private List<String> dislikedSongs = new ArrayList<>();
//@Column(columnDefinition = "uuid", name = "caregiver_id", nullable = false)
//private UUID caregiverID;
