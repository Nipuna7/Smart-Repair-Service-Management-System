# 📘 Complete Postman Testing Guide for Smart Repair & Service Management System

## Prerequisites
1. Make sure PostgreSQL is running on localhost:5432
2. Database `srsmsystem` should exist
3. Spring Boot application is running on http://localhost:8080

---

## 🚀 Step-by-Step Testing Guide

### STEP 1: Start Your Application

1. Open terminal in IntelliJ or use command prompt
2. Navigate to project directory:
   ```
   cd "C:\Users\EX BOOK\Desktop\Smart Repair & Service Management System\backend\demo"
   ```
3. Run the application:
   ```
   mvn spring-boot:run
   ```
4. Wait for message: "Started SrsmsystemApplication"

---

## 📝 ENDPOINT 1: Register a Customer (Default Role)

### Request Details:
- **Method:** POST
- **URL:** `http://localhost:8080/auth/register`
- **Headers:**
  - Key: `Content-Type`
  - Value: `application/json`

### Request Body (JSON):
```json
{
    "username": "john_customer",
    "email": "john@example.com",
    "password": "password123",
    "fullName": "John Doe",
    "phoneNumber": "1234567890"
}
```

### Steps in Postman:
1. Click **New** → **HTTP Request**
2. Select **POST** method
3. Enter URL: `http://localhost:8080/auth/register`
4. Click **Headers** tab
   - Add: Key = `Content-Type`, Value = `application/json`
5. Click **Body** tab
   - Select **raw**
   - Select **JSON** from dropdown
   - Paste the JSON above
6. Click **Send**

### Expected Response (200 OK):
```json
{
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "type": "Bearer",
    "id": 1,
    "username": "john_customer",
    "email": "john@example.com",
    "roles": ["CUSTOMER"]
}
```

### ✅ What to Check:
- Status: 200 OK
- Response contains JWT token
- Roles array contains "CUSTOMER"
- **SAVE THE TOKEN** for next requests!

---

## 📝 ENDPOINT 2: Login

### Request Details:
- **Method:** POST
- **URL:** `http://localhost:8080/auth/login`
- **Headers:**
  - Key: `Content-Type`
  - Value: `application/json`

### Request Body (JSON):
```json
{
    "username": "john_customer",
    "password": "password123"
}
```

### Steps in Postman:
1. Create new request or duplicate previous
2. Change URL to: `http://localhost:8080/auth/login`
3. Method: **POST**
4. Headers: `Content-Type: application/json`
5. Body → raw → JSON → paste login JSON
6. Click **Send**

### Expected Response (200 OK):
```json
{
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "type": "Bearer",
    "id": 1,
    "username": "john_customer",
    "email": "john@example.com",
    "roles": ["CUSTOMER"]
}
```

### ✅ What to Check:
- Status: 200 OK
- Returns JWT token
- User details are correct

---

## 📝 ENDPOINT 3: Access Customer Dashboard (Protected)

### Request Details:
- **Method:** GET
- **URL:** `http://localhost:8080/customer/dashboard`
- **Headers:**
  - Key: `Authorization`
  - Value: `Bearer YOUR_JWT_TOKEN_HERE`

### Steps in Postman:
1. Create new **GET** request
2. URL: `http://localhost:8080/customer/dashboard`
3. Click **Headers** tab
4. Add header:
   - Key: `Authorization`
   - Value: `Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...` (paste your token from login/register)
   - **Important:** Include the word "Bearer" with a space before the token
5. Click **Send**

### Expected Response (200 OK):
```json
{
    "message": "Welcome to Customer Dashboard",
    "user": "john_customer",
    "authorities": [
        {
            "authority": "CUSTOMER"
        }
    ]
}
```

### ✅ What to Check:
- Status: 200 OK
- Message shows dashboard access
- User matches your username

---

## 📝 ENDPOINT 4: Access Customer Profile

### Request Details:
- **Method:** GET
- **URL:** `http://localhost:8080/customer/profile`
- **Headers:**
  - Key: `Authorization`
  - Value: `Bearer YOUR_JWT_TOKEN_HERE`

### Steps: Same as above, just change URL to `/customer/profile`

