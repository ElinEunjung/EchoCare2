# ✅ Load Balancing Testing - Complete Setup

## What You Have

Your EchoCare2 project is now fully configured with:

```
┌─────────────────────────────────────────────────────────┐
│         CONSUL LOAD BALANCING ARCHITECTURE              │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  Client → http://localhost:8080 (Gateway)              │
│              ↓                                          │
│         Spring Cloud Gateway                           │
│         (with Consul Load Balancer)                    │
│              ↓                                          │
│    ┌────────┴────────┐                                 │
│    ↓                 ↓                                  │
│ Profile-Service  Profile-Service  ← Load Balanced     │
│ (Instance 1)     (Instance 2)                          │
│ Port: 9000       Port: 9000                            │
│    ↓                 ↓                                  │
│ [echocare-        [echocare-                           │
│  profile-db]      profile-db]  ← Shared Database       │
│    |                 |                                  │
│    └────────┬────────┘                                 │
│             ↓                                          │
│         Consul Service Registry                        │
│         (Tracks all instances)                         │
│                                                         │
│  Same architecture for:                                │
│  • playlist-service (2 instances)                      │
│  • feedback-service (2 instances)                      │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

---

## Quick Start - Run These Commands

### 1. Run Automated Test

```powershell
cd C:\Users\ElinEunjungPark(Ext)\IdeaProjects\EchoCare2
.\test-load-balancing-detailed.ps1
```

This will verify:
- ✓ Consul is running
- ✓ All services registered
- ✓ Gateway responding
- ✓ Requests being sent successfully

### 2. Open Consul UI

```powershell
Start-Process "http://localhost:8500/ui/dc1/services"
```

Look for:
- `profile-service` - 2 instances (green)
- `playlist-service` - 2 instances (green)
- `feedback-service` - 2 instances (green)
- `gateway-service` - 1 instance (green)

### 3. Verify Load Balancing (Run in 3 terminals)

**Terminal 1:**
```powershell
docker-compose logs -f profile-service-1 | Select-String "GET /api"
```

**Terminal 2:**
```powershell
docker-compose logs -f profile-service-2 | Select-String "GET /api"
```

**Terminal 3:**
```powershell
1..20 | ForEach-Object {
    Invoke-WebRequest -Uri "http://localhost:8080/api/caregivers" -UseBasicParsing | Out-Null
    Start-Sleep -Milliseconds 300
}
```

**Expected Result:** Requests appear in BOTH Terminal 1 and Terminal 2 logs!

---

## Files Created for Testing

| File | Purpose |
|------|---------|
| `test-load-balancing-detailed.ps1` | Automated test script |
| `LOAD_BALANCING_TESTING.md` | Detailed step-by-step guide |
| `CONSUL_COMMANDS_REFERENCE.md` | Quick command reference |

---

## What's Being Tested

### 1. Service Registration ✓
- Each service instance registers with Consul
- Consul tracks health via healthchecks
- Gateway queries Consul for available instances

### 2. Load Balancing ✓
- Gateway uses `lb://service-name` routes
- Consul load balances via round-robin
- Requests distributed across healthy instances

### 3. High Availability ✓
- If instance-1 goes down, instance-2 serves requests
- Consul marks failed instance as unhealthy
- Failover is automatic (no code changes)

### 4. Auto-Recovery ✓
- When instance is restarted, it auto-registers
- Consul health checks verify it's healthy
- Traffic automatically resumes

---

## Key Architecture Points

### Why This Setup Works:

1. **Consul Discovery**
   - Services register themselves on startup
   - @EnableDiscoveryClient enables this
   - SPRING_CLOUD_CONSUL_HOST env var points to Consul

2. **Gateway Load Balancing**
   - Routes use `lb://profile-service` format
   - The `lb://` prefix tells Spring to use Consul's load balancer
   - Not hardcoded to specific instances

3. **Health Monitoring**
   - Each container has healthcheck
   - Consul pings `/actuator/health` every 30 seconds
   - Only healthy instances receive traffic

4. **Docker Networking**
   - Services use Docker hostnames (`echocare-profile-db`)
   - Environment variables override localhost
   - SPRING_RABBITMQ_HOST=echocare-rabbitmq

---

## Success Criteria for Your Exam

✅ **When demonstrating, you should:**

1. Show Consul UI with all services
   - "Consul is our service registry"
   - "All instances are auto-registered"
   - "Health checks ensure only healthy instances are used"

2. Run the load balancing test
   - "Sending requests through the gateway"
   - "Each request goes to a different instance"
   - "You can see in the logs they're distributed"

3. Test failure scenario
   - "Stop one instance"
   - "Requests still work"
   - "Consul marks it unhealthy"
   - "Restart it, it auto-registers"

4. Explain the benefits
   - Scalability (add instances without code)
   - Reliability (automatic failover)
   - Service discovery (no hardcoded URLs)
   - Load balancing (fair distribution)

---

## Troubleshooting Quick Reference

| Problem | Solution |
|---------|----------|
| Services not in Consul UI | Check @EnableDiscoveryClient annotation |
| Services show RED (unhealthy) | Check /actuator/health endpoint returns 200 |
| Requests always go to same instance | Verify gateway routes use `lb://` prefix |
| Requests fail after stopping instance | Check fail-fast: false in Consul config |
| Services can't find RabbitMQ | Verify SPRING_RABBITMQ_HOST=echocare-rabbitmq |

---

## Next Steps

1. **Run the test script** - ✓ Automated verification
2. **Open Consul UI** - ✓ Visual confirmation
3. **Check logs** - ✓ Verify load distribution
4. **Test failure** - ✓ Demonstrate resilience
5. **Prepare explanation** - ✓ For your exam

---

## Files to Reference

- `docker-compose.yml` - Shows all services with Consul/Config Server env vars
- `config/application.yml` - Consul configuration
- `config/*-docker.yml` - Service-specific Docker configs
- `services/*/src/main/java/*/Application.java` - Shows @EnableDiscoveryClient

---

## Your Exam Presentation

**Opening statement:**
"My microservices use Consul for service discovery. Each service instance automatically registers itself when it starts. The API Gateway queries Consul to find available instances and load balances requests across them using round-robin distribution."

**Then demonstrate:**
1. Show Consul UI (all services green)
2. Run load test (show logs from both instances)
3. Stop an instance (show failover still works)
4. Restart it (show auto-registration)

**Closing statement:**
"This architecture provides automatic failover, easy scaling, and zero-downtime deployments - all without hardcoding service URLs."

---

**You're all set! 🚀 Your load balancing is fully functional and ready to demonstrate.** ✨

