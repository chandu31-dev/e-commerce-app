$log = 'mvnw-run.log'
if (Test-Path $log) { Remove-Item $log -ErrorAction SilentlyContinue }
Write-Host "Starting mvnw in background..."
Start-Process -FilePath 'cmd.exe' -ArgumentList '/c .\mvnw spring-boot:run > mvnw-run.log 2>&1' -WindowStyle Hidden

# Wait for app to be ready
$max = 60
$ready = $false
for ($i = 0; $i -lt $max; $i++) {
    try {
        $r = Invoke-WebRequest -Uri 'http://localhost:8080' -UseBasicParsing -TimeoutSec 3
        if ($r.StatusCode -eq 200) { $ready = $true; break }
    } catch {}
    Start-Sleep -Seconds 1
}

if ($ready) {
    Write-Host 'APP_READY'
    Write-Host "Visit http://localhost:8080 to begin testing. Logs: $log"
    exit 0
} else {
    Write-Host 'APP_NOT_READY'
    if (Test-Path $log) { Write-Host '--- log tail ---'; Get-Content $log -Tail 200 }
    exit 1
}