# Load Balancing Architecture Overview

## System Architecture

```
┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
┃                      CLIENT REQUEST                            ┃
┃                                                                ┃
┃              http://localhost:8080/api/caregivers              ┃
┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┳━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
                               │
                               ▼
         ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
         ┃    API GATEWAY SERVICE       ┃
         ┃    (Spring Cloud Gateway)    ┃
         ┃    Port: 8080                ┃
         ┃                              ┃
         ┃  Routes:                     ┃
         ┃  /api/caregivers → lb://ps  ┃
         ┃  /api/profiles → lb://ps    ┃
         ┃  /api/playlists → lb://pl   ┃
         ┃  /api/feedback → lb://fb    ┃
         ┗━━━━━━━━━━┳━━━━━━━━━━━━━━━━━┛
                    │
                    ▼
         ┏━━━━━━━━━━━━━━━━━━━━━━━━━┓
         ┃   CONSUL LOAD BALANCER   ┃
         ┃   (Service Discovery)    ┃
         ┃   Port: 8500             ┃
         ┃                          ┃
         ┃   Query: lb://ps?health  ┃
         ┃   Returns: 2 instances   ┃
         ┗━━━━━━━━━┬━━━━━━━━━━━━━━┛
                   │
        ┌──────────┼──────────┐
        │          │          │
        ▼          ▼          ▼
   Instance 1  Instance 2  Instance 3
   Healthy ✓  Healthy ✓   Down ✗
   
   (Only healthy instances get traffic)
```

---

## Request Flow Example

```
1. CLIENT sends request
   GET http://localhost:8080/api/caregivers
                            │
                            ▼
2. GATEWAY receives request
   • Matches route: /api/caregivers → lb://profile-service
   • Asks Consul: "Give me healthy profile-service instances"
                            │
                            ▼
3. CONSUL responds
   [
     { id: "profile-service-profile-1", address: "172.18.0.5", port: 9000, status: "passing" },
     { id: "profile-service-profile-2", address: "172.18.0.6", port: 9000, status: "passing" }
   ]
                            │
                            ▼
4. GATEWAY load balances
   • Round-robin: Request #1 → Instance 1
   • Round-robin: Request #2 → Instance 2
   • Round-robin: Request #3 → Instance 1
   • Round-robin: Request #4 → Instance 2
                            │
                            ▼
5. SERVICE processes request
   Instance 1 (or Instance 2) handles the request
   Connects to database
   Returns response
                            │
                            ▼
6. RESPONSE returns to client
   HTTP 200 OK + data
```

---

## Configuration Files Overview

### docker-compose.yml (Service Definitions)

```yaml
profile-service-1:
  environment:
    SPRING_PROFILES_ACTIVE: docker
    SPRING_CLOUD_CONSUL_HOST: consul          # ← Points to Consul
    SPRING_CLOUD_CONSUL_PORT: 8500
    SPRING_DATASOURCE_URL: jdbc:postgresql://echocare-profile-db:5432/profile_db
    SPRING_RABBITMQ_HOST: echocare-rabbitmq   # ← Docker hostname
  depends_on:
    consul:
      condition: service_healthy              # ← Wait for Consul
```

### config/application.yml (Shared Config)

```yaml
spring:
  cloud:
    consul:
      host: consul                   # ← Consul server
      port: 8500
      discovery:
        enabled: true
        fail-fast: false            # ← Don't fail if Consul down
        health-check-interval: 10s
        instance-id: ${spring.application.name}-${INSTANCE_ID}
```

