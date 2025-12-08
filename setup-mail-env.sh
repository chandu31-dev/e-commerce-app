#!/usr/bin/env bash
# Example setup script for Unix/macOS (Mailtrap and Gmail)
# Mailtrap (recommended for testing)
# Replace the placeholders with your Mailtrap credentials
export MAIL_HOST=smtp.mailtrap.io
export MAIL_PORT=2525
export MAIL_USERNAME=your_mailtrap_username
export MAIL_PASSWORD=your_mailtrap_password
export MAIL_ADMIN=admin@example.com

# To use Gmail instead, uncomment and set these (Gmail may require App Passwords / OAuth):
# export MAIL_HOST=smtp.gmail.com
# export MAIL_PORT=587
# export MAIL_USERNAME=your@gmail.com
# export MAIL_PASSWORD=your_app_password

echo "Mail environment variables set for this shell. Run ./mvnw spring-boot:run"