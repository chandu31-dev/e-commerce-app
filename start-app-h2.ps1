#!/usr/bin/env pwsh
# Start Catchy application with H2 test profile (in-memory database)

$env:SPRING_PROFILES_ACTIVE = "test"
Write-Host "Starting Catchy application with H2 in-memory database (test profile)..."
Write-Host ""

& .\mvnw.cmd spring-boot:run -q
