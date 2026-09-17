# RestaurantHub, Phase 1

A restaurant ordering and management platform. This phase covers project setup, PostgreSQL configuration, JWT authentication, and Docker support. Menu, cart, orders, payments, coupons, events, the admin dashboard, marketing, and the AI chatbot come in later phases.

## Tech stack

- Backend: Java 21, Spring Boot 3.3, Spring Security, Spring Data JPA, JWT (jjwt), Maven
- Frontend: React 18, Vite, Redux Toolkit, React Router, Tailwind CSS
- Database: PostgreSQL 16
- Cache: Redis 7
- Docs: Swagger/OpenAPI at `/swagger-ui.html`

## What's built in Phase 1

- User entity and repository (role stored as `CUSTOMER`, `RESTAURANT_ADMIN`, `RESTAURANT_STAFF`, `SUPER_ADMIN`)
- Register and login endpoints with BCrypt password hashing
- JWT generation and validation, stateless Spring Security filter chain
- Role-based URL protection (`/api/admin/**`, `/api/super-admin/**`)
- Global exception handler with consistent error responses
- Swagger UI wired for every controller
- Unit tests for `AuthService` with JUnit 5 and Mockito
- Docker Compose for Postgres, Redis, and the backend
- React app with login, register, and a protected home page

## Local setup without Docker

### 1. Database

Install PostgreSQL and Redis locally, or run just those two services from Docker Compose:

```bash
docker compose up postgres redis
```

Create the database if it does not exist:

```sql
CREATE DATABASE restaurant_hub;
```

### 2. Backend

```bash
cd backend
cp ../.env.example .env   # then export the values, or set them in your run config
./mvnw spring-boot:run
```

The backend needs `mvnw` and `.mvn/wrapper` files, which Maven generates the first time you run `mvn wrapper:wrapper` locally (they are not included here to keep the scaffold small). If you don't have the wrapper, run with a local Maven install instead:

```bash
mvn spring-boot:run
```

Backend runs on `http://localhost:8080`. Swagger UI is at `http://localhost:8080/swagger-ui.html`.

### 3. Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend runs on `http://localhost:5173` and proxies `/api` calls to the backend.

## Running everything with Docker

```bash
export JWT_SECRET=$(openssl rand -base64 32)
docker compose up --build
```

This starts Postgres, Redis, and the backend. Run the frontend separately with `npm run dev` for now. It gets a Docker service and an nginx-served build once more pages exist.

## Environment variables

See `.env.example` for the full list. At minimum, set `JWT_SECRET` to a long random string before running anything outside your local machine.

## API testing examples

Register:

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Priyanshu","email":"priyanshu@test.com","password":"password123","phone":"9999999999"}'
```

Login:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"priyanshu@test.com","password":"password123"}'
```

Both return an `accessToken`. Use it on protected routes:

```bash
curl http://localhost:8080/api/some-protected-route \
  -H "Authorization: Bearer <accessToken>"
```

## Common errors and fixes

- **`Connection refused` on port 5432**: Postgres isn't running, or `DB_HOST`/`DB_PORT` don't match your setup. Start Postgres first.
- **`401 Unauthorized` on a route that should work**: check the token hasn't expired (default 24 hours) and that the header is exactly `Authorization: Bearer <token>`.
- **CORS error in the browser console**: the frontend origin must match what's listed in `SecurityConfig.corsConfigurationSource()`. It's set to `http://localhost:5173` by default.
- **`mvnw: command not found`**: the Maven wrapper files aren't in this scaffold. Run `mvn wrapper:wrapper` once inside `backend/` to generate them, or use a local Maven install.
- **`relation "users" does not exist`**: `ddl-auto` is set to `update`, so the table is created on first successful startup. If it still fails, check the datasource URL and credentials.

## Phase 2: menu system

Adds `Restaurant`, `Category`, and `MenuItem` entities, Cloudinary image upload, and public browse/search APIs.

### What's new

