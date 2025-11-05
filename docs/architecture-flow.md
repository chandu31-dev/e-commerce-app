# Catchy — Architecture Flow (Code-side and UI-side)

This document describes the request/processing flows of the Catchy application from two perspectives:
- Code-side (backend) — how HTTP requests are handled, security/auth, services, persistence, and email/token flows.
- UI-side (frontend/templates) — how pages and user actions map to backend endpoints, cookies, and local flows (guest cart, signup/verification).

Both flows include Mermaid diagrams you can view in editors that support Mermaid (VS Code Mermaid Preview, GitHub, etc.). If your renderer doesn't support Mermaid, each diagram is followed by a short textual explanation.

---

**Legend (high-level components)**
- Controller = Spring MVC controller / REST endpoint
- Filter = Security/JWT filter
- Service = Business logic (CartService, AuthService, TokenService, MailService, OrderService)
- Repo = Spring Data JPA repository
- DB = H2 (dev) / MySQL (prod)
- Template = Thymeleaf HTML pages in `src/main/resources/templates`

---

## 1) Code-side (Backend) Flow

Mermaid flowchart:

```mermaid
flowchart TD
  subgraph HTTP
    A[Client Request]
  end

  subgraph Server
    F[JwtAuthenticationFilter] -->|valid JWT| Auth[Set SecurityContext]
    F -->|no/invalid JWT| Next[Proceed unauthenticated]

    A --> F
    F --> C[DispatcherServlet -> Controller]

    C --> S1[Controller (Auth/Products/Cart/Order/Admin)]
    S1 --> Serv[Service Layer]
    Serv --> Repo[Repository Layer]
    Repo --> DB[(Database)]

    %% Token flows
    S1 -.-> TokenService[TokenService]
    TokenService --> VerificationTokens[(verification_tokens)]
    TokenService --> PasswordResetTokens[(password_reset_tokens)]

    %% Email
    TokenService --> MailService[ MailService ]
    MailService --> SMTP[SMTP or Console]
  end

  style DB fill:#f9f,stroke:#333,stroke-width:1px
  style SMTP fill:#ff9,stroke:#333,stroke-width:1px
```

Textual explanation (code-side):
- Request enters `JwtAuthenticationFilter` which:
  - Extracts JWT from `Authorization: Bearer <token>` header.
  - Validates signature and expiration.
  - Loads UserDetails via `UserDetailsServiceImpl`.
  - On success, sets `SecurityContext` and proceeds to controllers.
  - On failure, continues without authentication (requests for public endpoints still work).

- DispatcherServlet maps to Controllers (examples):
  - `AuthController` — signup, login, verify, request-reset, reset.
  - `CartController` — add/update/remove cart items (supports `sessionId` cookie for guest carts).
  - `ProductController` — list/search products, product details.
  - `OrderController` / `AdminController` — place orders, update order status, upload product images.

- Controller delegates to Services (single-responsibility):
  - `AuthService` — creates users (initially `verified=false`), calls `TokenService` to create verification tokens.
  - `TokenService` — creates/validates tokens, persists them, scheduled cleanup of expired tokens.
  - `MailService` — attempts to send verification/reset emails via `JavaMailSender` or logs the message to console when SMTP is not configured.
  - `CartService` / `OrderService` — manage cart and order lifecycle.

- Repositories persist entities (`User`, `Product`, `Order`, `OrderItem`, `CartItem`, `VerificationToken`, `PasswordResetToken`).

- Important config points:
  - `application.properties` (dev): H2 in-memory DB + `schema.sql` initialization (or Flyway migrations if enabled in prod).
  - Security: `UserPrincipal.isEnabled()` returns `user.verified` so unverified users cannot authenticate.

---

## 2) UI-side (Frontend / Templates) Flow

Mermaid flowchart:

```mermaid
flowchart TD
  A[User visits site] --> B(Index / products page)
  B -->|click product| C(Product details page)
  C -->|Add to cart| D(Cart page)
  D -->|Proceed to checkout| E(Login or Continue as guest)

  subgraph GuestCart
    E -->|guest| G[set `CART_SESSION` cookie]
    G --> CartAPI[CartController (session methods)]
    CartAPI --> DB[(cart_items)]
  end

  subgraph Authenticated
    E -->|login/signup| L[Login form / Signup form]
    L --> AuthAPI[AuthController]
    AuthAPI --> TokenService
    AuthAPI --> DB[(users)]
  end

  E -->|checkout| O[OrderController -> create Order]
  O --> Payment[PaymentService (scaffold)] --> DB[(orders)]

  style DB fill:#f9f,stroke:#333,stroke-width:1px
```

