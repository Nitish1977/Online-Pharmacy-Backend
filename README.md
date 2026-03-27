# 🏥 Online Pharmacy Backend — Spring Boot Microservices

## Architecture Overview

```
                     ┌──────────────────────────────┐
                     │    Eureka Server  :8761       │
                     │  (Service Discovery Registry) │
                     └──────────────┬───────────────┘
                                    │  All services register here
   ┌────────────────────────────────────────────────────────────────┐
   │                API Gateway  :8080                               │
   │      Spring Cloud Gateway + JWT Filter + lb:// routing         │
   └───────────┬──────────────┬────────────┬─────────────┬──────────┘
               │              │            │             │
   ┌───────────▼──┐  ┌────────▼───┐  ┌────▼───────┐  ┌──▼──────────┐
   │Identity :8081│  │Catalog:8082│  │Orders:8083 │  │Admin  :8084 │
   │signup/login  │  │medicines   │  │cart        │  │dashboard    │
   │JWT issue     │  │prescrip.   │  │checkout    │  │medicine mgmt│
   │DB:pharmacy_  │  │DB:pharmacy_│  │payments    │  │order mgmt   │
   │   identity   │  │   catalog  │  │DB:pharmacy_│  │reports      │
   └──────────────┘  └────────────┘  │   orders  │  │DB:pharmacy_ │
                                      └───────────┘  │   admin    │
                                                      └────────────┘
```

---

## 📋 Prerequisites

| Tool  | Version |
|-------|---------|
| Java  | 17+     |
| Maven | 3.8+    |
| MySQL | 8.x     |

---

## ⚙️ Step 1 — Configure MySQL Password

Edit `application.properties` in these 4 services:
```
identity-service/src/main/resources/application.properties
catalog-service/src/main/resources/application.properties
order-service/src/main/resources/application.properties
admin-service/src/main/resources/application.properties
```
Change:
```properties
spring.datasource.password=root    ← your MySQL password
```
> Note: eureka-server and api-gateway do NOT use MySQL.

---

## ▶️ Step 2 — Start All Services (ORDER MATTERS)

Open 6 terminals. Start in this exact order:

```bash
# Terminal 1 — START FIRST
cd eureka-server && mvn spring-boot:run

# Terminal 2
cd identity-service && mvn spring-boot:run

# Terminal 3
cd catalog-service && mvn spring-boot:run

# Terminal 4
cd order-service && mvn spring-boot:run

# Terminal 5
cd admin-service && mvn spring-boot:run

# Terminal 6 — START LAST
cd api-gateway && mvn spring-boot:run
```

---

## 🔍 Step 3 — Verify Eureka Registration

Open: http://localhost:8761

You should see all 5 services registered:
```
IDENTITY-SERVICE    UP
CATALOG-SERVICE     UP
ORDER-SERVICE       UP
ADMIN-SERVICE       UP
API-GATEWAY         UP
```

---

## 🌐 Step 4 — Swagger UI URLs

| Service          | Swagger URL                           |
|------------------|---------------------------------------|
| Identity Service | http://localhost:8081/swagger-ui.html |
| Catalog Service  | http://localhost:8082/swagger-ui.html |
| Order Service    | http://localhost:8083/swagger-ui.html |
| Admin Service    | http://localhost:8084/swagger-ui.html |
| Eureka Dashboard | http://localhost:8761                 |

All APIs also accessible through the Gateway on port 8080.

---

## 🧪 Step 5 — Testing Guide

### IDENTITY SERVICE (http://localhost:8081/swagger-ui.html)

#### TC-01 Signup
POST /api/auth/signup
```json
{
  "name": "Rahul Kumar",
  "email": "rahul@example.com",
  "mobile": "9876543210",
  "password": "password123"
}
```
Expected: 200 OK with token. COPY THE TOKEN.

#### TC-02 Login
POST /api/auth/login
```json
{ "email": "rahul@example.com", "password": "password123" }
```
Expected: 200 OK with JWT token

#### TC-03 Wrong Password (Negative)
Expected: 401 Unauthorized

#### TC-04 Validation Error (Negative)
```json
{ "name": "", "email": "bad", "mobile": "123", "password": "ab" }
```
Expected: 400 Bad Request with field errors

---

### CATALOG SERVICE (http://localhost:8082/swagger-ui.html)
Click Authorize → enter: Bearer <your-token>

#### TC-05 Add Regular Medicine
POST /api/catalog/medicines
```json
{
  "name": "Paracetamol 500mg", "brand": "Calpol",
  "category": "Pain Relief", "price": 25.50,
  "stock": 100, "requiresPrescription": false,
  "expiryDate": "2026-12-31", "dosage": "1 tablet every 6 hours"
}
```
Expected: 200 OK — note the id

#### TC-06 Add Prescription Medicine
```json
{
  "name": "Amoxicillin 500mg", "brand": "Novamox",
  "category": "Antibiotic", "price": 85.00,
  "stock": 50, "requiresPrescription": true,
  "expiryDate": "2025-10-31", "dosage": "1 capsule 3x daily"
}
```

#### TC-07 Get All Medicines (no token needed)
GET /api/catalog/medicines → Expected: full medicine list

#### TC-08 Search Medicines
GET /api/catalog/medicines?search=Paracetamol → filtered list

