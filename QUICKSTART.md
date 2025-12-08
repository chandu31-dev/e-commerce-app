# Quick Start Guide - PayU + Features Integration

## ✅ BUILD STATUS: SUCCESSFUL
- **JAR Built**: `catchy-0.0.1-SNAPSHOT.jar` (65.8 MB)
- **All Tests Passing**: 7/7 ✅
- **Compile Errors**: 0 ✅
- **Runtime**: Ready

---

## QUICK START

### 1. Prerequisites
```
Java 21+ 
Maven 3.8+
MySQL 8.0+
Git
```

### 2. Environment Setup (Production)

#### For PayU Payments:
```bash
# Linux/Mac
export PAYU_MERCHANT_KEY="your_merchant_key"
export PAYU_MERCHANT_SALT="your_merchant_salt"

# Windows PowerShell
$env:PAYU_MERCHANT_KEY="your_merchant_key"
$env:PAYU_MERCHANT_SALT="your_merchant_salt"
```

#### For Gmail/SMTP:
```bash
export MAIL_HOST="smtp.gmail.com"
export MAIL_USERNAME="your-email@gmail.com"
export MAIL_PASSWORD="your-app-password"
```

#### For Database:
```bash
export DB_HOST="localhost"
export DB_PORT="3306"
export DB_NAME="catchy"
export DB_USER="root"
export DB_PASSWORD="3110"
```

### 3. Build Application
```bash
cd catchy
./mvnw clean package -DskipTests
```

### 4. Run Application
```bash
./mvnw spring-boot:run
```

**Application will start on**: `http://localhost:8080`

### 5. Run Tests
```bash
./mvnw test
```

### 6. Access the Application
- **Frontend**: http://localhost:8080
- **API**: http://localhost:8080/api
- **H2 Console** (dev): http://localhost:8080/h2-console
- **Admin Email**: chandukiranpotru0@gmail.com
- **Admin Password**: admin123

---

## PAYMENT FLOW (PayU - INR)

### Checkout Process
1. User adds items to cart
2. Applies coupon (if available)
3. Proceeds to checkout
4. Cart merged if guest account
5. **Click Pay** → Redirects to PayU
6. Complete payment in INR
7. PayU confirms payment
8. Order created with payment status

### Test Payment (PayU Sandbox)
- Merchant Key: `TESTKEY` (default)
- Merchant Salt: `TESTSALT` (default)
- Payment URL: `https://secure.payu.in/_payment`

---

## DATABASE SETUP

### MySQL Database
```sql
CREATE DATABASE catchy CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE catchy;
-- Tables auto-created by Hibernate (ddl-auto=update)
```

### Test Database
- **Type**: H2 In-Memory
- **Auto-Init**: From `schema.sql`
- **Data Seeding**: From `DataSeeder.java`

---

## KEY FEATURES IMPLEMENTED

### 1. **PayU Payments (INR)**
   - Real INR currency support
   - SHA512 hash verification
   - Order integration
   - Payment status tracking

### 2. **Sample Products in INR**
   - Electronics: ₹24,999 - ₹207,499
   - Fashion: ₹10,799 - ₹24,999
   - Books: ₹399 - ₹1,599
   - Home & Garden: ₹2,499 - ₹6,699
   - Sports: ₹3,299 - ₹16,599

### 3. **Guest Account Features**
   - Guest wishlist
   - Guest shopping cart
   - Merge on login/social-auth

### 4. **Multi-Vendor**
   - Vendor profiles
   - Vendor payouts
   - Payout scheduling
   - CSV export

### 5. **Advanced Search**
   - JPA Specifications
   - Category filtering
   - Price range filtering
   - Autocomplete suggestions

### 6. **Reviews & Moderation**
   - Verified purchase flag
   - Moderation queue
   - Admin approval/rejection
   - Rate limiting (30 sec per product per user)

### 7. **Subscriptions**
   - Recurring payment plans
   - Subscription status tracking
   - Webhook handling

### 8. **Order Fulfillment**
   - Shipment tracking
   - Courier updates
   - Tracking numbers

---

## API QUICK REFERENCE

### Products
```bash
# Search products
GET /products/api/search-advanced?name=iPhone&priceMin=50000&priceMax=100000

# Suggestions
GET /products/api/suggest?q=sam
```

### Cart
```bash
# Merge guest cart
POST /api/auth/merge-cart
{
  "items": [
    {"productId": 1, "quantity": 2}
  ]
}
```

