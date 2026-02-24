# RabbitMQ Message Flow Verification Script
# Purpose: Verify message flow: Profile Service -> RabbitMQ -> Playlist Service

param(
    [string]$ProfileServiceUrl = "http://localhost:8081",
    [string]$PlaylistServiceUrl = "http://localhost:8082",
    [string]$RabbitMQUrl = "http://localhost:15672",
    [string]$RabbitMQAdmin = "guest",
    [string]$RabbitMQPassword = "guest",
    [switch]$Verbose,
    [int]$Iterations = 3
)

function Write-Success { Write-Host $args -ForegroundColor Green }
function Write-Error { Write-Host $args -ForegroundColor Red }
function Write-Info { Write-Host $args -ForegroundColor Cyan }
function Write-Warning { Write-Host $args -ForegroundColor Yellow }
function Write-Verbose { if ($VerbosePreference -ne "SilentlyContinue") { Write-Host $args -ForegroundColor Gray } }
function Write-Header {
    Write-Host ""
    Write-Host "=============================================" -ForegroundColor Magenta
    Write-Host $args -ForegroundColor Magenta
    Write-Host "=============================================" -ForegroundColor Magenta
    Write-Host ""
}

function Write-SubHeader {
    Write-Host ""
    Write-Host "--- $args ---" -ForegroundColor Cyan
    Write-Host ""
}

$VerbosePreference = if ($Verbose) { "Continue" } else { "SilentlyContinue" }

Write-Header "RabbitMQ Message Flow Verification"

$authString = "$($RabbitMQAdmin):$($RabbitMQPassword)"
$authBytes = [System.Text.Encoding]::ASCII.GetBytes($authString)
$authBase64 = [System.Convert]::ToBase64String($authBytes)
$rabbitHeaders = @{
    Authorization = "Basic $authBase64"
    Accept        = "application/json"
}

Write-Info "Verifying system connectivity..."

try {
    $health = Invoke-RestMethod -Uri "$RabbitMQUrl/api/overview" -Headers $rabbitHeaders -Method GET -ErrorAction Stop
    Write-Success "[OK] RabbitMQ is accessible"
}
catch {
    Write-Error "[ERROR] Cannot connect to RabbitMQ!"
    exit 1
}

try {
    Invoke-RestMethod -Uri "$ProfileServiceUrl/api/profiles/health" -Method GET -ErrorAction Stop | Out-Null
    Write-Success "[OK] Profile Service is accessible"
}
catch {
    Write-Error "[ERROR] Profile Service is not accessible!"
    exit 1
}

try {
    Invoke-RestMethod -Uri "$PlaylistServiceUrl/api/playlists/health" -Method GET -ErrorAction Stop | Out-Null
    Write-Success "[OK] Playlist Service is accessible"
}
catch {
    Write-Error "[ERROR] Playlist Service is not accessible!"
    exit 1
}

Write-Host ""

$testResults = @()