Textual explanation (UI-side):
- Pages & templates:
  - `index.html` — welcome, product listing.
  - `products.html` / `product-details.html` — browse and view products.
  - `cart.html` — shows cart items, quantity controls, checkout button.
  - `checkout.html` — checkout flow that requires login or guest continuation.
  - `login.html`, `signup.html` — authentication forms.
  - `request-reset.html`, `reset-password.html`, `verification-result.html` — flows for account recovery and verification.

- Guest cart behavior:
  - When an anonymous user adds items, the app sets a `CART_SESSION` cookie (UUID).
  - `CartController` stores/retrieves `CartItem` entries either by `user_id` (authenticated) or `session_id` (guest).
  - When the user logs in, cart merge logic (if present) can move items from `session_id` to `user`.

- Signup & verification UI flow:
  - User fills `signup.html` and POSTs to `/api/auth/signup`.
  - The server creates a `User` with `verified=false` and `VerificationToken`.
  - `MailService` logs or sends a link: `/verify?token=<token>`.
  - User clicks the link; `AuthController` validates token, sets `user.verified=true`, and shows `verification-result.html`.

- Password reset UI flow:
  - User requests reset at `request-reset.html` -> server creates `PasswordResetToken` and emails link.
  - Link opens `reset-password.html` -> user enters new password -> POST to `/api/auth/reset` which validates token and updates password.

---

## 3) Key files / classes (map)

- Security
  - `src/main/java/com/catchy/security/JwtAuthenticationFilter.java` — JWT extraction and setting SecurityContext
  - `src/main/java/com/catchy/security/UserDetailsServiceImpl.java` — load user by email
  - `src/main/java/com/catchy/security/UserPrincipal.java` — wraps `User` for Spring Security

- Auth & tokens
  - `src/main/java/com/catchy/controller/AuthController.java`
  - `src/main/java/com/catchy/service/AuthService.java`
  - `src/main/java/com/catchy/service/TokenService.java`
  - `src/main/java/com/catchy/service/MailService.java`
  - `src/main/java/com/catchy/model/VerificationToken.java`
  - `src/main/java/com/catchy/model/PasswordResetToken.java`

- Cart
  - `src/main/java/com/catchy/controller/CartController.java`
  - `src/main/java/com/catchy/service/CartService.java`
  - `src/main/java/com/catchy/model/CartItem.java`

- Orders & products
  - `src/main/java/com/catchy/controller/OrderController.java`
  - `src/main/java/com/catchy/service/OrderService.java`
  - `src/main/java/com/catchy/controller/AdminController.java` (image uploads, order status updates)
  - `src/main/java/com/catchy/model/Product.java`
  - `src/main/java/com/catchy/model/Order.java`

- Resources
  - Templates: `src/main/resources/templates/*.html`
  - Static uploads: `src/main/resources/static/uploads/`
  - DB migrations: `src/main/resources/db/migration/V1__init.sql` (or `schema.sql` for H2 dev)

---

## 4) Common flows (short) — quick references

- Login flow (code):
  1. POST `/api/auth/login` -> AuthController
  2. AuthController authenticates via AuthenticationManager -> creates JWT
  3. Client stores JWT (localStorage / cookie) and sends in `Authorization` header for protected calls

- Signup & verification (code):
  1. POST `/api/auth/signup` -> create User (verified=false)
  2. `TokenService.createVerificationToken(user)` -> persist token
  3. `MailService.sendVerification(user.email, token)` -> email link
  4. GET `/verify?token=...` -> `AuthController` validates token, sets `verified=true`

- Add-to-cart (guest):
  1. User clicks Add to Cart (JS POST /api/cart)
  2. Server checks `CART_SESSION` cookie; if missing, generates UUID and sets cookie
  3. `CartService.addToCartBySession(sessionId, productId, qty)` persists item

---

## 5) Notes & next improvements
- Diagrams are Mermaid; use a Mermaid previewer or GitHub rendering to view graphics.
- You can convert `docs/architecture-flow.md` diagrams to PNG/SVG if needed.
- If you'd like, I can:
  - Add sequence diagrams for specific flows (signup, checkout, token cleanup)
  - Export diagrams as images and place them in `docs/` for easy viewing
  - Create a simplified `README_TESTING.md` with commands to exercise each flow (signup, verify, reset, cart)

---

Document created on: 2025-11-05
