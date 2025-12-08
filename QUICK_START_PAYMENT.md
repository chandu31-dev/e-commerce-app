# Quick Start - Payment Integration

## 🚀 Getting Started (5 minutes)

### 1. Set Environment Variables

**Windows PowerShell:**
```powershell
$env:STRIPE_SECRET_KEY = "sk_test_xxx..."
$env:STRIPE_PUBLISHABLE_KEY = "pk_test_xxx..."
$env:STRIPE_WEBHOOK_SECRET = "whsec_xxx..."
```

**Linux/Mac:**
```bash
export STRIPE_SECRET_KEY="sk_test_xxx..."
export STRIPE_PUBLISHABLE_KEY="pk_test_xxx..."
export STRIPE_WEBHOOK_SECRET="whsec_xxx..."
```

### 2. Build & Run

```bash
# Build project
mvn clean install

# Run application
mvn spring-boot:run
```

### 3. Test Payment Flow

#### Step 1: Create Order
```bash
POST http://localhost:8080/api/orders
Content-Type: application/json

{
    "totalPrice": 5999.50
}
```

#### Step 2: Get Payment Config
```bash
GET http://localhost:8080/api/payments/public/config
```

Response includes publishable key for frontend.

#### Step 3: Create Payment Intent
```bash
POST http://localhost:8080/api/payments/create-intent
Content-Type: application/json
Authorization: Bearer {JWT_TOKEN}

{
    "orderId": 1,
    "amountInr": 5999.50
}
```

Response:
```json
{
    "success": true,
    "clientSecret": "pi_xxx_secret_xxx",
    "paymentIntentId": "pi_xxx",
    "amount": 5999.50,
    "currency": "inr"
}
```

#### Step 4: Confirm Payment (After Stripe processes it)
```bash
POST http://localhost:8080/api/payments/confirm/{paymentIntentId}
Authorization: Bearer {JWT_TOKEN}
```

---

## 💳 Test Card Numbers

All test cards use expiry **12/99** and CVC **123** (or any future date and 3 digits):

| Card Type | Number | Result |
|-----------|--------|--------|
| Visa | 4242 4242 4242 4242 | ✅ Success |
| Visa | 4000 0000 0000 0002 | ❌ Declined |
| Mastercard | 5555 5555 5555 4444 | ✅ Success |
| American Express | 378282246310005 | ✅ Success |

---

## 📋 Important: INR Currency

All amounts are in **Indian Rupees (₹)**:

| Amount | Display | Stripe API |
|--------|---------|-----------|
| 1 INR | ₹ 1.00 | 100 paise |
| 100 INR | ₹ 100.00 | 10000 paise |
| 5999.50 INR | ₹ 5,999.50 | 599950 paise |

**Rule**: Multiply INR amount by 100 to get paise (Stripe uses smallest unit)

---

## 🔧 Code Examples

### Java: Create Payment
```java
@Autowired
private PaymentService paymentService;

// In your controller/service
Order order = orderRepository.findById(1L).get();
BigDecimal amountInr = BigDecimal.valueOf(5999.50);

PaymentResponse response = paymentService.createPaymentIntentForOrder(order, amountInr);

if (response.isSuccess()) {
    String clientSecret = response.getClientSecret();
    // Send to frontend
}
```

### Java: Confirm Payment
```java
PaymentResponse response = paymentService.confirmPaymentIntent(paymentIntentId);

if (response.isSuccess()) {
    System.out.println("Payment Status: " + response.getStatus());
    System.out.println("Amount: ₹ " + response.getAmount());
}
```

### Java: Format Currency
```java
import com.catchy.util.CurrencyUtil;

BigDecimal amount = BigDecimal.valueOf(5999.50);

// Convert to paise for Stripe
long paise = CurrencyUtil.convertInrToPaise(amount); // 599950

// Format for display
String display = CurrencyUtil.formatInr(amount); // "₹ 5,999.50"

// Get currency info
String symbol = CurrencyUtil.getCurrencySymbol(); // "₹"
String code = CurrencyUtil.getCurrencyCode(); // "INR"
```

