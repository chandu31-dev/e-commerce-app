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

if (-not $ready) {
    Write-Host 'APP_NOT_READY'
    if (Test-Path $log) { Write-Host '--- log tail ---'; Get-Content $log -Tail 200 }
    exit 2
}

Write-Host 'APP_READY'

# Prepare test data
$ts = [int][double]::Parse((Get-Date -UFormat %s))
$email = "vendor+$ts@example.local"
$pw = 'P@ssw0rd1'
$signupBody = @{name='AutoVendor'; email=$email; password=$pw; role='VENDOR'} | ConvertTo-Json
$session = New-Object Microsoft.PowerShell.Commands.WebRequestSession

Write-Host 'POST /api/auth/signup ...'
try {
    $resp = Invoke-WebRequest -Uri 'http://localhost:8080/api/auth/signup' -Method POST -ContentType 'application/json' -Body $signupBody -WebSession $session -UseBasicParsing -TimeoutSec 20
    Write-Host 'SIGNUP_STATUS:' $resp.StatusCode
    Write-Host 'SIGNUP_BODY:' $resp.Content
} catch {
    Write-Host 'SIGNUP_FAILED'
    if ($_.Exception.Response) {
        $r = $_.Exception.Response
        $sr = New-Object System.IO.StreamReader($r.GetResponseStream())
        Write-Host 'ERR_STATUS:' $r.StatusCode
        Write-Host $sr.ReadToEnd()
    }
    if (Test-Path $log) { Write-Host '--- log tail ---'; Get-Content $log -Tail 200 }
    exit 3
}

$sc = $null
if ($resp.Headers -and $resp.Headers['Set-Cookie']) {
    $sc = $resp.Headers['Set-Cookie']
    Write-Host 'SET_COOKIE_HEADER:' $sc
} else {
    Write-Host 'No Set-Cookie in signup response'
}

if ($sc) {
    $cookiePart = ($sc -split ';')[0]
    $parts = $cookiePart -split '='
    if ($parts.Length -ge 2) {
        $cname = $parts[0]
        $cval = $parts[1]
        $cookie = New-Object System.Net.Cookie($cname,$cval,'/','localhost')
        $session.Cookies.Add($cookie)
        Write-Host 'Cookie added to session:' $cname
    }
}

Write-Host 'GET /vendor/register ...'
try {
    $rpage = Invoke-WebRequest -Uri 'http://localhost:8080/vendor/register' -WebSession $session -UseBasicParsing -TimeoutSec 10
    Write-Host 'VENDOR_REGISTER_PAGE_STATUS:' $rpage.StatusCode
} catch {
    Write-Host 'VENDOR_REGISTER_PAGE_FAILED'
    if ($_.Exception.Response) {
        $rr = $_.Exception.Response
        $sr = New-Object System.IO.StreamReader($rr.GetResponseStream())
        Write-Host $sr.ReadToEnd()
    }
    if (Test-Path $log) { Write-Host '--- log tail ---'; Get-Content $log -Tail 200 }
    exit 4
}

$form = @{shopName='Auto Shop'; contactEmail=$email; description='Automated test shop'}
Write-Host 'POST /vendor/api/register ...'
try {
    $r2 = Invoke-WebRequest -Uri 'http://localhost:8080/vendor/api/register' -Method POST -WebSession $session -Body $form -UseBasicParsing -TimeoutSec 20
    Write-Host 'VENDOR_API_STATUS:' $r2.StatusCode
    Write-Host 'VENDOR_API_BODY:' $r2.Content
} catch {
    Write-Host 'VENDOR_API_FAILED'
    if ($_.Exception.Response) {
        $rr = $_.Exception.Response
        $sr = New-Object System.IO.StreamReader($rr.GetResponseStream())
        Write-Host 'ERR_STATUS:' $rr.StatusCode
        Write-Host $sr.ReadToEnd()
    }
    if (Test-Path $log) { Write-Host '--- log tail ---'; Get-Content $log -Tail 200 }
    exit 5
}

Write-Host 'END_TO_END_COMPLETE'
if (Test-Path $log) { Write-Host '--- log tail ---'; Get-Content $log -Tail 200 }
exit 0
