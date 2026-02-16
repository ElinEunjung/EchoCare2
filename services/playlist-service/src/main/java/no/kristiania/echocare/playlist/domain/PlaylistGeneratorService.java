package no.kristiania.echocare.playlist.domain;

import lombok.RequiredArgsConstructor;
import no.kristiania.echocare.playlist.api.dto.TrackDto;
import no.kristiania.echocare.playlist.api.request.GeneratePlaylistRequest;
import no.kristiania.echocare.playlist.api.response.PlaylistResponse;
import no.kristiania.echocare.playlist.domain.value.CareNeed;
import no.kristiania.echocare.playlist.domain.value.DementiaStage;
import no.kristiania.echocare.playlist.integration.ProfileClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PlaylistGeneratorService {

    private final ProfileClient profileClient;

    public PlaylistResponse generate(GeneratePlaylistRequest req) {
        Map<String, Object> p = profileClient.getProfile(req.patientId());
        String era = (String) p.get("era"); // from PatientProfileResponse
        String favoriteArtists = (String) p.get("favoriteArtists");
        String favoriteGenres  = (String) p.get("favoriteGenres");

        var stage = req.stageOverride() != null
                ? req.stageOverride()
                : DementiaStage.valueOf((String) p.get("stage"));

        var care = req.careNeed();

        // Very small rule-of-thumb selector:
        List<TrackDto> tracks = new ArrayList<TrackDto>();

        // Bias: start familiar & slow if anxiety/depression, more upbeat if activity
        if (care == CareNeed.EASE_ANXIETY || care == CareNeed.EASE_DEPRESSION) {
            // "slow start"
            tracks.add(new TrackDto("Familiar Calm 1", pickArtist(favoriteArtists), 68, era));
            tracks.add(new TrackDto("Familiar Calm 2", pickArtist(favoriteArtists), 72, era));
            tracks.add(new TrackDto("Gentle Uplift",   pickGenreArtist(favoriteGenres), 84, era));
        } else if (care == CareNeed.CALMING_AGITATION || care == CareNeed.STRESS_RELIEF) {
            tracks.add(new TrackDto("Soothing Memory", pickArtist(favoriteArtists), 70, era));
            tracks.add(new TrackDto("Nostalgic Soft",  pickGenreArtist(favoriteGenres), 76, era));
            tracks.add(new TrackDto("Safe Reprise",    pickArtist(favoriteArtists), 78, era));
        } else if (care == CareNeed.ACTIVITY_SUPPORT) {
            tracks.add(new TrackDto("Energetic Classic", pickArtist(favoriteArtists), 100, era));
            tracks.add(new TrackDto("Walk Tempo",        pickGenreArtist(favoriteGenres), 110, era));
            tracks.add(new TrackDto("Light Groove",      pickArtist(favoriteArtists), 118, era));
        }

        // Stage adaptation (simple)
        tracks = adaptForStage(tracks, stage);

        String strategy = care.name() + "_" + stage.name() + (isSlowStart(care) ? "_SLOW_START" : "");
        return new PlaylistResponse(strategy, tracks);
    }

    private boolean isSlowStart(CareNeed care) {
        return care == CareNeed.EASE_ANXIETY || care == CareNeed.EASE_DEPRESSION;
    }

    private List<TrackDto> adaptForStage(List<TrackDto> base, DementiaStage stage) {
        // MVP: coarse adjustment
        return base.stream().map(t -> switch (stage) {
            case MILD -> t; // unchanged
            case MODERATE -> new TrackDto(t.title(), t.artist(), Math.max(60, Math.min(t.bpm(), 100)), t.eraTag());
            case SEVERE -> new TrackDto(shorten(t.title()), t.artist(), Math.max(60, Math.min(t.bpm(), 90)), t.eraTag());
        }).toList();
    }

    private String pickArtist(String artistsCsv) {
        String[] parts = artistsCsv.split(",");
        return parts.length > 0 ? parts[0].trim() : "Favorite Artist";
    }
    private String pickGenreArtist(String genresCsv) {
        String[] parts = genresCsv.split(",");
        return (parts.length > 0 ? parts[0].trim() : "Genre") + " Artist";
    }
    private String shorten(String s) {
        return s.length() <= 18 ? s : s.substring(0, 18);
    }
}
