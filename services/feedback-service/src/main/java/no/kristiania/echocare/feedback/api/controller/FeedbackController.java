package no.kristiania.echocare.feedback.api.controller;

import lombok.RequiredArgsConstructor;
import no.kristiania.echocare.feedback.api.dto.event.FeedbackEventDTO;
import no.kristiania.echocare.feedback.api.dto.request.SubmitFeedbackRequest;
import no.kristiania.echocare.feedback.api.dto.response.FeedbackResponse;
import no.kristiania.echocare.feedback.service.FeedbackService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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
    public ResponseEntity<FeedbackEventDTO> submitFeedback(
            @RequestBody SubmitFeedbackRequest request
    ) {
        FeedbackEventDTO response = feedbackService.submitFeedback(request);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/profile/{profileId}")
    //Unsure if it should be profileId or caregiverId, but we can change it later if needed
    public ResponseEntity<List<FeedbackResponse>> getFeedbackForProfile(@PathVariable UUID profileId) {
        List<FeedbackResponse> feedbackResponses = feedbackService.getFeedbackForProfile(profileId);
        return ResponseEntity.ok(feedbackResponses);
    }
}

