# =============================================================================
# EchoCare Profile Service - Auth Test Script
# Tests the /api/auth/register and /api/auth/login endpoints
# =============================================================================
param(
    [string]$BaseUrl = "http://localhost:8081",
    [string]$Username = "testuser_$(Get-Random -Minimum 1000 -Maximum 9999)",
    [string]$Password = "testpassword123",
    [string]$Email = "testuser_$(Get-Random -Minimum 1000 -Maximum 9999)@example.com"
)
# Colors for output
function Write-Success { Write-Host $args -ForegroundColor Green }
function Write-Error { Write-Host $args -ForegroundColor Red }
function Write-Info { Write-Host $args -ForegroundColor Cyan }
function Write-Warning { Write-Host $args -ForegroundColor Yellow }
Write-Host ""
Write-Host "=============================================" -ForegroundColor Magenta
Write-Host "   EchoCare Auth Integration Test Script    " -ForegroundColor Magenta
Write-Host "=============================================" -ForegroundColor Magenta
Write-Host ""
# =============================================================================
# Test 1: Register a new user
# =============================================================================
Write-Info "TEST 1: Register New User"
Write-Host "   URL: $BaseUrl/api/auth/register"
Write-Host "   Username: $Username"
Write-Host "   Email: $Email"
Write-Host ""
$registerBody = @{
    username = $Username
    password = $Password
    email    = $Email
} | ConvertTo-Json
try {
    $registerResponse = Invoke-RestMethod -Uri "$BaseUrl/api/auth/register" `
        -Method POST `
        -ContentType "application/json" `
        -Body $registerBody `
        -ErrorAction Stop
    Write-Success "Registration successful!"
    Write-Host "   Caregiver ID: $($registerResponse.caregiverId)"
    Write-Host "   Username: $($registerResponse.username)"
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
# Test 2: Try to register with duplicate username (should fail)
# =============================================================================
Write-Info "TEST 2: Register Duplicate Username (should fail)"
try {
    $duplicateResponse = Invoke-RestMethod -Uri "$BaseUrl/api/auth/register" `
        -Method POST `
        -ContentType "application/json" `
        -Body $registerBody `
        -ErrorAction Stop
    Write-Error "Test failed - duplicate registration should have been rejected!"
    exit 1
}
catch {
    Write-Success "Correctly rejected duplicate username!"
    Write-Host "   Error: $($_.Exception.Message)"
    Write-Host ""
}
# =============================================================================
# Test 3: Login with registered user
# =============================================================================
Write-Info "TEST 3: Login with Registered User"
$loginBody = @{
    username = $Username
    password = $Password
} | ConvertTo-Json
try {
    $loginResponse = Invoke-RestMethod -Uri "$BaseUrl/api/auth/login" `
        -Method POST `
        -ContentType "application/json" `
        -Body $loginBody `
        -ErrorAction Stop
    Write-Success "Login successful!"
    Write-Host "   Caregiver ID: $($loginResponse.caregiverId)"
    Write-Host "   Username: $($loginResponse.username)"
    Write-Host "   New Token: $($loginResponse.token.Substring(0, 50))..."
    Write-Host ""
    $loginToken = $loginResponse.token
}
catch {
    Write-Error "Login failed!"
    Write-Host "   Error: $($_.Exception.Message)"
    exit 1
}
# =============================================================================
# Test 4: Login with wrong password (should fail)
# =============================================================================
Write-Info "TEST 4: Login with Wrong Password (should fail)"
$wrongPasswordBody = @{
    username = $Username
    password = "wrongpassword"
} | ConvertTo-Json
try {
    $wrongResponse = Invoke-RestMethod -Uri "$BaseUrl/api/auth/login" `
        -Method POST `
        -ContentType "application/json" `
        -Body $wrongPasswordBody `
        -ErrorAction Stop
    Write-Error "Test failed - wrong password should have been rejected!"
    exit 1
}
catch {
    Write-Success "Correctly rejected wrong password!"
    Write-Host "   Error: $($_.Exception.Message)"
    Write-Host ""
}
# =============================================================================
# Test 5: Login with non-existent user (should fail)
# =============================================================================
Write-Info "TEST 5: Login with Non-existent User (should fail)"
$nonExistentBody = @{
    username = "nonexistent_user_12345"
    password = "somepassword"
} | ConvertTo-Json
try {
    $nonExistentResponse = Invoke-RestMethod -Uri "$BaseUrl/api/auth/login" `
        -Method POST `
        -ContentType "application/json" `
        -Body $nonExistentBody `
        -ErrorAction Stop
    Write-Error "Test failed - non-existent user should have been rejected!"
    exit 1
}
catch {
    Write-Success "Correctly rejected non-existent user!"
    Write-Host "   Error: $($_.Exception.Message)"
    Write-Host ""
}
# =============================================================================
# Summary
# =============================================================================
Write-Host "=============================================" -ForegroundColor Magenta
Write-Host "            TEST SUMMARY                     " -ForegroundColor Magenta
Write-Host "=============================================" -ForegroundColor Magenta
Write-Success "All tests passed!"
Write-Host ""
Write-Host "Test User Credentials:" -ForegroundColor Yellow
Write-Host "   Username: $Username"
Write-Host "   Password: $Password"
Write-Host "   Email: $Email"
Write-Host "   Caregiver ID: $caregiverId"
Write-Host ""
Write-Host "JWT Token (use for authenticated requests):" -ForegroundColor Yellow
Write-Host "   Authorization: Bearer $loginToken"
Write-Host ""
