# API Testing Guide: RabbitMQ & Zipkin Flow

Here is a complete, step-by-step guide with the exact JSON payloads and API endpoints you need to test the rabbitMQ messaging and Zipkin tracing end-to-end. 

Ensure your services, MySQL, RabbitMQ, and Zipkin are running before you start. All API calls go through the **API Gateway (`http://localhost:8080`)**.

---

### Step 1: Create an Admin User
By default, the `identity-service` registers everyone as a `CUSTOMER`. We will create a user and manually promote them to `ADMIN` in the database so they can receive the Low Stock Alert email.

**1. Register the Admin**
```http
POST http://localhost:8080/api/auth/signup
Content-Type: application/json

{
  "name": "Admin Manager",
  "email": "admin@example.com",
  "mobile": "9999999990",
  "password": "password123"
}
```
*(Replace `admin@example.com` with your real email if you want to actually receive the test email via Gmail SMTP)*

**2. Promote to Admin via Database**
Run this SQL query directly in your MySQL database for the `identity_service`:
```sql
USE pharmacy_identity;
UPDATE users SET role = 'ADMIN' WHERE email = 'admin@example.com';
```

**3. Login as Admin & Get Token**
```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "admin@example.com",
  "password": "password123"
}
```
*(Note down the `"token"` value from the response as `ADMIN_TOKEN`)*

---

### Step 2: Create a Customer User
**1. Register & Login the Customer**
```http
POST http://localhost:8080/api/auth/signup
Content-Type: application/json

{
  "name": "John Doe",
  "email": "customer@example.com",
  "mobile": "1234567890",
  "password": "password123"
}
```

**2. Login as Customer & Get Token**
```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "customer@example.com",
  "password": "password123"
}
```
*(Note down the `"token"` value from the response as `CUSTOMER_TOKEN`)*

---

### Step 3: Add a Medicine (Admin Action)
We will add a medicine with a stock of **6** (which is just 1 above our low-stock threshold of `<= 5`).

```http
POST http://localhost:8080/api/catalog/medicines
Authorization: Bearer <ADMIN_TOKEN>
Content-Type: application/json

{
  "name": "Test Medicine 500mg",
  "description": "Test Medicine for RabbitMQ",
  "price": 50.0,
  "stock": 6,
  "manufacturer": "Pharma Inc",
  "requiresPrescription": false
}
```
*(Note down the `"id"` from the response. We will assume it is `1`)*

---

### Step 4: Add to Cart (Customer Action)
Now the customer will add `2` units of this medicine to their cart. This will bring the total stock down to `4` (triggering the alert).

```http
POST http://localhost:8080/api/orders/cart
Authorization: Bearer <CUSTOMER_TOKEN>
Content-Type: application/json

{
  "customerId": 1, 
  "medicineId": 1,
  "medicineName": "Test Medicine 500mg",
  "quantity": 2,
  "unitPrice": 50.0,
  "requiresPrescription": false
}
```
*(Make sure the `customerId` matches your customer account ID from the DB)*

---

### Step 5: Checkout & Trigger RabbitMQ (Customer Action)
This is the action that triggers the [OrderPlacedEvent](file:///c:/Users/nitis/Downloads/online-pharmacy-backend%20%283%29/online-pharmacy/order-service/src/main/java/com/pharmacy/order/dto/OrderPlacedEvent.java#9-24).

```http
POST http://localhost:8080/api/orders/checkout/start
Authorization: Bearer <CUSTOMER_TOKEN>
Content-Type: application/json

{
  "customerId": 1,
  "deliveryAddress": "123 Main St, Bangalore",
  "pincode": "560001",
  "deliverySlot": "Tomorrow 10AM-12PM"
}
```

---

### Step 6: Verify the Results!

1. **Verify Stock Update**: Look at your `pharmacy_catalog.medicines` database. The stock for `Test Medicine 500mg` should now be exactly `4`.
2. **Verify Identity Service Logs**: Look at the console running `identity-service`. You should see logs confirming the RabbitMQ flow:
   ```
   Received low stock alert for Test Medicine 500mg: 4 items remaining
   Low stock alert email sent successfully to admin@example.com for Test Medicine 500mg
   ```
3. **Verify Zipkin Tracing**:
   - Go to `http://localhost:9411`.
   - Click "Run Query".
   - You will see traces mapping the API requests that just traveled through `api-gateway` -> `order-service`, etc.
