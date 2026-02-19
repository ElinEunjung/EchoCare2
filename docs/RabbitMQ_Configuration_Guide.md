# RabbitMQ Configuration Guide for EchoCare Microservices

## 📋 Table of Contents
1. [Overview](#overview)
2. [Architecture Diagram](#architecture-diagram)
3. [Configuration Files Explained](#configuration-files-explained)
4. [Key Concepts](#key-concepts)
5. [Message Flow Examples](#message-flow-examples)
6. [How to Use](#how-to-use)
7. [Troubleshooting](#troubleshooting)

---

## 🎯 Overview

This document explains the RabbitMQ configuration classes created for the EchoCare microservices architecture. RabbitMQ is used as a message broker to enable **event-driven communication** between services, promoting loose coupling and asynchronous processing.

### Services and Their Roles

| Service | Role | Publishes | Consumes |
|---------|------|-----------|----------|
| **Profile Service** | Publisher | Profile events (created, updated) | None |
| **Feedback Service** | Publisher | Feedback events (submitted) | None |
| **Playlist Service** | Consumer | None | Profile & Feedback events |

---

## 🏗️ Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│                         RabbitMQ Message Broker                      │
├─────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  ┌─────────────────────┐        ┌──────────────────────┐           │
│  │ profile.exchange    │        │ feedback.exchange    │           │
│  │   (Topic)           │        │   (Topic)            │           │
│  └──────────┬──────────┘        └──────────┬───────────┘           │
│             │                               │                        │
│   ┌─────────┴─────────┐         ┌─────────┴──────────┐            │
│   │ Routing Keys:     │         │ Routing Key:       │            │
│   │ - profile.created │         │ - feedback.submitted│            │
│   │ - profile.updated │         └────────────────────┘            │
│   └─────────┬─────────┘                    │                        │
│             │                               │                        │
│   ┌─────────▼──────────────────────────────▼───────────┐           │
│   │  Queues for Playlist Service (Consumer)            │           │
│   │  ┌──────────────────────────────────────┐          │           │
│   │  │ playlist.profile-events.queue        │          │           │
│   │  │ (binds to profile.created & updated) │          │           │
│   │  └──────────────────────────────────────┘          │           │
│   │  ┌──────────────────────────────────────┐          │           │
│   │  │ playlist.feedback-events.queue       │          │           │
│   │  │ (binds to feedback.submitted)        │          │           │
│   │  └──────────────────────────────────────┘          │           │
│   └─────────────────────────────────────────────────────┘           │
│                                                                      │
│   ┌──────────────────────────────────────────┐                     │
│   │  Queues for Publisher Services           │                     │
│   │  (for monitoring/debugging)              │                     │
│   │  ┌────────────────────────┐              │                     │
│   │  │ profile.created.queue  │              │                     │
│   │  │ profile.updated.queue  │              │                     │
│   │  │ feedback.submitted.queue│              │                     │
│   │  └────────────────────────┘              │                     │
│   └──────────────────────────────────────────┘                     │
│                                                                      │
└─────────────────────────────────────────────────────────────────────┘

         ▲                    ▲                          ▲
         │                    │                          │
    ┌────┴─────┐        ┌────┴──────┐           ┌──────┴────────┐
    │ Profile  │        │ Feedback  │           │  Playlist     │
    │ Service  │        │ Service   │           │  Service      │
    │ (8081)   │        │ (8083)    │           │  (8082)       │
    │          │        │           │           │               │
    │ Publisher│        │ Publisher │           │   Consumer    │
    └──────────┘        └───────────┘           └───────────────┘
```

---

## 📝 Configuration Files Explained

### 1. Profile Service - RabbitMQConfig.java

**Location:** `services/profile-service/src/main/java/no/kristiania/echocare/profile/config/RabbitMQConfig.java`

**Purpose:** Configures RabbitMQ for publishing profile-related events.

#### What It Does:

1. **Creates Exchange**
   ```java
   @Bean
   public TopicExchange profileExchange()
   ```
   - **Exchange Name:** `profile.exchange`
   - **Type:** Topic Exchange (allows pattern-based routing)
   - **Purpose:** Routes profile events to interested consumers

2. **Creates Queues**
   ```java
   @Bean
   public Queue profileCreatedQueue()
   @Bean
   public Queue profileUpdatedQueue()
   ```
   - **Queue Names:** 
     - `profile.created.queue`
     - `profile.updated.queue`
   - **Durable:** Yes (survives broker restarts)
   - **Purpose:** Store events for monitoring/debugging

3. **Creates Bindings**
   ```java
   @Bean
   public Binding profileCreatedBinding()
   @Bean
   public Binding profileUpdatedBinding()
   ```
   - **Routing Keys:**
     - `profile.created` → triggers when new profile is created
     - `profile.updated` → triggers when profile is modified
   - **Purpose:** Connect queues to exchange with specific routing patterns

4. **Configures Message Conversion**
   ```java
   @Bean
   public MessageConverter jsonMessageConverter()
   ```
   - **Converter:** Jackson2JsonMessageConverter
   - **Purpose:** Automatically converts Java objects ↔ JSON
   - **Benefit:** No manual serialization needed

5. **Configures RabbitTemplate**
   ```java
   @Bean
   public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory)
   ```
   - **Purpose:** Main interface for sending messages
   - **Usage:** `rabbitTemplate.convertAndSend(exchange, routingKey, message)`

---

### 2. Feedback Service - RabbitMQConfig.java

**Location:** `services/feedback-service/src/main/java/no/kristiania/echocare/feedback/config/RabbitMQConfig.java`

**Purpose:** Configures RabbitMQ for publishing feedback events.

#### What It Does:

1. **Creates Exchange**
   ```java
   @Bean
   public TopicExchange feedbackExchange()
   ```
   - **Exchange Name:** `feedback.exchange`
   - **Type:** Topic Exchange
   - **Future-proof:** Can add more event types (updated, deleted, rated)

2. **Creates Queue**
   ```java
   @Bean
   public Queue feedbackSubmittedQueue()
   ```
   - **Queue Name:** `feedback.submitted.queue`
   - **Durable:** Yes
   - **Critical:** Feedback data must not be lost

3. **Creates Binding**
   ```java
   @Bean
   public Binding feedbackSubmittedBinding()
   ```
   - **Routing Key:** `feedback.submitted`
   - **Trigger:** When caregiver submits feedback

4. **Message Converter & Template**
   - Same as Profile Service
   - Enables sending feedback POJOs directly

---

### 3. Playlist Service - RabbitMQConfig.java (Most Complex)

**Location:** `services/playlist-service/src/main/java/no/kristiania/echocare/playlist/config/RabbitMQConfig.java`

**Purpose:** Configures RabbitMQ for consuming events from multiple services.

#### What It Does:

1. **Declares External Exchanges**
   ```java
   @Bean
   public TopicExchange profileExchange()
   @Bean
   public TopicExchange feedbackExchange()
   ```
   - **Why declare in consumer?** 
     - Ensures exchanges exist before binding
     - Makes service dependencies explicit
     - Allows standalone startup
   - **Idempotent:** Multiple declarations are safe if properties match

2. **Creates Consumer-Specific Queues**
   ```java
   @Bean
   public Queue profileEventsQueue()  // playlist.profile-events.queue
   @Bean
   public Queue feedbackEventsQueue() // playlist.feedback-events.queue
   ```
   - **Naming Convention:** `<service>.<event-source>-events.queue`
   - **Why separate queues?**
     - Each consumer processes at its own pace
     - Isolation: One service's failure doesn't affect others
     - Scalability: Can scale consumers independently

3. **Creates Multiple Bindings**
   ```java
   @Bean
   public Binding profileCreatedBinding()   // profile.created → queue
   @Bean
   public Binding profileUpdatedBinding()   // profile.updated → queue
   @Bean
   public Binding feedbackSubmittedBinding() // feedback.submitted → queue
   ```
   - **Effect:** One queue receives multiple event types
   - **Benefit:** Centralized processing logic

4. **Message Converter & Template**
   - Same JSON converter for type-safe message consumption
   - Template available if playlist-service needs to publish events

---

## 🔑 Key Concepts

### 1. Topic Exchange

**What is it?**
A routing mechanism that uses pattern matching on routing keys.

**How it works:**
```
Message: routingKey = "profile.created"
Binding: pattern = "profile.created"  → MATCH ✅
Binding: pattern = "profile.*"        → MATCH ✅
Binding: pattern = "profile.#"        → MATCH ✅
Binding: pattern = "feedback.created" → NO MATCH ❌
```

**Wildcards:**
- `*` (star) = matches exactly one word
- `#` (hash) = matches zero or more words

**Example:**
- `profile.*` matches: `profile.created`, `profile.updated`
- `profile.#` matches: `profile`, `profile.created`, `profile.created.admin`

### 2. Queue Durability

**Durable Queue:**
```java
new Queue(queueName, true)  // durable = true
```
- Queue definition survives broker restarts
- Messages are persisted to disk
- **Use when:** Message loss is unacceptable

**Non-Durable Queue:**
```java
new Queue(queueName, false)  // durable = false
```
- Queue is lost on broker restart
- **Use when:** Messages are ephemeral/temporary

**EchoCare Choice:** All queues are durable (healthcare data is critical)

### 3. Message Conversion

**Without Converter:**
```java
// Manual serialization required
String json = objectMapper.writeValueAsString(profileEvent);
rabbitTemplate.send(exchange, routingKey, new Message(json.getBytes()));
```

**With Converter:**
```java
// Automatic serialization
rabbitTemplate.convertAndSend(exchange, routingKey, profileEvent);
```

**Benefit:** Cleaner code, type safety, less boilerplate

### 4. Publish-Subscribe Pattern

**Traditional Direct Call:**
```
Profile Service → HTTP POST → Playlist Service
```
- Tight coupling
- Synchronous (blocks)
- Single consumer

**Event-Driven with RabbitMQ:**
```
Profile Service → publish event → RabbitMQ
                                      ↓
                               Playlist Service
                               Analytics Service  (can add later)
                               Notification Service (can add later)
```
- Loose coupling
- Asynchronous
- Multiple consumers
- Producer doesn't know consumers

---

## 📊 Message Flow Examples

### Example 1: Profile Created Flow

**Step-by-Step:**

1. **Caregiver creates a new patient profile via API**
   ```http
   POST /profiles HTTP/1.1
   Content-Type: application/json
   
   {
     "patientName": "John Doe",
     "careNeed": "DEMENTIA",
     "stage": "EARLY"
   }
   ```

2. **Profile Service saves to database**
   ```java
   Profile profile = profileRepository.save(newProfile);
   ```

3. **Profile Service publishes event**
   ```java
   ProfileEvent event = new ProfileEvent(
       profile.getId(),
       profile.getPatientName(),
       profile.getCareNeed(),
       profile.getStage()
   );
   
   rabbitTemplate.convertAndSend(
       "profile.exchange",      // exchange
       "profile.created",       // routing key
       event                    // message (converted to JSON)
   );
   ```

4. **RabbitMQ receives and routes message**
   - Checks bindings for `profile.exchange`
   - Finds queues bound to `profile.created`
   - Delivers message to: `playlist.profile-events.queue`

5. **Playlist Service consumes event**
   ```java
   @RabbitListener(queues = "${rabbitmq.queue.profile-events}")
   public void handleProfileEvent(ProfileEvent event) {
       if (event.getEventType().equals("CREATED")) {
           // Generate initial recommendations for new profile
           playlistService.generateInitialPlaylists(event.getProfileId());
       }
   }
   ```

6. **Result:** Playlists automatically generated without direct HTTP call

---

### Example 2: Feedback Submitted Flow

**Step-by-Step:**

1. **Caregiver submits feedback**
   ```http
   POST /feedback HTTP/1.1
   Content-Type: application/json
   
   {
     "playlistId": 123,
     "rating": 5,
     "comment": "Very helpful, patient was calm"
   }
   ```

2. **Feedback Service saves to database**
   ```java
   Feedback feedback = feedbackRepository.save(newFeedback);
   ```

3. **Feedback Service publishes event**
   ```java
   FeedbackEvent event = new FeedbackEvent(
       feedback.getId(),
       feedback.getPlaylistId(),
       feedback.getRating(),
       feedback.getComment()
   );
   
   rabbitTemplate.convertAndSend(
       "feedback.exchange",
       "feedback.submitted",
       event
   );
   ```

4. **Playlist Service consumes event**
   ```java
   @RabbitListener(queues = "${rabbitmq.queue.feedback-events}")
   public void handleFeedbackEvent(FeedbackEvent event) {
       // Update recommendation algorithm based on feedback
       if (event.getRating() >= 4) {
           // Boost similar playlists
           playlistService.increaseSimilarPlaylistScores(event.getPlaylistId());
       } else {
           // Reduce similar playlists
           playlistService.decreaseSimilarPlaylistScores(event.getPlaylistId());
       }
   }
   ```

5. **Result:** Recommendation system learns from feedback automatically

---

## 🚀 How to Use

### For Publishers (Profile & Feedback Services)

1. **Inject RabbitTemplate**
   ```java
   @Service
   public class ProfileEventPublisher {
       
       @Autowired
       private RabbitTemplate rabbitTemplate;
       
       @Value("${rabbitmq.exchange.profile}")
       private String profileExchange;
       
       @Value("${rabbitmq.routing-key.profile-created}")
       private String profileCreatedKey;
   }
   ```

2. **Publish Events**
   ```java
   public void publishProfileCreated(Profile profile) {
       ProfileEvent event = new ProfileEvent(
           profile.getId(),
           profile.getPatientName(),
           profile.getCareNeed(),
           LocalDateTime.now()
       );
       
       rabbitTemplate.convertAndSend(
           profileExchange,
           profileCreatedKey,
           event
       );
       
       log.info("Published profile created event: {}", event);
   }
   ```

### For Consumers (Playlist Service)

1. **Create Listener Class**
   ```java
   @Component
   public class ProfileEventListener {
       
       @Autowired
       private PlaylistService playlistService;
       
       @RabbitListener(queues = "${rabbitmq.queue.profile-events}")
       public void handleProfileEvent(ProfileEvent event) {
           log.info("Received profile event: {}", event);
           
           switch (event.getEventType()) {
               case "CREATED":
                   playlistService.generateInitialPlaylists(event);
                   break;
               case "UPDATED":
                   playlistService.updatePlaylists(event);
                   break;
           }
       }
   }
   ```

2. **Enable RabbitMQ Listeners**
   ```java
   @SpringBootApplication
   @EnableRabbit  // Add this annotation
   public class PlaylistServiceApplication {
       public static void main(String[] args) {
           SpringApplication.run(PlaylistServiceApplication.class, args);
       }
   }
   ```

---

## 🔧 Configuration Properties

All services use properties from `application.yml`:

```yaml
spring:
  rabbitmq:
    host: localhost      # RabbitMQ server host
    port: 5672          # AMQP port (not management UI port 15672)
    username: guest     # RabbitMQ username
    password: guest     # RabbitMQ password
    virtual-host: /     # Virtual host (namespace)
    listener:
      simple:
        acknowledge-mode: auto      # auto, manual, none
        prefetch: 1                 # Messages fetched at once
        retry:
          enabled: true
          initial-interval: 1000    # 1 second
          max-attempts: 3
          multiplier: 2.0           # Exponential backoff

rabbitmq:
  exchange:
    profile: profile.exchange
    feedback: feedback.exchange
  queue:
    profile-created: profile.created.queue
    profile-updated: profile.updated.queue
    feedback-submitted: feedback.submitted.queue
  routing-key:
    profile-created: profile.created
    profile-updated: profile.updated
    feedback-submitted: feedback.submitted
```

---

## 🐛 Troubleshooting

### Issue 1: Messages Not Being Received

**Symptoms:**
- Producer publishes messages
- Consumer doesn't receive them

**Possible Causes & Solutions:**

1. **Queue not bound to exchange**
   ```bash
   # Check bindings in RabbitMQ Management UI
   # http://localhost:15672
   # Navigate to: Exchanges → profile.exchange → Bindings
   ```

2. **Routing key mismatch**
   ```java
   // Publisher
   rabbitTemplate.convertAndSend("profile.exchange", "profile.created", event);
   
   // Binding
   .with("profile.created")  // Must match exactly!
   ```

3. **Listener not registered**
   ```java
   // Make sure you have @EnableRabbit on main application class
   @SpringBootApplication
   @EnableRabbit  // ← This is required!
   public class PlaylistServiceApplication { }
   ```

### Issue 2: Connection Refused

**Symptoms:**
```
java.net.ConnectException: Connection refused
```

**Solutions:**

1. **Check RabbitMQ is running**
   ```bash
   docker ps | grep rabbitmq
   ```

2. **Verify port configuration**
   ```yaml
   spring:
     rabbitmq:
       port: 5672  # AMQP port, NOT 15672 (management UI)
   ```

3. **Check host configuration**
   ```yaml
   # application.yml (local)
   spring:
     rabbitmq:
       host: localhost
   
   # application-docker.yml (containerized)
   spring:
     rabbitmq:
       host: rabbitmq  # Docker service name
   ```

### Issue 3: Message Conversion Errors

**Symptoms:**
```
org.springframework.amqp.support.converter.MessageConversionException
```

**Solutions:**

1. **Ensure MessageConverter is configured**
   ```java
   @Bean
   public MessageConverter jsonMessageConverter() {
       return new Jackson2JsonMessageConverter();
   }
   ```

2. **Check event class has proper getters/setters**
   ```java
   public class ProfileEvent {
       private Long profileId;
       
       // Must have getters/setters or use @Data (Lombok)
       public Long getProfileId() { return profileId; }
       public void setProfileId(Long profileId) { this.profileId = profileId; }
   }
   ```

3. **Add Jackson annotations if needed**
   ```java
   @JsonIgnoreProperties(ignoreUnknown = true)  // Ignore unknown fields
   public class ProfileEvent { }
   ```

### Issue 4: Messages Accumulating in Queue

**Symptoms:**
- Messages in queue keep increasing
- Consumer isn't processing fast enough

**Solutions:**

1. **Scale consumers**
   ```java
   @RabbitListener(
       queues = "${rabbitmq.queue.profile-events}",
       concurrency = "3-10"  // 3 to 10 concurrent listeners
   )
   ```

2. **Optimize processing logic**
   ```java
   @RabbitListener(queues = "${rabbitmq.queue.profile-events}")
   @Async  // Process in separate thread pool
   public void handleProfileEvent(ProfileEvent event) { }
   ```

3. **Check for errors in listener**
   ```java
   @RabbitListener(queues = "${rabbitmq.queue.profile-events}")
   public void handleProfileEvent(ProfileEvent event) {
       try {
           // Process event
       } catch (Exception e) {
           log.error("Failed to process event: {}", event, e);
           throw e;  // Retry or send to DLQ (Dead Letter Queue)
       }
   }
   ```

---

## 📚 Additional Resources

### RabbitMQ Management UI

**Access:** http://localhost:15672  
**Credentials:** guest / guest

**Useful Views:**
- **Exchanges:** See all exchanges and their bindings
- **Queues:** Monitor queue depth, message rates
- **Connections:** View active connections from services
- **Channels:** See message flow in real-time

### Monitoring Commands

```bash
# List all queues
docker exec echocare-rabbitmq rabbitmqctl list_queues

# List all exchanges
docker exec echocare-rabbitmq rabbitmqctl list_exchanges

# List all bindings
docker exec echocare-rabbitmq rabbitmqctl list_bindings

# Check service health
curl http://localhost:15672/api/healthchecks/node
```

### Testing Events Manually

You can publish test messages using RabbitMQ Management UI:

1. Go to **Exchanges**
2. Click on `profile.exchange`
3. Scroll to **Publish message**
4. Enter:
   ```json
   {
     "profileId": 123,
     "patientName": "Test Patient",
     "careNeed": "DEMENTIA",
     "eventType": "CREATED"
   }
   ```
5. Set **Routing key:** `profile.created`
6. Click **Publish message**
7. Check consumer logs to verify receipt

---

## ✅ Best Practices Summary

1. **Always use durable queues** for important data
2. **Use Topic Exchange** for flexibility
3. **Enable JSON converter** for type safety
4. **Configure retry logic** for transient failures
5. **Monitor queue depth** to detect processing issues
6. **Use meaningful routing keys** (e.g., `service.action`)
7. **Add logging** in publishers and consumers
8. **Handle errors gracefully** with try-catch
9. **Document event schemas** for other teams
10. **Test with RabbitMQ Management UI** before coding

---

## 🎓 Learning Summary

You now have:
- ✅ RabbitMQ configuration classes for all three services
- ✅ Topic exchanges for flexible event routing
- ✅ Durable queues for reliable message storage
- ✅ JSON message conversion for type-safe messaging
- ✅ Proper bindings with routing keys
- ✅ Complete documentation with examples

**Next Steps:**
1. Create event POJO classes (ProfileEvent, FeedbackEvent)
2. Create publisher services (@Service classes with RabbitTemplate)
3. Create consumer listeners (@Component with @RabbitListener)
4. Test event flow end-to-end
5. Monitor with RabbitMQ Management UI

Happy messaging! 🚀

