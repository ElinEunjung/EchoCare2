# RabbitMQ Comprehensive Testing Script
# Purpose: Complete RabbitMQ system testing
# Tests: Health, Exchanges, Queues, Bindings, Message Flow, Cache

param(
    [string]$ProfileServiceUrl = "http://localhost:8081",
    [string]$PlaylistServiceUrl = "http://localhost:8082",
    [string]$RabbitMQUrl = "http://localhost:15672",
    [string]$RabbitMQAdmin = "guest",
    [string]$RabbitMQPassword = "guest",
    [int]$WaitForConsumption = 5
)

function Write-Success { Write-Host $args -ForegroundColor Green }
function Write-Error { Write-Host $args -ForegroundColor Red }
function Write-Info { Write-Host $args -ForegroundColor Cyan }
function Write-Warning { Write-Host $args -ForegroundColor Yellow }
function Write-Critical { Write-Host $args -ForegroundColor Red -BackgroundColor Yellow }
function Write-Header {
    Write-Host ""
    Write-Host "=============================================" -ForegroundColor Magenta
    Write-Host $args -ForegroundColor Magenta
    Write-Host "=============================================" -ForegroundColor Magenta
    Write-Host ""
}

function Write-SubHeader {
    Write-Host ""
    Write-Host "----- $args -----" -ForegroundColor Cyan
    Write-Host ""
}

Write-Header "RabbitMQ Comprehensive Testing"

$authString = "$($RabbitMQAdmin):$($RabbitMQPassword)"
$authBytes = [System.Text.Encoding]::ASCII.GetBytes($authString)
$authBase64 = [System.Convert]::ToBase64String($authBytes)
$rabbitHeaders = @{
    Authorization = "Basic $authBase64"
    Accept        = "application/json"
}

$testResults = @{
    RabbitMQHealth      = $false
    ExchangesFound      = $false
    QueuesFound         = $false
    BindingsCorrect     = $false
    MessagePublished    = $false
    MessageConsumed     = $false
    CachePopulated      = $false
    PlaylistGenerated   = $false
}

Write-Header "Step 1: RabbitMQ Health Checks"

Write-Info "Checking RabbitMQ Management API..."
try {
    $health = Invoke-RestMethod -Uri "$RabbitMQUrl/api/overview" -Headers $rabbitHeaders -Method GET -ErrorAction Stop
    Write-Success "[OK] RabbitMQ is running"
    Write-Host "   Version: $($health.rabbitmq_version)"
    Write-Host "   Status: $($health.status)"
    $testResults.RabbitMQHealth = $true
}
catch {
    Write-Error "[ERROR] Cannot connect to RabbitMQ!"
    Write-Host "   Error: $($_.Exception.Message)"
    exit 1
}

Write-Info "Checking Profile Service..."
try {
    Invoke-RestMethod -Uri "$ProfileServiceUrl/api/profiles/health" -Method GET -ErrorAction Stop | Out-Null
    Write-Success "[OK] Profile Service is accessible"
}
catch {
    Write-Error "[ERROR] Profile Service is not accessible!"
}

Write-Info "Checking Playlist Service..."
try {
    Invoke-RestMethod -Uri "$PlaylistServiceUrl/api/playlists/health" -Method GET -ErrorAction Stop | Out-Null
    Write-Success "[OK] Playlist Service is accessible"
}
catch {
    Write-Error "[ERROR] Playlist Service is not accessible!"
}

Write-Header "Step 2: RabbitMQ Configuration Analysis"

Write-SubHeader "Exchanges"

try {
    $exchanges = Invoke-RestMethod -Uri "$RabbitMQUrl/api/exchanges" -Headers $rabbitHeaders -Method GET -ErrorAction Stop

    $profileExchange = $exchanges | Where-Object { $_.name -eq "profile.exchange" }
    if ($profileExchange) {
        Write-Success "[OK] profile.exchange found"
        Write-Host "   Type: $($profileExchange.type)"
        Write-Host "   Durable: $($profileExchange.durable)"
        $testResults.ExchangesFound = $true
    } else {
        Write-Warning "[WARNING] profile.exchange NOT found"
    }
}
catch {
    Write-Error "[ERROR] Cannot fetch exchanges"
}

