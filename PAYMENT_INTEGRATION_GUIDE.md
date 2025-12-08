# Payment Integration Setup Guide - Indian Rupees (INR)

## Overview
This guide explains the complete payment integration setup for Catchy e-commerce platform using **Stripe** with **Indian Rupees (₹)** currency support.

---

## Prerequisites
- Stripe account (free at https://stripe.com)
- Spring Boot 3.5.7 or higher
- Java 21

---

## Step 1: Get Stripe API Keys

1. Go to [Stripe Dashboard](https://dashboard.stripe.com)
2. Sign up or log in to your account
3. Navigate to **Developers → API Keys**
4. Copy your keys:
   - **Publishable Key** (starts with `pk_`)
   - **Secret Key** (starts with `sk_`)
   - **Webhook Secret** (from Webhooks section)

---

## Step 2: Configure Environment Variables

Set up the following environment variables on your system:

### Windows (PowerShell)
```powershell
$env:STRIPE_SECRET_KEY = "sk_test_your_secret_key_here"
$env:STRIPE_PUBLISHABLE_KEY = "pk_test_your_publishable_key_here"
$env:STRIPE_WEBHOOK_SECRET = "whsec_test_your_webhook_secret_here"
```

### Linux/Mac
```bash
export STRIPE_SECRET_KEY="sk_test_your_secret_key_here"
export STRIPE_PUBLISHABLE_KEY="pk_test_your_publishable_key_here"
export STRIPE_WEBHOOK_SECRET="whsec_test_your_webhook_secret_here"
```

Or add to `application-prod.properties`:
```properties
stripe.secret-key=sk_test_your_secret_key_here
stripe.publishable-key=pk_test_your_publishable_key_here
stripe.webhook-secret=whsec_test_your_webhook_secret_here
```

---

## Step 3: Database Schema

The Payment model has been created with the following fields:

```sql
CREATE TABLE payments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    stripe_payment_intent_id VARCHAR(255) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    amount DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(5) DEFAULT 'inr',
    payment_method VARCHAR(50),
    transaction_id VARCHAR(255),
    failure_reason VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id)
);
```

The migration is automatically handled by Flyway/Hibernate.

---

## Step 4: API Endpoints

### 1. Create Payment Intent
**POST** `/api/payments/create-intent`

Request:
```json
{
    "orderId": 1,
    "amountInr": 5999.50
}
```

Response:
```json
{
    "success": true,
    "paymentId": 123,
    "orderId": 1,
    "stripePaymentIntentId": "pi_xxx...",
    "clientSecret": "pi_xxx_secret_xxx...",
    "amount": 5999.50,
    "currency": "inr",
    "currencySymbol": "₹",
    "status": "PENDING",
    "message": "Payment intent created successfully"
}
```

### 2. Confirm Payment
**POST** `/api/payments/confirm/{paymentIntentId}`

Response:
```json
{
    "success": true,
    "paymentId": 123,
    "orderId": 1,
    "status": "SUCCEEDED",
    "amount": 5999.50,
    "currency": "inr",
    "transactionId": "ch_xxx...",
    "message": "Payment successful!"
}
```

### 3. Get Payment by Order
**GET** `/api/payments/order/{orderId}`

### 4. Get Public Config
**GET** `/api/payments/public/config`

Response:
```json
{
    "publishableKey": "pk_test_xxx...",
    "currency": "inr",
    "currencySymbol": "₹",
    "currencyCode": "INR"
}
```

### 5. Format Amount
**GET** `/api/payments/format-amount?amount=5999.50`

Response:
```json
{
    "amount": "5999.50",
    "formatted": "₹ 5,999.50",
    "currency": "INR",
    "symbol": "₹",
    "paise": "599950"
}
```

---

## Step 5: Frontend Integration

### In Your HTML/Template

```html
<script src="https://js.stripe.com/v3/"></script>
<script>
    // Initialize Stripe with your publishable key
    const stripe = Stripe('pk_test_xxx...');
    const elements = stripe.elements();
    const cardElement = elements.create('card');
    cardElement.mount('#card-element');
    
    // Handle card payment
    const form = document.getElementById('paymentForm');
    form.addEventListener('submit', async (event) => {
        event.preventDefault();
        
        // Create payment intent on backend
        const response = await fetch('/api/payments/create-intent', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                orderId: 1,
                amountInr: 5999.50
            })
        });
        
        const data = await response.json();
        
        // Confirm payment with card
        const result = await stripe.confirmCardPayment(data.clientSecret, {
            payment_method: {
                card: cardElement
            }
        });
        
        if (result.error) {
            console.error('Payment failed:', result.error.message);
        } else {
            console.log('Payment successful!', result.paymentIntent);
        }
    });
</script>
```

---

## Step 6: Webhook Setup

### Configure Webhook in Stripe Dashboard

1. Go to **Developers → Webhooks**
2. Click **Add endpoint**
3. Enter your URL: `https://yourdomain.com/api/webhooks/stripe`
4. Select events:
   - `payment_intent.succeeded`
   - `payment_intent.payment_failed`
   - `payment_intent.canceled`
   - `charge.refunded`
5. Copy the webhook secret and add to environment variables

### Webhook Listener
The webhook controller is available at:
```
/api/webhooks/stripe
```

---

## Step 7: Currency Utilities

### Using CurrencyUtil Class

```java
import com.catchy.util.CurrencyUtil;

// Convert INR to Paise
BigDecimal amountInr = BigDecimal.valueOf(500);
long paise = CurrencyUtil.convertInrToPaise(amountInr); // 50000

// Convert Paise to INR
BigDecimal inr = CurrencyUtil.convertPaiseToInr(50000); // 500.00

// Format with currency symbol
String formatted = CurrencyUtil.formatInr(amountInr); // ₹ 500.00

// Get currency info
String code = CurrencyUtil.getCurrencyCode(); // "INR"
String symbol = CurrencyUtil.getCurrencySymbol(); // "₹"

// Validate amount
boolean valid = CurrencyUtil.isValidAmount(amountInr); // true if > 0
```

---

## Step 8: Payment Flow

### Complete Payment Flow:

```
1. User adds items to cart
   ↓
2. User clicks "Checkout"
   ↓
3. Order created in database (status: PENDING)
   ↓
4. Frontend calls POST /api/payments/create-intent
   ↓
5. Backend creates Stripe PaymentIntent in INR
   ↓
6. Backend creates Payment record in database
   ↓
7. Frontend receives clientSecret
   ↓
8. Frontend shows Stripe card element
   ↓
9. User enters card details and clicks "Pay"
   ↓
10. Frontend calls stripe.confirmCardPayment()
    ↓
11. Stripe processes payment
    ↓
12. Stripe sends webhook event to backend
    ↓
13. Backend updates Payment status
    ↓
14. Frontend confirms payment success
    ↓
15. User redirected to order confirmation page
```

---

## Testing with Test Card Numbers

Use these in test mode:

| Card Type | Card Number | Expiry | CVC |
|-----------|-------------|--------|-----|
| Visa | 4242 4242 4242 4242 | Any future date | Any 3 digits |
| Visa (decline) | 4000 0000 0000 0002 | Any future date | Any 3 digits |
| Mastercard | 5555 5555 5555 4444 | Any future date | Any 3 digits |
| Amex | 3782 822463 10005 | Any future date | Any 4 digits |

---

## Environment Configuration

### application.properties (Development)
```properties
# Stripe Configuration
stripe.secret-key=${STRIPE_SECRET_KEY:sk_test_dummy}
stripe.publishable-key=${STRIPE_PUBLISHABLE_KEY:pk_test_dummy}
stripe.webhook-secret=${STRIPE_WEBHOOK_SECRET:whsec_test_dummy}

# Currency Settings - Indian Rupees (INR)
payment.currency=inr
payment.currency-symbol=₹
payment.currency-code=INR
```

### application-prod.properties (Production)
```properties
stripe.secret-key=${STRIPE_SECRET_KEY}
stripe.publishable-key=${STRIPE_PUBLISHABLE_KEY}
stripe.webhook-secret=${STRIPE_WEBHOOK_SECRET}

payment.currency=inr
payment.currency-symbol=₹
payment.currency-code=INR
```

---

## Important Notes

### INR Currency Details
- **Smallest unit**: Paise (1 INR = 100 Paise)
- **Symbol**: ₹
- **Code**: INR
- **All amounts** are converted to Paise for Stripe API (multiply by 100)
- **All amounts** should be displayed with ₹ symbol to users

### Security
- ✅ Never expose secret keys in frontend code
- ✅ Always verify webhook signatures
- ✅ Use HTTPS for all payment endpoints
- ✅ Store sensitive data (transaction IDs) securely
- ✅ Never log complete card details

### Error Handling
Common error responses:

```json
{
    "success": false,
    "message": "Invalid amount. Must be greater than 0"
}
```

```json
{
    "success": false,
    "message": "Order not found"
}
```

---

## Troubleshooting

### Payment Intent Creation Fails
- Check Stripe API keys are correctly set
- Verify amount is positive and valid
- Ensure order exists in database

### Webhook Events Not Received
- Verify webhook URL is accessible
- Check webhook secret matches
- Monitor Stripe Dashboard → Logs

### Currency Display Issues
- Ensure CurrencyUtil is used for formatting
- Verify database stores amounts as DECIMAL(10,2)
- Check frontend displays ₹ symbol

---

## Files Created/Modified

### New Files
- `PaymentIntentRequest.java` - Request DTO
- `PaymentResponse.java` - Response DTO
- `PaymentConfig.java` - Configuration class
- `CurrencyUtil.java` - Currency utility functions
- `StripeWebhookController.java` - Webhook handler
- `checkout.html` - Enhanced checkout page

### Modified Files
- `pom.xml` - Added Stripe dependency
- `application.properties` - Added Stripe configuration
- `PaymentController.java` - Enhanced with new endpoints
- `PaymentService.java` - Enhanced payment operations

---

## Support & References

- [Stripe Documentation](https://stripe.com/docs)
- [Stripe Java SDK](https://github.com/stripe/stripe-java)
- [Stripe Test Mode](https://stripe.com/docs/testing)
- [Stripe Webhooks](https://stripe.com/docs/webhooks)
- [INR Payment Guide](https://stripe.com/docs/currencies/india)

---

**Last Updated**: 2025-11-12
**Version**: 1.0.0
**Currency**: Indian Rupees (INR - ₹)