- `Restaurant`, `Category`, `MenuItem` entities and repositories. `MenuItem` uses a JPA `Specification` for dynamic filtering (restaurant, category, vegetarian, available, text search on name/description), combined with Spring Data's `Pageable` for pagination and sorting.
- `User.restaurant` is now a real relation instead of a placeholder id, so `RESTAURANT_ADMIN` and `RESTAURANT_STAFF` accounts are tied to one restaurant.
- `CurrentUserProvider` reads the authenticated user from the database (never trusts a restaurant id sent by the client) and every admin write checks the caller actually owns that restaurant. `SUPER_ADMIN` can act on any restaurant by passing `restaurantId` explicitly in the request body.
- `ImageStorageService` uploads to Cloudinary and returns the HTTPS URL. Menu item images, restaurant logos, and cover images all go through it.
- Public endpoints (no login needed): `GET /api/restaurants/{id}`, `GET /api/categories?restaurantId=`, `GET /api/menu` (search/filter/paginate), `GET /api/menu/{id}`.
- Admin endpoints (`RESTAURANT_ADMIN` or `SUPER_ADMIN`): full CRUD on categories and menu items, restaurant profile update, image uploads for restaurant logo/cover and menu item photos.
- Unit tests for `MenuItemService` covering cross-restaurant category rejection and the super-admin/restaurant-admin authorization split.

### New environment variables

```
CLOUDINARY_CLOUD_NAME=
CLOUDINARY_API_KEY=
CLOUDINARY_API_SECRET=
```

Get these from your Cloudinary dashboard (free tier is enough for development). Without them, everything except image upload still works.

### API testing examples

Browse the menu (public, no token needed):

```bash
curl "http://localhost:8080/api/menu?restaurantId=1&veg=true&page=0&size=10&sort=price,asc"
```

Create a category (needs a `RESTAURANT_ADMIN` token from `/api/auth/login`):

```bash
curl -X POST http://localhost:8080/api/admin/categories \
  -H "Authorization: Bearer <accessToken>" \
  -H "Content-Type: application/json" \
  -d '{"name":"Starters","displayOrder":1}'
```

Create a menu item:

```bash
curl -X POST http://localhost:8080/api/admin/menu \
  -H "Authorization: Bearer <accessToken>" \
  -H "Content-Type: application/json" \
  -d '{
        "name":"Paneer Tikka",
        "description":"Chargrilled cottage cheese",
        "price":249.00,
        "categoryId":1,
        "vegetarian":true,
        "spiceLevel":"MEDIUM",
        "available":true,
        "preparationTimeMinutes":15,
        "featured":false,
        "popular":false
      }'
```

Upload a menu item image:

```bash
curl -X POST http://localhost:8080/api/admin/menu/1/image \
  -H "Authorization: Bearer <accessToken>" \
  -F "file=@/path/to/image.jpg"
```

Note: there's no account yet with the `RESTAURANT_ADMIN` role or a restaurant row to attach it to. Until Phase 7 (staff management) or a manual insert, create one directly in the database:

```sql
INSERT INTO restaurants (name, description, address, delivery_fee, tax_percentage, minimum_order, created_at, updated_at)
VALUES ('Spice House', 'Home-style North Indian food', 'MG Road, Agra', 30.00, 5.00, 199.00, now(), now());

-- then update a registered user's role and restaurant_id to make them an admin of that restaurant
UPDATE users SET role = 'RESTAURANT_ADMIN', restaurant_id = 1 WHERE email = 'you@example.com';
```

### Common errors and fixes (Phase 2 additions)

- **`403 Forbidden` on admin category/menu routes**: your token's account either isn't `RESTAURANT_ADMIN`/`SUPER_ADMIN`, or (for `RESTAURANT_ADMIN`) their `restaurant_id` doesn't match the restaurant you're editing. Check the `users` table.
- **`restaurantId is required for super admin requests`**: a `SUPER_ADMIN` token must include `restaurantId` in the category/menu item request body, since they aren't tied to one restaurant.
- **`Category does not belong to this restaurant`**: you passed a `categoryId` that exists but was created under a different restaurant.
- **Image upload fails with a Cloudinary error**: check `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY`, `CLOUDINARY_API_SECRET` are set and correct.

## Phase 3: cart and orders

