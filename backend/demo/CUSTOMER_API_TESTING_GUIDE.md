# Customer API Testing Guide - Postman

This guide provides step-by-step instructions for testing all Customer endpoints using Postman.

## Base URL
```
http://localhost:8080
```

## Prerequisites
1. PostgreSQL database is running
2. Spring Boot application is running
3. Postman is installed

---

## Step 1: Register a New Customer

### Endpoint
```
POST /auth/register
```

### Request Body (JSON)
```json
{
  "username": "john_customer",
  "email": "john@example.com",
  "password": "password123",
  "fullName": "John Doe",
  "phoneNumber": "0771234567",
  "address": "123 Main St, Colombo"
}
```

### Expected Response (201 Created)
```json
{
  "message": "User registered successfully",
  "userId": 1
}
```

---

## Step 2: Login to Get JWT Token

### Endpoint
```
POST /auth/login
```

### Request Body (JSON)
```json
{
  "username": "john_customer",
  "password": "password123"
}
```

### Expected Response (200 OK)
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "username": "john_customer",
  "email": "john@example.com",
  "roles": ["CUSTOMER"]
}
```

**IMPORTANT:** Copy the `token` value - you'll need it for all subsequent requests!

---

## Step 3: Configure Bearer Token in Postman

For all endpoints below, you need to add the JWT token:

1. Go to the **Authorization** tab in Postman
2. Select **Type**: `Bearer Token`
3. Paste the token you copied from the login response
4. Click **Send** for each request

---

## PROFILE MANAGEMENT APIs

### 3.1 Get Customer Dashboard

**Endpoint:** `GET /customer/dashboard`

**Headers:**
- Authorization: Bearer {your_token}

**Expected Response (200 OK):**
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

---

### 3.2 Get Customer Profile

**Endpoint:** `GET /customer/profile`

**Headers:**
- Authorization: Bearer {your_token}

**Expected Response (200 OK):**
```json
{
  "id": 1,
  "username": "john_customer",
  "email": "john@example.com",
  "fullName": "John Doe",
  "phoneNumber": "0771234567",
  "address": "123 Main St, Colombo"
}
```

---

### 3.3 Update Customer Profile

**Endpoint:** `PUT /customer/profile`

**Headers:**
- Authorization: Bearer {your_token}
- Content-Type: application/json

**Request Body (JSON):**
```json
{
  "email": "john.updated@example.com",
  "fullName": "John Updated Doe",
  "phone": "0779876543",
  "address": "456 New Street, Colombo"
}
```

**Expected Response (200 OK):**
```json
{
  "id": 1,
  "username": "john_customer",
  "email": "john.updated@example.com",
  "fullName": "John Updated Doe",
  "phoneNumber": "0779876543",
  "address": "456 New Street, Colombo"
}
```

---

### 3.4 Change Password

**Endpoint:** `PUT /customer/profile/change-password`

**Headers:**
- Authorization: Bearer {your_token}
- Content-Type: application/json

**Request Body (JSON):**
```json
{
  "currentPassword": "password123",
  "newPassword": "newpassword456",
  "confirmPassword": "newpassword456"
}
```

**Expected Response (200 OK):**
```json
{
  "message": "Password changed successfully"
}
```

---

## VEHICLE MANAGEMENT APIs

### 4.1 Add a New Vehicle

**Endpoint:** `POST /customer/vehicles`

**Headers:**
- Authorization: Bearer {your_token}
- Content-Type: application/json

**Request Body (JSON):**
```json
{
  "vehicleNumber": "ABC-1234",
  "make": "Toyota",
  "model": "Corolla",
  "year": 2022,
  "vehicleType": "CAR"
}
```

**Expected Response (201 Created):**
```json
{
  "id": 1,
  "vehicleNumber": "ABC-1234",
  "make": "Toyota",
  "model": "Corolla",
  "year": 2022,
  "vehicleType": "CAR",
  "customerId": 1,
  "customerName": "John Doe",
  "createdAt": "2026-01-14T10:30:00",
  "updatedAt": "2026-01-14T10:30:00"
}
```

---

### 4.2 Get All My Vehicles

**Endpoint:** `GET /customer/vehicles`

**Headers:**
- Authorization: Bearer {your_token}

**Expected Response (200 OK):**
```json
[
  {
    "id": 1,
    "vehicleNumber": "ABC-1234",
    "make": "Toyota",
    "model": "Corolla",
    "year": 2022,
    "vehicleType": "CAR",
    "customerId": 1,
    "customerName": "John Doe",
    "createdAt": "2026-01-14T10:30:00",
    "updatedAt": "2026-01-14T10:30:00"
  }
]
```

---

### 4.3 Get Vehicle by ID

**Endpoint:** `GET /customer/vehicles/{id}`

**Example:** `GET /customer/vehicles/1`

**Headers:**
- Authorization: Bearer {your_token}

**Expected Response (200 OK):**
```json
{
  "id": 1,
  "vehicleNumber": "ABC-1234",
  "make": "Toyota",
  "model": "Corolla",
  "year": 2022,
  "vehicleType": "CAR",
  "customerId": 1,
  "customerName": "John Doe",
  "createdAt": "2026-01-14T10:30:00",
  "updatedAt": "2026-01-14T10:30:00"
}
```

---

### 4.4 Update Vehicle

**Endpoint:** `PUT /customer/vehicles/{id}`

**Example:** `PUT /customer/vehicles/1`

**Headers:**
- Authorization: Bearer {your_token}
- Content-Type: application/json

**Request Body (JSON):**
```json
{
  "vehicleNumber": "ABC-1234",
  "make": "Toyota",
  "model": "Corolla Altis",
  "year": 2023,
  "vehicleType": "CAR"
}
```

**Expected Response (200 OK):**
```json
{
  "id": 1,
  "vehicleNumber": "ABC-1234",
  "make": "Toyota",
  "model": "Corolla Altis",
  "year": 2023,
  "vehicleType": "CAR",
  "customerId": 1,
  "customerName": "John Doe",
  "createdAt": "2026-01-14T10:30:00",
  "updatedAt": "2026-01-14T11:00:00"
}
```

---

### 4.5 Delete Vehicle

**Endpoint:** `DELETE /customer/vehicles/{id}`

**Example:** `DELETE /customer/vehicles/1`

**Headers:**
- Authorization: Bearer {your_token}

**Expected Response (200 OK):**
```json
{
  "message": "Vehicle deleted successfully"
}
```

---

## REPAIR / SERVICE REQUEST APIs

### 5.1 Create Repair Request

**Endpoint:** `POST /customer/repairs`

**Headers:**
- Authorization: Bearer {your_token}
- Content-Type: application/json

**Request Body (JSON):**
```json
{
  "vehicleId": 1,
  "serviceType": "ENGINE_REPAIR",
  "issueDescription": "Engine makes strange noise and loses power when accelerating. Started 3 days ago.",
  "priority": "HIGH"
}
```

**Service Type Options:**
- `BREAKDOWN`
- `ENGINE_REPAIR`
- `ELECTRICAL`
- `BODY_REPAIR`
- `TIRE_SERVICE`
- `INSPECTION`
- `REGULAR_SERVICE`
- `OTHER`

**Priority Options (optional - auto-assigned if not provided):**
- `URGENT`
- `HIGH`
- `NORMAL`
- `LOW`

**Expected Response (201 Created):**
```json
{
  "id": 1,
  "repairRequestNumber": "RR-20260114-0001",
  "vehicleId": 1,
  "vehicleNumber": "ABC-1234",
  "vehicleMake": "Toyota",
  "vehicleModel": "Corolla",
  "customerId": 1,
  "customerName": "John Doe",
  "customerEmail": "john@example.com",
  "technicianId": null,
  "technicianName": null,
  "serviceType": "ENGINE_REPAIR",
  "issueDescription": "Engine makes strange noise and loses power when accelerating. Started 3 days ago.",
  "status": "REQUESTED",
  "priority": "HIGH",
  "estimatedCost": null,
  "finalCost": null,
  "paymentStatus": "PENDING",
  "estimateApproved": null,
  "createdAt": "2026-01-14T11:30:00",
  "assignedAt": null,
  "inProgressAt": null,
  "completedAt": null,
  "cancelledAt": null,
  "updatedAt": "2026-01-14T11:30:00",
  "cancellationReason": null
}
```

---

### 5.2 Get All My Repair Requests

**Endpoint:** `GET /customer/repairs`

**Headers:**
- Authorization: Bearer {your_token}

**Expected Response (200 OK):**
```json
[
  {
    "id": 1,
    "repairRequestNumber": "RR-20260114-0001",
    "vehicleId": 1,
    "vehicleNumber": "ABC-1234",
    "vehicleMake": "Toyota",
    "vehicleModel": "Corolla",
    "customerId": 1,
    "customerName": "John Doe",
    "customerEmail": "john@example.com",
    "technicianId": null,
    "technicianName": null,
    "serviceType": "ENGINE_REPAIR",
    "issueDescription": "Engine makes strange noise...",
    "status": "REQUESTED",
    "priority": "HIGH",
    "estimatedCost": null,
    "finalCost": null,
    "paymentStatus": "PENDING",
    "estimateApproved": null,
    "createdAt": "2026-01-14T11:30:00",
    "assignedAt": null,
    "inProgressAt": null,
    "completedAt": null,
    "cancelledAt": null,
    "updatedAt": "2026-01-14T11:30:00",
    "cancellationReason": null
  }
]
```

---

### 5.3 Get Repair Request by ID

**Endpoint:** `GET /customer/repairs/{id}`

**Example:** `GET /customer/repairs/1`

**Headers:**
- Authorization: Bearer {your_token}

**Expected Response (200 OK):**
```json
{
  "id": 1,
  "repairRequestNumber": "RR-20260114-0001",
  "vehicleId": 1,
  "vehicleNumber": "ABC-1234",
  "vehicleMake": "Toyota",
  "vehicleModel": "Corolla",
  "customerId": 1,
  "customerName": "John Doe",
  "customerEmail": "john@example.com",
  "technicianId": null,
  "technicianName": null,
  "serviceType": "ENGINE_REPAIR",
  "issueDescription": "Engine makes strange noise...",
  "status": "REQUESTED",
  "priority": "HIGH",
  "estimatedCost": null,
  "finalCost": null,
  "paymentStatus": "PENDING",
  "estimateApproved": null,
  "createdAt": "2026-01-14T11:30:00",
  "assignedAt": null,
  "inProgressAt": null,
  "completedAt": null,
  "cancelledAt": null,
  "updatedAt": "2026-01-14T11:30:00",
  "cancellationReason": null
}
```

---

### 5.4 Approve/Reject Cost Estimate

**Endpoint:** `PUT /customer/repairs/{id}/approve-estimate?approved={true/false}`

**Example (Approve):** `PUT /customer/repairs/1/approve-estimate?approved=true`

**Example (Reject):** `PUT /customer/repairs/1/approve-estimate?approved=false`

**Headers:**
- Authorization: Bearer {your_token}

**Note:** This endpoint only works when repair status is `ESTIMATE_SUBMITTED`

**Expected Response (200 OK):**
```json
{
  "id": 1,
  "repairRequestNumber": "RR-20260114-0001",
  "status": "APPROVED",
  "estimateApproved": true,
  ...
}
```

---

### 5.5 Cancel Repair Request

**Endpoint:** `DELETE /customer/repairs/{id}?cancellationReason={reason}`

**Example:** `DELETE /customer/repairs/1?cancellationReason=Changed my mind`

**Headers:**
- Authorization: Bearer {your_token}

**Note:** Cancellation only allowed for status `REQUESTED` or `ASSIGNED`

**Expected Response (200 OK):**
```json
{
  "id": 1,
  "repairRequestNumber": "RR-20260114-0001",
  "status": "CANCELLED",
  "cancellationReason": "Changed my mind",
  "cancelledAt": "2026-01-14T12:00:00",
  ...
}
```

---

### 5.6 Get My Complete Repair History

**Endpoint:** `GET /customer/repairs/history`

**Headers:**
- Authorization: Bearer {your_token}

**Expected Response (200 OK):**
```json
[
  {
    "id": 2,
    "repairRequestNumber": "RR-20260114-0002",
    "status": "COMPLETED",
    "createdAt": "2026-01-13T10:00:00",
    ...
  },
  {
    "id": 1,
    "repairRequestNumber": "RR-20260114-0001",
    "status": "CANCELLED",
    "createdAt": "2026-01-14T11:30:00",
    ...
  }
]
```

**Note:** Results are sorted by creation date (newest first)

---

### 5.7 Get Repair History for Specific Vehicle

**Endpoint:** `GET /customer/vehicles/{vehicleId}/repairs`

**Example:** `GET /customer/vehicles/1/repairs`

**Headers:**
- Authorization: Bearer {your_token}

**Expected Response (200 OK):**
```json
[
  {
    "id": 2,
    "repairRequestNumber": "RR-20260113-0002",
    "vehicleId": 1,
    "vehicleNumber": "ABC-1234",
    "status": "COMPLETED",
    "createdAt": "2026-01-13T10:00:00",
    ...
  },
  {
    "id": 1,
    "repairRequestNumber": "RR-20260114-0001",
    "vehicleId": 1,
    "vehicleNumber": "ABC-1234",
    "status": "CANCELLED",
    "createdAt": "2026-01-14T11:30:00",
    ...
  }
]
```

---

## Common Error Responses

### 401 Unauthorized (Missing or Invalid Token)
```json
{
  "path": "/customer/dashboard",
  "error": "Unauthorized",
  "message": "Full authentication is required to access this resource",
  "status": 401
}
```

**Solution:** Make sure you've added the Bearer token in Authorization tab

---

### 403 Forbidden (Wrong Role)
```json
{
  "message": "Access denied: Access Denied"
}
```

**Solution:** Make sure you're logged in as CUSTOMER role

---

### 400 Bad Request (Validation Error)
```json
{
  "message": "Issue description must be at least 10 characters long"
}
```

**Solution:** Check your request body matches the validation requirements

---

### 404 Not Found (Resource Not Found)
```json
{
  "message": "Vehicle not found with id: 999"
}
```

**Solution:** Make sure the resource ID exists and belongs to you

---

## Testing Workflow Summary

1. **Register** a new customer (`POST /auth/register`)
2. **Login** to get JWT token (`POST /auth/login`)
3. **Copy token** and add to Authorization header for all requests
4. **Test Profile APIs** (dashboard, get profile, update profile, change password)
5. **Test Vehicle APIs** (add vehicle, get vehicles, update vehicle, delete vehicle)
6. **Test Repair APIs** (create repair, get repairs, approve estimate, cancel, history)

---

## Repair Status Flow

```
REQUESTED → ASSIGNED → IN_PROGRESS → ESTIMATE_SUBMITTED → APPROVED → COMPLETED → DELIVERED
                                                        ↓
                                                   CANCELLED
