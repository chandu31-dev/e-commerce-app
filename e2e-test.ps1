# E2E test script: start app, signup, login, vendor register, check dashboard

# Kill processes listening on 8080
try {
    $listeners = Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue
    if ($listeners) {
        $pids = $listeners | Select-Object -ExpandProperty OwningProcess -Unique
        foreach ($pid in $pids) {
            Write-Host "Stopping process $pid"
            Stop-Process -Id $pid -Force -ErrorAction SilentlyContinue
        }
    }
} catch { Write-Host "No listeners to stop: $_" }

# Start jar in background
Write-Host "Starting jar..."
Start-Process -FilePath 'java' -ArgumentList '-jar','target\\catchy-0.0.1-SNAPSHOT.jar' -NoNewWindow -RedirectStandardOutput run.out -RedirectStandardError err.log
Start-Sleep -Seconds 2

# Wait for server up
$up = $false
for ($i=0; $i -lt 60; $i++) {
    try {
        $r = Invoke-WebRequest -Uri 'http://localhost:8080' -UseBasicParsing -TimeoutSec 3 -ErrorAction Stop
        if ($r.StatusCode -eq 200) { Write-Host "Server is UP"; $up = $true; break }
    } catch { Start-Sleep -Seconds 1 }
}
if (-not $up) {
    Write-Host "Server did not become ready. Last 200 lines of run.out:";
    if (Test-Path run.out) { Get-Content run.out -Tail 200 };
    if (Test-Path err.log) { Write-Host "----- err.log -----"; Get-Content err.log -Tail 200 }
    exit 1
}

# Helper: pretty convert object
function pretty($o) { try { $o | ConvertTo-Json -Depth 5 } catch { $o.ToString() } }

# 1) Signup new user
$ts = [int][double]::Parse((Get-Date -UFormat %s))
$email = "e2e+$ts@test.local"
$pw = 'e2ePass123'
$signupBody = @{ name = 'E2E User'; email = $email; password = $pw; role = 'USER' } | ConvertTo-Json
Write-Host "Signing up: $email"
try {
    $signup = Invoke-RestMethod -Uri 'http://localhost:8080/api/auth/signup' -Method Post -ContentType 'application/json' -Body $signupBody -TimeoutSec 30
    Write-Host "Signup response:"; Write-Host (pretty $signup)
} catch { Write-Host "Signup error:"; Write-Host $_.Exception.Message; if ($_.Exception.Response) { try { $s = (New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())).ReadToEnd(); Write-Host $s } catch {} } }

# 2) Login new user
$loginBody = @{ email = $email; password = $pw } | ConvertTo-Json
Write-Host "Logging in as new user"
try {
    $login = Invoke-RestMethod -Uri 'http://localhost:8080/api/auth/login' -Method Post -ContentType 'application/json' -Body $loginBody -TimeoutSec 30
    Write-Host "Login response:"; Write-Host (pretty $login)
    $token = $login.token
} catch { Write-Host "Login error:"; Write-Host $_.Exception.Message; if ($_.Exception.Response) { try { $s = (New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())).ReadToEnd(); Write-Host $s } catch {} } }

if (-not $token) { Write-Host "No token received; aborting vendor flow"; exit 1 }

# 3) Vendor register
Write-Host "Registering vendor"
$headers = @{ Authorization = "Bearer $token" }
$form = @{ shopName='E2E Shop'; contactEmail=$email; description='desc'; phoneNumber='12345'; address='E2E address' }
try {
    $vendor = Invoke-RestMethod -Uri 'http://localhost:8080/vendor/api/register' -Method Post -Form $form -Headers $headers -TimeoutSec 30
    Write-Host "Vendor register response:"; Write-Host (pretty $vendor)
} catch { Write-Host "Vendor register error:"; Write-Host $_.Exception.Message; if ($_.Exception.Response) { try { $s = (New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())).ReadToEnd(); Write-Host $s } catch {} } }

# 4) Access vendor dashboard
Write-Host "Accessing vendor dashboard"
try {
    $dash = Invoke-WebRequest -Uri 'http://localhost:8080/vendor/dashboard' -Headers $headers -UseBasicParsing -TimeoutSec 30
    Write-Host "Dashboard status: $($dash.StatusCode)";
    $html = $dash.Content
    Write-Host "Dashboard content length: $($html.Length)"
} catch { Write-Host "Dashboard access error:"; Write-Host $_.Exception.Message; if ($_.Exception.Response) { try { $s = (New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())).ReadToEnd(); Write-Host $s } catch {} } }

# 5) Test seeded test user login
Write-Host "Logging seeded test user"
try {
    $testLogin = Invoke-RestMethod -Uri 'http://localhost:8080/api/auth/login' -Method Post -ContentType 'application/json' -Body (@{ email='user@catchy.com'; password='user123' } | ConvertTo-Json) -TimeoutSec 30
    Write-Host "Seed login:"; Write-Host (pretty $testLogin)
} catch { Write-Host "Seed login error:"; Write-Host $_.Exception.Message }

Write-Host "E2E script completed. Tail of application log:"
if (Test-Path run.out) { Get-Content run.out -Tail 200 }
if (Test-Path err.log) { Write-Host "----- err.log -----"; Get-Content err.log -Tail 200 }

Write-Host "Done." 