for ($iteration = 1; $iteration -le $Iterations; $iteration++) {

    Write-Header "Iteration $iteration of $Iterations"

    $timestamp = [DateTimeOffset]::UtcNow.ToUnixTimeSeconds()
    $testUsername = "flowtest_$iteration`_$timestamp"
    $testEmail = "flowtest_$iteration`_$timestamp@test.com"
    $testPassword = "testpass123"
    $testPatientName = "Flow Test Patient $iteration - $timestamp"

    $iterationResult = @{
        Iteration = $iteration
        Timestamp = Get-Date
        Success = $true
        Steps = @()
    }

    Write-SubHeader "Step 1: Register Caregiver"

    Write-Verbose "Creating account for: $testUsername"

    $registerBody = @{
        username = $testUsername
        password = $testPassword
        email    = $testEmail
    } | ConvertTo-Json

    try {
        $registerResponse = Invoke-RestMethod -Uri "$ProfileServiceUrl/api/auth/register" -Method POST -ContentType "application/json" -Body $registerBody -ErrorAction Stop

        Write-Success "[OK] Registration successful"
        $token = $registerResponse.token
        $caregiverId = $registerResponse.caregiverId

        Write-Verbose "Caregiver ID: $caregiverId"

        $iterationResult.Steps += @{
            Name = "Register"
            Status = "Success"
            Details = "Caregiver ID: $caregiverId"
        }
    }
    catch {
        Write-Error "[FAIL] Registration failed!"
        Write-Host "   Error: $($_.Exception.Message)"
        $iterationResult.Success = $false
        $iterationResult.Steps += @{
            Name = "Register"
            Status = "Failed"
            Details = $_.Exception.Message
        }
        continue
    }

    Write-SubHeader "Step 2: Check Queue Before Profile Creation"

    Write-Verbose "Recording current queue state..."

    try {
        $queuesBefore = Invoke-RestMethod -Uri "$RabbitMQUrl/api/queues" -Headers $rabbitHeaders -Method GET -ErrorAction Stop
        $profileQueueBefore = $queuesBefore | Where-Object { $_.name -eq "profile.events.queue" }
        $queueCountBefore = if ($profileQueueBefore) { $profileQueueBefore.messages } else { -1 }

        if ($queueCountBefore -ge 0) {
            Write-Success "[OK] Queue status recorded"
            Write-Host "   Messages before: $queueCountBefore"
        }
        else {
            Write-Warning "[WARNING] Could not find profile.events.queue"
            $queueCountBefore = 0
        }

        $iterationResult.Steps += @{
            Name = "Record Queue Before"
            Status = "Success"
            Details = "Queue messages: $queueCountBefore"
        }
    }
    catch {
        Write-Warning "[WARNING] Could not record queue state"
        $iterationResult.Steps += @{
            Name = "Record Queue Before"
            Status = "Warning"
            Details = $_.Exception.Message
        }
    }

    Write-SubHeader "Step 3: Create Profile (Publish RabbitMQ Message)"

    Write-Verbose "Creating patient profile: $testPatientName"

    $profileBody = @{
        patientName     = $testPatientName
        era            = "1960-1970"
        dementiaStage  = "mild"
        favoriteArtists = @("The Beatles", "Elvis Presley", "Frank Sinatra")
        symptoms       = @("memory_loss", "anxiety")
    } | ConvertTo-Json

    $headers = @{
        "Authorization" = "Bearer $token"
        "Content-Type"  = "application/json"
    }

    try {
        $profileResponse = Invoke-RestMethod -Uri "$ProfileServiceUrl/api/profiles" -Method POST -Headers $headers -Body $profileBody -ErrorAction Stop

        Write-Success "[OK] Profile created (message published)"
        $profileId = $profileResponse.id
        Write-Host "   Profile ID: $profileId"
        Write-Verbose "   Era: $($profileResponse.era)"
        Write-Verbose "   Dementia Stage: $($profileResponse.dementiaStage)"

        $iterationResult.Steps += @{
            Name = "Create Profile"
            Status = "Success"
            Details = "Profile ID: $profileId"
        }
    }
    catch {
        Write-Error "[FAIL] Profile creation failed!"
        Write-Host "   Error: $($_.Exception.Message)"
        $iterationResult.Success = $false
        $iterationResult.Steps += @{
            Name = "Create Profile"
            Status = "Failed"
            Details = $_.Exception.Message
        }
        continue
    }

    Write-SubHeader "Step 4: Monitor Message Consumption"

    Write-Host "Waiting 3 seconds for message consumption..."

    for ($i = 1; $i -le 3; $i++) {
        Start-Sleep -Seconds 1

        try {
            $queuesNow = Invoke-RestMethod -Uri "$RabbitMQUrl/api/queues" -Headers $rabbitHeaders -Method GET -ErrorAction Stop
            $profileQueueNow = $queuesNow | Where-Object { $_.name -eq "profile.events.queue" }
            $queueCountNow = $profileQueueNow.messages

            Write-Verbose "   [$i/3] Queue length: $queueCountNow messages"
        }
        catch {
            # Continue
        }
    }

    Write-SubHeader "Step 5: Check Queue After Consumption"

    try {
        $queuesAfter = Invoke-RestMethod -Uri "$RabbitMQUrl/api/queues" -Headers $rabbitHeaders -Method GET -ErrorAction Stop
        $profileQueueAfter = $queuesAfter | Where-Object { $_.name -eq "profile.events.queue" }
        $queueCountAfter = $profileQueueAfter.messages

        Write-Host "   Before: $queueCountBefore"
        Write-Host "   After: $queueCountAfter"

        if ($queueCountAfter -lt $queueCountBefore) {
            Write-Success "[OK] Message was consumed"
            Write-Host "   Consumed: $($queueCountBefore - $queueCountAfter) message(s)"
            $consumptionSuccess = $true
        }
        else {
            Write-Warning "[WARN] Message may not have been consumed"
            Write-Host "   Queue status unchanged or grew"
            $consumptionSuccess = $false
            $iterationResult.Success = $false
        }

        $iterationResult.Steps += @{
            Name = "Verify Consumption"
            Status = if ($consumptionSuccess) { "Success" } else { "Warning" }
            Details = "Before: $queueCountBefore, After: $queueCountAfter"
        }
    }
    catch {
        Write-Warning "[WARNING] Could not verify consumption"
        $iterationResult.Steps += @{
            Name = "Verify Consumption"
            Status = "Warning"
            Details = $_.Exception.Message
        }
    }

    Write-SubHeader "Step 6: Verify Cache Population in Playlist Service"

    Write-Verbose "Attempting to generate playlist with minimal data..."

    try {
        $playlistBody = @{
            patientId       = $profileId
            careNeed        = "stress_relief"
            era             = "dummy"
            dementiaStage   = "dummy"
            favoriteArtists = @()
        } | ConvertTo-Json

        $playlistResponse = Invoke-RestMethod -Uri "$PlaylistServiceUrl/api/playlists/generate" -Method POST -Headers $headers -Body $playlistBody -ErrorAction Stop

        Write-Success "[OK] Playlist generated using cached profile"
        Write-Host "   Playlist ID: $($playlistResponse.playlistId)"
        Write-Host "   Tracks: $($playlistResponse.tracks.Count)"
        Write-Host "   Era (from cache): $($playlistResponse.era)"
        Write-Host "   Dementia Stage (from cache): $($playlistResponse.dementiaStage)"

        $cacheSuccess = $true

        if ($playlistResponse.era -eq "1960-1970") {
            Write-Success "Cache was correctly populated and used"
        }
        else {
            Write-Warning "Era doesn't match cached value - cache may not be working"
            $cacheSuccess = $false
        }

        $iterationResult.Steps += @{
            Name = "Verify Cache"
            Status = if ($cacheSuccess) { "Success" } else { "Warning" }
            Details = "Playlist ID: $($playlistResponse.playlistId), Tracks: $($playlistResponse.tracks.Count)"
        }
    }
    catch {
        Write-Error "[FAIL] Playlist generation failed - cache not populated"
        Write-Host "   Error: $($_.Exception.Message)"
        Write-Warning "   RabbitMQ message was NOT consumed by playlist-service"
        $iterationResult.Success = $false
        $iterationResult.Steps += @{
            Name = "Verify Cache"
            Status = "Failed"
            Details = $_.Exception.Message
        }
    }

    $testResults += $iterationResult

    Write-SubHeader "Iteration $iteration Summary"

    if ($iterationResult.Success) {
        Write-Success "All checks passed"
    }
    else {
        Write-Error "Some checks failed"
    }

    Write-Host ""
}