```

**Customer Actions:**
- Create repair (status: REQUESTED)
- Cancel repair (when REQUESTED or ASSIGNED)
- Approve/Reject estimate (when ESTIMATE_SUBMITTED)
- View history (all statuses)

---

## Priority Auto-Assignment Rules

- `BREAKDOWN` → `URGENT` (vehicle not operational)
- `ENGINE_REPAIR`, `ELECTRICAL` → `HIGH` (safety/functionality issues)
- `BODY_REPAIR`, `TIRE_SERVICE`, `INSPECTION` → `NORMAL`
- `REGULAR_SERVICE`, `OTHER` → `LOW` (routine maintenance)

---

## Tips for Testing

1. **Always check token expiration** - Token expires after 24 hours (86400000 ms)
2. **Use environment variables in Postman** - Store token and base URL
3. **Create a Postman Collection** - Save all requests for reuse
4. **Test error scenarios** - Try accessing resources that don't belong to you
5. **Test validation** - Try sending invalid data to see validation errors
6. **Test workflows** - Create vehicle → Create repair → Cancel repair
7. **Check timestamps** - All timestamps are in ISO 8601 format

---

## Postman Environment Setup

Create a new environment with these variables:

| Variable | Initial Value | Current Value |
|----------|--------------|---------------|
| base_url | http://localhost:8080 | http://localhost:8080 |
| token | | (paste token after login) |
| customer_id | | (paste customer ID after login) |
| vehicle_id | | (paste vehicle ID after creation) |
| repair_id | | (paste repair ID after creation) |

Then use `{{base_url}}`, `{{token}}`, etc. in your requests.

---

## Success! 🎉

You have successfully consolidated all vehicle and repair management features into the CustomerService class. All endpoints are now accessible under the `/customer` prefix.

**Key Benefits:**
- ✅ Simplified architecture - one service for all customer features
- ✅ Consistent authentication handling
- ✅ Easy to maintain and extend
- ✅ Clear API structure under `/customer` prefix
- ✅ Automatic ownership validation built-in

For any issues or questions, please refer to the Spring Boot console logs for detailed error messages.

