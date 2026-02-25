package no.kristiania.echocare.feedback.service;

import lombok.RequiredArgsConstructor;
import no.kristiania.echocare.feedback.api.dto.event.FeedbackEventDTO;
import no.kristiania.echocare.feedback.api.dto.request.SubmitFeedbackRequest;
import no.kristiania.echocare.feedback.api.dto.response.FeedbackResponse;
import no.kristiania.echocare.feedback.domain.entity.Feedback;
import no.kristiania.echocare.feedback.repository.FeedbackRepository;
import no.kristiania.echocare.feedback.integration.FeedbackEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

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
     */
    @Transactional
    public FeedbackEventDTO submitFeedback(SubmitFeedbackRequest request) {
        // Validate required field
        if (request.liked() == null) {
            throw new IllegalArgumentException("Liked field cannot be null");
        }

        // Save feedback to database
        Feedback feedback = new Feedback();
        feedback.setPatientProfileId(request.patientProfileId());
        feedback.setPlaylistId(request.playlistId());
        feedback.setLiked(request.liked());

        Feedback savedFeedback = feedbackRepository.save(feedback);

        // Publish event to RabbitMQ
        eventPublisher.publishFeedbackSubmitted(savedFeedback);

        // Return event DTO
        return new FeedbackEventDTO(savedFeedback);
    }

    public List<FeedbackResponse> getFeedbackForProfile(UUID profileId) {
        List<Feedback> feedbackList = feedbackRepository.findByPatientProfileId(profileId);
        return feedbackList.stream()
                .map(FeedbackResponse::new)
                .toList();
    }
}

