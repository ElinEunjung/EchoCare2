
# =============================================================================
# RabbitMQ Queue Cleanup Script
# =============================================================================
# Purpose: Clean up orphaned queues that are blocking message flow
# =============================================================================

param(
    [switch]$Force  # Skip confirmation prompts
)

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

Write-Header "RabbitMQ Queue Cleanup"

# Check if Docker is running
Write-Info "Checking if Docker is running..."
try {
    $null = docker ps 2>&1
    Write-Success "[OK] Docker is running"
}
catch {
    Write-Error "[ERROR] Docker is not running or not installed!"
    Write-Host "   Please start Docker Desktop and try again"
    exit 1
}

# Check if RabbitMQ container exists
Write-Info "Checking for RabbitMQ container..."
$rabbitmqContainer = docker ps --filter "name=echocare-rabbitmq" --format "{{.Names}}"

if (-not $rabbitmqContainer) {
    Write-Error "[ERROR] RabbitMQ container 'echocare-rabbitmq' is not running!"
    Write-Host "   Start it with: docker-compose up -d rabbitmq"
    exit 1
}

Write-Success "[OK] Found RabbitMQ container: $rabbitmqContainer"

# List current queues
Write-Header "Current Queue Status"

Write-Info "Fetching queue list..."
try {
    $queues = docker exec echocare-rabbitmq rabbitmqctl list_queues name messages 2>&1 | Out-String
    Write-Host $queues
}
catch {
    Write-Warning "[WARNING] Could not list queues: $($_.Exception.Message)"
}

# Queues to delete
$queuesToDelete = @(
    "profile.created.queue",
    "profile.updated.queue"
)

Write-Header "Queues to Delete"

foreach ($queue in $queuesToDelete) {
    Write-Host "   - $queue"
}

Write-Host ""

# Confirm deletion
if (-not $Force) {
    Write-Warning "This will DELETE the above queues and ALL their messages!"
    $confirm = Read-Host "Continue? (y/N)"

    if ($confirm -ne 'y' -and $confirm -ne 'Y') {
        Write-Info "Operation cancelled"
        exit 0
    }
}

# Delete queues
Write-Header "Deleting Queues"

foreach ($queue in $queuesToDelete) {
    Write-Info "Deleting queue: $queue..."

    try {
        $result = docker exec echocare-rabbitmq rabbitmqctl delete_queue $queue 2>&1

        if ($LASTEXITCODE -eq 0) {
            Write-Success "[OK] Deleted: $queue"
        } else {
            Write-Warning "[WARNING] Queue may not exist: $queue"
            Write-Host "   $result"
        }
    }
    catch {
        Write-Warning "[WARNING] Could not delete $queue"
        Write-Host "   Error: $($_.Exception.Message)"
    }
}

# Verify deletion
Write-Header "Verification"

Write-Info "Fetching updated queue list..."
try {
    $queuesAfter = docker exec echocare-rabbitmq rabbitmqctl list_queues name messages 2>&1 | Out-String
    Write-Host $queuesAfter

    # Check if problem queues still exist
    if ($queuesAfter -match "profile\.created\.queue") {
        Write-Warning "[WARNING] profile.created.queue still exists!"
    } else {
        Write-Success "[OK] profile.created.queue removed"
    }

    if ($queuesAfter -match "profile\.updated\.queue") {
        Write-Warning "[WARNING] profile.updated.queue still exists!"
    } else {
        Write-Success "[OK] profile.updated.queue removed"
    }

    # Check that playlist queue exists
    if ($queuesAfter -match "playlist\.profile-events\.queue") {
        Write-Success "[OK] playlist.profile-events.queue exists (correct!)"
    } else {
        Write-Warning "[WARNING] playlist.profile-events.queue not found - will be created when playlist-service starts"
    }
}
catch {
    Write-Warning "[WARNING] Could not verify: $($_.Exception.Message)"
}

Write-Header "Next Steps"

Write-Host "1. Restart Profile Service:"
Write-Host "   cd services/profile-service"
Write-Host "   ./mvnw spring-boot:run"
Write-Host ""

Write-Host "2. Restart Playlist Service:"
Write-Host "   cd services/playlist-service"
Write-Host "   ./mvnw spring-boot:run"
Write-Host ""

Write-Host "3. Run diagnostic script:"
Write-Host "   cd test-data"
Write-Host "   .\diagnose-async-e2e-fixed.ps1"
Write-Host ""

Write-Success "Cleanup complete!"
Write-Host ""
Write-Host "Expected queue after services start:"
Write-Host "   - playlist.profile-events.queue (messages: 0)"
Write-Host ""
Write-Host "These queues should NOT exist:"
Write-Host "   - profile.created.queue"
Write-Host "   - profile.updated.queue"
Write-Host ""

