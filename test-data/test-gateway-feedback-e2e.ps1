# =============================================================================
# EchoCare - Gateway + Feedback End-to-End Test Script
# Tests the full flow: Register → Profile → Playlist → Feedback → Verify
# All requests go through the API Gateway on port 8080
# =============================================================================
param(
    [string]$GatewayUrl  = "http://localhost:8080",
    [string]$RabbitMqUrl = "http://localhost:15672",
    [string]$RabbitUser  = "guest",
    [string]$RabbitPass  = "guest",
    [switch]$Cleanup,
    [switch]$Verbose
)

# =============================================================================
# Output helpers
# =============================================================================
function Write-Success { param([string]$msg) Write-Host "  [OK] $msg" -ForegroundColor Green }
function Write-Fail    { param([string]$msg) Write-Host "  [FAIL] $msg" -ForegroundColor Red }
function Write-Info    { param([string]$msg) Write-Host "  > $msg" -ForegroundColor Cyan }
function Write-Section { param([string]$msg)
    Write-Host ""
    Write-Host "=================================================" -ForegroundColor DarkGray
    Write-Host "  $msg" -ForegroundColor Magenta
    Write-Host "=================================================" -ForegroundColor DarkGray
}
function Write-Verbose-Info { param([string]$msg)
    if ($Verbose) { Write-Host "    [verbose] $msg" -ForegroundColor DarkGray }
}

# =============================================================================
# Shared test state
# =============================================================================
$script:Token          = $null
$script:CaregiverId    = $null
$script:ProfileId      = $null
$script:PlaylistId     = $null
$script:SongId         = $null
$script:FeedbackId     = $null

$script:PassCount      = 0
$script:FailCount      = 0
$script:Results        = @()

function Record-Result {
    param([string]$name, [bool]$passed, [string]$detail = "")
    if ($passed) {
        $script:PassCount++
        $script:Results += [PSCustomObject]@{ Test = $name; Status = "PASS"; Detail = $detail }
        Write-Success "$name"
    } else {
        $script:FailCount++
        $script:Results += [PSCustomObject]@{ Test = $name; Status = "FAIL"; Detail = $detail }
        Write-Fail "$name  ($detail)"
    }
}

# =============================================================================
# 0. PREREQUISITES CHECK
# =============================================================================
function Test-Prerequisites {
    Write-Section "0. Prerequisites Check"

    # Docker running?
    try {
        $dockerPs = docker ps 2>&1
        if ($LASTEXITCODE -ne 0) { throw "docker ps failed" }
        Record-Result "Docker is running" $true
    } catch {
        Record-Result "Docker is running" $false "Docker does not appear to be running: $_"
        return $false
    }

    # Required containers
    $required = @(
        @{ Name = "echocare-gateway";     Label = "Gateway Service"   },
        @{ Name = "echocare-rabbitmq";    Label = "RabbitMQ"          },
        @{ Name = "echocare-profile-db";  Label = "Profile DB"        },
        @{ Name = "echocare-playlist-db"; Label = "Playlist DB"       },
        @{ Name = "echocare-feedback-db"; Label = "Feedback DB"       }
    )

    $allUp = $true
    foreach ($c in $required) {
        if ($dockerPs -match $c.Name) {
            Record-Result "$($c.Label) container running" $true
        } else {
            Record-Result "$($c.Label) container running" $false "Container '$($c.Name)' not found in docker ps"
            $allUp = $false
        }
    }

    # Gateway reachable?
    try {
        $null = Invoke-RestMethod -Uri "$GatewayUrl/actuator/health" -Method GET -TimeoutSec 5 -ErrorAction Stop
        Record-Result "Gateway reachable on $GatewayUrl" $true
    } catch {
        $statusCode = $_.Exception.Response.StatusCode.value__
        # 401 means the gateway is up but JWT is required — that's fine
        # 404 means the gateway is up but endpoint not configured — also fine, it's running
        if ($statusCode -eq 401 -or $statusCode -eq 404 -or $statusCode -eq 403) {
            Record-Result "Gateway reachable on $GatewayUrl" $true
        } else {
            Record-Result "Gateway reachable on $GatewayUrl" $false "$_"
            $allUp = $false
        }
    }

    return $allUp
}

