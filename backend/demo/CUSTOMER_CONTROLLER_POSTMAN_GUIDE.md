# 🧪 Complete Postman Testing Guide for CustomerController

## 📋 Table of Contents
1. [Prerequisites](#prerequisites)
2. [Setup Steps](#setup-steps)
3. [Authentication Flow](#authentication-flow)
4. [Customer Dashboard Endpoint](#1-customer-dashboard)
5. [Profile Management Endpoints](#2-profile-management)

---

## Prerequisites

### ✅ Before You Start:
1. **PostgreSQL Database** is running on `localhost:5432`
2. Database named `srsmsystem` exists
3. **Spring Boot Application** is running
4. **Postman** is installed

### 🚀 Start Your Application:

**Option 1: Using Maven**
```bash
cd "C:\Users\EX BOOK\Desktop\Smart Repair & Service Management System\backend\demo"
mvn spring-boot:run
```

**Option 2: Using IntelliJ IDEA**
- Right-click on `SrsmsystemApplication.java`
- Select "Run 'SrsmsystemApplication'"

**✅ Wait for this message:**
```
Started SrsmsystemApplication in X.XXX seconds
```

---

## Setup Steps

### Step 1: Open Postman
- Launch Postman application
- Click **"New"** → **"HTTP Request"** or press `Ctrl+N`

### Step 2: Create a Collection (Optional but Recommended)
1. Click **"Collections"** in left sidebar
2. Click **"+"** to create new collection
3. Name it: `Smart Repair System - Customer`
4. Save it

---

## Authentication Flow

### 🔐 STEP A: Register a Customer Account

**This creates a new customer account that you'll use for testing**

1. **Create New Request in Postman:**
   - Method: `POST`
   - URL: `http://localhost:8080/auth/register`

2. **Set Headers:**
   - Click **"Headers"** tab
   - Add: `Content-Type` = `application/json`

3. **Set Request Body:**
   - Click **"Body"** tab
   - Select **"raw"**
   - Select **"JSON"** from dropdown (right side)
   - Paste this JSON:

```json
{
    "username": "john_customer",
    "email": "john@example.com",
    "password": "password123",
    "fullName": "John Doe Customer",
    "phoneNumber": "1234567890",
    "address": "123 Main Street, City, Country"
}
```

4. **Click "Send"** button

5. **Expected Response (200 OK):**
```json
{
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huX2N1c3RvbWVyIiwiaWF0IjoxNjQwOTk1MjAwLCJleHAiOjE2NDEwODE2MDB9...",
    "type": "Bearer",
    "id": 1,
    "username": "john_customer",
    "email": "john@example.com",
    "roles": [
        "CUSTOMER"
    ]
}
```

6. **IMPORTANT: Copy the Token!**
   - Copy the entire token value (the long string after `"token":`)
   - Save it in a notepad - you'll need it for ALL customer endpoints!

---

### 🔑 STEP B: Login (Alternative to Registration)

**If you already have an account, use login instead:**

1. **Create New Request:**
   - Method: `POST`
   - URL: `http://localhost:8080/auth/login`

2. **Headers:**
   - `Content-Type` = `application/json`

3. **Body (raw JSON):**
```json
{
    "username": "john_customer",
    "password": "password123"
}
```

4. **Click "Send"**

5. **Copy the JWT token from response**

---

## Customer Controller Endpoints Testing

### 1. Customer Dashboard

#### 📍 Endpoint: GET /customer/dashboard

**Purpose:** Access the customer dashboard to verify authentication works

#### Steps:

1. **Create New Request:**
   - Method: `GET`
   - URL: `http://localhost:8080/customer/dashboard`

2. **Set Authorization Header:**
   - Click **"Headers"** tab
   - Click **"Add"** or add manually:
     - Key: `Authorization`
     - Value: `Bearer eyJhbGciOiJIUzI1NiJ9...` 
     - ⚠️ **IMPORTANT:** Must have "Bearer " (with space) before the token!

   **Example:**
   ```
   Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huX2N1c3RvbWVyIiwiaWF0IjoxNjQwOTk1MjAwLCJleHAiOjE2NDEwODE2MDB9.abc123xyz...
   ```

3. **Click "Send"**

4. **Expected Response (200 OK):**
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

#### ✅ Success Criteria:
- Status Code: `200 OK`
- Response contains welcome message
- Username matches your login
- Authorities show "CUSTOMER" role

#### ❌ Common Errors:

**401 Unauthorized:**
```json
{
    "message": "Unauthorized"
}
```
**Solution:** Check if token is correct and not expired

**403 Forbidden:**
```json
{
    "message": "Access Denied"
}
```
**Solution:** User doesn't have CUSTOMER role

---

### 2. Profile Management

#### 📍 Endpoint: GET /customer/profile

**Purpose:** Retrieve the logged-in customer's profile information

#### Steps:

1. **Create New Request:**
   - Method: `GET`
   - URL: `http://localhost:8080/customer/profile`

2. **Set Authorization Header:**
   - Key: `Authorization`
   - Value: `Bearer YOUR_JWT_TOKEN`

3. **Click "Send"**

4. **Expected Response (200 OK):**
```json
{
    "id": 1,
    "username": "john_customer",
    "email": "john@example.com",
    "fullName": "John Doe Customer",
    "phone": "1234567890",
    "address": "123 Main Street, City, Country"
}
```

#### ✅ Success Criteria:
- Status: `200 OK`
- All profile fields are returned
- Data matches what you registered

---

#### 📍 Endpoint: PUT /customer/profile

**Purpose:** Update customer profile information

#### Steps:

1. **Create New Request:**
   - Method: `PUT`
   - URL: `http://localhost:8080/customer/profile`

2. **Set Headers:**
   - `Authorization` = `Bearer YOUR_JWT_TOKEN`
   - `Content-Type` = `application/json`

3. **Set Request Body:**
   - Click **"Body"** tab
   - Select **"raw"** and **"JSON"**
   - Paste:

```json
{
    "email": "john.updated@example.com",
    "fullName": "John Updated Doe",
    "phone": "9876543210",
    "address": "456 New Avenue, Updated City"
}
```

**Note:** You can update any combination of fields - all are optional!

4. **Click "Send"**

5. **Expected Response (200 OK):**
```json
{
    "id": 1,
    "username": "john_customer",
    "email": "john.updated@example.com",
    "fullName": "John Updated Doe",
    "phone": "9876543210",
    "address": "456 New Avenue, Updated City"
}
```

#### ✅ Success Criteria:
- Status: `200 OK`
- Updated fields reflect new values
- Username remains unchanged (cannot be updated)

#### ❌ Common Errors:

**400 Bad Request - Email already exists:**
```json
{
    "message": "Email already in use by another account"
}
```

---

#### 📍 Endpoint: PUT /customer/profile/change-password

**Purpose:** Change the customer's password

#### Steps:

1. **Create New Request:**
   - Method: `PUT`
   - URL: `http://localhost:8080/customer/profile/change-password`

2. **Set Headers:**
   - `Authorization` = `Bearer YOUR_JWT_TOKEN`
   - `Content-Type` = `application/json`

3. **Set Request Body:**
```json
{
    "currentPassword": "password123",
    "newPassword": "newPassword456",
    "confirmPassword": "newPassword456"
}
```

4. **Click "Send"**

5. **Expected Response (200 OK):**
```json
{
    "message": "Password changed successfully"
}
```

#### ✅ Success Criteria:
- Status: `200 OK`
- Success message is returned
- Can login with new password

#### ❌ Common Errors:

**400 Bad Request - Current password incorrect:**
```json
{
    "message": "Current password is incorrect"
}
```

**400 Bad Request - Passwords don't match:**
```json
{
    "message": "New password and confirmation do not match"
}
```

**400 Bad Request - Password too short:**
```json
{
    "message": "New password must be at least 6 characters long"
}
```

**400 Bad Request - Same password:**
```json
{
    "message": "New password must be different from current password"
}
```

---

## 🎯 Complete Testing Workflow

### Test Scenario 1: New Customer Registration & Profile Management

```
1. Register new customer (POST /auth/register)
   ↓
2. Save JWT token
   ↓
3. Access dashboard (GET /customer/dashboard)
   ↓
4. View profile (GET /customer/profile)
   ↓
5. Update profile (PUT /customer/profile)
   ↓
6. Verify updates (GET /customer/profile again)
   ↓
7. Change password (PUT /customer/profile/change-password)
   ↓
8. Logout and login with new password (POST /auth/login)
```

---

## 🛠️ Postman Tips & Tricks

### Tip 1: Save Authorization for Collection

**Set token once for all requests:**

1. Right-click your collection
2. Select **"Edit"**
3. Go to **"Authorization"** tab
4. Type: Select **"Bearer Token"**
5. Token: Paste your JWT token
6. Click **"Save"**

Now all requests in this collection will automatically use this token!

### Tip 2: Use Environment Variables

**Create variables for reusable values:**

1. Click the **"⚙️ gear icon"** (top right) → **"Manage Environments"**
2. Click **"Add"**
3. Environment Name: `Local Development`
4. Add variables:
   - `base_url` = `http://localhost:8080`
   - `jwt_token` = `your_token_here`
5. Save

**Use in requests:**
- URL: `{{base_url}}/customer/profile`
- Authorization: `Bearer {{jwt_token}}`

### Tip 3: Save Responses as Examples

After getting a successful response:
1. Click **"Save Response"** → **"Save as Example"**
2. This helps document your API and compare future responses

---

## 🔍 Troubleshooting Guide

### Problem 1: "Connection Refused" Error
**Solution:**
- Ensure Spring Boot application is running
- Check console for "Started SrsmsystemApplication"
- Verify port 8080 is not used by another application

### Problem 2: "401 Unauthorized"
**Solution:**
- Check Authorization header is set correctly
- Ensure "Bearer " prefix is included (with space)
- Token might be expired (tokens last 24 hours by default)
- Try logging in again to get a fresh token

### Problem 3: "403 Forbidden"
**Solution:**
- User doesn't have CUSTOMER role
- Check response from login/register - should show `"roles": ["CUSTOMER"]`

### Problem 4: "500 Internal Server Error"
**Solution:**
- Check Spring Boot console logs for detailed error
- Verify database is running and connected
- Check application.properties for correct database credentials

### Problem 5: Token Expired
**Symptoms:** Getting 401 after token was working
**Solution:**
- JWT tokens expire after 24 hours (86400000 ms)
- Simply login again to get a new token
- Update your saved token in Postman

---

## 📊 Expected Status Codes

| Endpoint | Success | Auth Error | Forbidden | Validation Error |
|----------|---------|------------|-----------|------------------|
| GET /customer/dashboard | 200 | 401 | 403 | - |
| GET /customer/profile | 200 | 401 | 403 | - |
| PUT /customer/profile | 200 | 401 | 403 | 400 |
| PUT /customer/profile/change-password | 200 | 401 | 403 | 400 |

---

## 🎓 Next Steps

After successfully testing these endpoints:

1. **Test with different users** - Create multiple customer accounts
2. **Test validation** - Try invalid inputs (empty fields, wrong formats)
3. **Test security** - Try accessing without token, with expired token
4. **Test role-based access** - Try accessing admin/technician endpoints with customer token

---

## 📝 Summary Checklist

- [ ] Application is running
- [ ] PostgreSQL database is connected
- [ ] Created customer account via `/auth/register`
- [ ] Saved JWT token
- [ ] Tested `/customer/dashboard` - Got 200 OK
- [ ] Tested `/customer/profile` (GET) - Retrieved profile
- [ ] Tested `/customer/profile` (PUT) - Updated profile successfully
- [ ] Tested `/customer/profile/change-password` - Changed password
- [ ] Verified new password works by logging in again

---

## 🆘 Need Help?

**Check Application Logs:**
```bash
# In your Spring Boot console, look for:
- Request mappings on startup
- Any error messages when you make requests
- Database connection status
```

**Common Log Messages:**
- ✅ `Mapped "{[/customer/dashboard],methods=[GET]}"` - Endpoint is registered
- ❌ `Access is denied` - Role/permission issue
- ❌ `JWT token is expired` - Need to login again

---

Good luck with your testing! 🚀

