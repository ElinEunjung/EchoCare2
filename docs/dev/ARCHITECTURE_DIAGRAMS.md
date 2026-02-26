# Load Balancing Architecture - Visual Guide

## 📊 Before vs After

### BEFORE (Single Instance - No Load Balancing)
```
┌─────────────────┐
│    Frontend     │
│  (React/Vue)    │
└────────┬────────┘
         │
         │ Knows 3 different URLs ❌
         │
         ├──────────────────────────┐
         │                          │
         ↓                          ↓                          ↓
    :8081/api/profiles        :8082/api/playlists       :8083/api/feedback
         │                          │                          │
         ↓                          ↓                          ↓
   ┌─────────────┐            ┌─────────────┐            ┌─────────────┐
   │  Profile    │            │  Playlist   │            │  Feedback   │
   │  Service    │            │  Service    │            │  Service    │
   │ (1 instance)│            │ (1 instance)│            │ (1 instance)│
   └─────────────┘            └─────────────┘            └─────────────┘
         │                          │                          │
         ↓                          ↓                          ↓
   ┌─────────────┐            ┌─────────────┐            ┌─────────────┐
   │ Profile DB  │            │ Playlist DB │            │ Feedback DB │
   └─────────────┘            └─────────────┘            └─────────────┘

❌ Problems:
- Frontend needs to know 3 URLs
- No redundancy (crash = downtime)
- Can't scale under load
- Doesn't meet exam requirements
```

### AFTER (Multiple Instances - With Load Balancing)
```
┌─────────────────┐
│    Frontend     │
│  (React/Vue)    │
└────────┬────────┘
         │
         │ Only knows 1 URL ✅
         │
         ↓
    :8080 (Gateway - Single Entry Point)
         │
         ↓
   ┌─────────────────────┐
   │   API Gateway       │
   │ ┌─────────────────┐ │
   │ │ Load Balancer   │ │ ← Routes & Distributes
   │ └─────────────────┘ │
   └──┬──────┬──────┬───┘
      │      │      │
      │      │      │
      ↓      ↓      ↓
 /profiles /playlists /feedback
      │      │      │
      │      │      │
      │      │      └──────┐
      │      │             │
      │      └─────┐       │
      │            │       │
      ↓            ↓       ↓
┌───────────┐ ┌───────────┐ ┌───────────┐
│ Profile-1 │ │Playlist-1 │ │Feedback-1 │
│           │ │           │ │           │
└───────────┘ └───────────┘ └───────────┘
┌───────────┐ ┌───────────┐ ┌───────────┐
│ Profile-2 │ │Playlist-2 │ │Feedback-2 │
│           │ │           │ │           │
└───────────┘ └───────────┘ └───────────┘
      │            │             │
      │            │             │
      └────┬───────┴──────┬──────┘
           │              │
           ↓              ↓
      ┌─────────┐    ┌─────────┐
      │Profile  │    │Playlist │
      │Database │    │Database │
      └─────────┘    └─────────┘

✅ Benefits:
- Frontend: ONE URL (localhost:8080)
- Automatic load distribution
- High availability (redundancy)
- Meets ALL exam requirements
```

---

## 🔄 Request Flow Example

### Scenario: User loads their profile 10 times

```
Request 1: Frontend → Gateway → profile-service-1 → Database → Response
Request 2: Frontend → Gateway → profile-service-2 → Database → Response
Request 3: Frontend → Gateway → profile-service-1 → Database → Response
Request 4: Frontend → Gateway → profile-service-2 → Database → Response
Request 5: Frontend → Gateway → profile-service-1 → Database → Response
Request 6: Frontend → Gateway → profile-service-2 → Database → Response
Request 7: Frontend → Gateway → profile-service-1 → Database → Response
Request 8: Frontend → Gateway → profile-service-2 → Database → Response
Request 9: Frontend → Gateway → profile-service-1 → Database → Response
Request 10: Frontend → Gateway → profile-service-2 → Database → Response

Result: 50% handled by instance-1, 50% by instance-2 ✅
```

---

## 🎯 How Docker DNS Load Balancing Works

### Network Alias Configuration
```yaml
profile-service-1:
  networks:
    echocare-network:
      aliases:
        - profile-service  # ← Shared DNS name

profile-service-2:
  networks:
    echocare-network:
      aliases:
        - profile-service  # ← Same DNS name
```

### DNS Resolution (Round-Robin)
```
Gateway queries: "profile-service"
   ↓
Docker DNS responds with:
   Query 1 → IP: 172.18.0.5 (profile-service-1)
   Query 2 → IP: 172.18.0.6 (profile-service-2)
   Query 3 → IP: 172.18.0.5 (profile-service-1)
   Query 4 → IP: 172.18.0.6 (profile-service-2)
   ...and so on (round-robin)
```

---

## 🛡️ High Availability Example

### Scenario: Instance 1 crashes