### Payment
```bash
# Initiate PayU payment
POST /api/payment/initiate/123

# Verify payment
POST /api/payment/verify?txnid=TXN123&amount=5000&status=success&hash=abc123

# Check status
GET /api/payment/status/123
```

### Reviews
```bash
# Post review
POST /api/reviews
{
  "productId": 1,
  "rating": 5,
  "comment": "Excellent product!"
}

# Approve review (admin)
PUT /api/admin/reviews/1/approve

# Reject review (admin)
PUT /api/admin/reviews/1/reject
```

### Subscriptions
```bash
# Create subscription
POST /api/subscriptions/create
{
  "planType": "MONTHLY",
  "amount": 999.00
}

# Cancel subscription
POST /api/subscriptions/cancel/1

# List subscriptions
GET /api/subscriptions
```

---

## TESTING

### Run All Tests
```bash
./mvnw test
```

### Run Specific Test
```bash
./mvnw test -Dtest=WishlistMergeTests
./mvnw test -Dtest=CartMergeTests
./mvnw test -Dtest=ReviewThrottleTests
```

### Test Results
```
✅ WishlistMergeTests - 1/1 PASS
✅ CartMergeTests - 1/1 PASS
✅ ReviewThrottleTests - 1/1 PASS
✅ IntegrationSmokeTests - 1/1 PASS
✅ CatchyApplicationTests - 1/1 PASS
✅ WowServiceTest - 2/2 PASS

Total: 7/7 PASS
```

---

## FILE STRUCTURE

```
catchy/
├── src/
│   ├── main/
│   │   ├── java/com/catchy/
│   │   │   ├── payment/
│   │   │   │   ├── PayUConfig.java
│   │   │   │   └── PayUPaymentService.java
│   │   │   ├── service/
│   │   │   │   ├── WishlistService.java
│   │   │   │   ├── CartService.java
│   │   │   │   ├── CouponService.java
│   │   │   │   ├── ReviewThrottleService.java
│   │   │   │   ├── PayoutService.java
│   │   │   │   └── SubscriptionService.java
│   │   │   ├── controller/
│   │   │   │   ├── PaymentController.java
│   │   │   │   └── ... (other controllers)
│   │   │   └── model/
│   │   │       ├── ... (all entities)
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── application-test.properties
│   │       └── schema.sql
│   └── test/
│       └── java/com/catchy/
│           ├── WishlistMergeTests.java
│           ├── CartMergeTests.java
│           └── ReviewThrottleTests.java
├── target/
│   └── catchy-0.0.1-SNAPSHOT.jar ✅
├── pom.xml
├── INTEGRATION_COMPLETE.md
└── README.md
```

---

## TROUBLESHOOTING

### MySQL Connection Error
```
Error: Communications link failure
Solution: Ensure MySQL is running and database exists
```

### PayU Configuration Missing
```
Error: PAYU_MERCHANT_KEY environment variable not set
Solution: Set environment variables before running (see Environment Setup)
```

### Port Already in Use
```
Error: Address already in use
Solution: Change port in application.properties or kill process on port 8080
```

### Test Failures
```
Solution: 
1. Clear H2 database: delete target/h2 folder
2. Rebuild: ./mvnw clean test
3. Check logs for specific errors
```

---

## DEPLOYMENT CHECKLIST

- [ ] PayU merchant credentials obtained
- [ ] SMTP email configured
- [ ] MySQL database created
- [ ] Environment variables set
- [ ] Application JAR built
- [ ] Tests passing (7/7)
- [ ] Port 8080 available
- [ ] Database backups configured
- [ ] Payment verification working
- [ ] Logging configured

---

## SUPPORT & RESOURCES

### Documentation
- `INTEGRATION_COMPLETE.md` - Full integration details
- `README.md` - Project overview
- `pom.xml` - Dependencies

### Configuration Files
- `application.properties` - Production config
- `application-test.properties` - Test config
- `schema.sql` - Database schema

### Key Classes
- `PayUPaymentService.java` - PayU integration
- `PaymentController.java` - Payment API
- `DataSeeder.java` - Sample data in INR

---

## NEXT STEPS

1. ✅ Integration complete
2. ✅ All tests passing
3. ✅ Build successful
4. → Configure PayU merchant account
5. → Set environment variables
6. → Deploy to production
7. → Monitor payment transactions

---

**Version**: 1.0.0
**Last Updated**: 2025-12-06
**Currency**: INR ₹
**Payment Gateway**: PayU
**Framework**: Spring Boot 3.5.7
**Java**: 21+

---

*Happy Coding! 🚀*
