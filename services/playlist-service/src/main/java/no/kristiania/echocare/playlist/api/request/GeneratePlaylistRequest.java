package no.kristiania.echocare.playlist.api.request;




import java.util.UUID;


public record GeneratePlaylistRequest(
        UUID patientId

) {
}
