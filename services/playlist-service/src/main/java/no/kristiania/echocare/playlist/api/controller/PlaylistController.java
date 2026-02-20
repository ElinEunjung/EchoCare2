package no.kristiania.echocare.playlist.api.controller;

import lombok.RequiredArgsConstructor;
import no.kristiania.echocare.playlist.service.PlaylistGeneratorService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/playlists")
@RequiredArgsConstructor
public class PlaylistController {

    private final PlaylistGeneratorService service;


}
