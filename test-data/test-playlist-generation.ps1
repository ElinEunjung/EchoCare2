# =============================================================================
# EchoCare Playlist Service - Playlist Generation Test Script
# =============================================================================
# Purpose: Test playlist generation with different care needs and dementia stages
# Tests all combinations to verify the playlist generator produces appropriate results
# =============================================================================

param(
    [string]$BaseUrl = "http://localhost:8082/playlist",
    [string]$ProfileServiceUrl = "http://localhost:8081",
    [switch]$SkipProfileCheck = $false
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

Write-Header "   EchoCare Playlist Generation Test Script   "

# =============================================================================
# Configuration & Setup
# =============================================================================
Write-Info "Configuration:"
Write-Host "   Playlist Service: $BaseUrl"
Write-Host "   Profile Service: $ProfileServiceUrl"
Write-Host ""

# Test patient IDs (these should exist in profile-service database)
$testPatientIds = @(
    "550e8400-e29b-41d4-a716-446655440000",  # Default test patient
    "550e8400-e29b-41d4-a716-446655440001"   # Another test patient
)

# Care needs to test - must match service expectations (lowercase with underscores)
$careNeeds = @(
    "reducing_anxiety",
    "stress_relief",
    "easing_depression",
    "activity_support",
    "calming_agitation"
)

# Dementia stages to test (must match service expectations)
$stages = @(
    "mild",
    "moderate",
    "severe"
)

# =============================================================================
# Health Check
# =============================================================================
Write-Info "Checking Playlist Service Health..."
try {
    $healthResponse = Invoke-RestMethod -Uri "$BaseUrl/api/playlists/health" `
        -Method GET `
        -ErrorAction Stop

    Write-Success "œ… [OK] Playlist Service is running"
    Write-Host ""
}
catch {
    Write-Error "Œ [ERROR] Playlist Service is not accessible!"
    Write-Host "   Make sure the service is running on $BaseUrl"
    Write-Host "   Error: $($_.Exception.Message)"
    exit 1
}

# =============================================================================
# Check Profile Service (optional)
# =============================================================================
if (-not $SkipProfileCheck) {
    Write-Info "Checking Profile Service availability..."
    try {
        $profileHealth = Invoke-RestMethod -Uri "$ProfileServiceUrl/api/profiles/health" `
            -Method GET `
            -ErrorAction Stop

        Write-Success "[OK] Profile Service is running"
        Write-Host ""
    }
    catch {
        Write-Warning "[WARNING] Profile Service may not be accessible"
        Write-Host "   Continuing anyway - playlist service may use fallback data"
        Write-Host ""
    }
}

# =============================================================================
# Test Results Tracking
# =============================================================================
$testResults = @{
    Total = 0
    Passed = 0
    Failed = 0
    Details = @()
}

# =============================================================================
# Function to test playlist generation
# =============================================================================
function Test-PlaylistGeneration {
    param(
        [string]$PatientId,
        [string]$CareNeed,
        [string]$Stage,
        [object]$Profile
    )

    $testResults.Total++

    Write-Info "Test: CareNeed=$CareNeed, Stage=$Stage"

    # Build request with profile data
    $requestBody = @{
        patientId = $PatientId
        careNeed = $CareNeed
        era = $Profile.era
        dementiaStage = $Stage
        favoriteArtists = $Profile.favoriteArtists
    } | ConvertTo-Json

    try {
        $response = Invoke-RestMethod -Uri "$BaseUrl/api/playlists/generate" `
            -Method POST `
            -ContentType "application/json" `
            -Body $requestBody `
            -ErrorAction Stop

        # Validate response
        $trackCount = $response.tracks.Count
        $hasPatientId = $null -ne $response.patientId

        if ($trackCount -gt 0 -and $hasPatientId) {
            Write-Success "   œ… [OK] [OK] Generated playlist with $trackCount tracks"

            # Show sample tracks
            $sampleCount = [Math]::Min(3, $trackCount)
            Write-Host "   Sample tracks:" -ForegroundColor Gray
            for ($i = 0; $i -lt $sampleCount; $i++) {
                $track = $response.tracks[$i]
                Write-Host "      - $($track.title) by $($track.artist) (BPM: $($track.bpm), Energy: $($track.energy))" -ForegroundColor DarkGray
            }

            # Analyze playlist characteristics
            $avgBpm = ($response.tracks | Measure-Object -Property bpm -Average).Average
            $avgEnergy = ($response.tracks | Measure-Object -Property energy -Average).Average

            Write-Host "   Playlist Stats:" -ForegroundColor Gray
            Write-Host "      Avg BPM: $([math]::Round($avgBpm, 1))" -ForegroundColor DarkGray
            Write-Host "      Avg Energy: $([math]::Round($avgEnergy, 1))" -ForegroundColor DarkGray

            $testResults.Passed++
            $testResults.Details += @{
                CareNeed = $CareNeed
                Stage = $Stage
                Status = "PASSED"
                TrackCount = $trackCount
                AvgBpm = [math]::Round($avgBpm, 1)
                AvgEnergy = [math]::Round($avgEnergy, 1)
            }

            Write-Host ""
            return $true
        }
        else {
            Write-Error "   Œ [ERROR] [ERROR] Invalid response (no tracks or missing patient ID)"
            $testResults.Failed++
            $testResults.Details += @{
                CareNeed = $CareNeed
                Stage = $Stage
                Status = "FAILED"
                Reason = "[ERROR] [ERROR] Invalid response"
            }
            Write-Host ""
            return $false
        }
    }
    catch {
        Write-Error "   Œ [ERROR] [ERROR] Request failed: $($_.Exception.Message)"
        if ($_.ErrorDetails) {
            Write-Host "      Details: $($_.ErrorDetails.Message)" -ForegroundColor Red
        }
        $testResults.Failed++
        $testResults.Details += @{
            CareNeed = $CareNeed
            Stage = $Stage
            Status = "FAILED"
            Reason = $_.Exception.Message
        }
        Write-Host ""
        return $false
    }
}

# =============================================================================
# Run Tests for All Combinations
# =============================================================================
Write-Header "Testing All Care Need & Stage Combinations"

$patientId = $testPatientIds[0]
Write-Info "Using Patient ID: $patientId"
Write-Host ""

foreach ($careNeed in $careNeeds) {
    Write-Host "============================================" -ForegroundColor DarkCyan
    Write-Host "Care Need: $careNeed" -ForegroundColor Cyan
    Write-Host "============================================" -ForegroundColor DarkCyan
    Write-Host ""

    foreach ($stage in $stages) {
        Test-PlaylistGeneration -PatientId $patientId -CareNeed $careNeed -Stage $stage -Profile $patientProfile
    }
}

# =============================================================================
# Test Summary
# =============================================================================
Write-Header "Test Summary"

Write-Host "Total Tests Run: $($testResults.Total)" -ForegroundColor White
Write-Success "Passed: $($testResults.Passed)"
if ($testResults.Failed -gt 0) {
    Write-Error "Failed: $($testResults.Failed)"
} else {
    Write-Host "Failed: $($testResults.Failed)" -ForegroundColor Gray
}
Write-Host ""

# Display detailed results table
Write-Info "Detailed Results:"
Write-Host ""
Write-Host "Care Need          | Stage     | Tracks | Avg BPM | Avg Energy | Status" -ForegroundColor Yellow
Write-Host "-------------------|-----------|--------|---------|------------|--------" -ForegroundColor Yellow

foreach ($detail in $testResults.Details) {
    $careNeedPadded = $detail.CareNeed.PadRight(18)
    $stagePadded = $detail.Stage.PadRight(9)

    if ($detail.Status -eq "PASSED") {
        $tracksPadded = $detail.TrackCount.ToString().PadRight(6)
        $bpmPadded = $detail.AvgBpm.ToString().PadRight(7)
        $energyPadded = $detail.AvgEnergy.ToString().PadRight(10)
        Write-Host "$careNeedPadded | $stagePadded | $tracksPadded | $bpmPadded | $energyPadded | " -NoNewline -ForegroundColor Gray
        Write-Success "PASSED"
    }
    else {
        $reason = $detail.Reason.Substring(0, [Math]::Min(40, $detail.Reason.Length))
        Write-Host "$careNeedPadded | $stagePadded | " -NoNewline -ForegroundColor Gray
        Write-Error "FAILED - $reason"
    }
}

Write-Host ""

# =============================================================================
# Exit with appropriate code
# =============================================================================
if ($testResults.Failed -eq 0) {
    Write-Success "[SUCCESS] All tests passed!"
    exit 0
}
else {
    Write-Error "[ERROR] Some tests failed. Please review the errors above."
    exit 1
}