Write-SubHeader "Queues"

try {
    $queues = Invoke-RestMethod -Uri "$RabbitMQUrl/api/queues" -Headers $rabbitHeaders -Method GET -ErrorAction Stop

    $profileQueue = $queues | Where-Object { $_.name -eq "profile.events.queue" }

    if ($profileQueue) {
        Write-Success "[OK] profile.events.queue found"
        Write-Host "   Messages: $($profileQueue.messages)"
        Write-Host "   Consumers: $(if ($profileQueue.consumers) { $profileQueue.consumers } else { 0 })"
        $testResults.QueuesFound = $true
    } else {
        Write-Warning "[WARNING] profile.events.queue NOT found"
    }
}
catch {
    Write-Error "[ERROR] Cannot fetch queues"
}

Write-SubHeader "Queue Bindings"

try {
    $bindings = Invoke-RestMethod -Uri "$RabbitMQUrl/api/bindings" -Headers $rabbitHeaders -Method GET -ErrorAction Stop

    $profileBindings = $bindings | Where-Object { $_.source -eq "profile.exchange" }

    if ($profileBindings) {
        Write-Success "[OK] Profile Exchange Bindings found"
        foreach ($binding in $profileBindings) {
            Write-Host "   - $($binding.source) to $($binding.destination) (key: $($binding.routing_key))"
        }
        if ($profileBindings.Count -ge 2) {
            $testResults.BindingsCorrect = $true
        }
    } else {
        Write-Warning "[WARNING] No profile exchange bindings found"
    }
}
catch {
    Write-Error "[ERROR] Cannot fetch bindings"
}

Write-Header "Step 3: Message Publishing and Consumption Test"

Write-SubHeader "Creating test profile"

$timestamp = [DateTimeOffset]::UtcNow.ToUnixTimeSeconds()
$testUsername = "test_$timestamp"
$testEmail = "test_$timestamp@test.com"
$testPassword = "testpass123"

Write-Info "Registering caregiver..."
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
}
catch {
    Write-Error "[ERROR] Registration failed"
    Write-Host "   Error: $($_.Exception.Message)"
    exit 1
}

Write-Info "Recording queue status before profile creation..."
try {
    $queuesBefore = Invoke-RestMethod -Uri "$RabbitMQUrl/api/queues" -Headers $rabbitHeaders -Method GET -ErrorAction Stop
    $profileQueueBefore = $queuesBefore | Where-Object { $_.name -eq "profile.events.queue" }
    $messageCountBefore = if ($profileQueueBefore) { $profileQueueBefore.messages } else { 0 }
    Write-Host "   Queue messages: $messageCountBefore"
}
catch {
    Write-Warning "[WARNING] Could not read initial queue state"
    $messageCountBefore = 0
}

Write-Info "Creating patient profile..."
$profileBody = @{
    patientName     = "Test Patient"
    era            = "1960-1970"
    dementiaStage  = "mild"
    favoriteArtists = @("The Beatles", "Elvis Presley")
    symptoms       = @("memory_loss")
} | ConvertTo-Json

$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type"  = "application/json"
}

try {
    $profileResponse = Invoke-RestMethod -Uri "$ProfileServiceUrl/api/profiles" -Method POST -Headers $headers -Body $profileBody -ErrorAction Stop

    Write-Success "[OK] Profile created"
    $profileId = $profileResponse.id
    Write-Host "   Profile ID: $profileId"
    $testResults.MessagePublished = $true
}
catch {
    Write-Error "[ERROR] Profile creation failed"
    Write-Host "   Error: $($_.Exception.Message)"
    exit 1
}

