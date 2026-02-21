package no.kristiania.echocare.playlist.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.kristiania.echocare.playlist.api.dto.SongDTO;
import no.kristiania.echocare.playlist.api.dto.request.GeneratePlaylistRequest;
import no.kristiania.echocare.playlist.api.dto.response.PlaylistResponse;
import no.kristiania.echocare.playlist.domain.entity.Playlist;
import no.kristiania.echocare.playlist.domain.entity.Song;
import no.kristiania.echocare.playlist.exception.NoSongsAvailableException;
import no.kristiania.echocare.playlist.integration.ProfileServiceClient;
import no.kristiania.echocare.playlist.repository.PlaylistRepository;
import no.kristiania.echocare.playlist.repository.SongRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaylistGeneratorService {
    private final SongRepository songRepository;
    private final PlaylistRepository playlistRepository;

    // TODO: Use ProfileServiceClient to fetch patient profile data for enhanced playlist generation
    private final ProfileServiceClient profileServiceClient;

    // Valid input values
    private static final Set<String> VALID_CARE_NEEDS = Set.of(
            "stress_relief",
            "activity_support",
            "calming_agitation",
            "easing_depression",
            "reducing_anxiety"
    );

    private static final Set<String> VALID_STAGES = Set.of(
            "mild",
            "moderate",
            "severe"
    );

    // Song filtering thresholds
    private static final int STRESS_RELIEF_MAX_ENERGY = 5;
    private static final int ACTIVITY_SUPPORT_MIN_ENERGY = 7;
    private static final int CALMING_AGITATION_MAX_ENERGY = 4;
    private static final int DEPRESSION_MIN_BPM = 60;
    private static final int DEPRESSION_MAX_BPM = 90;
    private static final int ANXIETY_MAX_ENERGY = 5;
    private static final int ANXIETY_MAX_BPM = 80;

    // Playlist size limits by dementia stage
    private static final int MILD_STAGE_SONG_LIMIT = 12;
    private static final int MODERATE_STAGE_SONG_LIMIT = 8;
    private static final int SEVERE_STAGE_SONG_LIMIT = 5;


    /**
     * Generate playlist based on patient profile and care need
     * User Story 2: Caregiver selects situation and dementia stage to get suitable playlist
     */
    @Transactional
    public PlaylistResponse generatePlaylist(GeneratePlaylistRequest request) {
        // Validate inputs
        String careNeed = validateCareNeed(request.careNeed());
        String stage = validateDementiaStage(request.dementiaStage());

        List<Song> allSongs = songRepository.findAll();

        if (allSongs.isEmpty()) {
            throw new NoSongsAvailableException("No songs in database");
        }

        // Filter by care need, stage, era, and favorite artists
        List<Song> selectedSongs = selectSongs(
                allSongs, careNeed, stage, request.era(), request.favoriteArtists());

        // Add randomization to avoid same playlist every time
        // Convert to mutable list before shuffling (stream().toList() returns immutable list)
        List<Song> mutableSongs = new ArrayList<>(selectedSongs);
        Collections.shuffle(mutableSongs);

        // Create and save playlist
        Playlist playlist = new Playlist();
        playlist.setPatientProfileId(request.patientId());
        playlist.setCareNeed(careNeed);
        playlist.setDementiaStage(stage);
        playlist.setEra(request.era()); // Save era for future reference
        playlist.setSongs(mutableSongs);

        playlistRepository.save(playlist);

        // Build response
        List<SongDTO> songDTOs = mutableSongs.stream()
                .map(this::mapToSongDTO)
                .toList();

        String message = String.format(
                "Generated %d songs for %s (dementia stage: %s)",
                songDTOs.size(),
                careNeed.replace("_", " "),
                stage
        );

        log.info("Playlist generated successfully: {}", playlist.getId());

        return new PlaylistResponse(
                playlist.getPatientProfileId(),  // patientId
                playlist.getId(),                 // playlistId
                playlist.getCareNeed(),          // careNeed
                playlist.getEra(),               // era (from saved playlist)
                playlist.getDementiaStage(),     // dementiaStage
                songDTOs,                        // tracks
                message                           // message
        );
    }

    private SongDTO mapToSongDTO(Song song) {
        return new SongDTO(
                song.getId(),
                song.getTitle(),
                song.getArtist(),
                song.getReleaseYear(),
                song.getBpm(),
                song.getEnergy()
        );
    }

    public List<PlaylistResponse> getPlaylistsByProfile(UUID profileId) {
        return playlistRepository.findByPatientProfileId(profileId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    private PlaylistResponse mapToResponse(Playlist playlist) {
        List<SongDTO> songDTOs = playlist.getSongs().stream()
                .map(this::mapToSongDTO)
                .toList();

        return new PlaylistResponse(
                playlist.getPatientProfileId(),  // patientId (first parameter)
                playlist.getId(),                 // playlistId (second parameter)
                playlist.getCareNeed(),          // careNeed
                playlist.getEra(),               // era
                playlist.getDementiaStage(),     // dementiaStage
                songDTOs,                        // tracks
                "Playlist retrieved successfully" // message
        );
    }


    private List<Song> selectSongs(
            List<Song> allSongs, String careNeed, String stage, String era, List<String> favoriteArtists
    ) {
        // Select songs by care need
        List<Song> selectedSongs = selectedByCareNeed(allSongs, careNeed);

        // Filter by era if provided
        if (era != null && !era.isEmpty()) {
            selectedSongs = filterByEra(selectedSongs, era);
        }

        // Prioritize favorite artists if provided
        if (favoriteArtists != null && !favoriteArtists.isEmpty()) {
            selectedSongs = prioritizeFavoriteArtists(selectedSongs, favoriteArtists);
        }

        // Adapt to dementia stage (this already limits the number of songs)
        return adaptToDementiaStage(selectedSongs, stage);
    }

    /**
     * Filter songs by era (e.g., "1960s", "1970-1980")
     */
    private List<Song> filterByEra(List<Song> songs, String era) {
        int[] yearRange = parseEra(era);
        return songs.stream()
                .filter(song -> song.getReleaseYear() != null &&
                        song.getReleaseYear() >= yearRange[0] &&
                        song.getReleaseYear() <= yearRange[1])
                .toList();
    }

    /**
     * Parse era string to year range
     * Examples: "1960s" -> [1960, 1969], "1970-1980" -> [1970, 1980]
     */
    private int[] parseEra(String era) {
        try {
            if (era.endsWith("s")) {
                // Format: "1960s"
                int decade = Integer.parseInt(era.substring(0, 4));
                return new int[]{decade, decade + 9};
            } else if (era.contains("-")) {
                // Format: "1970-1980"
                String[] parts = era.split("-");
                return new int[]{Integer.parseInt(parts[0]), Integer.parseInt(parts[1])};
            } else {
                // Single year
                int year = Integer.parseInt(era);
                return new int[]{year, year};
            }
        } catch (NumberFormatException e) {
            log.warn("Invalid era format: {}, ignoring era filter", era);
            return new int[]{0, 9999}; // Allow all years if parsing fails
        }
    }

    /**
     * Move songs from favorite artists to the front of the list
     */
    private List<Song> prioritizeFavoriteArtists(List<Song> songs, List<String> favoriteArtists) {
        List<Song> prioritized = new ArrayList<>();
        List<Song> others = new ArrayList<>();

        for (Song song : songs) {
            if (song.getArtist() != null &&
                favoriteArtists.stream().anyMatch(artist -> song.getArtist().equalsIgnoreCase(artist))) {
                prioritized.add(song);
            } else {
                others.add(song);
            }
        }

        prioritized.addAll(others);
        return prioritized;
    }

    private List<Song> adaptToDementiaStage(List<Song> selectedSongs, String stage) {
        // Adjust the number of songs based on dementia stage
        return switch (stage) {
            case "mild" -> selectedSongs.stream()
                    .limit(MILD_STAGE_SONG_LIMIT)
                    .toList();

            case "moderate" -> selectedSongs.stream()
                    .limit(MODERATE_STAGE_SONG_LIMIT)
                    .toList();

            case "severe" -> selectedSongs.stream()
                    .limit(SEVERE_STAGE_SONG_LIMIT)
                    .toList();

            default -> selectedSongs;
        };
    }

    private List<Song> selectedByCareNeed(List<Song> allSongs, String careNeed) {
        return switch (careNeed) {
            case "stress_relief" -> allSongs.stream()
                    .filter(song -> song.getEnergy() != null && song.getEnergy() <= STRESS_RELIEF_MAX_ENERGY)
                    .toList();
            case "activity_support" -> allSongs.stream()
                    .filter(song -> song.getEnergy() != null && song.getEnergy() >= ACTIVITY_SUPPORT_MIN_ENERGY)
                    .toList();
            case "calming_agitation" -> allSongs.stream()
                    .filter(song -> song.getEnergy() != null && song.getEnergy() <= CALMING_AGITATION_MAX_ENERGY)
                    .toList();
            case "easing_depression" -> allSongs.stream()
                    .filter(song -> song.getBpm() != null &&
                            song.getBpm() >= DEPRESSION_MIN_BPM &&
                            song.getBpm() <= DEPRESSION_MAX_BPM)
                    .toList();
            case "reducing_anxiety" -> allSongs.stream()
                    .filter(song -> song.getEnergy() != null &&
                            song.getBpm() != null &&
                            song.getEnergy() <= ANXIETY_MAX_ENERGY &&
                            song.getBpm() <= ANXIETY_MAX_BPM)
                    .toList();
            default -> throw new IllegalArgumentException("Invalid care need: " + careNeed);
        };
    }

    private String validateDementiaStage(String s) {
        String normalized = s.toLowerCase();
        if (!VALID_STAGES.contains(normalized)) {
            throw new IllegalArgumentException(
                    "Invalid dementia stage name. Valid: mild, moderate, severe"
            );

        }
        return normalized;
    }

    private String validateCareNeed(String s) {
        String normalized = s.toLowerCase();
        if (!VALID_CARE_NEEDS.contains(normalized)) {
            throw new IllegalArgumentException("Invalid care need name." +
                    "Valid: stress_relief, activity_support, calming_agitation," +
                    "easing_depression, reducing_anxiety"
            );

        }
        return normalized;
    }

    /**
     * Get song by ID - Used by Feedback Service
     */
    public SongDTO getSongById(UUID songId) {
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new IllegalArgumentException("Song not found with id: " + songId));
        return mapToSongDTO(song);
    }

}