Adds `Address`, `Cart`, `CartItem`, `Order`, `OrderItem` entities, plus the full add-to-cart-through-order-tracking flow.

### What's new

- **Cart**: one cart per user, created lazily. A cart can only hold items from one restaurant at a time; adding an item from a different restaurant is rejected until the cart is cleared. Adding the same item twice merges quantities instead of duplicating the row.
- **Addresses**: customers manage their own saved delivery addresses. Address ownership is checked on every read/update/delete.
- **Orders**: `POST /api/orders` turns the current cart into an order. The backend recomputes every price from the current `MenuItem` row (never trusts anything the client sent earlier), applies the restaurant's tax percentage and delivery fee, and snapshots item name/price and the delivery address onto the order so later menu or address edits never change historical orders. The cart is cleared once the order is created.
- **Order status machine**: `PENDING → CONFIRMED → PREPARING → READY → OUT_FOR_DELIVERY → DELIVERED`, with `CANCELLED` allowed only from `PENDING` or `CONFIRMED`. Every transition is checked against this map in `OrderStatusValidator`; anything else returns a 409-style `InvalidOrderStateException`.
- **Authorization**: a customer can only see their own orders. A `RESTAURANT_ADMIN`/`RESTAURANT_STAFF` can see and update status for orders belonging to their own restaurant only. `SUPER_ADMIN` can see and manage any restaurant's orders. `RESTAURANT_STAFF` can move order status but has no access to the rest of `/api/admin/**`.
- Discount is wired in as a fixed `0` for now. It becomes real once coupons land in Phase 5, without changing the order calculation shape.

### API testing examples

Add to cart (customer token required):

```bash
curl -X POST http://localhost:8080/api/cart/items \
  -H "Authorization: Bearer <accessToken>" \
  -H "Content-Type: application/json" \
  -d '{"menuItemId":1,"quantity":2}'
```

Save an address:

```bash
curl -X POST http://localhost:8080/api/addresses \
  -H "Authorization: Bearer <accessToken>" \
  -H "Content-Type: application/json" \
  -d '{"label":"Home","line1":"12 MG Road","city":"Agra","postalCode":"282001","phone":"9999999999"}'
```

Place the order:

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Authorization: Bearer <accessToken>" \
  -H "Content-Type: application/json" \
  -d '{"addressId":1}'
```

Move it along (needs a `RESTAURANT_ADMIN`/`RESTAURANT_STAFF` token for that restaurant):

```bash
curl -X PATCH http://localhost:8080/api/orders/1/status \
  -H "Authorization: Bearer <adminAccessToken>" \
  -H "Content-Type: application/json" \
  -d '{"status":"CONFIRMED"}'
```

### Common errors and fixes (Phase 3 additions)

- **`Your cart is empty`**: add at least one item via `/api/cart/items` before calling `/api/orders`.
- **`Your cart has items from another restaurant...`**: call `DELETE /api/cart` first, then add items from the new restaurant.
- **`Cannot move an order from X to Y`**: you skipped a stage or tried to move a `DELIVERED`/`CANCELLED` order. Check the status machine above.
- **`403` on `/api/orders/{id}` or its status update**: the order doesn't belong to you (as customer) or to your restaurant (as staff/admin).

## Phase 4: Razorpay payments

Adds the `Payment` entity and the full create-order → pay → verify → webhook flow.

### What's new

- **`POST /api/payments/create`**: takes a RestaurantHub `orderId`, checks the caller owns it and it's still `PENDING`, converts the order total to paise, and creates a Razorpay order via the SDK. Returns the Razorpay order id and public key id, never the key secret.
- **`POST /api/payments/verify`**: called from the browser after Razorpay's checkout popup completes. Verifies the HMAC signature server-side using the key secret. On success, moves the order from `PENDING` to `CONFIRMED` through the same `OrderStatusValidator` used everywhere else. On failure, marks the payment `FAILED` and returns a `402`.
- **`POST /api/payments/webhook`**: the real source of truth in production, since a browser can close before `verify` runs. Verifies the webhook signature against `RAZORPAY_WEBHOOK_SECRET` using the raw request body, then handles `payment.captured` and `payment.failed` events. It's idempotent, a webhook delivered twice for the same payment does not double-confirm an order. This is the one endpoint that's public, Razorpay's servers don't send a JWT.
- **`GET /api/payments/order/{orderId}`**: check payment status for one of your own orders.
- Every amount is still calculated server-side. The frontend never tells the backend what the total is, it only relays what Razorpay's checkout returns.
- Frontend: `Checkout.jsx` now creates the order, creates the Razorpay payment order, opens Razorpay's hosted checkout, and calls `/api/payments/verify` in the success handler.

### New environment variables

```
RAZORPAY_KEY_ID=
RAZORPAY_KEY_SECRET=
RAZORPAY_WEBHOOK_SECRET=
```

Get the first two from your Razorpay dashboard (test mode keys work fine for development). The webhook secret comes from setting up a webhook in the dashboard pointing at `https://your-backend-domain/api/payments/webhook`, subscribed to `payment.captured` and `payment.failed`. For local development, use a tool like `ngrok` to expose `localhost:8080` so Razorpay's servers can reach the webhook.