### JavaScript: Process Payment
```javascript
// Initialize Stripe
const stripe = Stripe('pk_test_xxx...');
const elements = stripe.elements();
const cardElement = elements.create('card');
cardElement.mount('#card-element');

// Create payment intent
const response = await fetch('/api/payments/create-intent', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
        orderId: 1,
        amountInr: 5999.50
    })
});

const { clientSecret, success } = await response.json();

if (success) {
    // Confirm payment
    const result = await stripe.confirmCardPayment(clientSecret, {
        payment_method: {
            card: cardElement
        }
    });
    
    if (result.paymentIntent.status === 'succeeded') {
        console.log('✅ Payment successful!');
    }
}
```

---

## 📊 API Endpoints Quick Reference

| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/api/payments/create-intent` | Create Stripe payment intent |
| POST | `/api/payments/confirm/{id}` | Confirm payment after Stripe processes |
| GET | `/api/payments/order/{orderId}` | Get payment details by order |
| GET | `/api/payments/public/config` | Get public Stripe config |
| GET | `/api/payments/format-amount` | Format amount with currency |
| POST | `/api/webhooks/stripe` | Stripe webhook handler |

---

## ⚙️ Configuration Files

### application.properties
```properties
stripe.secret-key=${STRIPE_SECRET_KEY}
stripe.publishable-key=${STRIPE_PUBLISHABLE_KEY}
stripe.webhook-secret=${STRIPE_WEBHOOK_SECRET}
payment.currency=inr
payment.currency-symbol=₹
payment.currency-code=INR
```

### Maven Dependency (pom.xml)
```xml
<dependency>
    <groupId>com.stripe</groupId>
    <artifactId>stripe-java</artifactId>
    <version>25.6.0</version>
</dependency>
```

---

## 🔐 Security Checklist

- ✅ Secret keys stored in environment variables (never in code)
- ✅ HTTPS used for all payment endpoints
- ✅ Webhook signatures verified
- ✅ Payment amounts validated on backend
- ✅ Order ownership verified before processing
- ✅ Transaction IDs stored securely
- ✅ No card data stored locally
- ✅ Logging excludes sensitive information

---

## 🐛 Debugging

### Check Stripe Configuration
```bash
curl -X GET http://localhost:8080/api/payments/public/config
```

Should return publishable key and currency info.

### Monitor Webhook Events
1. Go to Stripe Dashboard → Developers → Webhooks
2. Click your endpoint
3. View recent events
4. Check "Event attempts" for errors

### Check Logs
```bash
# Spring Boot logs
tail -f logs/spring.log

# Look for:
# - "Payment Intent created"
# - "Payment confirmed"
# - Any error messages
```

---

## 🚨 Common Issues

### Issue: "Stripe secret key not configured"
**Solution**: Set environment variable `STRIPE_SECRET_KEY` or add to `application.properties`

### Issue: "Payment intent not created"
**Solution**: 
- Verify Stripe credentials
- Check amount > 0
- Ensure order exists

### Issue: "Webhook events not received"
**Solution**:
- Verify webhook URL is publicly accessible
- Check webhook secret in environment
- Monitor Stripe Dashboard → Logs

### Issue: "Currency not displaying correctly"
**Solution**: Use `CurrencyUtil.formatInr()` method for display

---

## 📞 Support

- **Stripe Docs**: https://stripe.com/docs
- **Stripe Support**: https://support.stripe.com
- **GitHub Issues**: Create issue with logs and error details

---

**Remember**: 
- 🔐 All amounts are in INR (₹)
- 💰 Smallest unit is Paise (1 INR = 100 Paise)
- ✅ Always test with test cards first
- 🚀 Move keys to environment variables in production
