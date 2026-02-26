# Code Cleanup Recommendations

## Summary

Based on your testing and observations, here's what code is **actually needed** vs. **can be removed**.

## ✅ KEEP - Actually Used

### 1. ProfileServiceClient (Playlist Service)
**Status:** ✅ **ACTIVELY USED**

**Location:** `services/playlist-service/src/main/java/no/kristiania/echocare/playlist/integration/ProfileServiceClient.java`

**Why Keep:**
- Used by `ProfileCacheService` when cache miss occurs
- Provides fallback when RabbitMQ is unavailable
- Essential for service resilience
- Proven working in your tests

**Evidence:**
```log
ProfileCacheService - Profile cache miss - fetching from service
ProfileServiceClient - Fetching profile data from Profile Service
```

---

## ⚠️ DECIDE - Placeholder Code

### 2. PlaylistServiceClient (Feedback Service)
**Status:** ⚠️ **PLACEHOLDER ONLY - NOT IMPLEMENTED**

**Location:** `services/feedback-service/src/main/java/no/kristiania/echocare/feedback/integration/PlaylistServiceClient.java`

**Current Code:**
```java
@Component
public class PlaylistServiceClient {
    // Placeholder for PlaylistServiceClient implementation
    // This client can be used to call the Playlist Service if needed in the future
}
```

**Question to Answer:** Does Feedback Service need to synchronously call Playlist Service?

#### Option A: Remove It (Recommended if not needed)

**When to Remove:**
- Frontend provides valid `playlistId` in feedback submission
- No validation needed (trust frontend data)
- Simpler architecture

**How to Remove:**
```bash
# Delete the file
rm services/feedback-service/src/main/java/no/kristiania/echocare/feedback/integration/PlaylistServiceClient.java
```

#### Option B: Implement It (If validation needed)

**When to Implement:**
- Need to verify playlist exists before saving feedback
- Need to fetch song details for enriching feedback data
- Want to validate patientProfileId matches playlist

**Implementation:**
```java
@Component
@RequiredArgsConstructor
public class PlaylistServiceClient {
    private final RestTemplate restTemplate;
    
    @Value("${services.playlist.url:http://localhost:8082}")
    private String playlistServiceUrl;
    
    public PlaylistDTO getPlaylist(UUID playlistId) {
        String url = playlistServiceUrl + "/api/playlists/" + playlistId;
        return restTemplate.getForObject(url, PlaylistDTO.class);
    }
    
    public SongDTO getSong(UUID songId) {
        String url = playlistServiceUrl + "/api/playlists/songs/" + songId;
        return restTemplate.getForObject(url, SongDTO.class);
    }
}
```

**Also need:**
1. Add `RestTemplate` bean in config
2. Add `services.playlist.url` to application.yml
3. Use in `FeedbackService.submitFeedback()` for validation

---

### 3. Feedback "UPDATED" Event Type
**Status:** ⚠️ **DEFINED BUT NOT IMPLEMENTED**

**Locations:**
- `services/feedback-service/src/main/java/no/kristiania/echocare/feedback/api/dto/event/FeedbackEventDTO.java` (has eventType field)
- `services/playlist-service/src/main/java/no/kristiania/echocare/playlist/integration/FeedbackEventConsumer.java` (has UPDATED handling)

**Current Situation:**
- DTO supports "SUBMITTED" and "UPDATED" event types
- Consumer checks for "UPDATED" but logs warning (not implemented)
- No endpoint to UPDATE feedback
- No `publishFeedbackUpdated()` method

**Question to Answer:** Can caregivers update feedback after submission?

#### Option A: Remove "UPDATED" Support (Recommended if immutable)

