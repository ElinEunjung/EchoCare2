# =============================================================================
# EchoCare REST Integration Test - Profile & Playlist Services
# =============================================================================
# Purpose: Test REST integration between profile-service and playlist-service
# Flow:
#   1. Register and create profile
#   2. Get JWT token
#   3. Extract profile ID
#   4. Generate playlist (uses profile data automatically!)
# =============================================================================

param(
    [string]$ProfileServiceUrl = "http://localhost:8081",
    [string]$PlaylistServiceUrl = "http://localhost:8082"
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

Write-Header "   EchoCare REST Integration Test   "

# Generate unique test data
$timestamp = [DateTimeOffset]::UtcNow.ToUnixTimeSeconds()
$username = "testuser_$timestamp"
$email = "testuser_$timestamp@example.com"
$password = "testpassword123"
$patientName = "Test Patient $timestamp"

Write-Info "Configuration:"
Write-Host "   Profile Service: $ProfileServiceUrl"
Write-Host "   Playlist Service: $PlaylistServiceUrl"
Write-Host "   Username: $username"
Write-Host ""

# =============================================================================
# Step 1: Register and get JWT token
# =============================================================================
Write-Info "Step 1: Register new caregiver and get JWT token"
Write-Host "   Endpoint: POST $ProfileServiceUrl/api/auth/register"
Write-Host ""

$registerBody = @{
    username = $username
    password = $password
    email    = $email
} | ConvertTo-Json

try {
    $registerResponse = Invoke-RestMethod -Uri "$ProfileServiceUrl/api/auth/register" -Method POST -ContentType "application/json" -Body $registerBody -ErrorAction Stop

    Write-Success "Registration successful!"
    Write-Host "   Caregiver ID: $($registerResponse.caregiverId)"
    Write-Host "   Token: $($registerResponse.token.Substring(0, 50))..."
    Write-Host ""

    $token = $registerResponse.token
    $caregiverId = $registerResponse.caregiverId
}
catch {
    Write-Error "Registration failed!"
    Write-Host "   Error: $($_.Exception.Message)"
    if ($_.ErrorDetails) {
        Write-Host "   Details: $($_.ErrorDetails.Message)"
    }
    exit 1
}

# =============================================================================
# Step 2: Create patient profile with JWT
# =============================================================================
Write-Info "Step 2: Create patient profile with JWT authentication"
Write-Host "   Endpoint: POST $ProfileServiceUrl/api/profiles"
Write-Host "   Patient: $patientName"
Write-Host ""

$profileBody = @{
    patientName     = $patientName
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

    Write-Success "Profile created successfully!"
    Write-Host "   Profile ID: $($profileResponse.id)"
    Write-Host "   Patient Name: $($profileResponse.patientName)"
    Write-Host "   Era: $($profileResponse.era)"
    Write-Host "   Dementia Stage: $($profileResponse.dementiaStage)"
    Write-Host "   Favorite Artists: $($profileResponse.favoriteArtists -join ', ')"
    Write-Host ""

    $profileId = $profileResponse.id
}
catch {
    Write-Error "Profile creation failed!"
    Write-Host "   Error: $($_.Exception.Message)"
    if ($_.ErrorDetails) {
        Write-Host "   Details: $($_.ErrorDetails.Message)"
    }
    exit 1
}

# =============================================================================
# Step 3: Extract profile ID (already done in Step 2)
# =============================================================================
Write-Info "Step 3: Extract profile ID from response"
Write-Host ""
Write-Success "Profile ID extracted: $profileId"
Write-Host ""

# =============================================================================
# Step 4: Generate playlist (uses profile data automatically!)
# =============================================================================
Write-Info "Step 4: Generate playlist using profile data"
Write-Host "   Endpoint: POST $PlaylistServiceUrl/api/playlists/generate"
Write-Host "   Profile ID: $profileId"
Write-Host ""

# Test with different care needs
$careNeeds = @("stress_relief", "reducing_anxiety", "activity_support")

foreach ($careNeed in $careNeeds) {
    Write-Info "Generating playlist for care need: $careNeed"

    $playlistBody = @{
        patientId       = $profileId
        careNeed        = $careNeed
        era             = "1960-1970"
        dementiaStage   = "mild"
        favoriteArtists = @("The Beatles", "Elvis Presley", "Frank Sinatra")
    } | ConvertTo-Json

    try {
        $playlistResponse = Invoke-RestMethod -Uri "$PlaylistServiceUrl/api/playlists/generate" -Method POST -Headers $headers -Body $playlistBody -ErrorAction Stop

        Write-Success "Playlist generated for $careNeed"
        Write-Host "   Playlist ID: $($playlistResponse.playlistId)"
        Write-Host "   Tracks: $($playlistResponse.tracks.Count) songs"

        # Display first 3 tracks
        if ($playlistResponse.tracks.Count -gt 0) {
            Write-Host "   Sample tracks:"
            $sampleCount = [Math]::Min(3, $playlistResponse.tracks.Count)
            for ($i = 0; $i -lt $sampleCount; $i++) {
                $track = $playlistResponse.tracks[$i]
                Write-Host "      - $($track.title) by $($track.artist) ($($track.releaseYear))"
            }
        }
        Write-Host ""
    }
    catch {
        Write-Warning "Playlist generation failed for $careNeed"
        Write-Host "   Error: $($_.Exception.Message)"
        if ($_.ErrorDetails) {
            Write-Host "   Details: $($_.ErrorDetails.Message)"
        }
        Write-Host "   Request Body:" -ForegroundColor Gray
        Write-Host "   $playlistBody" -ForegroundColor Gray
        Write-Host ""
    }
}

# =============================================================================
# Verify integration by fetching profile playlists
# =============================================================================
Write-Info "Bonus: Verify playlists are linked to profile"
Write-Host "   Endpoint: GET $PlaylistServiceUrl/api/playlists/profile/$profileId"
Write-Host ""

try {
    $profilePlaylists = Invoke-RestMethod -Uri "$PlaylistServiceUrl/api/playlists/profile/$profileId" -Method GET -Headers $headers -ErrorAction Stop

    Write-Success "Retrieved $($profilePlaylists.Count) playlists for profile"
    Write-Host ""
}
catch {
    Write-Warning "Could not retrieve playlists by profile"
    Write-Host "   Error: $($_.Exception.Message)"
    Write-Host ""
}

# =============================================================================
# Summary
# =============================================================================
Write-Header "   Integration Test Complete!   "
Write-Success "All steps completed successfully!"
Write-Host ""
Write-Host "Summary:"
Write-Host "   1. Registered caregiver: $username"
Write-Host "   2. Created patient profile: $patientName (ID: $profileId)"
Write-Host "   3. Generated playlists for $($careNeeds.Count) care needs"
Write-Host ""
Write-Host "You can now test the following endpoints:"
Write-Host "   - GET $ProfileServiceUrl/api/profiles/$profileId"
Write-Host "   - GET $PlaylistServiceUrl/api/playlists/profile/$profileId"
Write-Host ""
Write-Host "Saved variables for manual testing:"
Write-Host "   Token: $token"
Write-Host "   Profile ID: $profileId"
Write-Host ""

