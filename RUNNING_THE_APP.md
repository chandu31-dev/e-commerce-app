# Catchy E-Commerce App - Setup & Running Guide

## Quick Start (Development)

### 1. Prerequisites
- Java 21+
- Maven 3.6+
- MySQL 8.0+ (running on localhost:3306)
- Stripe Account (for payment testing)

### 2. Database Setup
The app automatically creates the database if it doesn't exist. Ensure MySQL is running.

**Default credentials** (in `application.properties`):
```
Database: catchy
Username: root
Password: 3110
```

### 3. Environment Variables Setup (Windows PowerShell)

#### Option A: Temporary (current session only)
```powershell
$env:STRIPE_SECRET_KEY = 'sk_test_your_secret_key_here'
$env:STRIPE_PUBLISHABLE_KEY = 'pk_test_your_publishable_key_here'
$env:STRIPE_WEBHOOK_SECRET = 'whsec_your_webhook_secret_here'
```

#### Option B: Permanent (Windows system-wide)
```powershell
[Environment]::SetEnvironmentVariable("STRIPE_SECRET_KEY", "sk_test_your_secret_key_here", "User")
[Environment]::SetEnvironmentVariable("STRIPE_PUBLISHABLE_KEY", "pk_test_your_publishable_key_here", "User")
[Environment]::SetEnvironmentVariable("STRIPE_WEBHOOK_SECRET", "whsec_your_webhook_secret_here", "User")
```

Or run the provided script:
```cmd
.\setup-stripe-env.bat
```

### 4. Build & Run

#### Development Mode (without tests)
```bash
# From project root:
./mvnw -DskipTests=true spring-boot:run
```

#### Full Build with Tests
```bash
./mvnw clean install
./mvnw spring-boot:run
```

#### Production Build (JAR)
```bash
./mvnw clean package
java -jar target/catchy-0.0.1-SNAPSHOT.jar
```

### 5. Access the Application
- **Home Page**: http://localhost:8080/
- **Checkout (Payment)**: http://localhost:8080/checkout
- **Admin Dashboard**: http://localhost:8080/admin-dashboard
- **Vendor Dashboard**: http://localhost:8080/vendor-dashboard

---

## Payment Integration (Stripe)

### Getting Your Stripe Keys