### API testing examples

Create a Razorpay order for an existing order:

```bash
curl -X POST http://localhost:8080/api/payments/create \
  -H "Authorization: Bearer <accessToken>" \
  -H "Content-Type: application/json" \
  -d '{"orderId":1}'
```

This returns `razorpayOrderId`, `amountInPaise`, and `razorpayKeyId`. In test mode, complete the payment using Razorpay's test card numbers (documented on their dashboard), then verify:

```bash
curl -X POST http://localhost:8080/api/payments/verify \
  -H "Authorization: Bearer <accessToken>" \
  -H "Content-Type: application/json" \
  -d '{"razorpayOrderId":"order_xxx","razorpayPaymentId":"pay_xxx","razorpaySignature":"<signature from Razorpay checkout>"}'
```

### Common errors and fixes (Phase 4 additions)

- **`This order is not awaiting payment`**: the order is already `CONFIRMED`, `CANCELLED`, or further along. Only `PENDING` orders can be paid.
- **`This order has already been paid`**: a successful payment already exists for this order. Check `GET /api/payments/order/{orderId}`.
- **`Payment signature could not be verified`**: usually a mismatched `RAZORPAY_KEY_SECRET`, or the three values from the checkout response weren't passed through unmodified.
- **Webhook signature verification failed**: `RAZORPAY_WEBHOOK_SECRET` doesn't match what's configured in the Razorpay dashboard for that webhook, or the raw body was modified/parsed before reaching the handler. Spring must receive it as a raw string, not as a parsed object, for the signature check to work.
- **`401` when Razorpay calls your webhook**: make sure `/api/payments/webhook` is listed as `permitAll()` in `SecurityConfig`. It's already set up that way here, but double-check if you rename the path.

## Next: Phase 5

Coupons and deals: coupon CRUD for admins, server-side validation (expiry, minimum order, usage limits), and wiring real discounts into the order total calculation.

## Phase 5: coupons and deals

- `Coupon` (per restaurant, percentage or fixed discount, min order value, max discount cap, start/expiry dates, total and per-user usage limits) and `CouponUsage` (one row per redemption, used to enforce limits).
- `POST /api/coupons/validate` checks a code against the live cart subtotal before checkout, no order created yet.
- `POST /api/orders` now accepts an optional `couponCode`. The backend re-validates everything server-side at checkout time (a coupon can expire or hit its limit between "apply" and "place order"), computes the discount, subtracts it from the total, and records a `CouponUsage` row only once the order is actually saved.
- Admin CRUD at `/api/admin/coupons`, same ownership rules as menu/categories (`RESTAURANT_ADMIN` manages their own restaurant, `SUPER_ADMIN` any restaurant via `restaurantId`).
- Frontend: a coupon field on the checkout page shows the discount before you pay.

## Phase 6: event ticketing