# =============================================================================
# 1. REGISTER CAREGIVER  →  capture JWT
# =============================================================================
function Test-RegisterCaregiver {
    Write-Section "1. Register Caregiver"

    $timestamp = Get-Date -Format "yyyyMMddHHmmss"
    $username  = "testcaregiver_$timestamp"
    $password  = "password123"
    $email     = "testcaregiver_$timestamp@echocare.test"

    Write-Info "POST $GatewayUrl/api/auth/register"
    Write-Verbose-Info "Body: username=$username, email=$email"

    $body = @{
        username = $username
        password = $password
        email    = $email
    } | ConvertTo-Json

    try {
        $response = Invoke-RestMethod `
            -Uri         "$GatewayUrl/api/auth/register" `
            -Method      POST `
            -ContentType "application/json" `
            -Body        $body `
            -TimeoutSec  10 `
            -ErrorAction Stop

        $script:Token       = $response.token
        $script:CaregiverId = $response.caregiverId

        Write-Verbose-Info "CaregiverId : $($script:CaregiverId)"
        Write-Verbose-Info "Token       : $($script:Token.Substring(0, [Math]::Min(50,$script:Token.Length)))..."

        Record-Result "Register caregiver" ($null -ne $script:Token) "token missing in response"
        Record-Result "Response contains caregiverId" ($null -ne $script:CaregiverId) "caregiverId missing"
        return $true
    } catch {
        $detail = "$($_.Exception.Message)"
        if ($_.ErrorDetails.Message) { $detail += " | $($_.ErrorDetails.Message)" }
        Record-Result "Register caregiver" $false $detail
        return $false
    }
}

# =============================================================================
# 2. CREATE PATIENT PROFILE  →  capture profileId
# =============================================================================
function Test-CreatePatientProfile {
    Write-Section "2. Create Patient Profile"

    Write-Info "POST $GatewayUrl/api/profiles"

    $body = @{
        patientName     = "Test Patient E2E"
        era             = "1960s"
        dementiaStage   = "MODERATE"
        symptoms        = @("Anxiety", "Agitation")
        favoriteArtists = @("Frank Sinatra", "Nat King Cole")
    } | ConvertTo-Json

    Write-Host "DEBUG - Request body:" -ForegroundColor Yellow
    Write-Host $body -ForegroundColor Yellow

    try {
        $response = Invoke-RestMethod `
            -Uri         "$GatewayUrl/api/profiles" `
            -Method      POST `
            -ContentType "application/json" `
            -Headers     @{ Authorization = "Bearer $($script:Token)" } `
            -Body        $body `
            -TimeoutSec  10 `
            -ErrorAction Stop

        $script:ProfileId = $response.id

        Write-Verbose-Info "ProfileId: $($script:ProfileId)"

        Record-Result "Create patient profile"     ($null -ne $script:ProfileId)    "id missing in response"
        Record-Result "Profile name matches"       ($response.patientName -eq "Test Patient E2E") "name mismatch: $($response.patientName)"
        Record-Result "Profile dementiaStage matches" ($response.dementiaStage -eq "MODERATE") "dementiaStage: $($response.dementiaStage)"
        return $true
    } catch {
        $detail = "$($_.Exception.Message)"
        if ($_.ErrorDetails.Message) { $detail += " | $($_.ErrorDetails.Message)" }
        Record-Result "Create patient profile" $false $detail
        return $false
    }
}

# =============================================================================
# 3. GENERATE PLAYLIST  →  capture playlistId + first songId
# =============================================================================
function Test-GeneratePlaylist {
    Write-Section "3. Generate Playlist"

    Write-Info "POST $GatewayUrl/api/playlists/generate"

    $body = @{
        patientId       = $script:ProfileId
        careNeed        = "CALMING_AGITATION"
        era             = "1960s"
        dementiaStage   = "MODERATE"
        favoriteArtists = @("Frank Sinatra", "Nat King Cole")
    } | ConvertTo-Json

    Write-Verbose-Info "Body: $body"

    try {
        $response = Invoke-RestMethod `
            -Uri         "$GatewayUrl/api/playlists/generate" `
            -Method      POST `
            -ContentType "application/json" `
            -Headers     @{ Authorization = "Bearer $($script:Token)" } `
            -Body        $body `
            -TimeoutSec  15 `
            -ErrorAction Stop

        $script:PlaylistId = $response.playlistId
        # Grab the first song from the playlist
        if ($response.songs -and $response.songs.Count -gt 0) {
            $script:SongId = $response.songs[0].songId
        }

        Write-Verbose-Info "PlaylistId : $($script:PlaylistId)"
        Write-Verbose-Info "SongId     : $($script:SongId)"

        Record-Result "Generate playlist"       ($null -ne $script:PlaylistId) "playlistId missing"
        Record-Result "Playlist contains songs" ($null -ne $script:SongId)     "no songs in playlist response"
        return $true
    } catch {
        $detail = "$($_.Exception.Message)"
        if ($_.ErrorDetails.Message) { $detail += " | $($_.ErrorDetails.Message)" }
        Record-Result "Generate playlist" $false $detail
        return $false
    }
}

