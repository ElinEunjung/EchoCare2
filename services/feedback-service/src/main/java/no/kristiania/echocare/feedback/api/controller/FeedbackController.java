package no.kristiania.echocare.feedback.api.controller;

import lombok.RequiredArgsConstructor;
import no.kristiania.echocare.feedback.api.dto.event.FeedbackEventDTO;
import no.kristiania.echocare.feedback.api.dto.request.CreateFeedbackRequest;
import no.kristiania.echocare.feedback.service.FeedbackService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for feedback operations
 * Handles feedback submission from caregivers
 */
@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping
    public ResponseEntity<FeedbackEventDTO> submitFeedback(@RequestBody CreateFeedbackRequest request) {
        FeedbackEventDTO response = feedbackService.submitFeedback(request);
        return ResponseEntity.ok(response);
    }
}

