# RepairRequestController - Postman Testing Guide

## Step-by-Step Testing Guide for RepairRequestController

This guide will help you test both endpoints using Postman.

---

## Prerequisites

1. ✅ Spring Boot application is running on `http://localhost:8080`
2. ✅ Database is configured and running
3. ✅ You have Postman installed
4. ✅ You have a customer account registered

---

## STEP 1: Register a Customer Account (If you don't have one)

### Request Details:
- **Method:** `POST`
- **URL:** `http://localhost:8080/api/auth/register`
- **Headers:** 
  - `Content-Type: application/json`

### Request Body:
```json
{
  "username": "john_customer",
  "email": "john@example.com",
  "password": "password123",
  "fullName": "John Customer",
  "phoneNumber": "0771234567"
}
```

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

**📝 Note:** Copy the `token` value - you'll need it for authentication!

---

## STEP 2: Login (Get JWT Token)

### Request Details:
- **Method:** `POST`
- **URL:** `http://localhost:8080/api/auth/login`
- **Headers:** 
  - `Content-Type: application/json`

### Request Body:
```json
{
  "username": "john@example.com",
  "password": "password123"
}
```

### Expected Response (200 OK):
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJqb2huQGV4YW1wbGUuY29tIiwiaWF0IjoxNzM1OTg2MDAwLCJleHAiOjE3MzYwNzI0MDB9.abc123...",
  "type": "Bearer",
  "id": 1,
  "username": "john_customer",
  "email": "john@example.com",
  "roles": ["CUSTOMER"]
}
```

**📝 Important:** Copy the `token` value from the response. This is your JWT token!

---

## STEP 3: Add a Vehicle (Required for testing repair endpoints)

### Request Details:
- **Method:** `POST`
- **URL:** `http://localhost:8080/api/vehicles/add`
- **Headers:** 
  - `Content-Type: application/json`
  - `Authorization: Bearer YOUR_JWT_TOKEN_HERE`

### Request Body:
```json
{
  "vehicleNumber": "ABC-1234",
  "make": "Toyota",
  "model": "Corolla",
  "year": 2022,
  "vehicleType": "CAR"
}
```

### How to Add Authorization Header in Postman:
1. Click on the **"Authorization"** tab
2. Select Type: **"Bearer Token"**
3. Paste your JWT token in the **"Token"** field

### Expected Response (201 Created):
```json
{
  "id": 1,
  "vehicleNumber": "ABC-1234",
  "make": "Toyota",
  "model": "Corolla",
  "year": 2022,
  "vehicleType": "CAR",
  "customerId": 1,
  "customerName": "John Customer",
  "createdAt": "2026-01-04T10:30:00",
  "updatedAt": "2026-01-04T10:30:00"
}
```

**📝 Note:** Copy the `id` value (this is your vehicleId) - you'll need it for testing repair endpoints!

---

## STEP 4: Test ENDPOINT 1 - Validate Vehicle Ownership

### Request Details:
- **Method:** `GET`
- **URL:** `http://localhost:8080/api/repairs/validate-ownership/1`
  - Replace `1` with your actual vehicleId from Step 3
- **Headers:** 
  - `Authorization: Bearer YOUR_JWT_TOKEN_HERE`

### Postman Setup:
1. Create a new request
2. Set method to **GET**
3. Enter URL: `http://localhost:8080/api/repairs/validate-ownership/1`
4. Go to **Authorization** tab
5. Select Type: **Bearer Token**
6. Paste your JWT token

### Expected Response (200 OK):
```json
{
  "message": "Vehicle ownership validated successfully"
}
```

### Possible Error Responses:

**404 Not Found - Vehicle doesn't exist:**
```json
{
  "message": "Vehicle not found with id: 1"
}
```

**403 Forbidden - Vehicle belongs to another customer:**
```json
{
  "message": "Access denied. You can only access your own vehicles"
}
```

**401 Unauthorized - Missing or invalid token:**
```json
{
  "path": "/api/repairs/validate-ownership/1",
  "error": "Unauthorized",
  "message": "Full authentication is required to access this resource",
  "status": 401
}
```

---

## STEP 5: Test ENDPOINT 2 - Create Repair Request

### Request Details:
- **Method:** `POST`
- **URL:** `http://localhost:8080/api/repairs`
- **Headers:** 
  - `Content-Type: application/json`
  - `Authorization: Bearer YOUR_JWT_TOKEN_HERE`

### Request Body:
```json
{
  "vehicleId": 1,
  "serviceType": "BREAKDOWN",
  "issueDescription": "Engine not starting, needs immediate attention",
  "priority": "HIGH"
}
```

### Postman Setup:
1. Create a new request
2. Set method to **POST**
3. Enter URL: `http://localhost:8080/api/repairs`
4. Go to **Authorization** tab
5. Select Type: **Bearer Token**
6. Paste your JWT token
7. Go to **Body** tab
8. Select **raw**
9. Select **JSON** from dropdown
10. Paste the request body

### Expected Response (201 Created):
```json
{
  "id": 1,
  "vehicleId": 1,
  "vehicleNumber": "ABC-1234",
  "vehicleMake": "Toyota",
  "vehicleModel": "Corolla",
  "customerId": 1,
  "customerName": "John Customer",
  "customerEmail": "john@example.com",
  "technicianId": null,
  "technicianName": null,
  "serviceType": "BREAKDOWN",
  "issueDescription": "Engine not starting, needs immediate attention",
  "status": "REQUESTED",
  "priority": "HIGH",
  "estimatedCost": null,
  "finalCost": null,
  "paymentStatus": "PENDING",
  "estimateApproved": null,
  "createdAt": "2026-01-04T10:35:00",
  "assignedAt": null,
  "inProgressAt": null,
  "completedAt": null,
  "cancelledAt": null,
  "updatedAt": "2026-01-04T10:35:00",
  "cancellationReason": null
}
```

