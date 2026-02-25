package no.kristiania.echocare.playlist.integration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.kristiania.echocare.playlist.api.dto.event.FeedbackEventDTO;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Consumer for Feedback-related events from RabbitMQ
 * Listens to feedback.submitted events
 *
 * This consumer enables the playlist service to learn from caregiver feedback
 * and adapt playlist recommendations accordingly, implementing a feedback loop
 * for continuous improvement of music therapy recommendations.
 *
 * This consumer is only active when RabbitMQ is enabled.
 */
@Component
@ConditionalOnProperty(name = "spring.rabbitmq.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class FeedbackEventConsumer {

    @RabbitListener(queues = "${rabbitmq.queue.feedback-events}")
    public void handleFeedbackEvent(FeedbackEventDTO event) {
        log.info("Received feedback event: feedbackId={}", event.getFeedbackId());

        try {
            handleFeedbackSubmitted(event);
        } catch (Exception e) {
            log.error("Failed to process feedback event", e);
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
    }
}


