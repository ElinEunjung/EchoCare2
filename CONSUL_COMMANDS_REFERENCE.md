# Consul Load Balancing - Quick Reference Commands

## Consul UI & API

```powershell
# Open Consul UI
Start-Process "http://localhost:8500/ui/dc1/services"

# List all services
Invoke-RestMethod -Uri "http://localhost:8500/v1/catalog/services" -UseBasicParsing | ConvertTo-Json

# Check specific service (profile-service)
Invoke-RestMethod -Uri "http://localhost:8500/v1/health/service/profile-service" -UseBasicParsing | ConvertTo-Json -Depth 3

# Check only healthy instances
Invoke-RestMethod -Uri "http://localhost:8500/v1/health/service/profile-service?passing" -UseBasicParsing
```

---

## Real-Time Load Balancing Tests

### Watch Requests Being Distributed

**Terminal 1 - Instance 1:**
```powershell
docker-compose logs -f profile-service-1
```

**Terminal 2 - Instance 2:**
```powershell
docker-compose logs -f profile-service-2
```

**Terminal 3 - Send Requests:**
```powershell
1..20 | ForEach-Object {
    Write-Host "Request $_"
    Invoke-WebRequest -Uri "http://localhost:8080/api/caregivers" -UseBasicParsing | Out-Null
    Start-Sleep -Milliseconds 300
}
```

---

## High Availability Tests

### Stop One Instance

```powershell
docker-compose stop profile-service-2
```

### Verify Requests Still Work

```powershell
1..5 | ForEach-Object {
    Invoke-WebRequest -Uri "http://localhost:8080/api/caregivers" -UseBasicParsing
    Write-Host "Request $_ OK"
    Start-Sleep -Milliseconds 500
}
```

### Restart Instance

```powershell
docker-compose start profile-service-2
```

### Verify Re-Registration

```powershell
Start-Sleep -Seconds 30
Start-Process "http://localhost:8500/ui/dc1/services/profile-service"
# Should show all instances green
```

---

## Monitor Gateway Routing

```powershell
# Watch gateway logs in real-time
docker-compose logs -f gateway-service | Select-String "uri\|LoadBalancer\|lb://"
```

---

## Service Health Checks

### Get Health of All Services

```powershell
$services = @("profile-service", "playlist-service", "feedback-service", "gateway-service")
$services | ForEach-Object {
    $instances = Invoke-RestMethod -Uri "http://localhost:8500/v1/health/service/$_" -UseBasicParsing
    Write-Host "$_: $($instances.Count) instances"
    $instances | ForEach-Object {
        Write-Host "  - $($_.Service.ID): $($_.Status)"
    }
}
```

---

## Quick Diagnostics

### Is Config Server Healthy?

```powershell
Invoke-WebRequest -Uri "http://localhost:8888/actuator/health" -UseBasicParsing
```

### Is Gateway Healthy?

```powershell
Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -UseBasicParsing
```

### Check Service Registration Details

```powershell
# Profile Service instance-1
Invoke-RestMethod -Uri "http://localhost:8500/v1/catalog/service/profile-service" -UseBasicParsing | 
    ForEach-Object { Write-Host "$($_.ServiceID) on $($_.ServiceAddress):$($_.ServicePort)" }
```

---

## Stress Testing (Advanced)

### Send 100 Requests Rapidly

```powershell
$stopwatch = [System.Diagnostics.Stopwatch]::StartNew()
1..100 | ForEach-Object {
    try {
        Invoke-WebRequest -Uri "http://localhost:8080/api/caregivers" -UseBasicParsing -TimeoutSec 5 | Out-Null
        Write-Host "." -NoNewline -ForegroundColor Green
    } catch {
        Write-Host "F" -NoNewline -ForegroundColor Red
    }
    if ($_ % 20 -eq 0) { Write-Host " ($_)" }
}
$stopwatch.Stop()
Write-Host "`nCompleted in $($stopwatch.ElapsedMilliseconds)ms"
```

---

## Container Management

### View All Running Containers

```powershell
docker-compose ps
```

### View Specific Service Logs

```powershell
# Last 50 lines
docker-compose logs --tail=50 profile-service-1

# Follow logs in real-time
docker-compose logs -f profile-service-1

# With timestamp
docker-compose logs -f --timestamps profile-service-1
```

### Check Resource Usage

```powershell
docker stats --no-stream
```

---

## Database Verification

### Connect to Profile Database

```powershell
# From host machine
psql -h localhost -p 5435 -U profile_user -d profile_db

# Count profiles
SELECT COUNT(*) FROM patient_profile;
```

### Check Database Status

```powershell
docker-compose exec profile-db pg_isready -U profile_user -d profile_db
docker-compose exec playlist-db pg_isready -U playlist_user -d playlist_db
docker-compose exec feedback-db pg_isready -U feedback_user -d feedback_db
```

---

## RabbitMQ Verification

### Open RabbitMQ Management

```powershell
Start-Process "http://localhost:15672"
# Username: guest
# Password: guest
```

### Check RabbitMQ Health

```powershell
docker-compose exec rabbitmq rabbitmq-diagnostics ping
```

---

## Clean Up & Reset

### Stop All Services

```powershell
docker-compose down
```

### Stop with Volume Removal (Clean Reset)

```powershell
docker-compose down -v
```

### Rebuild All Images

```powershell
docker-compose build --no-cache
```

### Full Reset & Start

```powershell
docker-compose down -v
docker-compose build --no-cache
docker-compose up -d
```

---

## Key Indicators of Successful Load Balancing

✅ **Consul UI shows:**
- All services registered
- Multiple instances per service
- Green status (healthy)

✅ **Requests distributed:**
- Logs from instance-1 show some requests
- Logs from instance-2 show some requests
- Round-robin pattern visible

✅ **Resilience works:**
- Requests continue when one instance is down
- Failed instance is marked unhealthy
- Instance auto-registers when restarted

✅ **Gateway routing:**
- Uses `lb://service-name` format
- No hardcoded localhost URLs
- Routes through Consul load balancer

---

## For Your Exam

**Be ready to:**
1. Navigate Consul UI and explain what you see
2. Show logs proving load distribution
3. Explain the architecture (services → gateway → Consul LB → instances)
4. Demonstrate resilience (stop instance, show failover)
5. Answer: "How does the gateway know about the services?" (Consul!)

**Key phrases:**
- "Service discovery via Consul"
- "Client-side load balancing"
- "Health-based routing"
- "Automatic failover"
- "Zero-downtime deployment"

