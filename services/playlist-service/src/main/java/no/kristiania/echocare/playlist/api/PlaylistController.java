package no.kristiania.echocare.playlist.api;

import lombok.RequiredArgsConstructor;
import no.kristiania.echocare.playlist.api.request.GeneratePlaylistRequest;
import no.kristiania.echocare.playlist.api.response.PlaylistResponse;
import no.kristiania.echocare.playlist.domain.PlaylistGeneratorService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/playlists")
@RequiredArgsConstructor
public class PlaylistController {

    private final PlaylistGeneratorService service;

    public PlaylistResponse generate(@RequestBody GeneratePlaylistRequest req) {
        return service.generate(req);
    }

}
