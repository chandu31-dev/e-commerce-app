@echo off
REM Example setup script for Windows (Mailtrap and Gmail)
REM Mailtrap (recommended for testing)
REM Replace the placeholders with your Mailtrap credentials
set MAIL_HOST=smtp.mailtrap.io
set MAIL_PORT=2525
set MAIL_USERNAME=your_mailtrap_username
set MAIL_PASSWORD=your_mailtrap_password
set MAIL_ADMIN=admin@example.com

REM To use Gmail instead, uncomment and set these (Gmail may require App Passwords / OAuth):
REM set MAIL_HOST=smtp.gmail.com
REM set MAIL_PORT=587
REM set MAIL_USERNAME=your@gmail.com
REM set MAIL_PASSWORD=your_app_password

echo Mail environment variables set for current session. Now run:
echo mvnw.cmd spring-boot:run
pause