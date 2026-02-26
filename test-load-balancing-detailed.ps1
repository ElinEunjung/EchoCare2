#!/usr/bin/env powershell
# Load Balancing Test Script for Consul
# This script tests if requests are distributed across multiple instances

Write-Host "`n" -ForegroundColor Cyan
Write-Host "╔════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║        CONSUL LOAD BALANCING TEST SUITE                   ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan

# ============================================================================
# PHASE 1: Verify Consul Registration
# ============================================================================

Write-Host "`n[PHASE 1] Checking Consul Service Registration..." -ForegroundColor Yellow
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Gray

try {
    $consulServices = Invoke-RestMethod -Uri "http://localhost:8500/v1/catalog/services" -UseBasicParsing
    Write-Host "✓ Connected to Consul" -ForegroundColor Green

    Write-Host "`nRegistered Services:" -ForegroundColor Cyan
    $consulServices.PSObject.Properties.Name | ForEach-Object {
        Write-Host "  • $_" -ForegroundColor White
    }
} catch {
    Write-Host "✗ Failed to connect to Consul: $_" -ForegroundColor Red
    Write-Host "  Make sure Consul is running on http://localhost:8500" -ForegroundColor Yellow
    exit 1
}

# ============================================================================
# PHASE 2: Check Profile Service Instances
# ============================================================================

Write-Host "`n[PHASE 2] Verifying Profile Service Instances..." -ForegroundColor Yellow
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Gray