### Expected Response (200 OK):
```json
{
    "message": "Customer Profile",
    "user": "john_customer"
}
```

---

## 🚫 ENDPOINT 5: Test Access Denied (Try to Access Admin Endpoint as Customer)

### Request Details:
- **Method:** GET
- **URL:** `http://localhost:8080/admin/dashboard`
- **Headers:**
  - Key: `Authorization`
  - Value: `Bearer YOUR_CUSTOMER_JWT_TOKEN`

### Steps: Use customer token to access admin endpoint

### Expected Response (403 FORBIDDEN):
```json
{
    "message": "Access denied: Access Denied"
}
```

### ✅ What to Check:
- Status: 403 Forbidden
- Shows access denied message
- This proves role-based security is working!

---

## 🔧 Testing TECHNICIAN Role

### STEP 1: Manually Create a Technician User

Since register API gives CUSTOMER role by default, you need to:

**Option A: Create via Database**
```sql
-- Connect to PostgreSQL database 'srsmsystem'
-- Insert a technician user
INSERT INTO users (username, email, password, full_name, phone_number, enabled, created_at, updated_at)
VALUES ('jane_tech', 'jane@example.com', '$2a$10$encoded_password_here', 'Jane Smith', '9876543210', true, NOW(), NOW());

-- Get the user_id from the above insert (e.g., 2)
-- Assign TECHNICIAN role (role_id = 2 for TECHNICIAN)
INSERT INTO user_roles (user_id, role_id) VALUES (2, 2);
```

**Option B: Register then update role manually**
1. Register as normal user
2. Update in database to add TECHNICIAN role

### STEP 2: Login as Technician

**Request:**
- **Method:** POST
- **URL:** `http://localhost:8080/auth/login`
- **Body:**
```json
{
    "username": "jane_tech",
    "password": "password123"
}
```

**Save the technician token!**

### STEP 3: Test Technician Endpoints

#### A) Technician Dashboard
- **Method:** GET
- **URL:** `http://localhost:8080/technician/dashboard`
- **Headers:** `Authorization: Bearer TECHNICIAN_TOKEN`

**Expected Response (200 OK):**
```json
{
    "message": "Welcome to Technician Dashboard",
    "user": "jane_tech",
    "authorities": [{"authority": "TECHNICIAN"}]
}
```

#### B) Technician Tasks
- **Method:** GET
- **URL:** `http://localhost:8080/technician/tasks`
- **Headers:** `Authorization: Bearer TECHNICIAN_TOKEN`

**Expected Response (200 OK):**
```json
{
    "message": "Technician Tasks",
    "user": "jane_tech"
}
```

---

## 👑 Testing ADMIN Role

### STEP 1: Create Admin User (via Database)

```sql
-- Insert admin user
INSERT INTO users (username, email, password, full_name, enabled, created_at, updated_at)
VALUES ('admin', 'admin@example.com', '$2a$10$encoded_password_here', 'System Admin', true, NOW(), NOW());

-- Assign ADMIN role (role_id = 3 for ADMIN)
INSERT INTO user_roles (user_id, role_id) VALUES (3, 3);
```

### STEP 2: Login as Admin

**Request:**
```json
{
    "username": "admin",
    "password": "admin123"
}
```

### STEP 3: Test Admin Endpoints

#### A) Admin Dashboard
- **URL:** `http://localhost:8080/admin/dashboard`
- **Method:** GET
- **Headers:** `Authorization: Bearer ADMIN_TOKEN`

#### B) User Management
- **URL:** `http://localhost:8080/admin/users`
- **Method:** GET

#### C) System Settings
- **URL:** `http://localhost:8080/admin/settings`
- **Method:** GET

---

## ❌ Testing Error Scenarios

### 1. Missing Token (401 Unauthorized)
- Try accessing `/customer/dashboard` without Authorization header
- **Expected:** 401 Unauthorized

### 2. Invalid Token (401 Unauthorized)
- Use `Authorization: Bearer invalid_token_here`
- **Expected:** 401 Unauthorized

### 3. Expired Token (401 Unauthorized)
- Wait for token to expire (24 hours by default)
- **Expected:** 401 Unauthorized