Write-Header "Overall Test Summary"

Write-Host "Iterations completed: $($testResults.Count)"
Write-Host ""

$successCount = ($testResults | Where-Object { $_.Success }).Count
$failureCount = $testResults.Count - $successCount

Write-Host "Results:"
Write-Host "   Successful: $successCount"
Write-Host "   Failed: $failureCount"
Write-Host ""

if ($successCount -eq $testResults.Count) {
    Write-Success "ALL TESTS PASSED"
    Write-Host ""
    Write-Host "Message flow is working correctly:"
    Write-Host "   1. Profile Service publishes messages"
    Write-Host "   2. Messages arrive in queue"
    Write-Host "   3. Playlist Service consumes messages"
    Write-Host "   4. Profile cache is populated"
}
else {
    Write-Error "SOME TESTS FAILED"
    Write-Host ""
    Write-Host "Failed iterations:"

    foreach ($result in ($testResults | Where-Object { -not $_.Success })) {
        Write-Host "   Iteration $($result.Iteration):"

        foreach ($step in $result.Steps | Where-Object { $_.Status -ne "Success" }) {
            Write-Host "      - $($step.Name): $($step.Status)"
        }
    }

    Write-Host ""
    Write-Error "RabbitMQ message flow is not working correctly"
}

Write-Host ""
Write-Host "Troubleshooting Guide:" -ForegroundColor Cyan
Write-Host ""
Write-Host "If message consumption is failing:"
Write-Host "   1. Check playlist-service logs"
Write-Host "   2. Verify @RabbitListener is present"
Write-Host "   3. Check @EnableRabbit in PlaylistServiceApplication"
Write-Host "   4. Verify database connectivity"
Write-Host ""
Write-Host "For live monitoring:"
Write-Host "   .\monitor-rabbitmq-queues-fixed.ps1"
Write-Host ""
Write-Host "RabbitMQ Management UI:"
Write-Host "   http://localhost:15672 (guest/guest)"
Write-Host ""

Write-Success "Test Complete"
Write-Host ""

