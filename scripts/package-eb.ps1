param(
    [string]$JarPath = "target\catchy-0.0.1-SNAPSHOT.jar",
    [string]$OutputZip = "catchy-eb-deploy.zip"
)

if (-not (Test-Path $JarPath)) {
    Write-Error "Jar not found at $JarPath. Run .\mvnw -DskipTests package first."
    exit 1
}

$tmpDir = Join-Path $env:TEMP "catchy-eb"
if (Test-Path $tmpDir) { Remove-Item $tmpDir -Recurse -Force }
New-Item -ItemType Directory -Path $tmpDir | Out-Null

Copy-Item $JarPath -Destination (Join-Path $tmpDir "catchy-0.0.1-SNAPSHOT.jar")
Copy-Item "Procfile" -Destination $tmpDir

if (Test-Path $OutputZip) { Remove-Item $OutputZip -Force }
Add-Type -AssemblyName System.IO.Compression.FileSystem
[System.IO.Compression.ZipFile]::CreateFromDirectory($tmpDir, $OutputZip)

Write-Host "Created $OutputZip. Upload this file to Elastic Beanstalk (Java SE platform)." -ForegroundColor Green
