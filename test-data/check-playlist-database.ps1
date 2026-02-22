# Database Verification Script
# Checks if the playlist database has been seeded with test data
Write-Host "`n[CHECK] Checking Playlist Database Seed Data" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan
# Docker connection info
$dbHost = "localhost"
$dbPort = "5433"
$dbName = "playlist_db"
$dbUser = "playlist_user"
$dbPassword = "playlist_pass"
Write-Host "Database Connection Info:" -ForegroundColor Yellow
Write-Host "  Host: $dbHost" -ForegroundColor Gray
Write-Host "  Port: $dbPort" -ForegroundColor Gray
Write-Host "  Database: $dbName" -ForegroundColor Gray
Write-Host "  User: $dbUser`n" -ForegroundColor Gray
# Check if PostgreSQL client is available
$psqlAvailable = $null -ne (Get-Command psql -ErrorAction SilentlyContinue)
if (-not $psqlAvailable) {
    Write-Host "[WARNING] PostgreSQL client (psql) is not available" -ForegroundColor Yellow
    Write-Host "   Alternative: Check database through Docker:`n" -ForegroundColor Gray
    Write-Host "   docker exec -it echocare-playlist-db psql -U playlist_user -d playlist_db -c ''SELECT COUNT(*) FROM songs;''" -ForegroundColor Cyan
    Write-Host ""
    # Try using docker exec instead
    Write-Host "Attempting to check database using Docker..." -ForegroundColor Yellow
    try {
        $result = docker exec echocare-playlist-db psql -U playlist_user -d playlist_db -t -c "SELECT COUNT(*) FROM songs;" 2>&1
        if ($result) {
            # Handle array or string result
            if ($result -is [array]) {
                $countLine = $result | Where-Object { $_ -match '^\s*\d+\s*$' } | Select-Object -First 1
                $count = $countLine.Trim()
            } else {
                $count = $result.Trim()
            }
            Write-Host "[OK] Database accessible via Docker" -ForegroundColor Green
            Write-Host "   Songs in database: $count`n" -ForegroundColor Gray
            if ([int]$count -eq 60) {
                Write-Host "[SUCCESS] Database is properly seeded with 60 songs!" -ForegroundColor Green
            } elseif ([int]$count -gt 0) {
                Write-Host "[WARNING] Database has $count songs (expected 60)" -ForegroundColor Yellow
            } else {
                Write-Host "[ERROR] Database is empty! Seed data not loaded." -ForegroundColor Red
                Write-Host "   Make sure application.yml has SQL initialization configured." -ForegroundColor Gray
            }
        }
    }
    catch {
        Write-Host "[ERROR] Could not access database" -ForegroundColor Red
        Write-Host "   Error: $($_.Exception.Message)`n" -ForegroundColor Red
    }
} else {
    Write-Host "Checking database connection..." -ForegroundColor Yellow
    # Set password environment variable for psql
    $env:PGPASSWORD = $dbPassword
    try {
        # Test connection
        $testQuery = "SELECT COUNT(*) as count FROM songs;"
        $result = psql -h $dbHost -p $dbPort -U $dbUser -d $dbName -t -c $testQuery 2>&1
        if ($LASTEXITCODE -eq 0) {
            $count = $result.Trim()
            Write-Host "[OK] Database connection successful" -ForegroundColor Green
            Write-Host "   Songs in database: $count`n" -ForegroundColor Gray
            if ([int]$count -eq 60) {
                Write-Host "[SUCCESS] Database is properly seeded with 60 songs!" -ForegroundColor Green
                # Show sample songs
                Write-Host "`nSample songs from database:" -ForegroundColor Yellow
                $sampleQuery = "SELECT title, artist, release_year, bpm, energy FROM songs LIMIT 5;"
                psql -h $dbHost -p $dbPort -U $dbUser -d $dbName -c $sampleQuery
            } elseif ([int]$count -gt 0) {
                Write-Host "[WARNING] Database has $count songs (expected 60)" -ForegroundColor Yellow
            } else {
                Write-Host "[ERROR] Database is empty! Seed data not loaded." -ForegroundColor Red
                Write-Host "   Make sure application.yml has SQL initialization configured." -ForegroundColor Gray
            }
        } else {
            Write-Host "[ERROR] Database connection failed" -ForegroundColor Red
            Write-Host "   $result" -ForegroundColor Red
        }
    }
    catch {
        Write-Host "[ERROR] Error connecting to database" -ForegroundColor Red
        Write-Host "   $($_.Exception.Message)" -ForegroundColor Red
    }
    finally {
        # Clear password from environment
        Remove-Item Env:\PGPASSWORD -ErrorAction SilentlyContinue
    }
}
Write-Host ""
