package no.kristiania.echocare.feedback.api.dto.request;

import java.util.UUID;

/**
 * Feedback submission request from caregiver
 *
 * Feedback captures the caregiver's response to a playlist - whether the patient
 * liked it or not. This is immutable once submitted.
 *
 * Profile context (careNeed, dementiaStage) is retrieved via ProfileServiceClient
 * when needed for analytics or recommendations.
 */
public record SubmitFeedbackRequest(
        UUID playlistId,
        UUID patientProfileId,
        Boolean liked
) {}