```
NORMAL STATE:
┌──────────────┐
│   Gateway    │
└───┬──────┬───┘
    │      │
    ↓      ↓
┌────────┐ ┌────────┐
│Service1│ │Service2│
│  ✅    │ │  ✅    │
└────────┘ └────────┘

Requests distributed 50/50


INSTANCE 1 CRASHES:
┌──────────────┐
│   Gateway    │
└───┬──────┬───┘
    │      │
    ↓      ↓
┌────────┐ ┌────────┐
│Service1│ │Service2│
│  ❌    │ │  ✅    │  ← All traffic goes here
└────────┘ └────────┘

Requests: 100% to Service2
Service still works! ✅


INSTANCE 1 RECOVERS:
┌──────────────┐
│   Gateway    │
└───┬──────┬───┘
    │      │
    ↓      ↓
┌────────┐ ┌────────┐
│Service1│ │Service2│
│  ✅    │ │  ✅    │
└────────┘ └────────┘

Requests back to 50/50 ✅
```

---

## 📝 Container Architecture

### Running Containers (10 Total)

```
┌────────────────────────────────────────────────┐
│             ECHOCARE NETWORK                   │
│                                                │
│  ┌──────────────────────────────────────────┐ │
│  │          DATABASES (3)                   │ │
│  │  • echocare-profile-db   :5435          │ │
│  │  • echocare-playlist-db  :5433          │ │
│  │  • echocare-feedback-db  :5434          │ │
│  └──────────────────────────────────────────┘ │
│                                                │
│  ┌──────────────────────────────────────────┐ │
│  │      PROFILE SERVICE (2 instances)       │ │
│  │  • profile-service-1 [profile-service]  │ │
│  │  • profile-service-2 [profile-service]  │ │
│  └──────────────────────────────────────────┘ │
│                                                │
│  ┌──────────────────────────────────────────┐ │
│  │     PLAYLIST SERVICE (2 instances)       │ │
│  │  • playlist-service-1 [playlist-service]│ │
│  │  • playlist-service-2 [playlist-service]│ │
│  └──────────────────────────────────────────┘ │
│                                                │
│  ┌──────────────────────────────────────────┐ │
│  │     FEEDBACK SERVICE (2 instances)       │ │
│  │  • feedback-service-1 [feedback-service]│ │
│  │  • feedback-service-2 [feedback-service]│ │
│  └──────────────────────────────────────────┘ │
│                                                │
│  ┌──────────────────────────────────────────┐ │
│  │         GATEWAY (1 instance)             │ │
│  │  • echocare-gateway  :8080 (EXPOSED)    │ │
│  └──────────────────────────────────────────┘ │
│                                                │
│  ┌──────────────────────────────────────────┐ │
│  │          MESSAGE BROKER                  │ │
│  │  • echocare-rabbitmq :5672, :15672      │ │
│  └──────────────────────────────────────────┘ │
│                                                │
└────────────────────────────────────────────────┘

EXTERNAL ACCESS:
  Frontend → :8080 (Gateway only) ✅
```

---

## 🔍 Traffic Distribution Visualization

### Example: 100 Requests to /api/profiles

```
100 Requests
     ↓
  Gateway
     ↓
DNS Round-Robin
     ↓
     ├─────────────────┬─────────────────┐
     │                 │                 │
     ↓                 ↓                 ↓
Profile-1         Profile-2         (Repeat)
~50 requests      ~50 requests

Each instance handles ~50% of traffic
```

### Load Distribution Over Time

```
Time →

Requests to Profile-1:  █████ █████ █████ █████ █████  (50%)
Requests to Profile-2:  █████ █████ █████ █████ █████  (50%)
                        ─────────────────────────────────
Total:                  ██████████████████████████████ (100%)

Perfect distribution! ✅
```

---

## 🎓 Exam Demonstration

### Show Examiner:

1. **Single Entry Point:**
   ```
   All requests use: http://localhost:8080
   ```

2. **Routing:**
   ```
   /api/profiles  → profile-service
   /api/playlists → playlist-service
   /api/feedback  → feedback-service
   ```

3. **Load Balancing:**
   ```bash
   # Run this command
   docker ps
   
   # Show: 2 instances per service
   profile-service-1   ✅
   profile-service-2   ✅
   
   # Send requests and show logs
   docker logs profile-service-1
   docker logs profile-service-2
   
   # Both handle requests! ✅
   ```

---

## ✅ Success Checklist

- [ ] Gateway dependency added (`spring-cloud-starter-loadbalancer`)
- [ ] docker-compose.yml updated with 2 instances per service
- [ ] Network aliases configured for DNS load balancing
- [ ] Only port 8080 exposed (gateway)
- [ ] All services start successfully
- [ ] Requests distributed across instances
- [ ] High availability tested (stop 1 instance, service continues)

---

## 🎉 You Now Have:

✅ **Unique Access Point** - Gateway at port 8080  
✅ **Routing** - Gateway routes to correct service  
✅ **Load Balancing** - Docker DNS distributes requests  
✅ **High Availability** - Redundant instances  
✅ **Scalability** - Easy to add more instances  

**Exam requirements: FULLY MET!** 🎯