# =============================================================================
# 4. SUBMIT FEEDBACK (LIKE)  →  capture feedbackId
# =============================================================================
function Test-SubmitFeedback {
    Write-Section "4. Submit Feedback (Like)"

    Write-Info "POST $GatewayUrl/api/feedback"

    $body = @{
        playlistId       = $script:PlaylistId
        songId           = $script:SongId
        patientProfileId = $script:ProfileId
        liked            = $true
        dementiaStage    = "MODERATE"
        careNeed         = "CALMING_AGITATION"
    } | ConvertTo-Json

    Write-Verbose-Info "Body: $body"

    try {
        $response = Invoke-RestMethod `
            -Uri         "$GatewayUrl/api/feedback" `
            -Method      POST `
            -ContentType "application/json" `
            -Headers     @{ Authorization = "Bearer $($script:Token)" } `
            -Body        $body `
            -TimeoutSec  10 `
            -ErrorAction Stop

        $script:FeedbackId = $response.feedbackId

        Write-Verbose-Info "FeedbackId : $($script:FeedbackId)"
        Write-Verbose-Info "EventType  : $($response.eventType)"

        Record-Result "Submit feedback"           ($null -ne $script:FeedbackId) "feedbackId missing in response"
        Record-Result "Feedback liked=true saved" ($response.liked -eq $true)    "liked flag mismatch: $($response.liked)"
        Record-Result "EventType is SUBMITTED"    ($response.eventType -eq "SUBMITTED") "eventType: $($response.eventType)"
        return $true
    } catch {
        $detail = "$($_.Exception.Message)"
        if ($_.ErrorDetails.Message) { $detail += " | $($_.ErrorDetails.Message)" }
        Record-Result "Submit feedback" $false $detail
        return $false
    }
}

# =============================================================================
# 5. GET FEEDBACK BY PROFILE  →  verify persistence
# =============================================================================
function Test-GetFeedbackByProfile {
    Write-Section "5. Get Feedback by Profile"

    Write-Info "GET $GatewayUrl/api/feedback/profile/$($script:ProfileId)"

    try {
        $response = Invoke-RestMethod `
            -Uri         "$GatewayUrl/api/feedback/profile/$($script:ProfileId)" `
            -Method      GET `
            -Headers     @{ Authorization = "Bearer $($script:Token)" } `
            -TimeoutSec  10 `
            -ErrorAction Stop

        $count = if ($response -is [array]) { $response.Count } else { 1 }

        Write-Verbose-Info "Feedback entries returned: $count"

        Record-Result "Feedback list is not empty"       ($count -ge 1) "expected ≥1 entry, got $count"

        # Find the one we just submitted
        $match = $response | Where-Object {
            ($_.feedbackId -eq $script:FeedbackId) -or
            ($_.id         -eq $script:FeedbackId)
        }
        Record-Result "Submitted feedback found in list" ($null -ne $match) "feedbackId $($script:FeedbackId) not found in results"

    } catch {
        $detail = "$($_.Exception.Message)"
        if ($_.ErrorDetails.Message) { $detail += " | $($_.ErrorDetails.Message)" }
        Record-Result "Get feedback by profile" $false $detail
        return $false
    }
}

