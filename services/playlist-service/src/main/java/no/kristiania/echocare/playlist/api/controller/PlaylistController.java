package no.kristiania.echocare.playlist.api.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.kristiania.echocare.playlist.api.dto.request.GeneratePlaylistRequest;
import no.kristiania.echocare.playlist.api.dto.response.PlaylistResponse;
import no.kristiania.echocare.playlist.service.PlaylistGeneratorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/playlists")
@RequiredArgsConstructor
public class PlaylistController {

    private final PlaylistGeneratorService service;

    /**
     * Generate a personalized playlist for a patient
     * Makes synchronous call to Profile Service to fetch patient data
     */
    @PostMapping("/generate")
    public ResponseEntity<PlaylistResponse> generatePlaylist(@RequestBody GeneratePlaylistRequest request) {
        log.info("📨 Received playlist generation request for patient: {}", request.patientId());

        PlaylistResponse playlist = service.generatePlaylist(request);

        log.info("✅ Successfully generated playlist with {} tracks", playlist.tracks().size());

        return ResponseEntity.ok(playlist);
    }
}