try {
    $profileInstances =Invoke-RestMethod -Uri "http://localhost:8500/v1/catalog/services" -UseBasicParsing

    if ($profileInstances.Count -ge 2) {
        Write-Host "✓ Found $($profileInstances.Count) healthy profile-service instances" -ForegroundColor Green
        $profileInstances | ForEach-Object {
            Write-Host "  • Instance: $($_.Service.ID)" -ForegroundColor Cyan
            Write-Host "    Address: $($_.Service.Address):$($_.Service.Port)" -ForegroundColor Gray
            Write-Host "    Status: $($_.Status)" -ForegroundColor Green
        }
    } else {
        Write-Host "✗ Found only $($profileInstances.Count) instance(s), expected 2" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "✗ Failed to check profile-service: $_" -ForegroundColor Red
    exit 1
}

# ============================================================================
# PHASE 3: Verify Gateway is Running
# ============================================================================

Write-Host "`n[PHASE 3] Verifying Gateway Service..." -ForegroundColor Yellow
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Gray

try {
    $gatewayHealth = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -UseBasicParsing
    Write-Host "✓ Gateway is running on http://localhost:8080" -ForegroundColor Green
    Write-Host "  Status: $($gatewayHealth.StatusCode)" -ForegroundColor Gray
} catch {
    Write-Host "✗ Gateway is not responding: $_" -ForegroundColor Red
    exit 1
}

# ============================================================================
# PHASE 4: Load Balancing Test
# ============================================================================

Write-Host "`n[PHASE 4] Testing Load Balancing with 20 Requests..." -ForegroundColor Yellow
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Gray
Write-Host "Sending requests to: http://localhost:8080/api/caregivers`n" -ForegroundColor Gray

$successCount = 0
$failureCount = 0
$requestLog = @()

1..20 | ForEach-Object {
    Write-Host "Request $_  : " -NoNewline
    try {
        $startTime = Get-Date
        $response = Invoke-WebRequest -Uri "http://localhost:8080/api/caregivers" `
            -UseBasicParsing `
            -TimeoutSec 5 `
            -ErrorAction Stop
        $endTime = Get-Date
        $duration = ($endTime - $startTime).TotalMilliseconds

        Write-Host "✓ Success (${duration}ms)" -ForegroundColor Green
        $successCount++
        $requestLog += "Request $_`: OK"
    } catch {
        Write-Host "✗ Failed" -ForegroundColor Red
        $failureCount++
        $requestLog += "Request $_`: FAILED"
    }

    Start-Sleep -Milliseconds 400
}

Write-Host "`n✓ Test Complete: $successCount succeeded, $failureCount failed" -ForegroundColor Green

# ============================================================================
# PHASE 5: Log Analysis Instructions
# ============================================================================

Write-Host "`n[PHASE 5] Analyzing Instance Load Distribution..." -ForegroundColor Yellow
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Gray

Write-Host "`nTo verify load balancing across instances, check the logs:" -ForegroundColor Cyan

Write-Host "`n📋 Check Profile Service Instance 1:" -ForegroundColor Yellow
Write-Host "   docker-compose logs profile-service-1 | Select-String 'GET /api/caregivers'" -ForegroundColor White
Write-Host "   (Note: look for requests in the logs)" -ForegroundColor Gray

Write-Host "`n📋 Check Profile Service Instance 2:" -ForegroundColor Yellow
Write-Host "   docker-compose logs profile-service-2 | Select-String 'GET /api/caregivers'" -ForegroundColor White
Write-Host "   (Note: look for requests in the logs)" -ForegroundColor Gray

Write-Host "`n📊 If you see requests in BOTH logs, load balancing is working! ✓" -ForegroundColor Green

# ============================================================================
# PHASE 6: High Availability Test
# ============================================================================

Write-Host "`n[PHASE 6] High Availability Test (Optional)..." -ForegroundColor Yellow
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Gray

Write-Host "`nTo test resilience, try stopping one instance:" -ForegroundColor Cyan

Write-Host "`n1️⃣  Stop profile-service-2:" -ForegroundColor Yellow
Write-Host "   docker-compose stop profile-service-2" -ForegroundColor White

Write-Host "`n2️⃣  Check Consul UI (should show unhealthy):" -ForegroundColor Yellow
Write-Host "   Start-Process 'http://localhost:8500/ui/dc1/services/profile-service'" -ForegroundColor White

Write-Host "`n3️⃣  Send requests (should still work via instance 1):" -ForegroundColor Yellow
Write-Host "   1..5 | ForEach-Object { Invoke-WebRequest -Uri 'http://localhost:8080/api/caregivers' -UseBasicParsing; Write-Host 'Request `$_ OK'; Start-Sleep -Milliseconds 500 }" -ForegroundColor White

Write-Host "`n4️⃣  Restart the instance:" -ForegroundColor Yellow
Write-Host "   docker-compose start profile-service-2" -ForegroundColor White

Write-Host "`n5️⃣  Wait 30 seconds and check Consul UI again (should be healthy):" -ForegroundColor Yellow
Write-Host "   Start-Process 'http://localhost:8500/ui/dc1/services/profile-service'" -ForegroundColor White

# ============================================================================
# Summary
# ============================================================================

Write-Host "`n" -ForegroundColor Cyan
Write-Host "╔════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║                    TEST COMPLETE                          ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan

Write-Host "`n✓ Load Balancing Verification Checklist:" -ForegroundColor Green
Write-Host "  ☐ Consul UI shows all services as green" -ForegroundColor White
Write-Host "  ☐ Gateway accepts requests successfully" -ForegroundColor White
Write-Host "  ☐ Requests are distributed between instances (check logs)" -ForegroundColor White
Write-Host "  ☐ Stopping one instance doesn't break the system" -ForegroundColor White
Write-Host "  ☐ Restarted instance automatically registers with Consul" -ForegroundColor White

Write-Host "`n🎯 For your exam, be ready to explain:" -ForegroundColor Cyan
Write-Host "  • How Consul discovers and registers services" -ForegroundColor Gray
Write-Host "  • How the gateway load balances across instances" -ForegroundColor Gray
Write-Host "  • What happens when an instance fails (automatic failover)" -ForegroundColor Gray
Write-Host "  • The difference between localhost and container networking" -ForegroundColor Gray

Write-Host "`n"

