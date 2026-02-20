package no.kristiania.echocare.feedback.api.dto.request;

import lombok.Data;
import java.util.UUID;

/**
 * Request DTO for creating feedback (comes from client/frontend)
 */
@Data
public class CreateFeedbackRequest {
    private UUID playlistId;
    private UUID songId;
    private UUID patientProfileId;
    private Boolean liked;
    private Integer rating;
    private String situation;
}