### Service Type Options:
You can use any of these values for `serviceType`:
- `BREAKDOWN`
- `REGULAR_SERVICE`
- `INSPECTION`
- `BODY_REPAIR`
- `ENGINE_REPAIR`
- `ELECTRICAL`
- `TIRE_SERVICE`
- `OTHER`

### Priority Options:
You can use any of these values for `priority` (optional):
- `URGENT`
- `HIGH`
- `NORMAL` (default if not specified)
- `LOW`

### Possible Error Responses:

**404 Not Found - Vehicle doesn't exist:**
```json
{
  "message": "Vehicle not found with id: 1"
}
```

**403 Forbidden - Vehicle belongs to another customer:**
```json
{
  "message": "Access denied. You can only access your own vehicles"
}
```

**400 Bad Request - Validation error:**
```json
{
  "issueDescription": "Issue description is required"
}
```

**401 Unauthorized - Missing or invalid token:**
```json
{
  "path": "/api/repairs",
  "error": "Unauthorized",
  "message": "Full authentication is required to access this resource",
  "status": 401
}
```

---

## Complete Test Scenarios

### Scenario 1: Happy Path - Everything Works ✅

1. **Register** customer → Get token
2. **Login** → Get token
3. **Add vehicle** → Get vehicleId
4. **Validate ownership** (GET) → Success message
5. **Create repair** (POST) → Get repair details

### Scenario 2: Test with Different Vehicle ✅

1. Login as customer
2. Add another vehicle (different vehicleNumber)
3. Validate ownership for new vehicle
4. Create repair for new vehicle

### Scenario 3: Test Validation - Wrong Vehicle ❌

1. Login as customer A
2. Try to validate ownership of customer B's vehicle
3. Expected: 403 Forbidden error

### Scenario 4: Test Without Token ❌

1. Try to validate ownership without Authorization header
2. Expected: 401 Unauthorized error

### Scenario 5: Test with Invalid Data ❌

1. Login as customer
2. Try to create repair with empty issueDescription
3. Expected: 400 Bad Request with validation error

---

## Postman Collection Structure

You can organize your Postman requests like this:

```
📁 Smart Repair System
  📁 Auth
    ➤ Register Customer
    ➤ Login Customer
  📁 Vehicles
    ➤ Add Vehicle
    ➤ Get My Vehicles
  📁 Repair Requests
    ➤ Validate Vehicle Ownership
    ➤ Create Repair Request
```

---

## Tips for Using Postman

### 1. Save JWT Token as Environment Variable
1. Create an environment called "Smart Repair Dev"
2. Add variable: `jwt_token`
3. After login, manually copy token to this variable
4. Use `{{jwt_token}}` in Authorization header

### 2. Save Vehicle ID as Environment Variable
1. Add variable: `vehicle_id`
2. After adding vehicle, copy the id to this variable
3. Use `{{vehicle_id}}` in your requests

### 3. Use Pre-request Scripts (Advanced)
```javascript
// Automatically extract token from login response
pm.environment.set("jwt_token", pm.response.json().token);
```

### 4. Use Tests Tab (Advanced)
```javascript
// Test that response is successful
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Response has message", function () {
    pm.expect(pm.response.json()).to.have.property('message');
});
```

---

## Troubleshooting Common Issues

### Issue 1: 401 Unauthorized
**Solution:** 
- Check if JWT token is correct
- Check if token is expired (tokens expire after 24 hours)
- Re-login to get a new token

### Issue 2: 403 Forbidden - Access Denied
**Solution:**
- Make sure you're using CUSTOMER role token
- Check if vehicle belongs to the logged-in customer
- Verify vehicleId is correct

### Issue 3: 404 Not Found
**Solution:**
- Check if vehicle exists in database
- Verify the vehicleId in the URL
- Make sure you created a vehicle first

### Issue 4: 400 Bad Request
**Solution:**
- Check request body format
- Ensure all required fields are present
- Verify field values are valid (e.g., issueDescription min 10 characters)

### Issue 5: Connection Refused
**Solution:**
- Check if Spring Boot application is running
- Verify port is 8080
- Check application logs for errors

---

## Quick Reference

### Endpoint 1: Validate Vehicle Ownership
```
GET http://localhost:8080/api/repairs/validate-ownership/{vehicleId}
Authorization: Bearer {token}
```

### Endpoint 2: Create Repair Request
```
POST http://localhost:8080/api/repairs
Authorization: Bearer {token}
Content-Type: application/json

Body:
{
  "vehicleId": 1,
  "serviceType": "BREAKDOWN",
  "issueDescription": "Engine issue",
  "priority": "HIGH"
}
```

---

## Expected Flow

```
1. Register/Login (Get JWT Token)
         ↓
2. Add Vehicle (Get Vehicle ID)
         ↓
3. Validate Ownership (Verify access)
         ↓
4. Create Repair Request (Submit repair)
         ↓
5. Success! (Repair created with REQUESTED status)
```

---

## Summary Checklist

Before testing, make sure you have:
- ✅ Spring Boot app running
- ✅ Database configured
- ✅ Postman installed
- ✅ JWT token from login
- ✅ Vehicle ID from add vehicle
- ✅ Authorization header configured

Happy Testing! 🚀

