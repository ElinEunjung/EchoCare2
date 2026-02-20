package no.kristiania.echocare.feedback.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.kristiania.echocare.feedback.api.dto.event.FeedbackEventDTO;
import no.kristiania.echocare.feedback.domain.entity.Feedback;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Publisher service for Feedback events to RabbitMQ
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
            FeedbackEventDTO event = new FeedbackEventDTO(feedback);
            rabbitTemplate.convertAndSend(feedbackExchange, feedbackSubmittedRoutingKey, event);

            log.info("Published feedback.submitted event: feedbackId={}, patientProfileId={}",
                event.getFeedbackId(), event.getPatientProfileId());
        } catch (Exception e) {
            log.error("Failed to publish feedback event for feedbackId={}", feedback.getId(), e);
        }
    }
}