**When to Remove:**
- Feedback is immutable (caregivers can't change their rating)
- Simpler business logic
- Audit trail is clearer

**Changes:**
1. Simplify `FeedbackEventDTO`:
```java
public FeedbackEventDTO(Feedback feedback) {
    // ... other fields
    this.eventType = "SUBMITTED";  // Always SUBMITTED
    // ...
}
```

2. Simplify `FeedbackEventConsumer`:
```java
public void handleFeedbackEvent(FeedbackEventDTO event) {
    // Remove the if/else for eventType
    handleFeedbackSubmitted(event);  // Always handle as submitted
}
```

#### Option B: Implement "UPDATED" (If caregivers can change feedback)

**When to Implement:**
- Caregivers need to change their like/dislike after initial submission
- Support for "I changed my mind" scenarios

**Implementation Required:**

1. **Add UPDATE endpoint in FeedbackController:**
```java
@PutMapping("/{feedbackId}")
public ResponseEntity<FeedbackEventDTO> updateFeedback(
    @PathVariable UUID feedbackId,
    @RequestBody UpdateFeedbackRequest request
) {
    FeedbackEventDTO response = feedbackService.updateFeedback(feedbackId, request);
    return ResponseEntity.ok(response);
}
```

2. **Add service method:**
```java
public FeedbackEventDTO updateFeedback(UUID feedbackId, UpdateFeedbackRequest request) {
    Feedback feedback = feedbackRepository.findById(feedbackId)
        .orElseThrow(() -> new RuntimeException("Feedback not found"));
    
    feedback.setLiked(request.liked());
    // Update other fields...
    
    Feedback updated = feedbackRepository.save(feedback);
    eventPublisher.publishFeedbackUpdated(updated);
    
    return new FeedbackEventDTO(updated);
}
```

3. **Add publisher method:**
```java
public void publishFeedbackUpdated(Feedback feedback) {
    FeedbackEventDTO event = new FeedbackEventDTO(feedback);
    event.setEventType("UPDATED");
    rabbitTemplate.convertAndSend(feedbackExchange, feedbackUpdatedRoutingKey, event);
}
```

4. **Add routing key to application.yml:**
```yaml
rabbitmq:
  routing-key:
    feedback-submitted: feedback.submitted
    feedback-updated: feedback.updated  # Add this
```

5. **Update consumer:**
```java
} else if ("UPDATED".equals(event.getEventType())) {
    handleFeedbackUpdated(event);
}

private void handleFeedbackUpdated(FeedbackEventDTO event) {
    // Implement logic to handle updated feedback
    log.info("Feedback updated - adjust recommendations");
}
```

---

## 🗑️ REMOVE - Definitely Not Needed

### None Currently Identified

All current code serves a purpose or is a reasonable placeholder for future features.

---

## Recommendations Priority

### HIGH Priority - Decide Now

1. ✅ **Keep ProfileServiceClient** - It's working and needed
2. ⚠️ **Decide on PlaylistServiceClient**:
   - Remove if not validating playlist/song existence
   - Implement if you want validation

### MEDIUM Priority - Can Decide Later

3. ⚠️ **Decide on Feedback UPDATED**:
   - Simplify code if feedback is immutable
   - Implement fully if updates are needed

### LOW Priority - Nice to Have

4. Add circuit breaker to ProfileServiceClient (Resilience4j)
5. Add caching headers to REST responses
6. Add metrics/monitoring for sync vs async calls

---

## Decision Matrix

| Feature | Keep As-Is | Implement Fully | Remove |
|---------|-----------|-----------------|--------|
| ProfileServiceClient | ✅ YES | Already done | ❌ NO |
| PlaylistServiceClient | ⚠️ Maybe | If validation needed | ✅ If not needed |
| Feedback UPDATED | ⚠️ Maybe | If updates allowed | ✅ If immutable |

---

## My Recommendation

Based on typical dementia care applications:

### Remove PlaylistServiceClient
**Reason:** Frontend already knows the playlistId when submitting feedback (user just listened to that playlist). No need to validate it exists - trust the frontend data.

### Simplify Feedback Events (Remove UPDATED)
**Reason:** Feedback represents a moment in time - how the patient reacted to music at that specific moment. Changing it later loses that context. Keep it immutable for better analytics.

**Benefits:**
- ✅ Simpler code
- ✅ Clearer audit trail
- ✅ Easier to analyze patterns
- ✅ Less maintenance

---

## How to Clean Up

### Step 1: Remove PlaylistServiceClient

```bash
# Delete the placeholder file
rm services/feedback-service/src/main/java/no/kristiania/echocare/feedback/integration/PlaylistServiceClient.java

# Verify no imports reference it
grep -r "PlaylistServiceClient" services/feedback-service/src/
```

### Step 2: Simplify Feedback Event Handling

**In FeedbackEventConsumer.java:**
```java
public void handleFeedbackEvent(FeedbackEventDTO event) {
    log.info("Received feedback event: feedbackId={}", event.getFeedbackId());
    
    try {
        handleFeedbackSubmitted(event);  // Always treat as submitted
    } catch (Exception e) {
        log.error("Failed to process feedback event", e);
    }
}
```

**In FeedbackEventDTO.java** (feedback-service):
```java
public FeedbackEventDTO(Feedback feedback) {
    // ... fields
    this.eventType = "SUBMITTED";  // Hardcode to SUBMITTED
    // ...
}
```

### Step 3: Update Documentation

Add comments explaining design decisions:
```java
/**
 * Feedback is immutable once submitted.
 * This preserves the authentic patient response at that moment in time.
 * For analytics and pattern detection, historical accuracy is crucial.
 */
```

---

## Testing After Cleanup

1. ✅ Verify Feedback Service still compiles
2. ✅ Test feedback submission via Postman
3. ✅ Verify RabbitMQ event published
4. ✅ Verify Playlist Service consumes event
5. ✅ Check logs for any errors

---

## Questions to Ask Product Owner / Requirements

1. **Can caregivers update feedback after submission?**
   - Yes → Keep UPDATED, implement fully
   - No → Remove UPDATED support

2. **Should we validate playlist exists before saving feedback?**
   - Yes → Implement PlaylistServiceClient
   - No → Remove placeholder

3. **What happens if caregiver submits feedback for non-existent playlist?**
   - Block submission → Need validation
   - Allow (data cleanup later) → No validation needed

---

## Final Checklist

- [ ] Test synchronous communication (following SYNCHRONOUS-TESTING-GUIDE.md)
- [ ] Decide: Remove or implement PlaylistServiceClient?
- [ ] Decide: Simplify or implement Feedback UPDATED?
- [ ] Clean up code based on decisions
- [ ] Update README.md with architecture decisions
- [ ] Test all endpoints still work
- [ ] Commit changes with clear messages