1. Go to [Stripe Dashboard](https://dashboard.stripe.com/)
2. Login to your account
3. Navigate to **Developers** → **API Keys**
4. Copy your **Secret Key** (starts with `sk_test_` or `sk_live_`)
5. Copy your **Publishable Key** (starts with `pk_test_` or `pk_live_`)

### Setting Webhook Secret

1. In Stripe Dashboard, go to **Developers** → **Webhooks**
2. Create a new webhook endpoint pointing to: `https://your-domain/api/webhooks/stripe`
3. For local testing, use [Stripe CLI](https://stripe.com/docs/stripe-cli):
   ```bash
   stripe listen --forward-to localhost:8080/api/webhooks/stripe
   ```
4. Copy the webhook signing secret and set it as `STRIPE_WEBHOOK_SECRET`

### Testing with Stripe Test Cards

Use these test cards in the checkout form:

| Card Number | Use Case |
|---|---|
| 4242 4242 4242 4242 | Successful payment |
| 4000 0000 0000 0002 | Card decline |
| 5555 5555 5555 4444 | Mastercard |
| 3782 822463 10005 | Diners Club |

Expiration: Any future date  
CVC: Any 3-4 digits

### Currency: Indian Rupees (INR)

All amounts are handled in **Indian Rupees (₹)**.

**Amount Conversion:**
- Stripe API requires amounts in the smallest currency unit (paise for INR)
- 1 INR = 100 paise
- The app automatically converts INR → paise for Stripe

Example:
```
₹100 INR → 10,000 paise (sent to Stripe)
```

---

## Mail Configuration (Optional)

### Development (Default)
By default, emails are logged to console and saved to `target/` directory.

### Production (with Real SMTP)

#### Using Mailtrap (Recommended for Testing)

1. Go to [Mailtrap.io](https://mailtrap.io/)
2. Create a free account and inbox
3. Copy the SMTP credentials (username/password)
4. Set environment variables:

**PowerShell:**
```powershell
$env:MAIL_HOST = 'smtp.mailtrap.io'
$env:MAIL_PORT = '2525'
$env:MAIL_USERNAME = 'your-mailtrap-username'
$env:MAIL_PASSWORD = 'your-mailtrap-password'
```

#### Using Gmail

1. Enable [2-Step Verification](https://support.google.com/accounts/answer/185833)
2. Generate an [App Password](https://support.google.com/accounts/answer/185833)
3. Set environment variables:

**PowerShell:**
```powershell
$env:MAIL_HOST = 'smtp.gmail.com'
$env:MAIL_PORT = '587'
$env:MAIL_USERNAME = 'your-email@gmail.com'
$env:MAIL_PASSWORD = 'your-app-password'
```

Then run:
```bash
./mvnw -DskipTests=true spring-boot:run
```

---

## Troubleshooting

### Port 8080 Already in Use
```powershell
# Find process using port 8080
Get-NetTCPConnection -LocalPort 8080

# Kill the process (replace PID with actual process ID)
Stop-Process -Id <PID> -Force

# Or run on different port
./mvnw -DskipTests=true spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"
```

### Database Connection Failed
- Ensure MySQL is running: `Get-Service | Where-Object {$_.Name -like "*MySQL*"}`
- Verify credentials in `application.properties`
- Check MySQL is listening on localhost:3306

### Stripe Integration Not Working
- Verify environment variables are set: `$env:STRIPE_SECRET_KEY`
- Ensure keys start with `sk_test_` or `sk_live_`
- Check logs for: "Stripe initialized with secret key"

### Compilation Errors
```bash
# Clean and rebuild
./mvnw clean compile
./mvnw clean install
```

---

## Project Structure

```
src/main/
├── java/com/catchy/
│   ├── config/
│   │   ├── CacheConfig.java
│   │   ├── MailStartupChecker.java
│   │   └── PaymentConfig.java
│   ├── controller/
│   │   ├── PaymentController.java
│   │   └── StripeWebhookController.java
│   ├── service/
│   │   ├── PaymentService.java
│   │   ├── MailService.java
│   │   └── ... (other services)
│   ├── util/
│   │   └── CurrencyUtil.java (INR formatting & conversion)
│   ├── dto/
│   │   ├── PaymentIntentRequest.java
│   │   └── PaymentResponse.java
│   └── model/
│       ├── Payment.java
│       └── ... (other models)
├── resources/
│   ├── application.properties
│   ├── application-mail.example.properties
│   └── templates/
│       ├── checkout.html (Stripe.js integration)
│       └── ... (other templates)
```

---

## Key Features

### ✅ Payment Integration
- Stripe PaymentIntent API for secure payments
- Support for Indian Rupees (INR)
- Webhook handling for payment confirmations
- Refund support

### ✅ Security
- All sensitive keys stored in environment variables
- JWT-based authentication
- Spring Security integration
- Webhook signature verification

### ✅ Email Notifications
- Registration & email verification
- Order confirmations
- Payment receipts
- Password reset links

### ✅ Caching
- Spring Cache with Caffeine backend
- Configurable cache policies

---

## Useful Commands

```bash
# View app logs
./mvnw spring-boot:run | Tee-Object -FilePath logs.txt

# Run tests
./mvnw test

# Run specific test
./mvnw test -Dtest=PaymentServiceTest

# Generate JAR only (skip tests)
./mvnw package -DskipTests

# Run JAR directly
java -jar target/catchy-0.0.1-SNAPSHOT.jar

# Check Maven version
./mvnw --version

# Update dependencies
./mvnw dependency:update-snapshots
```

---

## Next Steps

1. **Configure Stripe**: Get test keys and set environment variables
2. **Setup Email** (Optional): Configure Mailtrap or Gmail for real emails
3. **Test Payment Flow**: Use test card 4242 4242 4242 4242
4. **Monitor Webhooks**: Use Stripe CLI or Stripe Dashboard
5. **Deploy**: Build JAR and deploy to production with live keys

---

## Support & Documentation

- [Stripe Documentation](https://stripe.com/docs)
- [Spring Boot Guide](https://spring.io/projects/spring-boot)
- [Thymeleaf Templates](https://www.thymeleaf.org/)
- [Project README](./README.md)

---

**Last Updated**: November 2025  
**Version**: 1.0 (Stable with Payment Integration & INR Support)