- `Event` (name, date, time, location, ticket price, capacity, remaining seats) and `Ticket` (a purchase of N seats by one customer).
- **Overselling is prevented at the database level**, not in application code: `EventRepository.reserveSeats()` runs `UPDATE events SET remaining_seats = remaining_seats - :qty WHERE id = :id AND remaining_seats >= :qty` and checks the row count it updated. If two customers race for the last seat, only one update succeeds; the loser gets `TicketUnavailableException` (409) instead of a negative seat count. No pessimistic locks or synchronized blocks needed, the WHERE clause does the job atomically.
- `POST /api/events/{id}/tickets` purchases tickets (simplified to immediate `PAID` status here; wiring Razorpay for ticket payments follows the exact same create/verify pattern as Phase 4 food orders).
- `DELETE /api/tickets/{id}` cancels a ticket and releases its seats back to the event.
- Admin CRUD at `/api/admin/events`, plus image upload.
- Frontend: an Events page listing upcoming events with a one-click "Book ticket" button.

## Phase 7: admin dashboard, customers, staff

- `GET /api/admin/dashboard?restaurantId=`: today's order count and revenue, pending vs. completed order counts, total distinct customers, top 5 items by quantity sold, coupon redemption count, and tickets sold, all computed with aggregate queries rather than loading full tables into memory.
- `GET /api/admin/customers?restaurantId=`: every customer who has ordered from the restaurant, with their order count, total spend, and last order date, one grouped query.
- Staff accounts: `RESTAURANT_ADMIN` can create/list/remove `RESTAURANT_STAFF` accounts scoped to their own restaurant at `/api/admin/restaurants/{restaurantId}/staff`. Staff accounts get a normal email/password login through the existing `/api/auth/login` and inherit the same JWT-based access, just with the `RESTAURANT_STAFF` role, which only unlocks order viewing and status updates, nothing else under `/api/admin/**`.

## Phase 8: marketing

- `MarketingCampaign` (subject, content, customer segment, scheduled time, status) for email-style campaigns. Creating one with a `scheduledAt` marks it `SCHEDULED`, otherwise it's a `DRAFT`. `POST /api/admin/marketing/campaigns/{id}/send` marks it `SENT` (actual dispatch would reuse `NotificationService` from Phase 10, sending to whichever segment of customers you choose).
- `MarketingAsset` for downloadable promotional material, business cards, decals, banners, uploaded through the same `ImageStorageService`/Cloudinary path as menu and event images.
- Both are restaurant-scoped and admin-only, same ownership pattern as everything else under `/api/admin/**`.

## Phase 9: AI customer-care chatbot

- `POST /api/customer-care/chat` is the only endpoint the frontend calls. It never talks to the LLM provider directly, that's `AiProviderClient`, and its API key lives only in backend environment variables.
- **What the bot can know**: `AiCustomerCareService` assembles a system prompt from a fixed set of read-only "tool" methods, restaurant info, opening hours, available menu items, active coupons, upcoming events, and *this customer's own* recent orders. The LLM never gets raw database access, it only ever sees the text these methods produce, so there's no way for a prompt injection to make it query something outside that set.
- **What the bot can do**: exactly one action, cancel an order. If a message contains "cancel" and an order number, the service checks the order belongs to the person chatting and is still `PENDING`/`CONFIRMED` before actually cancelling it through the same `OrderService.cancelOwnOrder()` a direct API call would use. Everything else the bot might seem to offer (refunds, price changes, coupon creation) it can only describe, since those tools were never given to it.
- Provider-agnostic by design: `AiProviderClient` speaks the OpenAI-compatible `/chat/completions` shape that Groq, OpenAI, and several others share, so switching providers is an `AI_BASE_URL`/`AI_MODEL`/`AI_API_KEY` change, not a code change.
- Rate limited per user (20 messages/minute) via an in-memory sliding window, since every message costs a real API call.
- Frontend: a floating chat launcher on every logged-in page (`ChatWidget.jsx`), matching the "Customer Care" bubble from the spec.

### New environment variables

```
AI_BASE_URL=https://api.groq.com/openai/v1
AI_API_KEY=
AI_MODEL=llama-3.1-8b-instant
```