#### TC-09 Upload Prescription
POST /api/catalog/prescriptions/upload
  customerId=1, file=<any PDF/JPG/PNG>
Expected: status PENDING — note prescription id

#### TC-10 Approve Prescription
PUT /api/catalog/prescriptions/{id}/status?status=APPROVED&remarks=Valid
Expected: status APPROVED

#### TC-11 Upload Invalid File (Negative)
Upload a .txt file → Expected: 400 Bad Request

---

### ORDER SERVICE (http://localhost:8083/swagger-ui.html)
Authorize with same token.

#### TC-12 Add to Cart
POST /api/orders/cart
```json
{
  "customerId": 1, "medicineId": 1,
  "medicineName": "Paracetamol 500mg",
  "quantity": 2, "unitPrice": 25.50,
  "requiresPrescription": false
}
```

#### TC-13 View Cart
GET /api/orders/cart/1 → list of cart items

#### TC-14 Update Cart Item
PUT /api/orders/cart/item/{cartItemId}?quantity=3

#### TC-15 Checkout
POST /api/orders/checkout/start
```json
{
  "customerId": 1,
  "deliveryAddress": "45 MG Road, Ludhiana",
  "pincode": "141001",
  "deliverySlot": "10AM-12PM"
}
```
Expected: status PAYMENT_PENDING — note order id

#### TC-16 Initiate Payment
POST /api/orders/payments/initiate?orderId=<id>
Expected: status PAID, paymentId generated

#### TC-17 Order History
GET /api/orders/my/1 → all customer orders

#### TC-18 Cancel Order
PUT /api/orders/{orderId}/cancel → status CUSTOMER_CANCELLED

#### TC-19 Checkout Empty Cart (Negative)
DELETE /api/orders/cart/clear/1 then POST checkout
Expected: 400 Bad Request — Cart is empty

---

### ADMIN SERVICE (http://localhost:8084/swagger-ui.html)
Make user admin first:
```sql
USE pharmacy_identity;
UPDATE users SET role = 'ADMIN' WHERE email = 'rahul@example.com';
```
Then login again to get fresh admin token. Authorize with admin token.

#### TC-20 Admin Dashboard
GET /api/admin/dashboard → KPIs: orders, revenue, low stock, expiring

#### TC-21 Add Medicine via Admin
POST /api/admin/medicines
```json
{
  "name": "Vitamin C 1000mg", "brand": "HealthVit",
  "category": "Vitamins", "price": 120.00,
  "stock": 5, "requiresPrescription": false,
  "expiryDate": "2027-06-30", "dosage": "1 tablet daily"
}
```

#### TC-22 Restock Medicine
PATCH /api/admin/medicines/{id}/stock?quantity=50 → stock increases by 50

#### TC-23 Full Order Status Lifecycle
PUT /api/admin/orders/{id}/status?status=PACKED
PUT /api/admin/orders/{id}/status?status=OUT_FOR_DELIVERY
PUT /api/admin/orders/{id}/status?status=DELIVERED

#### TC-24 Low Stock Report
GET /api/admin/medicines/low-stock → medicines with stock < 10

#### TC-25 Sales Report
GET /api/admin/reports/sales → revenue, delivered count, avg order value

#### TC-26 Customer Accessing Admin (Negative)
Use customer token on GET /api/admin/dashboard
Expected: 403 Forbidden

---

## 🔄 Complete Happy-Path Flow

```
1. POST /api/auth/signup              → Get JWT token
2. POST /api/catalog/medicines        → Add medicines
3. POST /api/orders/cart              → Add to cart
4. POST /api/orders/checkout/start    → Create order (PAYMENT_PENDING)
5. POST /api/orders/payments/initiate → Pay (PAID)
6. PUT  /api/admin/orders/{id}/status → PACKED
7. PUT  /api/admin/orders/{id}/status → OUT_FOR_DELIVERY
8. PUT  /api/admin/orders/{id}/status → DELIVERED
9. GET  /api/admin/reports/sales      → View revenue
```

---

## 🗄️ Databases (Auto-Created)

| Database            | Service          |
|---------------------|------------------|
| pharmacy_identity   | identity-service |
| pharmacy_catalog    | catalog-service  |
| pharmacy_orders     | order-service    |
| pharmacy_admin      | admin-service    |

---

## 🐛 Common Issues & Fixes

| Problem | Fix |
|---------|-----|
| Eureka shows no services | Start eureka-server FIRST, wait for it to be fully up |
| Access denied MySQL | Update spring.datasource.password in application.properties |
| Port already in use | lsof -ti:808X \| xargs kill -9 |
| 401 in Swagger | Click Authorize → enter Bearer <token> |
| 403 on Admin APIs | Set role=ADMIN in DB, re-login for new token |
| Gateway 503 | Ensure the target service is running and visible in Eureka |

---

## 📁 Project Structure

```
online-pharmacy/
├── eureka-server/       ← Port 8761 — Service Registry
├── api-gateway/         ← Port 8080 — Gateway + JWT + lb:// routing
├── identity-service/    ← Port 8081 — Auth / JWT
├── catalog-service/     ← Port 8082 — Medicines + Prescriptions
├── order-service/       ← Port 8083 — Cart / Checkout / Payment
└── admin-service/       ← Port 8084 — Dashboard / Reports
```
