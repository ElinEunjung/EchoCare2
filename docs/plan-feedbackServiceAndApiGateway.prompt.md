# Plan: Implement Caregiver Feedback System with API Gateway

Complete implementation of user story 3 enabling caregivers to provide feedback (like/dislike) on songs via API Gateway (port 8080) with JWT validation, full routing to all services, CORS support, and RabbitMQ event processing for personalization.

**Note:** Rating field has been excluded from this implementation. Feedback tracks like/dislike, dementiaStage, and careNeed instead of situation context.

## Steps

1. **Complete Feedback entity and repository in [`feedback-service`](C:\Users\ElinEunjungPark(Ext)\IdeaProjects\EchoCare2\services\feedback-service)** — Verify `dementiaStage` (String) and `careNeed` (String) fields exist in [`Feedback`](C:\Users\ElinEunjungPark(Ext)\IdeaProjects\EchoCare2\services\feedback-service\src\main\java\no\kristiania\echocare\feedback\domain\entity\Feedback.java) entity (already implemented), ensure `createdAt` (LocalDateTime) field with `@PrePersist` is correctly named (fix typo from `createAt` to `createdAt`), update constructor to populate dementiaStage and careNeed from request, verify `findByPatientProfileId(UUID)` method exists in [`FeedbackRepository`](C:\Users\ElinEunjungPark(Ext)\IdeaProjects\EchoCare2\services\feedback-service\src\main\java\no\kristiania\echocare\feedback\repository\FeedbackRepository.java)

2. **Complete [`FeedbackService`](C:\Users\ElinEunjungPark(Ext)\IdeaProjects\EchoCare2\services\feedback-service\src\main\java\no\kristiania\echocare\feedback\service\FeedbackService.java) and [`FeedbackController`](C:\Users\ElinEunjungPark(Ext)\IdeaProjects\EchoCare2\services\feedback-service\src\main\java\no\kristiania\echocare\feedback\api\controller\FeedbackController.java)** — Complete `getFeedbackByProfile(UUID)` service method implementation (already done, returns `List<FeedbackResponse>`), fix controller `GET /api/feedback/profile/{profileId}` endpoint signature (change from `Long` to `UUID` parameter, change return type from single `FeedbackEventDTO` to `List<FeedbackResponse>`), implement method body calling service, remove or properly implement `submitRating()` endpoint if not needed

3. **Implement feedback processing in [`FeedbackEventConsumer`](C:\Users\ElinEunjungPark(Ext)\IdeaProjects\EchoCare2\services\playlist-service\src\main\java\no\kristiania\echocare\playlist\integration\FeedbackEventConsumer.java)** — In `handleFeedbackSubmitted()`, log detailed feedback info (feedbackId, patientProfileId, songId, liked, dementiaStage, careNeed) for tracking preferences (simplest implementation as requested)

4. **Create `gateway-service` Spring Boot module with Spring Cloud Gateway 2023.0.0** — Initialize new service under `services/gateway-service/` with `pom.xml` extending spring-boot-starter-parent 3.2.5, add `spring-cloud-dependencies` BOM version 2023.0.0 in dependencyManagement for compatibility, add dependencies (`spring-cloud-starter-gateway`, `spring-boot-starter-webflux`, `jjwt-api` 0.12.3, `jjwt-impl` 0.12.3, `jjwt-jackson` 0.12.3, `lombok`), create main application class `GatewayApplication`, create `application.yml` with server.port=8080 and consistent `jwt.secret` (WOwTER+0vZAxLEr3LtLzJY1Y77gWWnnuQeUrvvTA9ZKvH47wGBwv1kJ+ghqrn7basSlRTQjjXNsaMJdZNbQRIw==)

