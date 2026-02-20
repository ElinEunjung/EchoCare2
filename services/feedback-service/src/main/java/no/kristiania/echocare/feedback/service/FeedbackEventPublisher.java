package no.kristiania.echocare.feedback.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.kristiania.echocare.feedback.api.dto.event.FeedbackEventDTO;
import no.kristiania.echocare.feedback.domain.entity.Feedback;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Publisher service for Feedback-related events
 *
 * This service is responsible for publishing feedback events to RabbitMQ
 * when feedback is submitted by caregivers. Other services (like playlist-service)
 * can subscribe to these events to improve their recommendations based on feedback.
 */
@Slf4j
@Service
@RequiredArgsConstructor

public class FeedbackEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.feedback}")
    private String feedbackExchange;

    @Value("${rabbitmq.routing-key.feedback-submitted}")
    private String feedbackSubmittedRoutingKey;


    public void publishFeedbackSubmitted(Feedback feedback) {
        try {
            FeedbackEventDTO event = buildFeedbackEvent(feedback);

            rabbitTemplate.convertAndSend(
                feedbackExchange,
                feedbackSubmittedRoutingKey,
                event
            );

            log.info("Published feedback submitted event: feedbackId={}, playlistId={}, songId={}, patientProfileId={}, liked={}, rating={}",
                event.getFeedbackId(), event.getPlaylistId(), event.getSongId(),
                event.getPatientProfileId(), event.getLiked(), event.getRating());
        } catch (Exception e) {
            log.error("Failed to publish feedback submitted event for feedbackId={}", feedback.getId(), e);
        }
    }

    private FeedbackEventDTO buildFeedbackEvent(Feedback feedback) {
        FeedbackEventDTO event = new FeedbackEventDTO();
        event.setFeedbackId(feedback.getId());
        event.setPlaylistId(feedback.getPlaylistId());
        event.setSongId(feedback.getSongId());
        event.setPatientProfileId(feedback.getPatientProfileId());
        event.setLiked(feedback.getLiked());
        event.setRating(feedback.getRating());
        event.setSituation(feedback.getSituation());
        event.setEventType("SUBMITTED");
        event.setTimestamp(LocalDateTime.now());

        return event;
    }
}

