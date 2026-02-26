# Load Balancing - Quick Commands Reference

## 🚀 Start System

```bash
# Navigate to project
cd C:\Users\ElinEunjungPark(Ext)\IdeaProjects\EchoCare2

# Stop old containers
docker-compose down

# Start with load balancing (10 containers)
docker-compose up --build
```

---

## 🔍 Verify Setup

```bash
# Check all containers running
docker ps

# Expected: 10 containers
# - 3 databases
# - 6 service instances (2 per service)
# - 1 gateway
# - 1 rabbitmq
```

---

## 🧪 Test Single Entry Point

```bash
# Profile Service
curl http://localhost:8080/api/profiles

# Playlist Service
curl http://localhost:8080/api/playlists/songs

# Feedback Service
curl http://localhost:8080/api/feedback

# ✅ All go through port 8080 (gateway)
```

---

## 📊 Test Load Balancing

```bash
# Send 10 requests
for ($i=1; $i -le 10; $i++) {
    curl http://localhost:8080/api/profiles
    Write-Host "Request $i sent"
}

# Check which instances handled requests
docker logs profile-service-1 | Select-String "GET /api/profiles"
docker logs profile-service-2 | Select-String "GET /api/profiles"

# Both should show activity! ✅
```

---

## 🛡️ Test High Availability

```bash
# Stop instance 1
docker stop profile-service-1

# Service still works! (goes to instance 2)
curl http://localhost:8080/api/profiles

# Restart instance 1
docker start profile-service-1

# Traffic resumes to both instances
```

---

## 📋 View Logs

```bash
# All containers
docker-compose logs -f

# Specific service
docker logs -f profile-service-1
docker logs -f profile-service-2
docker logs -f echocare-gateway

# Last 100 lines
docker logs --tail 100 profile-service-1
```

---

## 🔧 Troubleshooting

```bash
# Check container status
docker ps -a

# Restart specific container
docker restart profile-service-1

# View container details
docker inspect profile-service-1

# Check network
docker network inspect echocare2_echocare-network

# Remove all stopped containers
docker-compose down
```

---

## 🧹 Clean Up

```bash
# Stop all containers
docker-compose down

# Remove volumes (fresh start)
docker-compose down -v

# Remove all (including networks)
docker-compose down --remove-orphans
```

---

## 📊 Monitor

```bash
# Real-time stats
docker stats

# Container resources
docker stats profile-service-1 profile-service-2

# RabbitMQ Management UI
# http://localhost:15672
# Username: guest
# Password: guest
```

---

## 🎯 Testing Endpoints

### Via Gateway (Port 8080)

```bash
# Health check
curl http://localhost:8080/actuator/health

# Profile endpoints
curl http://localhost:8080/api/profiles
curl http://localhost:8080/api/profiles/{id}

# Playlist endpoints
curl http://localhost:8080/api/playlists/songs
curl http://localhost:8080/api/playlists/generate

# Feedback endpoints
curl http://localhost:8080/api/feedback
```

### Gateway Routes Info

```bash
# See configured routes
curl http://localhost:8080/actuator/gateway/routes | jq

# Specific route
curl http://localhost:8080/actuator/gateway/routes/profile-route | jq
```

---

## 🔄 Reload Configuration

```bash
# Rebuild specific service
docker-compose up -d --build profile-service-1

# Rebuild all services
docker-compose up -d --build

# No downtime rebuild
docker-compose up -d --no-deps --build profile-service-1
```

---

## 📈 Scale Services

```bash
# Add more instances in docker-compose.yml
# Then:
docker-compose up -d --build

# Or use Docker Compose scale (if not using container_name)
# docker-compose up -d --scale profile-service=3
```

---

## ✅ Success Verification

```bash
# 1. All containers running
docker ps | findstr "profile-service\|playlist-service\|feedback-service\|gateway"

# Expected: 7 containers (6 services + 1 gateway)

# 2. Gateway accessible
curl http://localhost:8080/actuator/health

# Expected: {"status":"UP"}

# 3. Load distribution
docker logs profile-service-1 --tail 50 | findstr "profiles"
docker logs profile-service-2 --tail 50 | findstr "profiles"

# Expected: Both show requests
```

---

## 🎓 Demo Commands (For Examiner)

```bash
# 1. Show architecture
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

# 2. Show single entry point
curl http://localhost:8080/api/profiles

# 3. Show load balancing
for ($i=1; $i -le 5; $i++) { 
    curl http://localhost:8080/api/profiles
}

# 4. Check distribution
docker logs profile-service-1 --tail 10
docker logs profile-service-2 --tail 10

# 5. Show high availability
docker stop profile-service-1
curl http://localhost:8080/api/profiles  # Still works!
docker start profile-service-1
```

---

## 🆘 Common Issues

### Port already in use
```bash
# Find what's using port 8080
netstat -ano | findstr :8080

# Kill the process (replace PID)
taskkill /PID <PID> /F
```

### Container won't start
```bash
# Check logs
docker logs profile-service-1

# Remove and rebuild
docker-compose rm -f profile-service-1
docker-compose up -d --build profile-service-1
```

### Database connection error
```bash
# Restart database
docker restart echocare-profile-db

# Wait for healthy
docker ps | findstr "profile-db"
```

---

## 📱 Frontend Integration

Update your frontend to use gateway:

```javascript
// Before
const API_URL = 'http://localhost:8081';

// After
const API_URL = 'http://localhost:8080';

// All endpoints stay the same!
fetch(`${API_URL}/api/profiles`)
```

---

## ✅ Quick Checklist

- [ ] docker-compose.yml updated with 2 instances per service
- [ ] `docker-compose up --build` successful
- [ ] 10 containers running
- [ ] Gateway accessible at `:8080`
- [ ] Services respond through gateway
- [ ] Load distributed across instances
- [ ] High availability tested

---

**Your load balancing is now fully implemented and ready!** 🎉