Any OpenAI-compatible provider works here, get an API key from Groq, OpenAI, or similar, and set `AI_BASE_URL` to match. Without a key, the bot responds with a fixed "temporarily unavailable" message instead of failing the request.

## Phase 10: notifications, logging, security hardening, deployment

- `NotificationService` sends order-placed, order-status-changed, payment-success, and ticket-purchased emails via `JavaMailSender`. It's a no-op (logs only) until `MAIL_ENABLED=true` and SMTP credentials are set, so nothing breaks in local dev without a mail server. Every send is logged with the event type and a masked email address, never the full address or message body.
- Adding SMS or push later means implementing the same `notify*` methods in a new class and calling both from the same three call sites (`OrderService`, `PaymentService`, `TicketService`), no changes needed elsewhere.
- Basic security-relevant logging added to `AuthService` (login success/failure by user id, never by credentials).
- **Test suite**: `OrderStatusValidatorTest`, `AuthServiceTest`, `MenuItemServiceTest`, `CartServiceTest`, `PaymentServiceTest`, `CouponServiceTest`, `TicketServiceTest` cover the business rules that actually matter, authorization boundaries, discount math, status transitions, overselling prevention, payment ownership checks. Run them with:

```bash
cd backend
mvn test
```

### New environment variables

```
MAIL_ENABLED=false
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=
MAIL_PASSWORD=
MAIL_FROM=no-reply@restauranthub.example
```

Gmail needs an app password, not your account password, if 2FA is on. Any SMTP provider works the same way.

### Security checklist (what's already in place)

- Passwords hashed with BCrypt, never logged, never returned in any response.
- JWT signed with a secret that must be overridden in production (`JWT_SECRET`), stateless sessions, no server-side session storage.
- Every admin write path checks the caller actually owns the restaurant they're modifying, resolved from the database, never trusted from client input.
- Payment amounts and order totals are always computed server-side; the frontend only ever displays numbers the backend already calculated.
- Payment verification uses HMAC signature checks (both the browser-return flow and the webhook), not "the frontend said it succeeded."
- CORS restricted to the configured frontend origin.
- The chatbot's API key never reaches the frontend, and its capabilities are structurally limited to a fixed tool set plus one authorization-checked action.
- Rate limiting on the chatbot endpoint.
- No secrets committed to the repo, everything sensitive comes from environment variables via `.env.example`.

### Deployment

1. **Database**: create a Postgres instance on Neon, Supabase, or AWS RDS. Copy its connection details into `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`.
2. **Backend**: deploy the `backend/` folder's Dockerfile to Render or AWS (ECS/App Runner/EC2). Set every environment variable from `.env.example` in that platform's dashboard, never commit a `.env` file.
3. **Frontend**: deploy `frontend/` to Vercel. Set the API base URL (currently proxied via `vite.config.js` for local dev) to your deployed backend's URL for production, e.g. by reading `import.meta.env.VITE_API_URL` in `axiosClient.js` and setting that env var in Vercel's project settings.
4. **Redis**: any managed Redis (Upstash, Render Redis, AWS ElastiCache) works, point `REDIS_HOST`/`REDIS_PORT` at it.
5. **Razorpay webhook**: once the backend has a public URL, register `https://your-backend-domain/api/payments/webhook` in the Razorpay dashboard and copy the webhook secret into `RAZORPAY_WEBHOOK_SECRET`.
6. **CORS**: update `SecurityConfig.corsConfigurationSource()` to allow your deployed frontend's origin instead of `localhost:5173`.

## All ten phases are now implemented

What's deliberately simplified and would need more work for a real commercial launch:
- Ticket payments are marked `PAID` immediately rather than going through Razorpay (the pattern from Phase 4 applies directly if needed).
- Marketing campaign "sending" only flips status, actual bulk email dispatch to a customer segment isn't wired to `NotificationService` yet.
- The Maven wrapper (`mvnw`) isn't included, run `mvn wrapper:wrapper` once locally or use a local Maven install.
- Multi-restaurant discovery (browsing a list of restaurants rather than a hardcoded `RESTAURANT_ID = 1` on the frontend) would be the natural next step before a real multi-tenant launch.