Write-Info "Waiting for message consumption..."
Start-Sleep -Seconds $WaitForConsumption

Write-Info "Checking queue after consumption..."
try {
    $queuesAfter = Invoke-RestMethod -Uri "$RabbitMQUrl/api/queues" -Headers $rabbitHeaders -Method GET -ErrorAction Stop
    $profileQueueAfter = $queuesAfter | Where-Object { $_.name -eq "profile.events.queue" }
    $messageCountAfter = if ($profileQueueAfter) { $profileQueueAfter.messages } else { 0 }

    Write-Host "   Before: $messageCountBefore"
    Write-Host "   After: $messageCountAfter"

    if ($messageCountAfter -lt $messageCountBefore) {
        Write-Success "[OK] Message was consumed"
        $testResults.MessageConsumed = $true
    } else {
        Write-Warning "[WARNING] Message was not consumed"
    }
}
catch {
    Write-Warning "[WARNING] Could not verify consumption"
}

Write-SubHeader "Verifying cache population"

Write-Info "Generating playlist..."
try {
    $playlistBody = @{
        patientId       = $profileId
        careNeed        = "stress_relief"
        era             = "dummy"
        dementiaStage   = "dummy"
        favoriteArtists = @()
    } | ConvertTo-Json

    $playlistResponse = Invoke-RestMethod -Uri "$PlaylistServiceUrl/api/playlists/generate" -Method POST -Headers $headers -Body $playlistBody -ErrorAction Stop

    Write-Success "[OK] Playlist generated"
    Write-Host "   Tracks: $($playlistResponse.tracks.Count)"
    Write-Host "   Era: $($playlistResponse.era)"

    if ($playlistResponse.era -eq "1960-1970") {
        Write-Success "[OK] Cache was used correctly"
        $testResults.CachePopulated = $true
        $testResults.PlaylistGenerated = $true
    } else {
        Write-Warning "[WARNING] Cache may not be working"
    }
}
catch {
    Write-Error "[ERROR] Playlist generation failed"
    Write-Host "   Error: $($_.Exception.Message)"
}

Write-Header "Step 4: Summary Report"

Write-Host ""
Write-Host "Test Results:" -ForegroundColor Cyan

$results = @(
    @{ Name = "RabbitMQ Health"; Status = $testResults.RabbitMQHealth }
    @{ Name = "Exchanges Found"; Status = $testResults.ExchangesFound }
    @{ Name = "Queues Found"; Status = $testResults.QueuesFound }
    @{ Name = "Bindings Correct"; Status = $testResults.BindingsCorrect }
    @{ Name = "Message Published"; Status = $testResults.MessagePublished }
    @{ Name = "Message Consumed"; Status = $testResults.MessageConsumed }
    @{ Name = "Cache Populated"; Status = $testResults.CachePopulated }
    @{ Name = "Playlist Generated"; Status = $testResults.PlaylistGenerated }
)

foreach ($result in $results) {
    $icon = if ($result.Status) { "OK" } else { "FAIL" }
    $color = if ($result.Status) { "Green" } else { "Red" }
    Write-Host "   [$icon] $($result.Name)" -ForegroundColor $color
}

Write-Host ""

$passedTests = ($results | Where-Object { $_.Status }).Count
$totalTests = $results.Count

if ($passedTests -eq $totalTests) {
    Write-Success "All tests passed"
    Write-Host "RabbitMQ is working correctly"
}
else {
    Write-Critical "Some tests failed"
    Write-Host "Check logs and troubleshooting section"
}

Write-Host ""
Write-Host "Useful Commands:" -ForegroundColor Cyan
Write-Host "   Live monitoring: .\monitor-rabbitmq-queues-fixed.ps1"
Write-Host "   Message flow: .\verify-message-flow-fixed.ps1"
Write-Host "   RabbitMQ UI: http://localhost:15672 (guest/guest)"
Write-Host ""

Write-Success "Test Complete"
Write-Host ""

