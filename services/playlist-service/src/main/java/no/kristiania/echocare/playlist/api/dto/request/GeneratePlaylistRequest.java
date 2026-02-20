package no.kristiania.echocare.playlist.api.dto.request;




import java.util.UUID;


public record GeneratePlaylistRequest(
        UUID patientId

) {
}
