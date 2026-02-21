package no.kristiania.echocare.playlist.api.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.kristiania.echocare.playlist.api.dto.SongDTO;
import no.kristiania.echocare.playlist.api.dto.request.GeneratePlaylistRequest;
import no.kristiania.echocare.playlist.api.dto.response.PlaylistResponse;
import no.kristiania.echocare.playlist.service.PlaylistGeneratorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/playlists")
@RequiredArgsConstructor
public class PlaylistController {

    private final PlaylistGeneratorService service;

    /**
     * User Story 2: Generate situation-aware playlist
     * For synchronous call to Profile Service
     */
    @PostMapping("/generate")
    public ResponseEntity<PlaylistResponse> generatePlaylist(@RequestBody GeneratePlaylistRequest request) {
        log.info("📨 Generate playlist for patient: {}", request.patientId());
        PlaylistResponse playlist = service.generatePlaylist(request);
        return ResponseEntity.ok(playlist);
    }

    /**
     * Get playlist by ID - Used by Feedback Service (synchronous call)
     */
    @GetMapping("/{playlistId}")
    public ResponseEntity<PlaylistResponse> getPlaylistById(@PathVariable UUID playlistId){
        log.info("Fetching playlist for playlistId: {}", playlistId);
        PlaylistResponse playlist = service.getPlaylistById(playlistId);
        return ResponseEntity.ok(playlist);
    }

    /**
     * Get song details - Used by Feedback Service
     */
    @GetMapping("/songs/{songId}")
    public ResponseEntity getSongById(@PathVariable UUID songId){
        log.info("Fetching song for songId: {}", songId);
        SongDTO song = service.getSongById(songId);
        return ResponseEntity.ok(song);
    }

    /**
     * Health check
     */
    @GetMapping("/health")
    public ResponseEntity health() {
        return ResponseEntity.ok("Playlist Service is running");
    }

}
