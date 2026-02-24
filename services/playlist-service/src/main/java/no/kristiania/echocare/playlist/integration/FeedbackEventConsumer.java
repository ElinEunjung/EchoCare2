package no.kristiania.echocare.playlist.integration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.kristiania.echocare.playlist.api.dto.event.FeedbackEventDTO;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumer for Feedback-related events from RabbitMQ
 * Listens to feedback.submitted events
 *
 * This consumer enables the playlist service to learn from caregiver feedback
 * and adapt playlist recommendations accordingly, implementing a feedback loop
 * for continuous improvement of music therapy recommendations.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FeedbackEventConsumer {

    @RabbitListener(queues = "${rabbitmq.queue.feedback-events}")
    public void handleFeedbackEvent(FeedbackEventDTO event) {
        log.info("Received feedback event: type={}, feedbackId={}, patientProfileId={}",
            event.getEventType(),
            event.getFeedbackId(),
            event.getPatientProfileId());

        try {
            if (event.getEventType().equals("SUBMITTED")) {
                handleFeedbackSubmitted(event);
            } else {
                log.warn("Unknown event type: {}", event.getEventType());
            }
        } catch (Exception e) {
            log.error("Failed to process feedback event: {}", event, e);
        }

        if (event.getLiked() != null) {
            if (event.getLiked()) {
                log.info("playlist {} was liked - Should suggest in future", event.getPlaylistId());
            } else {
                log.info("playlist {} was disliked - Should avoid in future", event.getPlaylistId());
            }
        }
    }

    private void handleFeedbackSubmitted(FeedbackEventDTO event) {
        log.info("Processing SUBMITTED feedback: feedbackId={}, patientProfileId={}",
            event.getFeedbackId(),
            event.getPatientProfileId());

        // TODO: Fetch full feedback details via Feedback Service API if needed for processing
        // Example: FeedbackDTO fullFeedback = feedbackServiceClient.getFeedbackById(event.getFeedbackId());

        // TODO: Process feedback and adjust future recommendations
        // Updating song preference scores based on likes/dislikes
    }
}


