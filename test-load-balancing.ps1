# Test Load Balancing with Authentication

Write-Host "=== Step 1: Login ===" -ForegroundColor Green
$loginResponse = Invoke-RestMethod -Method POST -Uri "http://localhost:8080/api/auth/login" `
  -ContentType "application/json" `
  -Body '{"username":"sarah_wilson","password":"password123"}'

$token = $loginResponse.token
Write-Host "✅ Login successful! Token: $($token.Substring(0,20))..." -ForegroundColor Green

Write-Host "`n=== Step 2: Send 10 Requests ===" -ForegroundColor Green
for ($i=1; $i -le 10; $i++) {
    try {
        $response = Invoke-RestMethod -Method GET -Uri "http://localhost:8080/api/profiles" `
          -Headers @{"Authorization"="Bearer $token"}
        Write-Host "✅ Request $i successful - Received $($response.Count) profiles" -ForegroundColor Green
    } catch {
        Write-Host "❌ Request $i failed: $($_.Exception.Message)" -ForegroundColor Red
    }
    Start-Sleep -Milliseconds 200
}

Write-Host "`n=== Step 3: Check Load Distribution ===" -ForegroundColor Green
Write-Host "`nProfile Service 1:" -ForegroundColor Cyan
docker logs profile-service-1 --tail 20 | Select-String "GET /api/profiles"

Write-Host "`nProfile Service 2:" -ForegroundColor Cyan
docker logs profile-service-2 --tail 20 | Select-String "GET /api/profiles"

Write-Host "`n✅ Load balancing test complete!" -ForegroundColor Green
