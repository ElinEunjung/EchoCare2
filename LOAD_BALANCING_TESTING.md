# Load Balancing Testing Guide - Step by Step

## Overview

Your EchoCare2 system now has:
- ✅ 2 instances of profile-service
- ✅ 2 instances of playlist-service  
- ✅ 2 instances of feedback-service
- ✅ 1 gateway-service (load balancer)
- ✅ Consul service discovery

Let's test the load balancing!

---

## Step 1: Run the Automated Test

```powershell
cd C:\Users\ElinEunjungPark(Ext)\IdeaProjects\EchoCare2
.\test-load-balancing-detailed.ps1
```

This will:
1. ✓ Check Consul connectivity
2. ✓ Verify all services are registered
3. ✓ Send 20 test requests
4. ✓ Show instructions for verifying load distribution

---

## Step 2: Open Consul UI

While the test runs (or after), open Consul in your browser:

```powershell
Start-Process "http://localhost:8500/ui/dc1/services"
```

**What to look for:**
- ✅ `profile-service` → Click it, you should see 2 healthy instances
- ✅ `playlist-service` → Click it, you should see 2 healthy instances
- ✅ `feedback-service` → Click it, you should see 2 healthy instances
- ✅ `gateway-service` → Click it, you should see 1 healthy instance
- ✅ All should have green checkmarks (healthy)

---

## Step 3: Check Service Logs to Confirm Load Distribution

### Setup Two Terminal Windows Side-by-Side:

**Terminal 1 - Profile Service Instance 1:**
```powershell
docker-compose logs -f profile-service-1 | Select-String "GET /api"
```

**Terminal 2 - Profile Service Instance 2:**
```powershell
docker-compose logs -f profile-service-2 | Select-String "GET /api"
```

### Send Requests in a Third Terminal:

```powershell
# Send 10 requests to the gateway
1..10 | ForEach-Object {
    Write-Host "Sending request $_..."
    Invoke-WebRequest -Uri "http://localhost:8080/api/caregivers" -UseBasicParsing | Out-Null
    Start-Sleep -Milliseconds 500
}
```

**Expected Result:** 
- Terminal 1 shows some requests
- Terminal 2 shows some requests
- Requests are split between both instances (load balancing working! ✓)

---

## Step 4: Verify High Availability

### Stop One Instance and Test Failover:

```powershell
# 1. Stop profile-service-2
docker-compose stop profile-service-2

# 2. Check Consul UI - instance should turn RED (unhealthy)
Start-Process "http://localhost:8500/ui/dc1/services/profile-service"

# 3. Send requests - should STILL WORK via instance 1
1..5 | ForEach-Object {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/api/caregivers" -UseBasicParsing
    Write-Host "Request $_ - Status: $($response.StatusCode) ✓"
    Start-Sleep -Milliseconds 500
}

# 4. Restart the instance
docker-compose start profile-service-2

# 5. Wait 30 seconds and check Consul UI again
Start-Sleep -Seconds 30
Start-Process "http://localhost:8500/ui/dc1/services/profile-service"
# Instance should be GREEN again!
```

**Expected Result:**
- ✓ Requests work even with one instance down
- ✓ Requests fail over to healthy instance
- ✓ Instance automatically re-registers when restarted

---

## Step 5: Monitor Real-Time Load Balancing

Watch the gateway logs in real-time:

```powershell
docker-compose logs -f gateway-service | Select-String "uri: lb://"
```

You should see log entries showing the gateway routing through Consul's load balancer.

---

## Step 6: Test All Services Load Balancing

### Test Playlist Service Load Balancing:

```powershell
# Terminal 1
docker-compose logs -f playlist-service-1 | Select-String "GET /api"

# Terminal 2
docker-compose logs -f playlist-service-2 | Select-String "GET /api"

# Send requests
1..10 | ForEach-Object {
    Invoke-WebRequest -Uri "http://localhost:8080/api/playlists" -UseBasicParsing -ErrorAction SilentlyContinue | Out-Null
    Start-Sleep -Milliseconds 500
}
```

### Test Feedback Service Load Balancing:

