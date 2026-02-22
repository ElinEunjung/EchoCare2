# =============================================================================
# EchoCare Diagnostic Script - Troubleshoot Async E2E Test
# =============================================================================
# Purpose: Diagnose why the async E2E test shows success but playlists are empty
# =============================================================================

param(
    [string]$ProfileServiceUrl = "http://localhost:8081",
    [string]$PlaylistServiceUrl = "http://localhost:8082",
    [string]$RabbitMQUrl = "http://localhost:15672",
    [string]$ProfileId = ""  # Optional: specific profile ID to check
)

# Colors for output
function Write-Success { Write-Host $args -ForegroundColor Green }
function Write-Error { Write-Host $args -ForegroundColor Red }
function Write-Info { Write-Host $args -ForegroundColor Cyan }
function Write-Warning { Write-Host $args -ForegroundColor Yellow }
function Write-Header {
    Write-Host ""
    Write-Host "=============================================" -ForegroundColor Magenta
    Write-Host $args -ForegroundColor Magenta
    Write-Host "=============================================" -ForegroundColor Magenta
    Write-Host ""
}

Write-Header "EchoCare Async E2E Diagnostic Tool"

# =============================================================================
# Step 1: Check if services are running
# =============================================================================
Write-Header "Step 1: Checking Service Availability"

$servicesOk = $true

# Check Profile Service
Write-Info "Checking Profile Service at $ProfileServiceUrl..."
try {
    $profileHealth = Invoke-RestMethod -Uri "$ProfileServiceUrl/api/profiles/health" -Method GET -ErrorAction Stop
    Write-Success "[OK] Profile Service is running: $profileHealth"
}
catch {
    Write-Error "[ERROR] Profile Service is NOT accessible!"
    Write-Host "   Error: $($_.Exception.Message)"
    $servicesOk = $false
}

# Check Playlist Service
Write-Info "Checking Playlist Service at $PlaylistServiceUrl..."
try {
    $playlistHealth = Invoke-RestMethod -Uri "$PlaylistServiceUrl/api/playlists/health" -Method GET -ErrorAction Stop
    Write-Success "[OK] Playlist Service is running: $playlistHealth"
}
catch {
    Write-Error "[ERROR] Playlist Service is NOT accessible!"
    Write-Host "   Error: $($_.Exception.Message)"
    $servicesOk = $false
}

# Check RabbitMQ
Write-Info "Checking RabbitMQ at $RabbitMQUrl..."
try {
    $rabbitmqHealth = Invoke-WebRequest -Uri $RabbitMQUrl -Method GET -ErrorAction Stop
    Write-Success "[OK] RabbitMQ Management UI is accessible"
}
catch {
    Write-Error "[ERROR] RabbitMQ is NOT accessible!"
    Write-Host "   Error: $($_.Exception.Message)"
    $servicesOk = $false
}

if (-not $servicesOk) {
    Write-Header "SERVICES NOT RUNNING - START GUIDE"
    Write-Info "To start the infrastructure:"
    Write-Host "   docker-compose up -d"
    Write-Host ""
    Write-Info "To start Profile Service (Terminal 1):"
    Write-Host "   cd services/profile-service"
    Write-Host "   ./mvnw spring-boot:run"
    Write-Host ""
    Write-Info "To start Playlist Service (Terminal 2):"
    Write-Host "   cd services/playlist-service"
    Write-Host "   ./mvnw spring-boot:run"
    Write-Host ""
    exit 1
}

# =============================================================================
# Step 2: Check network connectivity
# =============================================================================
Write-Header "Step 2: Checking Network Ports"

Write-Info "Checking listening ports..."
$ports = Get-NetTCPConnection | Where-Object {$_.LocalPort -in 8081,8082,5672,15672} | Select-Object LocalPort, State -Unique

if ($ports) {
    foreach ($port in $ports) {
        Write-Success "[OK] Port $($port.LocalPort) is $($port.State)"
    }
} else {
    Write-Warning "[WARNING] No services found on expected ports (8081, 8082, 5672, 15672)"
}

# =============================================================================
# Step 3: Test database connectivity
# =============================================================================
Write-Header "Step 3: Checking Database Connectivity"

Write-Info "Checking Docker containers..."
try {
    $containers = docker ps --format "table {{.Names}}\t{{.Status}}" | Out-String
    if ($containers -match "echocare") {
        Write-Success "[OK] Docker containers are running:"
        Write-Host $containers
    } else {
        Write-Warning "[WARNING] No EchoCare containers found"
        Write-Host "   Run: docker-compose up -d"
    }
}
catch {
    Write-Warning "[WARNING] Docker command not available or Docker not running"
    Write-Host "   Error: $($_.Exception.Message)"
}

# =============================================================================
# Step 4: Test profile creation flow
# =============================================================================
Write-Header "Step 4: Testing Profile Creation Flow"

$timestamp = [DateTimeOffset]::UtcNow.ToUnixTimeSeconds()
$testUsername = "diaguser_$timestamp"
$testEmail = "diaguser_$timestamp@test.com"
$testPassword = "testpass123"