# =============================================================================
# 6. VERIFY JWT REJECTION  →  ensure 401 without token
# =============================================================================
function Test-JwtRejection {
    Write-Section "6. JWT Validation (no token → expect 401)"

    Write-Info "POST $GatewayUrl/api/feedback  (no Authorization header)"

    try {
        $null = Invoke-RestMethod `
            -Uri         "$GatewayUrl/api/feedback" `
            -Method      POST `
            -ContentType "application/json" `
            -Body        "{}" `
            -TimeoutSec  5 `
            -ErrorAction Stop

        # Should NOT reach here
        Record-Result "Unauthenticated request rejected" $false "Expected 401 but got 200"
    } catch {
        $statusCode = $_.Exception.Response.StatusCode.value__
        Record-Result "Unauthenticated request rejected (401)" ($statusCode -eq 401) "Expected 401 but got $statusCode"
    }

    Write-Info "POST $GatewayUrl/api/feedback  (invalid/tampered token → expect 401)"
    try {
        $null = Invoke-RestMethod `
            -Uri         "$GatewayUrl/api/feedback" `
            -Method      POST `
            -ContentType "application/json" `
            -Headers     @{ Authorization = "Bearer eyJhbGciOiJIUzI1NiJ9.TAMPERED.signature" } `
            -Body        "{}" `
            -TimeoutSec  5 `
            -ErrorAction Stop

        Record-Result "Tampered token rejected" $false "Expected 401 but got 200"
    } catch {
        $statusCode = $_.Exception.Response.StatusCode.value__
        Record-Result "Tampered token rejected (401)" ($statusCode -eq 401) "Expected 401 but got $statusCode"
    }
}

# =============================================================================
# 7. VERIFY RABBITMQ EVENT
# =============================================================================
function Test-VerifyRabbitMQEvent {
    Write-Section "7. Verify RabbitMQ Feedback Event"

    $queueName  = "playlist.feedback-events.queue"
    $encodedVh  = "%2F"
    $apiUrl     = "$RabbitMqUrl/api/queues/$encodedVh/$queueName"

    Write-Info "GET $apiUrl  (basic auth: $RabbitUser)"

    # Build Basic auth header
    $creds  = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("${RabbitUser}:${RabbitPass}"))
    $headers = @{ Authorization = "Basic $creds" }

    try {
        $queue = Invoke-RestMethod `
            -Uri        $apiUrl `
            -Method     GET `
            -Headers    $headers `
            -TimeoutSec 10 `
            -ErrorAction Stop

        $ready    = $queue.messages_ready
        $total    = $queue.messages
        $consumed = $queue.message_stats.deliver_get

        Write-Verbose-Info "Queue messages_ready : $ready"
        Write-Verbose-Info "Queue messages total : $total"
        Write-Verbose-Info "Messages consumed    : $consumed"

        # Either there are messages waiting, or they have already been consumed
        $eventSeen = ($ready -gt 0) -or ($total -gt 0) -or ($consumed -gt 0)
        Record-Result "RabbitMQ feedback event published or consumed" $eventSeen `
            "messages_ready=$ready, total=$total, consumed=$consumed"

    } catch {
        $detail = "$($_.Exception.Message)"
        if ($_.ErrorDetails.Message) { $detail += " | $($_.ErrorDetails.Message)" }
        Record-Result "RabbitMQ feedback event check" $false $detail
    }
}

# =============================================================================
# CLEANUP  (optional, run with -Cleanup)
# =============================================================================
function Cleanup-TestData {
    Write-Section "Cleanup (informational - no hard deletes implemented)"
    Write-Info "ProfileId  : $($script:ProfileId)"
    Write-Info "PlaylistId : $($script:PlaylistId)"
    Write-Info "FeedbackId : $($script:FeedbackId)"
    Write-Host "  No destructive cleanup is performed automatically." -ForegroundColor DarkYellow
    Write-Host "  Remove test data manually from the DB if needed." -ForegroundColor DarkYellow
}

# =============================================================================
# SUMMARY REPORT
# =============================================================================
function Write-Summary {
    Write-Host ""
    Write-Host "=============================================" -ForegroundColor Magenta
    Write-Host "             TEST SUMMARY REPORT            " -ForegroundColor Magenta
    Write-Host "=============================================" -ForegroundColor Magenta
    Write-Host ""

    foreach ($r in $script:Results) {
        if ($r.Status -eq "PASS") {
            Write-Host "  [PASS] $($r.Test)" -ForegroundColor Green
        } else {
            Write-Host "  [FAIL] $($r.Test)" -ForegroundColor Red
            if ($r.Detail) {
                Write-Host "         $($r.Detail)" -ForegroundColor DarkRed
            }
        }
    }

    Write-Host ""
    Write-Host "  Passed : $($script:PassCount)" -ForegroundColor Green
    Write-Host "  Failed : $($script:FailCount)" -ForegroundColor $(if ($script:FailCount -gt 0) { "Red" } else { "Green" })
    Write-Host ""

    if ($script:FailCount -eq 0) {
        Write-Host "  [OK] All tests passed!" -ForegroundColor Green
    } else {
        Write-Host "  [FAIL] Some tests failed. Check output above for details." -ForegroundColor Red
    }
    Write-Host "=============================================" -ForegroundColor Magenta
}

# =============================================================================
# MAIN EXECUTION
# =============================================================================
Write-Host ""
Write-Host "=============================================" -ForegroundColor Magenta
Write-Host "  EchoCare Gateway + Feedback E2E Test      " -ForegroundColor Magenta
Write-Host "  Gateway : $GatewayUrl                     " -ForegroundColor Magenta
Write-Host "  RabbitMQ: $RabbitMqUrl                    " -ForegroundColor Magenta
Write-Host "=============================================" -ForegroundColor Magenta

# Step 0 — prerequisites
$prereqOk = Test-Prerequisites
if (-not $prereqOk) {
    Write-Host ""
    Write-Host "  Prerequisites failed. Start Docker containers first:" -ForegroundColor Red
    Write-Host "  docker compose up -d" -ForegroundColor Yellow
    Write-Summary
    exit 1
}

# Step 1 — register
$ok = Test-RegisterCaregiver
if (-not $ok) {
    Write-Host "  Cannot continue without a JWT token." -ForegroundColor Red
    Write-Summary; exit 1
}

# Step 2 — profile
$ok = Test-CreatePatientProfile
if (-not $ok) {
    Write-Host "  Cannot continue without a profileId." -ForegroundColor Red
    Write-Summary; exit 1
}

# Step 3 — playlist (non-fatal: we can still test feedback with placeholder IDs)
$ok = Test-GeneratePlaylist
if (-not $ok) {
    Write-Host "  Playlist generation failed. Using placeholder IDs for feedback test." -ForegroundColor Yellow
    $script:PlaylistId = [guid]::NewGuid().ToString()
    $script:SongId     = [guid]::NewGuid().ToString()
}

# Step 4 — feedback
Test-SubmitFeedback

# Step 5 — verify persistence
if ($null -ne $script:ProfileId) {
    Test-GetFeedbackByProfile
}

# Step 6 — JWT security
Test-JwtRejection

# Step 7 — RabbitMQ
Test-VerifyRabbitMQEvent

# Cleanup
if ($Cleanup) { Cleanup-TestData }

# Final report
Write-Summary

exit $(if ($script:FailCount -eq 0) { 0 } else { 1 })

