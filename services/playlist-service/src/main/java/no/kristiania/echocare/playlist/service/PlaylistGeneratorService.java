package no.kristiania.echocare.playlist.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.kristiania.echocare.playlist.api.dto.SongDTO;
import no.kristiania.echocare.playlist.api.dto.request.GeneratePlaylistRequest;
import no.kristiania.echocare.playlist.api.dto.response.PlaylistResponse;
import no.kristiania.echocare.playlist.domain.entity.CareNeed;
import no.kristiania.echocare.playlist.integration.ProfileServiceClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaylistGeneratorService {

    private final ProfileServiceClient profileServiceClient;

    /**
     * Generate playlist based on patient profile and care need
     * User Story 2: Caregiver selects situation and dementia stage to get suitable playlist
     */
    public PlaylistResponse generatePlaylist(GeneratePlaylistRequest request) {
        Map<String, Object> profileData = profileServiceClient.getProfile(request.patientId());

        String era = (String) profileData.get("era");
        String stage = (String) profileData.get("stage");

        @SuppressWarnings("unchecked")
        List<String> artists = (List<String>) profileData.getOrDefault("favoriteArtists", List.of());

        @SuppressWarnings("unchecked")
        List<String> genres = (List<String>) profileData.getOrDefault("favoriteGenres", List.of());

        // Build tracks based on care need and dementia stage
        List<SongDTO> tracks = buildTracksForCareNeed(request.careNeed(), stage, era, artists, genres);

        log.info("Generated {} tracks for patient {} with care need {} and stage {}",
                tracks.size(), request.patientId(), request.careNeed(), stage);

        return new PlaylistResponse(request.patientId(), tracks);
    }

    /**
     * Build tracks based on care need with simple rules
     * For EASE_ANXIETY and moderate stage: start with familiar slow songs
     */
    private List<SongDTO> buildTracksForCareNeed(CareNeed careNeed, String stage,
                                                   String era, List<String> artists, List<String> genres) {
        List<SongDTO> tracks = new ArrayList<>();

        // Determine BPM and energy based on care need
        int baseBpm = getBaseBpmForCareNeed(careNeed);
        int baseEnergy = getBaseEnergyForCareNeed(careNeed);

        // For moderate/severe stages, start with slower, more familiar songs
        boolean isModerateOrSevere = "MODERATE".equals(stage) || "SEVERE".equals(stage);

        if (isModerateOrSevere) {
            // Start with familiar slow songs from favorite artists
            artists.stream().limit(2).forEach(artist ->
                tracks.add(createSong("Familiar Song by " + artist, artist,
                    genres.isEmpty() ? "Easy Listening" : genres.get(0),
                    baseBpm - 20, era, baseEnergy - 1)));
        }

        // Add songs from favorite artists
        artists.stream().limit(3).forEach(artist ->
            tracks.add(createSong("Classic by " + artist, artist,
                genres.isEmpty() ? "Pop" : genres.get(0),
                baseBpm, era, baseEnergy)));

        // Add genre-based songs
        genres.stream().limit(2).forEach(genre ->
            tracks.add(createSong(genre + " Classic from " + era,
                "Popular " + genre + " Artist", genre, baseBpm, era, baseEnergy)));

        // Add nostalgic song from the era
        tracks.add(createSong("Nostalgic " + era + " Hit", "Classic Artist",
            "Classic", baseBpm - 10, era, baseEnergy - 1));

        return tracks;
    }

    /**
     * Get base BPM based on care need
     */
    private int getBaseBpmForCareNeed(CareNeed careNeed) {
        return switch (careNeed) {
            case EASE_ANXIETY, CALMING_AGITATION -> 80;  // Slow, calming
            case STRESS_RELIEF -> 90;                     // Gentle
            case EASE_DEPRESSION -> 100;                  // Moderate, uplifting
            case ACTIVITY_SUPPORT -> 120;                 // Energetic
        };
    }

    /**
     * Get base energy level based on care need
     */
    private int getBaseEnergyForCareNeed(CareNeed careNeed) {
        return switch (careNeed) {
            case EASE_ANXIETY, CALMING_AGITATION -> 2;   // Low energy
            case STRESS_RELIEF -> 3;                      // Low-moderate
            case EASE_DEPRESSION -> 4;                    // Moderate-high
            case ACTIVITY_SUPPORT -> 5;                   // High energy
        };
    }

    private SongDTO createSong(String title, String artist, String genre, int bpm, String era, int energy) {
        return new SongDTO(UUID.randomUUID(), title, artist, genre, bpm, era, energy);
    }

    public PlaylistResponse getPlaylistById(UUID playlistId) {
        // TODO: Implement playlist retrieval by ID
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public SongDTO getSongById(UUID songId) {
        // TODO: Implement song retrieval by ID
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