5. **Configure gateway routes preserving paths** — In `application.yml` add `spring.cloud.gateway.routes` for profile-service (id: profile-route, uri: http://localhost:8081, predicates: Path=/api/profiles/**, filters: none), playlist-service (id: playlist-route, uri: http://localhost:8082, predicates: Path=/api/playlists/**), feedback-service (id: feedback-route, uri: http://localhost:8083, predicates: Path=/api/feedback/**), auth route with no JWT filter (id: auth-route, uri: http://localhost:8081, predicates: Path=/api/auth/**)

6. **Implement JWT validation filter with proper ordering** — Copy [`JwtUtil`](C:\Users\ElinEunjungPark(Ext)\IdeaProjects\EchoCare2\services\profile-service\src\main\java\no\kristiania\echocare\profile\config\JwtUtil.java) from profile-service to `gateway-service/src/main/java/no/kristiania/echocare/gateway/config/JwtUtil.java`, create `JwtAuthenticationGatewayFilterFactory` extending `AbstractGatewayFilterFactory<JwtAuthenticationGatewayFilterFactory.Config>` with `@Order(1)` to validate JWT from Authorization header, extract caregiverId using `jwtUtil.extractCaregiverId(token)`, add to request header `X-Caregiver-Id` using `exchange.getRequest().mutate().header()`, apply filter to profile/playlist/feedback routes but exclude auth route using `.filters(f -> f.filter())` in route configuration

7. **Add CORS configuration for development** — Create `CorsGlobalConfiguration` class with `@Bean CorsWebFilter corsWebFilter()` allowing origins `*` (or specific frontend origin like http://localhost:5173), methods GET/POST/PUT/DELETE/OPTIONS, headers `*` including Authorization and Content-Type, credentials true using `CorsConfiguration` and `UrlBasedCorsConfigurationSource`

8. **Implement global error handler with specific error codes** — Create `GlobalErrorWebExceptionHandler` implementing `ErrorWebExceptionHandler` with `@Order(-2)` to return JSON error responses using `ServerResponse.status().bodyValue()`: 401 Unauthorized with `{error: "Invalid or expired JWT token"}` for `io.jsonwebtoken.JwtException`, 503 Service Unavailable with `{error: "Service temporarily unavailable"}` for `java.net.ConnectException`, 500 Internal Server Error for other exceptions

9. **Update parent POM and add Docker configuration** — Add `<module>services/gateway-service</module>` to root [`pom.xml`](C:\Users\ElinEunjungPark(Ext)\IdeaProjects\EchoCare2\pom.xml) modules section, add gateway service to [`docker-compose.yml`](C:\Users\ElinEunjungPark(Ext)\IdeaProjects\EchoCare2\docker-compose.yml) with container_name echocare-gateway, build context ./services/gateway-service, ports 8080:8080, environment `SPRING_PROFILES_ACTIVE=docker`, depends_on profile-db/playlist-db/feedback-db/rabbitmq, networks echocare-network

10. **Create Docker-specific gateway configuration** — Create `gateway-service/src/main/resources/application-docker.yml` with `spring.cloud.gateway.routes` using Docker internal service URLs (uri: http://profile-service:8081 instead of localhost), same jwt.secret value, ensure all services use service names not localhost for inter-service communication in Docker network

11. **Create comprehensive PowerShell end-to-end test script** — Create `test-data/test-gateway-feedback-e2e.ps1` with prerequisite check function `Test-Prerequisites` verifying Docker containers are running (docker ps | grep echocare), main test functions: `Test-RegisterCaregiver` (POST http://localhost:8080/api/auth/register with body `{username:"testcaregiver",password:"password123"}`, capture JWT from response), `Test-CreatePatientProfile` (POST /api/profiles with JWT header and body `{name:"Test Patient",birthYear:1950,careNeed:"CALMING_AGITATION",dementiaStage:"MODERATE"}`), `Test-GeneratePlaylist` (POST /api/playlists/generate with profileId from step 2), `Test-SubmitFeedback` (POST /api/feedback with playlistId, first songId from playlist, patientProfileId, liked:true, dementiaStage:"MODERATE", careNeed:"CALMING_AGITATION"), `Test-GetFeedbackByProfile` (GET /api/feedback/profile/{profileId} to verify feedback was saved), `Test-VerifyRabbitMQEvent` (invoke RabbitMQ management API http://localhost:15672/api/queues/%2F/playlist.feedback-events.queue with basic auth guest:guest, check messages_ready or get message), include cleanup function to delete test data and comprehensive error handling with success/failure reporting

12. **Add cleanup and error handling to test script** — Implement `Cleanup-TestData` function to optionally delete created test entities, wrap all API calls in try-catch blocks, add verbose output showing request/response for debugging, add timeout handling for API calls using `-TimeoutSec 10`, validate HTTP status codes (201 for creation, 200 for GET), extract and validate JSON responses, create summary report at end showing passed/failed tests

## Further Considerations

1. **Spring Cloud Gateway version compatibility (IMPLEMENTED IN STEP 4)** — Gateway requires Spring Boot 3.x and reactive WebFlux (not servlet-based). Use `spring-cloud-dependencies` BOM version 2023.0.0 in dependencyManagement for compatibility with Spring Boot 3.2.5, ensure no servlet dependencies are included

2. **Gateway filter ordering (IMPLEMENTED IN STEP 6)** — JWT filter executes before routing using `@Order(1)` annotation on filter factory class and `.order(1)` in filter chain configuration. Path exclusions for `/api/auth/**` implemented via separate route without JWT filter instead of using Predicate.not() for cleaner configuration

3. **Docker network communication (IMPLEMENTED IN STEPS 9-10)** — Services use service names (profile-service, playlist-service, feedback-service, rabbitmq) not localhost in Docker environment. Gateway uses `application-docker.yml` activated via `SPRING_PROFILES_ACTIVE=docker` environment variable with routes pointing to `http://profile-service:8081`, `http://playlist-service:8082`, `http://feedback-service:8083`

4. **JWT secret consistency (IMPLEMENTED IN STEPS 4,10)** — All services (profile-service, playlist-service, feedback-service, gateway-service) use identical `jwt.secret` value `WOwTER+0vZAxLEr3LtLzJY1Y77gWWnnuQeUrvvTA9ZKvH47wGBwv1kJ+ghqrn7basSlRTQjjXNsaMJdZNbQRIw==` from application.yml for token validation to work across services, verified in both local and Docker configurations

5. **Testing strategy with prerequisites (IMPLEMENTED IN STEPS 11-12)** — Test script includes `Test-Prerequisites` function checking if Docker containers are up before running tests using `docker ps` commands, cleanup steps to delete test data after execution, comprehensive error handling for network failures and service unavailability, detailed logging for debugging failed tests

---

## TODO List

### Feedback Service Completion
- [ ] **Update Feedback entity**
  - [ ] Fix typo: rename `createAt` field to `createdAt` (field name and all references)
  - [ ] Verify `dementiaStage` field exists (String, camelCase)
  - [ ] Verify `careNeed` field exists (String, camelCase)
  - [ ] Update constructor to populate `dementiaStage` and `careNeed` from request

- [ ] **Update FeedbackRepository**
  - [ ] Add method `List<Feedback> findByPatientProfileId(UUID patientProfileId);`

- [ ] **Complete FeedbackService**
  - [ ] Verify `getFeedbackByProfile(UUID)` method exists and returns `List<FeedbackResponse>`
  - [ ] Ensure service properly maps Feedback entities to FeedbackResponse DTOs

- [ ] **Complete FeedbackController**
  - [ ] Fix `getFeedbackForProfile()` return type to `List<FeedbackResponse>`
  - [ ] Change `@PathVariable Long profileId` to `UUID profileId`
  - [ ] Implement method body calling `feedbackService.getFeedbackByProfile(profileId)`
  - [ ] Remove `submitRating()` endpoint if not needed (or document its purpose)

### Playlist Service Enhancement
- [ ] **Update FeedbackEventConsumer**
  - [ ] Implement `handleFeedbackSubmitted()` method body
  - [ ] Add detailed logging: feedbackId, patientProfileId, songId, liked, dementiaStage, careNeed
  - [ ] Use log.info with structured message for preference tracking

### Gateway Service Creation
- [ ] **Create gateway-service module structure**
  - [ ] Create directory `services/gateway-service/`
  - [ ] Create `services/gateway-service/pom.xml`
  - [ ] Create `services/gateway-service/src/main/java/no/kristiania/echocare/gateway/GatewayApplication.java`
  - [ ] Create `services/gateway-service/src/main/resources/application.yml`
  - [ ] Create `services/gateway-service/src/main/resources/application-docker.yml`

- [ ] **Configure gateway pom.xml**
  - [ ] Set parent to spring-boot-starter-parent 3.2.5
  - [ ] Add dependencyManagement with spring-cloud-dependencies BOM 2023.0.0
  - [ ] Add spring-cloud-starter-gateway dependency
  - [ ] Add spring-boot-starter-webflux dependency
  - [ ] Add jjwt-api 0.12.3
  - [ ] Add jjwt-impl 0.12.3 (runtime scope)
  - [ ] Add jjwt-jackson 0.12.3 (runtime scope)
  - [ ] Add lombok dependency
  - [ ] Add spring-boot-maven-plugin

- [ ] **Create GatewayApplication class**
  - [ ] Add `@SpringBootApplication` annotation
  - [ ] Add main method with SpringApplication.run

- [ ] **Configure application.yml for gateway**
  - [ ] Set `server.port: 8080`
  - [ ] Set `spring.application.name: gateway-service`
  - [ ] Add `jwt.secret` with value matching other services
  - [ ] Configure route for auth (no JWT filter): id auth-route, uri http://localhost:8081, path /api/auth/**
  - [ ] Configure route for profiles: id profile-route, uri http://localhost:8081, path /api/profiles/**, with JWT filter
  - [ ] Configure route for playlists: id playlist-route, uri http://localhost:8082, path /api/playlists/**, with JWT filter
  - [ ] Configure route for feedback: id feedback-route, uri http://localhost:8083, path /api/feedback/**, with JWT filter

- [ ] **Configure application-docker.yml for gateway**
  - [ ] Update auth route uri to http://profile-service:8081
  - [ ] Update profile route uri to http://profile-service:8081
  - [ ] Update playlist route uri to http://playlist-service:8082
  - [ ] Update feedback route uri to http://feedback-service:8083
  - [ ] Keep same jwt.secret and port 8080

- [ ] **Implement JWT validation**
  - [ ] Create `config/JwtUtil.java` (copy from profile-service)
  - [ ] Create `config/JwtAuthenticationGatewayFilterFactory.java`
  - [ ] Add `@Component` and `@Order(1)` annotations
  - [ ] Extend `AbstractGatewayFilterFactory<Config>`
  - [ ] Inject JwtUtil dependency
  - [ ] Implement apply() method to extract Authorization header
  - [ ] Validate JWT using jwtUtil.isTokenValid()
  - [ ] Extract caregiverId using jwtUtil.extractCaregiverId()
  - [ ] Add X-Caregiver-Id header to request
  - [ ] Return 401 error for invalid/missing token
  - [ ] Create inner Config class

- [ ] **Implement CORS configuration**
  - [ ] Create `config/CorsGlobalConfiguration.java`
  - [ ] Add `@Configuration` annotation
  - [ ] Create `@Bean CorsWebFilter corsWebFilter()`
  - [ ] Configure CorsConfiguration: allowedOrigins=*, allowedMethods=GET/POST/PUT/DELETE/OPTIONS
  - [ ] Set allowedHeaders=* and allowCredentials=true
  - [ ] Use UrlBasedCorsConfigurationSource with /** pattern

- [ ] **Implement global error handler**
  - [ ] Create `config/GlobalErrorWebExceptionHandler.java`
  - [ ] Add `@Component` and `@Order(-2)` annotations
  - [ ] Implement `ErrorWebExceptionHandler` interface
  - [ ] Override handle() method
  - [ ] Check exception type: JwtException → 401, ConnectException → 503, default → 500
  - [ ] Return ServerResponse with JSON error body
  - [ ] Include error message and timestamp in response

### Project Configuration Updates
- [ ] **Update root pom.xml**
  - [ ] Add `<module>services/gateway-service</module>` to modules section

- [ ] **Update docker-compose.yml**
  - [ ] Add gateway service definition
  - [ ] Set container_name: echocare-gateway
  - [ ] Set build context: ./services/gateway-service
  - [ ] Map ports: 8080:8080
  - [ ] Set environment SPRING_PROFILES_ACTIVE: docker
  - [ ] Add depends_on: profile-db, playlist-db, feedback-db, rabbitmq
  - [ ] Add to echocare-network

- [ ] **Verify JWT secret consistency**
  - [ ] Check profile-service application.yml has same jwt.secret
  - [ ] Check playlist-service application.yml has same jwt.secret
  - [ ] Check feedback-service application.yml has same jwt.secret
  - [ ] Check all application-docker.yml files have same jwt.secret

### End-to-End Test Script
- [ ] **Create test script file**
  - [ ] Create `test-data/test-gateway-feedback-e2e.ps1`

- [ ] **Implement prerequisite check**
  - [ ] Create `Test-Prerequisites` function
  - [ ] Check Docker is running
  - [ ] Verify echocare containers are up (docker ps | grep echocare)
  - [ ] Check gateway responds on port 8080
  - [ ] Return boolean success/failure

- [ ] **Implement test functions**
  - [ ] Create `Test-RegisterCaregiver` function
    - [ ] POST to http://localhost:8080/api/auth/register
    - [ ] Body: {username:"testcaregiver_$(Get-Date -Format 'yyyyMMddHHmmss')", password:"password123"}
    - [ ] Capture JWT token from response
    - [ ] Return token or throw error
  - [ ] Create `Test-CreatePatientProfile` function
    - [ ] Accept JWT token parameter
    - [ ] POST to http://localhost:8080/api/profiles
    - [ ] Headers: Authorization: Bearer $token
    - [ ] Body: {name:"Test Patient", birthYear:1950, careNeed:"CALMING_AGITATION", dementiaStage:"MODERATE"}
    - [ ] Capture and return profileId
  - [ ] Create `Test-GeneratePlaylist` function
    - [ ] Accept JWT token and profileId parameters
    - [ ] POST to http://localhost:8080/api/playlists/generate
    - [ ] Body: {patientProfileId:$profileId, situation:"EVENING_ROUTINE"}
    - [ ] Capture playlistId and first songId
    - [ ] Return hashtable with both IDs
  - [ ] Create `Test-SubmitFeedback` function
    - [ ] Accept JWT, profileId, playlistId, songId parameters
    - [ ] POST to http://localhost:8080/api/feedback
    - [ ] Body: {playlistId:$playlistId, songId:$songId, patientProfileId:$profileId, liked:true, dementiaStage:"MODERATE", careNeed:"CALMING_AGITATION"}
    - [ ] Capture and return feedbackId
  - [ ] Create `Test-GetFeedbackByProfile` function
    - [ ] Accept JWT and profileId parameters
    - [ ] GET from http://localhost:8080/api/feedback/profile/$profileId
    - [ ] Verify response is array with at least 1 item
    - [ ] Validate feedback properties exist
  - [ ] Create `Test-VerifyRabbitMQEvent` function
    - [ ] Invoke-RestMethod to http://localhost:15672/api/queues/%2F/playlist.feedback-events.queue
    - [ ] Use basic auth: guest:guest
    - [ ] Check messages_ready > 0 or messages > 0
    - [ ] Optionally get message content

- [ ] **Implement main test execution**
  - [ ] Create main script flow
  - [ ] Call Test-Prerequisites first
  - [ ] Execute test functions in sequence with error handling
  - [ ] Capture results for each step
  - [ ] Generate summary report

- [ ] **Implement cleanup and reporting**
  - [ ] Create `Cleanup-TestData` function (optional, for future)
  - [ ] Add try-catch blocks around all API calls
  - [ ] Add verbose logging with Write-Host colored output
  - [ ] Add -TimeoutSec parameter to Invoke-RestMethod calls
  - [ ] Validate HTTP status codes
  - [ ] Create summary: "✓ Passed: X, ✗ Failed: Y"

### Testing and Validation
- [ ] **Build and test feedback-service locally**
  - [ ] Run `mvn clean install` in feedback-service
  - [ ] Fix any compilation errors
  - [ ] Run feedback-service on port 8083
  - [ ] Test POST /api/feedback endpoint
  - [ ] Test GET /api/feedback/profile/{id} endpoint

- [ ] **Build and test gateway-service locally**
  - [ ] Run `mvn clean install` in gateway-service
  - [ ] Fix any compilation errors
  - [ ] Run gateway-service on port 8080
  - [ ] Test routes are working (curl or Postman)
  - [ ] Verify JWT validation works
  - [ ] Verify CORS headers present

- [ ] **Test Docker integration**
  - [ ] Run `mvn clean package` on root pom
  - [ ] Run `docker compose build`
  - [ ] Run `docker compose up`
  - [ ] Verify all 4 services start successfully
  - [ ] Check gateway can reach other services via Docker network

- [ ] **Execute end-to-end test**
  - [ ] Run test-gateway-feedback-e2e.ps1
  - [ ] Verify all test steps pass
  - [ ] Check RabbitMQ management UI for feedback events
  - [ ] Review playlist-service logs for feedback event consumption
  - [ ] Verify feedback is persisted in feedback database

- [ ] **Final validation**
  - [ ] Check feedback entries are retrievable by profileId
  - [ ] Verify JWT works across all services via gateway
  - [ ] Test error scenarios (invalid JWT, missing fields)
  - [ ] Confirm CORS works for browser requests
  - [ ] Document any issues or limitations

