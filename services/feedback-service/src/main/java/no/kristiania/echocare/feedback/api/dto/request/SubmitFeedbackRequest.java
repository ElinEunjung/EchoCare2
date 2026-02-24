package no.kristiania.echocare.feedback.api.dto.request;

import java.util.UUID;

// Used for:
// Frontend/API → Feedback Service (incoming request)

public record SubmitFeedbackRequest(
        UUID playlistId,
        UUID songId,
        UUID patientProfileId,
        Boolean liked,
        String dementiaStage,
        String careNeed
) {}