```powershell
# Terminal 1
docker-compose logs -f feedback-service-1 | Select-String "GET /api"

# Terminal 2
docker-compose logs -f feedback-service-2 | Select-String "GET /api"

# Send requests
1..10 | ForEach-Object {
    Invoke-WebRequest -Uri "http://localhost:8080/api/feedback" -UseBasicParsing -ErrorAction SilentlyContinue | Out-Null
    Start-Sleep -Milliseconds 500
}
```

---

## Step 7: Query Consul API Directly

### Get all instances of a service:

```powershell
# Profile Service instances
$profileInstances = Invoke-RestMethod -Uri "http://localhost:8500/v1/health/service/profile-service?passing" -UseBasicParsing
Write-Host "Profile Service Instances:"
$profileInstances | ForEach-Object {
    Write-Host "  - $($_.Service.ID) on $($_.Service.Address):$($_.Service.Port)"
}

# Playlist Service instances
$playlistInstances = Invoke-RestMethod -Uri "http://localhost:8500/v1/health/service/playlist-service?passing" -UseBasicParsing
Write-Host "Playlist Service Instances:"
$playlistInstances | ForEach-Object {
    Write-Host "  - $($_.Service.ID) on $($_.Service.Address):$($_.Service.Port)"
}

# Feedback Service instances
$feedbackInstances = Invoke-RestMethod -Uri "http://localhost:8500/v1/health/service/feedback-service?passing" -UseBasicParsing
Write-Host "Feedback Service Instances:"
$feedbackInstances | ForEach-Object {
    Write-Host "  - $($_.Service.ID) on $($_.Service.Address):$($_.Service.Port)"
}
```

---

## Troubleshooting

### Problem: Consul UI shows services but they're RED (unhealthy)

**Check the logs:**
```powershell
docker-compose logs consul
docker-compose logs profile-service-1 | Select-String "Consul\|consul"
```

**Common causes:**
- Healthcheck endpoint returning error
- Service not fully started yet
- Port mismatch in healthcheck

### Problem: Services not registering with Consul

**Verify they have @EnableDiscoveryClient:**
```powershell
grep -r "@EnableDiscoveryClient" services/
```

**Check Consul environment variables:**
```powershell
docker-compose exec profile-service-1 env | Select-String "CONSUL"
```

### Problem: Requests not being distributed (always going to same instance)

**Check if gateway routes use lb:// prefix:**
```powershell
curl http://localhost:8888/gateway-service/docker | ConvertFrom-Json | ConvertTo-Json -Depth 5 | Select-String "lb://"
```

Should see entries like:
- `lb://profile-service`
- `lb://playlist-service`
- `lb://feedback-service`

---

## What to Present for Your Exam

### 1. Show Consul UI
- Open http://localhost:8500
- Point out all services with multiple instances
- Highlight that they're all healthy (green)

### 2. Explain the Architecture
"When a request comes to the gateway on port 8080, it uses Consul's service discovery to find available instances. For example, a request to `/api/caregivers` routes to `lb://profile-service`, which Consul load balances across both profile-service-1 and profile-service-2."

### 3. Demonstrate Load Balancing
- Run the test script
- Show logs from both instances handling requests
- Explain: "As you can see, instance-1 handled requests 1, 3, 5, etc., while instance-2 handled 2, 4, 6, etc. This is round-robin load balancing."

### 4. Demonstrate Resilience
- Stop one instance
- Show request still works
- Show Consul marks it as unhealthy
- Restart and show it re-registers

### 5. Explain Benefits
- **Scalability**: Add more instances without code changes
- **Reliability**: If one instance fails, others continue
- **Service Discovery**: Services find each other automatically
- **Load Distribution**: Requests are distributed fairly

---

## Key Files to Reference

- **docker-compose.yml**: Shows all service definitions and environment variables
- **config/application.yml**: Shared configuration including Consul settings
- **config/*-docker.yml**: Service-specific Docker configurations
- **ServiceApplication.java**: Shows @EnableDiscoveryClient annotation

---

## Success Checklist

✓ Consul UI shows all services as green  
✓ Gateway accepts requests on http://localhost:8080  
✓ Requests are distributed across instances (visible in logs)  
✓ Stopping one instance doesn't break the system  
✓ Instance automatically re-registers when restarted  
✓ All 6 service instances are discoverable via Consul API  

---

**You're ready to demonstrate load balancing for your exam!** 🚀

