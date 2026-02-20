package no.kristiania.echocare.feedback.service;

import lombok.RequiredArgsConstructor;
import no.kristiania.echocare.feedback.api.dto.event.FeedbackEventDTO;
import no.kristiania.echocare.feedback.api.dto.request.CreateFeedbackRequest;
import no.kristiania.echocare.feedback.domain.entity.Feedback;
import no.kristiania.echocare.feedback.domain.repository.FeedbackRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for feedback operations
 * Handles saving feedback and publishing events
 */
@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final FeedbackEventPublisher eventPublisher;

    /**
     * Submit feedback: save to database and publish event
     *
     * @param request the feedback request from the caregiver
     * @return the feedback event DTO for the response
     */
    @Transactional
    public FeedbackEventDTO submitFeedback(CreateFeedbackRequest request) {
        // Save feedback to database
        Feedback feedback = new Feedback(request);
        Feedback savedFeedback = feedbackRepository.save(feedback);

        // Publish event to RabbitMQ
        eventPublisher.publishFeedbackSubmitted(savedFeedback);

        // Return event DTO
        return new FeedbackEventDTO(savedFeedback);
    }
}

