package no.kristiania.echocare.playlist.domain;

import lombok.RequiredArgsConstructor;
import no.kristiania.echocare.playlist.api.dto.TrackDto;
import no.kristiania.echocare.playlist.api.request.GeneratePlaylistRequest;
import no.kristiania.echocare.playlist.api.response.PlaylistResponse;
import no.kristiania.echocare.playlist.integration.ProfileClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PlaylistGeneratorService {

    private final ProfileClient profileClient;


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
