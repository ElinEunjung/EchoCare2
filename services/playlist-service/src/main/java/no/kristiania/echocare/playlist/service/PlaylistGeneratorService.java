package no.kristiania.echocare.playlist.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.kristiania.echocare.playlist.api.dto.TrackDto;
import no.kristiania.echocare.playlist.api.dto.request.GeneratePlaylistRequest;
import no.kristiania.echocare.playlist.api.dto.response.PlaylistResponse;
import no.kristiania.echocare.playlist.integration.ProfileClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaylistGeneratorService {

    private final ProfileClient profileClient;

    /**
     * Generate a personalized playlist based on patient profile
     * Makes synchronous REST call to Profile Service
     */
    public PlaylistResponse generatePlaylist(GeneratePlaylistRequest request) {
        log.info("🎵 Generating playlist for patient: {}", request.patientId());

        // Synchronous call to Profile Service to fetch patient data
        log.info("📞 Calling Profile Service at /api/profiles/{}", request.patientId());
        Map<String, Object> profileData = profileClient.getProfile(request.patientId());

        log.info("✅ Received profile data: {}", profileData);

        // Extract profile information
        String patientName = (String) profileData.get("patientName");
        String era = (String) profileData.get("era");
        String dementiaStage = (String) profileData.get("dementiaStage");

        @SuppressWarnings("unchecked")
        List<String> favoriteArtists = (List<String>) profileData.getOrDefault("favoriteArtists", List.of());

        @SuppressWarnings("unchecked")
        List<String> favoriteGenres = (List<String>) profileData.getOrDefault("favoriteGenres", List.of());

        log.info("Patient: {}, Era: {}, Stage: {}", patientName, era, dementiaStage);
        log.info("Favorite Artists: {}", favoriteArtists);
        log.info("Favorite Genres: {}", favoriteGenres);

        // Generate tracks based on profile
        List<TrackDto> tracks = buildTracksFromProfile(
                era, dementiaStage, favoriteArtists, favoriteGenres
        );

        String strategy = "PERSONALIZED_" + dementiaStage;

        log.info("🎉 Generated {} tracks using strategy: {}", tracks.size(), strategy);

        return new PlaylistResponse(strategy, tracks);
    }

    /**
     * Build track list based on patient profile
     */
    private List<TrackDto> buildTracksFromProfile(
            String era, String dementiaStage,
            List<String> favoriteArtists, List<String> favoriteGenres) {

        List<TrackDto> tracks = new ArrayList<>();

        // Add tracks from favorite artists
        for (int i = 0; i < Math.min(3, favoriteArtists.size()); i++) {
            String artist = favoriteArtists.get(i);
            tracks.add(new TrackDto(
                    "Song " + (i + 1) + " by " + shorten(artist),
                    artist,
                    120,  // BPM
                    era,
                    5     // energy level
            ));
        }

        // Add tracks from favorite genres
        for (int i = 0; i < Math.min(2, favoriteGenres.size()); i++) {
            String genre = favoriteGenres.get(i);
            tracks.add(new TrackDto(
                    shorten(genre) + " Classic",
                    pickGenreArtist(genre),
                    110,  // Slightly slower BPM
                    era,
                    4     // medium energy
            ));
        }

        // Add era-specific tracks
        tracks.add(new TrackDto(
                "Nostalgic " + era + " Hit",
                "Classic Artist from " + era,
                100,
                era,
                3
        ));

        // Adjust for dementia stage
        if ("MODERATE".equals(dementiaStage) || "SEVERE".equals(dementiaStage)) {
            // Add more familiar, calming tracks
            tracks.add(new TrackDto(
                    "Familiar Melody",
                    "Comfort Artist",
                    80,   // Slower, calming
                    era,
                    2     // Lower energy
            ));
        }

        return tracks;
    }

    private String pickGenreArtist(String genre) {
        return genre + " Artist";
    }

    private String shorten(String s) {
        return s.length() <= 18 ? s : s.substring(0, 18);
    }
}