Write-Info "Registering test caregiver..."
Write-Host "   Username: $testUsername"

$registerBody = @{
    username = $testUsername
    password = $testPassword
    email    = $testEmail
} | ConvertTo-Json

try {
    $registerResponse = Invoke-RestMethod -Uri "$ProfileServiceUrl/api/auth/register" -Method POST -ContentType "application/json" -Body $registerBody -ErrorAction Stop
    Write-Success "[OK] Registration successful"
    Write-Host "   Caregiver ID: $($registerResponse.caregiverId)"
    Write-Host "   Token: $($registerResponse.token.Substring(0, 30))..."

    $token = $registerResponse.token
    $caregiverId = $registerResponse.caregiverId
}
catch {
    Write-Error "[ERROR] Registration failed!"
    Write-Host "   Error: $($_.Exception.Message)"
    if ($_.ErrorDetails) {
        Write-Host "   Details: $($_.ErrorDetails.Message)"
    }
    exit 1
}

# Create patient profile
Write-Info "Creating patient profile..."

$profileBody = @{
    patientName     = "Diagnostic Test Patient $timestamp"
    era            = "1960-1970"
    dementiaStage  = "mild"
    favoriteArtists = @("The Beatles", "Elvis Presley")
    symptoms       = @("memory_loss", "anxiety")
} | ConvertTo-Json

$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type"  = "application/json"
}

try {
    $profileResponse = Invoke-RestMethod -Uri "$ProfileServiceUrl/api/profiles" -Method POST -Headers $headers -Body $profileBody -ErrorAction Stop
    Write-Success "[OK] Profile created successfully"
    Write-Host "   Profile ID: $($profileResponse.id)"
    Write-Host "   Patient: $($profileResponse.patientName)"
    Write-Host "   Era: $($profileResponse.era)"

    $profileId = $profileResponse.id
}
catch {
    Write-Error "[ERROR] Profile creation failed!"
    Write-Host "   Error: $($_.Exception.Message)"
    if ($_.ErrorDetails) {
        Write-Host "   Details: $($_.ErrorDetails.Message)"
    }
    exit 1
}

# =============================================================================
# Step 5: Wait and verify RabbitMQ event processing
# =============================================================================
Write-Header "Step 5: Verifying Async Event Processing"

Write-Info "Waiting for RabbitMQ event to be consumed..."
Write-Host "   Waiting 5 seconds for async processing..."

Start-Sleep -Seconds 5

Write-Success "[OK] Wait completed"

# Try to verify via RabbitMQ Management API
Write-Info "Checking RabbitMQ queues..."
try {
    $credentials = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("guest:guest"))
    $rabbitHeaders = @{
        Authorization = "Basic $credentials"
    }

    $queues = Invoke-RestMethod -Uri "http://localhost:15672/api/queues" -Headers $rabbitHeaders -Method GET -ErrorAction Stop

    $profileQueues = $queues | Where-Object { $_.name -like "*profile*" }

    if ($profileQueues) {
        Write-Success "[OK] Found profile-related queues:"
        foreach ($queue in $profileQueues) {
            Write-Host "   - $($queue.name): $($queue.messages) messages, $($queue.messages_ready) ready"
        }
    } else {
        Write-Warning "[WARNING] No profile-related queues found"
    }
}
catch {
    Write-Warning "[WARNING] Could not check RabbitMQ queues"
    Write-Host "   Error: $($_.Exception.Message)"
}

# =============================================================================
# Step 6: Generate playlist
# =============================================================================
Write-Header "Step 6: Testing Playlist Generation"

Write-Info "Generating playlist for profile ID: $profileId"

$playlistBody = @{
    patientId       = $profileId
    careNeed        = "stress_relief"
    era             = "dummy"
    dementiaStage   = "mild"
    favoriteArtists = @("dummy")
} | ConvertTo-Json

try {
    $playlistResponse = Invoke-RestMethod -Uri "$PlaylistServiceUrl/api/playlists/generate" -Method POST -Headers $headers -Body $playlistBody -ErrorAction Stop
    Write-Success "[OK] Playlist generated successfully"
    Write-Host "   Playlist ID: $($playlistResponse.playlistId)"
    Write-Host "   Care Need: $($playlistResponse.careNeed)"
    Write-Host "   Era: $($playlistResponse.era)"
    Write-Host "   Tracks: $($playlistResponse.tracks.Count)"

    if ($playlistResponse.tracks.Count -gt 0) {
        Write-Host "   Sample tracks:"
        $sampleCount = [Math]::Min(3, $playlistResponse.tracks.Count)
        for ($i = 0; $i -lt $sampleCount; $i++) {
            $track = $playlistResponse.tracks[$i]
            Write-Host "      - $($track.title) by $($track.artist)"
        }
    }
}
catch {
    Write-Error "[ERROR] Playlist generation failed!"
    Write-Host "   Error: $($_.Exception.Message)"
    if ($_.ErrorDetails) {
        Write-Host "   Details: $($_.ErrorDetails.Message)"
    }
    Write-Warning "   This might mean:"
    Write-Host "   - Song database is empty"
    Write-Host "   - Profile cache is not populated"
    Write-Host "   - RabbitMQ event was not consumed"
}