### 4. Wrong Role (403 Forbidden)
- Customer trying to access `/admin/dashboard`
- **Expected:** 403 Forbidden

### 5. Invalid Credentials (401 Unauthorized)
- Login with wrong password
```json
{
    "username": "john_customer",
    "password": "wrongpassword"
}
```
- **Expected:** 401 with "Invalid username or password"

### 6. Duplicate Username (400 Bad Request)
- Register with existing username
- **Expected:** 400 with "Username is already taken!"

### 7. Validation Errors (400 Bad Request)
- Register with invalid email or short password
```json
{
    "username": "ab",
    "email": "invalid-email",
    "password": "123"
}
```
- **Expected:** 400 with field-specific validation errors

---

## 🎯 Postman Collection Structure

### Organize Your Tests:
1. **Folder: Authentication**
   - Register Customer
   - Login
   
2. **Folder: Customer Endpoints**
   - Customer Dashboard
   - Customer Profile
   
3. **Folder: Technician Endpoints**
   - Technician Dashboard
   - Technician Tasks
   
4. **Folder: Admin Endpoints**
   - Admin Dashboard
   - Admin Users
   - Admin Settings
   
5. **Folder: Error Scenarios**
   - Missing Token
   - Invalid Token
   - Wrong Role Access
   - Invalid Credentials

---

## 💡 Pro Tips for Postman

### 1. Use Environment Variables
- Create variable: `{{baseUrl}}` = `http://localhost:8080`
- Create variable: `{{token}}` = your JWT token
- Use in requests: `{{baseUrl}}/auth/login`
- Use in headers: `Bearer {{token}}`

### 2. Auto-save Token from Response
In the **Tests** tab of login request, add:
```javascript
var jsonData = pm.response.json();
pm.environment.set("token", jsonData.token);
```

### 3. Use Pre-request Scripts
Add to all protected endpoints:
```javascript
pm.request.headers.add({
    key: 'Authorization',
    value: 'Bearer ' + pm.environment.get('token')
});
```

---

## 🐛 Troubleshooting

### Issue: Connection Refused
- **Solution:** Make sure Spring Boot app is running

### Issue: 401 on all requests
- **Solution:** Check if token is valid and properly formatted with "Bearer " prefix

### Issue: Role not found error during register
- **Solution:** Roles aren't initialized. Check DataInitializer ran successfully

### Issue: Database connection error
- **Solution:** 
  - Verify PostgreSQL is running
  - Check database `srsmsystem` exists
  - Verify credentials in application.properties

---

## ✅ Complete Test Checklist

- [ ] Application starts without errors
- [ ] Register new customer (gets CUSTOMER role)
- [ ] Login with customer credentials
- [ ] Access customer dashboard with token
- [ ] Access customer profile with token
- [ ] Try accessing admin endpoint as customer (should fail)
- [ ] Create technician user (manually)
- [ ] Login as technician
- [ ] Access technician endpoints
- [ ] Create admin user (manually)
- [ ] Login as admin
- [ ] Access admin endpoints
- [ ] Test missing token error
- [ ] Test invalid token error
- [ ] Test wrong credentials error
- [ ] Test duplicate registration error
- [ ] Test validation errors

---

## 📊 Expected Results Summary

| Endpoint | Role Required | Without Token | With Valid Token | Wrong Role |
|----------|---------------|---------------|------------------|------------|
| `/auth/register` | None | ✅ 200 | ✅ 200 | N/A |
| `/auth/login` | None | ✅ 200 | ✅ 200 | N/A |
| `/customer/**` | CUSTOMER | ❌ 401 | ✅ 200 | ❌ 403 |
| `/technician/**` | TECHNICIAN | ❌ 401 | ✅ 200 | ❌ 403 |
| `/admin/**` | ADMIN | ❌ 401 | ✅ 200 | ❌ 403 |

---

## 🎓 Understanding the Flow

1. **Register/Login** → Get JWT token
2. **Store token** → Use in subsequent requests
3. **Add to header** → `Authorization: Bearer <token>`
4. **Access protected resources** → Based on user role
5. **Token expires** → Login again to get new token

---

**Good luck with testing! 🚀**

