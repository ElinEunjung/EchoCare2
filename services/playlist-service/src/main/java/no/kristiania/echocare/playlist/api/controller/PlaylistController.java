package no.kristiania.echocare.playlist.api.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.kristiania.echocare.playlist.api.dto.SongDTO;
import no.kristiania.echocare.playlist.api.dto.request.GeneratePlaylistRequest;
import no.kristiania.echocare.playlist.api.dto.response.PlaylistResponse;
import no.kristiania.echocare.playlist.service.PlaylistGeneratorService;
import no.kristiania.echocare.playlist.service.ProfileCacheService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/playlists")
@RequiredArgsConstructor
public class PlaylistController {

    private final PlaylistGeneratorService service;
    private final ProfileCacheService profileCacheService;

    /**
     * User Story 2: Generate situation-aware playlist
     * For synchronous call to Profile Service
     */
    @PostMapping("/generate")
    public ResponseEntity<PlaylistResponse> generatePlaylist(
            @RequestBody GeneratePlaylistRequest request
    ) {
        PlaylistResponse response = service.generatePlaylist(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get playlist by profileID - Used by Feedback Service (synchronous call)
     */
    @GetMapping("/profile/{profileId}")
    public ResponseEntity<List> getPlaylistById(
            @PathVariable UUID profileId
    ){

        List playlists = service.getPlaylistsByProfile(profileId);
        return ResponseEntity.ok(playlists);
    }

    /**
     * Get song details by ID
     */
    @GetMapping("/song/{songId}")
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

    /**
     * Clear profile cache - forces synchronous REST calls to Profile Service
     * Useful for testing sync communication
     */
    @DeleteMapping("/cache/clear")
    public ResponseEntity<Map<String, Object>> clearCache() {
        int sizeBeforeClear = profileCacheService.getCacheSize();
        profileCacheService.clearCache();

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Profile cache cleared");
        response.put("entriesRemoved", sizeBeforeClear);

        log.info("Cache cleared via admin endpoint - removed {} entries", sizeBeforeClear);
        return ResponseEntity.ok(response);
    }

    /**
     * Get cache status
     */
    @GetMapping("/cache/status")
    public ResponseEntity<Map<String, Object>> getCacheStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("cacheSize", profileCacheService.getCacheSize());
        status.put("message", "Profile cache status");

        return ResponseEntity.ok(status);
    }

}