# =============================================================================
# Step 7: Verify playlists are stored
# =============================================================================
Write-Header "Step 7: Verifying Playlist Persistence"

Write-Info "Fetching playlists for profile ID: $profileId"

try {
    $playlists = Invoke-RestMethod -Uri "$PlaylistServiceUrl/api/playlists/profile/$profileId" -Method GET -Headers $headers -ErrorAction Stop

    if ($playlists.Count -gt 0) {
        Write-Success "[OK] Found $($playlists.Count) playlist(s) for profile"

        foreach ($pl in $playlists) {
            Write-Host "   - Playlist ID: $($pl.playlistId)"
            Write-Host "     Care Need: $($pl.careNeed)"
            Write-Host "     Era: $($pl.era)"
            Write-Host "     Tracks: $($pl.tracks.Count)"
        }
    } else {
        Write-Warning "[WARNING] No playlists found for profile ID: $profileId"
        Write-Host ""
        Write-Host "   This means the playlist was NOT saved to the database!"
        Write-Host ""
        Write-Host "   Possible causes:"
        Write-Host "   1. Database connection issue"
        Write-Host "   2. Transaction rollback"
        Write-Host "   3. Playlist generation threw an exception"
        Write-Host ""
        Write-Host "   Check the playlist-service logs for errors"
    }
}
catch {
    Write-Error "[ERROR] Failed to fetch playlists!"
    Write-Host "   Error: $($_.Exception.Message)"
    if ($_.ErrorDetails) {
        Write-Host "   Details: $($_.ErrorDetails.Message)"
    }
}

# =============================================================================
# Step 8: Test with existing profile ID if provided
# =============================================================================
if ($ProfileId) {
    Write-Header "Step 8: Testing with Provided Profile ID"

    Write-Info "Checking profile: $ProfileId"

    try {
        $existingProfile = Invoke-RestMethod -Uri "$ProfileServiceUrl/api/profiles/$ProfileId" -Method GET -ErrorAction Stop
        Write-Success "[OK] Profile found"
        Write-Host "   Patient: $($existingProfile.patientName)"
        Write-Host "   Era: $($existingProfile.era)"
        Write-Host "   Stage: $($existingProfile.dementiaStage)"
    }
    catch {
        Write-Error "[ERROR] Profile not found or error"
        Write-Host "   Error: $($_.Exception.Message)"
    }

    Write-Info "Checking playlists for profile: $ProfileId"

    try {
        $existingPlaylists = Invoke-RestMethod -Uri "$PlaylistServiceUrl/api/playlists/profile/$ProfileId" -Method GET -ErrorAction Stop

        if ($existingPlaylists.Count -gt 0) {
            Write-Success "[OK] Found $($existingPlaylists.Count) playlist(s)"
            foreach ($pl in $existingPlaylists) {
                Write-Host "   - Playlist: $($pl.playlistId), Care Need: $($pl.careNeed), Tracks: $($pl.tracks.Count)"
            }
        } else {
            Write-Warning "[WARNING] No playlists found for this profile"
        }
    }
    catch {
        Write-Error "[ERROR] Failed to fetch playlists"
        Write-Host "   Error: $($_.Exception.Message)"
    }
}

# =============================================================================
# Summary
# =============================================================================
Write-Header "Diagnostic Summary"

Write-Host "Test Data Created:"
Write-Host "   Username: $testUsername"
Write-Host "   Password: $testPassword"
Write-Host "   Caregiver ID: $caregiverId"
Write-Host "   Profile ID: $profileId"
Write-Host "   Token: $($token.Substring(0, 30))..."
Write-Host ""

Write-Info "Common Issues and Solutions:"
Write-Host ""
Write-Host "1. If '405 Method Not Allowed' on /api/profiles:"
Write-Host "   -> You're using GET instead of POST"
Write-Host "   -> Use POST with JWT token to create profiles"
Write-Host ""
Write-Host "2. If empty playlist list:"
Write-Host "   -> Check if playlist generation threw errors"
Write-Host "   -> Check if song database has data"
Write-Host "   -> Verify RabbitMQ event was consumed"
Write-Host ""
Write-Host "3. If RabbitMQ errors:"
Write-Host "   -> Ensure docker-compose up -d was run"
Write-Host "   -> Check RabbitMQ logs: docker logs echocare-rabbitmq"
Write-Host ""
Write-Host "4. If '404 Not Found' on root (/):"
Write-Host "   -> This is normal - no root endpoint exists"
Write-Host "   -> Use /api/profiles/health or /api/playlists/health"
Write-Host ""

Write-Success "Diagnostic complete!"
Write-Host ""
Write-Host "To check service logs:"
Write-Host "   - Profile Service: Check console where 'mvnw spring-boot:run' is running"
Write-Host "   - Playlist Service: Check console where 'mvnw spring-boot:run' is running"
Write-Host "   - RabbitMQ: docker logs echocare-rabbitmq"
Write-Host ""

