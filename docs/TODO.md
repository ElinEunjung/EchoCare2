# TODO – EchoCare  Plan (Oct 29 – Nov 6, 2025)

> Goal: deliver a runnable MVP microservice system (Profile → Playlist → Feedback) that starts with `docker compose up`, has REST + RabbitMQ + Postgres, and includes README + architecture diagram + 2-page reflection.

---

## Week 1 (Oct 29 – Nov 3) — Build working skeleton

### 1. Repo & structure
- [x] Check monorepo layout:
    - [x] `echocare/`
    - [x] `services/profile-service/`
    - [x] `services/playlist-service/`
    - [x] `services/feedback-service/`
    - [x] `infra/docker-compose.yml`
    - [x] `README.md`
- [x] Make sure each service has its own `pom.xml` and runs alone (`mvn spring-boot:run`).

### 2. Profile Service (User Story 1)
- [x] Create `PatientProfile` entity (`id`, `patientName`, `musicPreference` as `@Embedded`, `symptoms`, `stage`).
- [x] Create value objects / enums:
    - [x]`MusicPreference` (era, favoriteArtists, favoriteGenres)
    - [x] `DementiaStage` (MILD, MODERATE, SEVERE)
- [x] Create DTOs:
    - [x] `CreatePatientProfileRequest`
    - [x] `PatientProfileResponse`
- [x] Create repository: `PatientProfileRepo extends JpaRepository`.
- [x] Create service: `PatientProfileService` (create, getById, getAll).
- [x] Create controller: `POST /profiles`, `GET /profiles/{id}`.
- [x] Add **H2** config to `application.yml` so the app can run without Docker.
- [x] Test in Postman / curl:
    - [x] `POST /profiles` (register mother with era, favorite artists, symptoms)
    - [x] `GET /profiles/{id}` (verify it’s stored)
- [x] Commit: `feat(profile): add patient registration endpoint`

### 3. Playlist Service (User Story 2 - MVP rule engine)
- [x] Bootstrap Spring Boot app for `playlist-service`.
- [x] Duplicate enums needed locally (do **not** import from profile-service):
    - [x] `CareNeed` (STRESS_RELIEF, ACTIVITY_SUPPORT, CALMING_AGITATION, EASE_DEPRESSION, EASE_ANXIETY)
    - [x] `DementiaStage` (MILD, MODERATE, SEVERE)
- [ ] Create REST client (simple) to call Profile Service: `GET http://profile-service:8081/profiles/{id}` (adjust port later).
- [ ] Create DTOs:
    - [ ] `GeneratePlaylistRequest` (patientId, careNeed, optional stageOverride)
    - [ ] `TrackDto`
    - [ ] `PlaylistResponse`
- [ ] Create service: `PlaylistGeneratorService` with simple rules:
    - [ ] Fetch profile
    - [ ] Bias by era
    - [ ] Adjust tempo by careNeed
    - [ ] Adapt for dementia stage
- [ ] Create controller: `POST /playlists/generate`
- [ ] Test locally with real profile id
- [ ] Commit: `feat(playlist): generate situation-aware playlist`

### 4. Feedback Service (skeleton)
- [ ] Bootstrap Spring Boot app for `feedback-service`.
- [ ] Add endpoint: `POST /feedback` (later will be called by consumer).
- [ ] Add in-memory storage or simple JPA entity for feedback.
- [ ] Commit: `feat(feedback): add feedback endpoint (skeleton)`

### 5. RabbitMQ wiring (producer side only)
- [ ] Add RabbitMQ dependency to `playlist-service`.
- [ ] Configure connection in `application.yml` (host from docker later).
- [ ] On playlist generated → publish event `playlist.generated` with patientId + careNeed + stage.
- [ ] Commit: `feat(playlist): publish playlist.generated event`

### 6. Docker Compose (infra draft)
- [ ] Create `infra/docker-compose.yml` with:
    - [ ] postgres (profile-db)
    - [ ] rabbitmq (with management)
    - [ ] **but** services can still run locally with H2 for now
- [ ] Commit: `chore(infra): add base docker compose for db and mq`

---

## Week 2 (Nov 4 – Nov 6) — Stabilize, gateway, docs

### 7. Switch to Postgres (per service)
- [ ] Add Postgres driver to each service.
- [ ] Add `spring.datasource.*` pointing to docker db.
- [ ] Add Flyway or `ddl-auto=update` for exam MVP.
- [ ] Test: run service while `docker compose up` is running.

### 8. Gateway + Discovery
- [ ] Add `gateway/` Spring Cloud Gateway service.
- [ ] Add Consul (or service discovery) to docker-compose.
- [ ] Configure routes:
    - [ ] `/api/profiles/** → profile-service`
    - [ ] `/api/playlists/** → playlist-service`
    - [ ] `/api/feedback/** → feedback-service`
- [ ] Commit: `feat(gateway): route to profile, playlist and feedback`

### 9. Feedback consumer
- [ ] In `feedback-service` add RabbitMQ listener to queue `playlist.generated`.
- [ ] On receive → store feedback placeholder (later caregiver rating).
- [ ] Commit: `feat(feedback): consume playlist.generated events`

### 10. Resilience & health
- [ ] Add Actuator to all services.
- [ ] Expose `/actuator/health`.
- [ ] Make gateway do health-based routing.
- [ ] Commit: `feat: add actuator health endpoints`

### 11. Documentation
- [ ] `README.md`:
    - [ ] Project intro (EchoCare: music intervention for dementia)
    - [ ] Architecture (3 services + gateway + rabbit + postgres)
    - [ ] How to run locally (`mvn spring-boot:run`)
    - [ ] How to run in docker (`docker compose up`)
    - [ ] Example requests (User Story 1 & 2)
- [ ] `docs/architecture.md` or image:
    - [ ] Profile → (REST) → Playlist → (AMQP) → Feedback
- [ ] `TODO.md` (this file)
- [ ] Commit: `docs: add readme and architecture`

### 12. Reflection document (max 2 pages)
- [ ] List issues met:
    - [ ] Maven + Java version mismatch (JDK 21 vs 17)
    - [ ] Lombok constructor not generated → annotation processing
    - [ ] Spring Boot startup failed: missing datasource
    - [ ] Deciding where enums live (shared vs per service)
    - [ ] Async vs sync communication choice
    - [ ] Docker vs local H2 for rapid dev
    - [ ] Time pressure → MVP first
- [ ] Describe architectural choices:
    - [ ] Per-service DB
    - [ ] Event-driven playlist feedback
    - [ ] Gateway to hide internal topology
- [ ] Commit: `docs: add reflection draft`

---

## Final verification (Nov 6)
- [ ] `docker compose up` → all services start
- [ ] `POST /profiles` → 201
- [ ] `GET /profiles/{id}` → 200
- [ ] `POST /playlists/generate` → 200, playlist returned
- [ ] playlist service publishes event → feedback service receives
- [ ] `README.md` + reflection present in repo
- [ ] Tag release: `v1.0.0-exam`

---
**Note:** during Week 1–2 you can keep using **H2** to move fast. We switch to Postgres only after skeleton is working.
