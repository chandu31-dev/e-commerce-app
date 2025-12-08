@echo off
REM Catchy Payment Integration - Environment Setup Script for Windows
REM This script sets up the required environment variables for Stripe payment integration

echo.
echo ========================================
echo Catchy Payment Integration Setup
echo ========================================
echo.
echo This script will configure Stripe API keys as environment variables.
echo.

REM Prompt user for Stripe keys
set /p SECRET_KEY="Enter your Stripe SECRET KEY (sk_test_xxx): "
set /p PUBLISHABLE_KEY="Enter your Stripe PUBLISHABLE KEY (pk_test_xxx): "
set /p WEBHOOK_SECRET="Enter your Stripe WEBHOOK SECRET (whsec_xxx): "

REM Validate that keys are not empty
if "%SECRET_KEY%"=="" (
    echo ERROR: Secret key cannot be empty!
    pause
    exit /b 1
)

if "%PUBLISHABLE_KEY%"=="" (
    echo ERROR: Publishable key cannot be empty!
    pause
    exit /b 1
)

if "%WEBHOOK_SECRET%"=="" (
    echo WARNING: Webhook secret is empty. You can configure it later.
)

REM Set environment variables for current session
setx STRIPE_SECRET_KEY "%SECRET_KEY%"
setx STRIPE_PUBLISHABLE_KEY "%PUBLISHABLE_KEY%"
setx STRIPE_WEBHOOK_SECRET "%WEBHOOK_SECRET%"

REM Also set for current session (setx doesn't apply immediately)
set STRIPE_SECRET_KEY=%SECRET_KEY%
set STRIPE_PUBLISHABLE_KEY=%PUBLISHABLE_KEY%
set STRIPE_WEBHOOK_SECRET=%WEBHOOK_SECRET%

echo.
echo ========================================
echo ✓ Environment Variables Configured
echo ========================================
echo.
echo Stripe Configuration:
echo - SECRET_KEY: %SECRET_KEY:~0,20%...
echo - PUBLISHABLE_KEY: %PUBLISHABLE_KEY:~0,20%...
echo - WEBHOOK_SECRET: %WEBHOOK_SECRET:~0,20%...
echo.
echo NOTE: Please restart your terminal/IDE for changes to take effect.
echo.
echo Next Steps:
echo 1. Restart your terminal or IDE
echo 2. Run: mvn clean install
echo 3. Run: mvn spring-boot:run
echo 4. Test at: http://localhost:8080/checkout
echo.
echo Use test card: 4242 4242 4242 4242
echo.
pause
