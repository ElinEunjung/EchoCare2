package no.kristiania.echocare.playlist.api.dto.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event DTO for receiving Feedback-related events from RabbitMQ
 * Consumed from feedback.submitted events
 * Contains only essential identifiers - fetch full details via Feedback API if needed
 */
@Data
@AllArgsConstructor
@NoArgsConstructor

public class FeedbackEventDTO {

    private UUID feedbackId;

    private UUID patientProfileId;

    private String eventType; // "SUBMITTED", "UPDATED", etc.

    private LocalDateTime timestamp;

}