### config/gateway-service-docker.yml (Gateway Config)

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: profile-route
          uri: lb://profile-service  # ← Load-balanced route!
          predicates:
            - Path=/api/profiles/**
```

---

## Service Registration Process

```
┌─ SERVICE STARTS ─────────────────────────────────┐
│                                                   │
│  1. Spring Boot loads @EnableDiscoveryClient     │
│  2. Reads SPRING_CLOUD_CONSUL_HOST=consul       │
│  3. Reads SPRING_CLOUD_CONSUL_PORT=8500         │
│  4. Connects to Consul at http://consul:8500    │
│  5. Registers: service-name-instance-id         │
│  6. Starts health check (every 10s)             │
│                                                   │
└──────────────────┬──────────────────────────────┘
                   │
                   ▼
        ┌─ CONSUL RECEIVES ─┐
        │                   │
        │  Service: name    │
        │  ID: instance-1   │
        │  Address: 172...  │
        │  Port: 9000       │
        │  Status: passing  │
        │                   │
        └───────────────────┘
                   │
                   ▼
        ┌─ CONSUL STORES ─┐
        │                 │
        │  catalog/       │
        │  └─ profile-s   │
        │     ├─ inst-1   │
        │     └─ inst-2   │
        │                 │
        └─────────────────┘
```

---

## Health Check Cycle

```
Every 30 seconds:

┌─ CONSUL ─────────────┐
│                      │
│  curl -f             │
│  http://localhost:9000/actuator/health
│                      │
└────────┬─────────────┘
         │
         ▼
┌─ SERVICE ─────────┐
│                   │
│  Checks:          │
│  • Is alive?      │
│  • DB connected?  │
│  • RabbitMQ ok?   │
│                   │
│  Returns 200 OK   │
│  {status: "UP"}   │
└─────┬─────────────┘
      │
      ▼
┌─ CONSUL UPDATES ──────┐
│                       │
│  Status: passing ✓    │
│  Last check: now      │
│                       │
│  If 3 checks fail:    │
│  Status: critical ✗   │
│  Remove from LB       │
│                       │
└───────────────────────┘
```

---

## Load Balancing Algorithm

### Round-Robin Distribution

```
Request Sequence:

Request 1 ──→ [lb://profile-service] ──→ Profile-Service-1 ✓
Request 2 ──→ [lb://profile-service] ──→ Profile-Service-2 ✓
Request 3 ──→ [lb://profile-service] ──→ Profile-Service-1 ✓
Request 4 ──→ [lb://profile-service] ──→ Profile-Service-2 ✓
Request 5 ──→ [lb://profile-service] ──→ Profile-Service-1 ✓
Request 6 ──→ [lb://profile-service] ──→ Profile-Service-2 ✓

Pattern: 1, 2, 1, 2, 1, 2... (equal distribution)

If Instance-2 goes down:

Request 7 ──→ [lb://profile-service] ──→ Profile-Service-1 ✓
Request 8 ──→ [lb://profile-service] ──→ Profile-Service-1 ✓
Request 9 ──→ [lb://profile-service] ──→ Profile-Service-1 ✓
   (All traffic to Instance-1 until Instance-2 recovers)
```

---

## Failover Scenario

```
NORMAL STATE:
┌─────────────────────────────────────┐
│  Profile-Service-1 ✓ HEALTHY        │
│  Profile-Service-2 ✓ HEALTHY        │
└─────────────────────────────────────┘
       Gateway routes to both


INSTANCE-2 FAILS:
Step 1: Health check fails
   Consul healthcheck: GET /actuator/health → 500 ERROR
   Consul status: critical ✗

Step 2: Consul removes from load balancer
   Available instances: [Profile-Service-1]
   
Step 3: Gateway adjusts routing
   All new requests → Profile-Service-1 ONLY
   
Step 4: No requests are lost!
   Users see no interruption


INSTANCE-2 RECOVERS:
Step 1: Service restarts
   Consul healthcheck: GET /actuator/health → 200 OK
   
Step 2: Consul marks as healthy
   Consul status: passing ✓
   Available instances: [Profile-Service-1, Profile-Service-2]
   
Step 3: Gateway resumes load balancing
   Request distribution: 1, 2, 1, 2, ...
   
Step 4: Zero-downtime recovery!
   Users see no interruption
```

---

## Ports Reference

| Service | Internal Port | Host Port | Purpose |
|---------|---------------|-----------|---------|
| Gateway | 8080 | 8080 | API entry point |
| Profile Service | 9000 | - | (not exposed) |
| Playlist Service | 8082 | - | (not exposed) |
| Feedback Service | 8083 | - | (not exposed) |
| Consul UI/API | 8500 | 8500 | Service registry |
| RabbitMQ AMQP | 5672 | 5672 | Message broker |
| RabbitMQ UI | 15672 | 15672 | Management console |
| Profile DB | 5432 | 5435 | Database (internal) |
| Playlist DB | 5432 | 5433 | Database (internal) |
| Feedback DB | 5432 | 5434 | Database (internal) |

---

## Key Differences: With vs Without Consul

### WITHOUT Consul (Before):
```
Gateway hardcoded routes:
  /api/profiles → http://profile-service:9000  (always one instance)
  
Problems:
  ✗ Hardcoded hostnames
  ✗ No load balancing
  ✗ Manual service discovery
  ✗ Difficult to scale
```

### WITH Consul (Now):
```
Gateway dynamic routes:
  /api/profiles → lb://profile-service  (any healthy instance)
  
Benefits:
  ✓ Automatic service discovery
  ✓ Load balancing across instances
  ✓ Health-based routing
  ✓ Easy to scale (just add more instances)
  ✓ Automatic failover
```

---

## Testing & Verification

### What to Check

1. **Consul Registration**
   ```
   http://localhost:8500/ui/dc1/services
   Should show: profile-service (2 instances, both green)
   ```

2. **Request Distribution**
   ```
   Send 10 requests to: http://localhost:8080/api/caregivers
   Check logs of both instances
   Each should handle ~5 requests
   ```

3. **Failover**
   ```
   Stop one instance
   Send 5 more requests
   Should work (go to remaining instance)
   Check Consul: stopped instance marked RED
   ```

4. **Recovery**
   ```
   Restart the instance
   Wait 30 seconds
   Check Consul: instance marked GREEN again
   New requests distributed between both
   ```

---

**This is a production-grade microservices architecture!** 🚀

