package no.kristiania.echocare.playlist.api.request;




import no.kristiania.echocare.playlist.domain.value.CareNeed;
import no.kristiania.echocare.playlist.domain.value.DementiaStage;

import java.util.UUID;


public record GeneratePlaylistRequest(
        UUID patientId,
        CareNeed careNeed,
        DementiaStage stageOverride // optional, can be null
) {
}
