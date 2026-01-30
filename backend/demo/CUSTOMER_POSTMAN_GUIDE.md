# Customer API Postman Testing Guide

## 📋 Table of Contents
1. [Prerequisites](#prerequisites)
2. [Authentication Setup](#authentication-setup)
3. [Dashboard](#dashboard)
4. [Profile Management](#profile-management)
5. [Vehicle Management](#vehicle-management)
6. [Repair Request Management](#repair-request-management)
7. [Cost Estimation & Approval](#cost-estimation--approval)
8. [Repair Tracking](#repair-tracking)
9. [Repair History](#repair-history)
10. [Troubleshooting](#troubleshooting)

---

## Prerequisites

**Base URL:** `http://localhost:8080`

**Required Headers for Protected Endpoints:**
- `Authorization: Bearer {your_jwt_token}`
- `Content-Type: application/json`

---

## Authentication Setup

### Step 1: Register a Customer Account

**Endpoint:** `POST /auth/register`

**URL:** `http://localhost:8080/auth/register`

**Headers:**
```
Content-Type: application/json
```

**Request Body:**
```json
{
  "username": "john_customer",
  "email": "john@example.com",
  "password": "password123",
  "fullName": "John Doe",
  "phoneNumber": "0771234567"
}
```

**Expected Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "username": "john_customer",
  "email": "john@example.com",
  "roles": ["ROLE_CUSTOMER"]
}
```

**Note:** Save the `token` value - you'll need it for all subsequent requests!

---

### Step 2: Login (if already registered)

**Endpoint:** `POST /auth/login`

**URL:** `http://localhost:8080/auth/login`

**Headers:**
```
Content-Type: application/json
```

**Request Body:**
```json
{
  "username": "john_customer",
  "password": "password123"
}
```

**Expected Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "username": "john_customer",
  "email": "john@example.com",
  "roles": ["ROLE_CUSTOMER"]
}
```

**⚠️ IMPORTANT:** Use the **LOGIN token** (not register token) for all protected endpoints!

---

## Dashboard

### 1. Access Customer Dashboard

**Endpoint:** `GET /customer/dashboard`

**URL:** `http://localhost:8080/customer/dashboard`

**Headers:**
```
Authorization: Bearer {your_login_token}
```

**Expected Response (200 OK):**
```json
{
  "message": "Welcome to Customer Dashboard",
  "user": "john_customer",
  "authorities": [
    {
      "authority": "ROLE_CUSTOMER"
    }
  ]
}
```

**Purpose:** Verify your authentication is working correctly.

---

## Profile Management

### 2. Get Customer Profile

**Endpoint:** `GET /customer/profile`

**URL:** `http://localhost:8080/customer/profile`

**Headers:**
```
Authorization: Bearer {your_login_token}
```

**Expected Response (200 OK):**
```json
{
  "id": 1,
  "username": "john_customer",
  "email": "john@example.com",
  "fullName": "John Doe",
  "phoneNumber": "0771234567"
}
```

---

### 3. Update Profile

**Endpoint:** `PUT /customer/profile`

**URL:** `http://localhost:8080/customer/profile`

**Headers:**
```
Authorization: Bearer {your_login_token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "fullName": "John Updated Doe",
  "phoneNumber": "0779876543",
  "email": "johnupdated@example.com"
}
```

**Expected Response (200 OK):**
```json
{
  "id": 1,
  "username": "john_customer",
  "email": "johnupdated@example.com",
  "fullName": "John Updated Doe",
  "phoneNumber": "0779876543"
}
```

---

### 4. Change Password

**Endpoint:** `PUT /customer/profile/change-password`

**URL:** `http://localhost:8080/customer/profile/change-password`

**Headers:**
```
Authorization: Bearer {your_login_token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "currentPassword": "password123",
  "newPassword": "newPassword456"
}
```

**Expected Response (200 OK):**
```json
{
  "message": "Password changed successfully"
}
```

**Note:** After changing password, you need to login again with the new password!

---

## Vehicle Management

### 5. Add a New Vehicle

**Endpoint:** `POST /customer/vehicles`

**URL:** `http://localhost:8080/customer/vehicles`

**Headers:**
```
Authorization: Bearer {your_login_token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "vehicleNumber": "ABC-1234",
  "make": "Toyota",
  "model": "Corolla",
  "year": 2022,
  "vehicleType": "CAR"
}
```

**Expected Response (201 CREATED):**
```json
{
  "id": 1,
  "vehicleNumber": "ABC-1234",
  "make": "Toyota",
  "model": "Corolla",
  "year": 2022,
  "vehicleType": "CAR",
  "ownerName": "John Doe"
}
```

**Note:** Save the vehicle `id` for future operations!

---

### 6. Get All My Vehicles

**Endpoint:** `GET /customer/vehicles`

**URL:** `http://localhost:8080/customer/vehicles`

**Headers:**
```
Authorization: Bearer {your_login_token}
```

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
    "ownerName": "John Doe"
  },
  {
    "id": 2,
    "vehicleNumber": "XYZ-5678",
    "make": "Honda",
    "model": "Civic",
    "year": 2023,
    "vehicleType": "CAR",
    "ownerName": "John Doe"
  }
]
```

---

### 7. Get a Specific Vehicle by ID

**Endpoint:** `GET /customer/vehicles/{id}`

**URL:** `http://localhost:8080/customer/vehicles/1`

**Headers:**
```
Authorization: Bearer {your_login_token}
```

**Expected Response (200 OK):**
```json
{
  "id": 1,
  "vehicleNumber": "ABC-1234",
  "make": "Toyota",
  "model": "Corolla",
  "year": 2022,
  "vehicleType": "CAR",
  "ownerName": "John Doe"
}
```

---

### 8. Update Vehicle Details

**Endpoint:** `PUT /customer/vehicles/{id}`

**URL:** `http://localhost:8080/customer/vehicles/1`

**Headers:**
```
Authorization: Bearer {your_login_token}
Content-Type: application/json
```

**Request Body:**
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
  "ownerName": "John Doe"
}
```

---

### 9. Delete Vehicle

**Endpoint:** `DELETE /customer/vehicles/{id}`

**URL:** `http://localhost:8080/customer/vehicles/1`

**Headers:**
```
Authorization: Bearer {your_login_token}
```

**Expected Response (200 OK):**
```json
{
  "message": "Vehicle deleted successfully"
}
```

**Note:** You cannot delete a vehicle that has pending repair requests!

---

## Repair Request Management

### 10. Create a Repair Request

**Endpoint:** `POST /customer/repairs`

**URL:** `http://localhost:8080/customer/repairs`

**Headers:**
```
Authorization: Bearer {your_login_token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "vehicleId": 1,
  "issueDescription": "Engine making unusual noise and oil leak detected",
  "serviceType": "REPAIR",
  "damagePhotos": ["photo1.jpg", "photo2.jpg"]
}
```

**Service Types:** `REPAIR`, `MAINTENANCE`, `INSPECTION`, `BODY_WORK`, `ELECTRICAL`

**Expected Response (201 CREATED):**
```json
{
  "id": 1,
  "requestNumber": "REQ-20260131-0001",
  "vehicleNumber": "ABC-1234",
  "issueDescription": "Engine making unusual noise and oil leak detected",
  "serviceType": "REPAIR",
  "status": "REQUESTED",
  "priority": "HIGH",
  "estimatedCost": null,
  "finalCost": null,
  "createdAt": "2026-01-31T10:30:00",
  "assignedTechnician": null
}
```

**Note:** 
- Status starts as `REQUESTED`
- Priority is auto-assigned based on service type
- Save the repair `id` for tracking!

---

### 11. View All My Repair Requests

**Endpoint:** `GET /customer/repairs`

**URL:** `http://localhost:8080/customer/repairs`

**Headers:**
```
Authorization: Bearer {your_login_token}
```

**Expected Response (200 OK):**
```json
[
  {
    "id": 1,
    "requestNumber": "REQ-20260131-0001",
    "vehicleNumber": "ABC-1234",
    "issueDescription": "Engine making unusual noise",
    "serviceType": "REPAIR",
    "status": "IN_PROGRESS",
    "priority": "HIGH",
    "estimatedCost": 15000.00,
    "finalCost": null,
    "createdAt": "2026-01-31T10:30:00"
  }
]
```

---

### 12. Get Specific Repair Request by ID

**Endpoint:** `GET /customer/repairs/{id}`

**URL:** `http://localhost:8080/customer/repairs/1`

**Headers:**
```
Authorization: Bearer {your_login_token}
```

**Expected Response (200 OK):**
```json
{
  "id": 1,
  "requestNumber": "REQ-20260131-0001",
  "vehicleNumber": "ABC-1234",
  "issueDescription": "Engine making unusual noise",
  "serviceType": "REPAIR",
  "status": "IN_PROGRESS",
  "priority": "HIGH",
  "estimatedCost": 15000.00,
  "finalCost": null,
  "createdAt": "2026-01-31T10:30:00",
  "assignedTechnician": "Mike Technician"
}
```

---

## Cost Estimation & Approval

### 13. Get Repair Cost Estimate

**Endpoint:** `GET /customer/repairs/{id}/estimate`

**URL:** `http://localhost:8080/customer/repairs/1/estimate`

**Headers:**
```
Authorization: Bearer {your_login_token}
```

**Expected Response (200 OK):**
```json
{
  "repairId": 1,
  "estimatedCost": 15000.00,
  "estimateDetails": "Parts: 10000, Labor: 5000",
  "estimateStatus": "PENDING_APPROVAL",
  "submittedAt": "2026-01-31T11:00:00"
}
```

---

### 14. Approve Cost Estimate

**Endpoint:** `POST /customer/repairs/{id}/approve`

**URL:** `http://localhost:8080/customer/repairs/1/approve`

**Headers:**
```
Authorization: Bearer {your_login_token}
```

**Expected Response (200 OK):**
```json
{
  "id": 1,
  "requestNumber": "REQ-20260131-0001",
  "status": "APPROVED",
  "estimatedCost": 15000.00,
  "message": "Cost estimate approved successfully"
}
```

**Note:** After approval, technician can start the repair work!

---

### 15. Reject Cost Estimate

**Endpoint:** `POST /customer/repairs/{id}/reject`

**URL:** `http://localhost:8080/customer/repairs/1/reject`

**Headers:**
```
Authorization: Bearer {your_login_token}
```

**Expected Response (200 OK):**
```json
{
  "id": 1,
  "requestNumber": "REQ-20260131-0001",
  "status": "ESTIMATE_REJECTED",
  "estimatedCost": 15000.00,
  "message": "Cost estimate rejected successfully"
}
```

**Note:** Technician needs to revise the estimate!

---

### 16. Approve/Reject Cost Estimate (Alternative Method)

**Endpoint:** `PUT /customer/repairs/{id}/approve-estimate`

**URL:** `http://localhost:8080/customer/repairs/1/approve-estimate?approved=true`

**Headers:**
```
Authorization: Bearer {your_login_token}
```

**Query Parameters:**
- `approved=true` for approval
- `approved=false` for rejection

**Expected Response (200 OK):**
```json
{
  "id": 1,
  "requestNumber": "REQ-20260131-0001",
  "status": "APPROVED",
  "estimatedCost": 15000.00
}
```

---

### 17. Get Final Cost

**Endpoint:** `GET /customer/repairs/{id}/final-cost`

**URL:** `http://localhost:8080/customer/repairs/1/final-cost`

**Headers:**
```
Authorization: Bearer {your_login_token}
```

**Expected Response (200 OK):**
```json
{
  "repairId": 1,
  "estimatedCost": 15000.00,
  "finalCost": 14500.00,
  "difference": -500.00,
  "status": "COMPLETED"
}
```

**Note:** Only available after repair is completed!

---

## Repair Tracking

### 18. Get Current Repair Status

**Endpoint:** `GET /customer/repairs/{id}/status`

**URL:** `http://localhost:8080/customer/repairs/1/status`

**Headers:**
```
Authorization: Bearer {your_login_token}
```

**Expected Response (200 OK):**
```json
{
  "id": 1,
  "requestNumber": "REQ-20260131-0001",
  "status": "IN_PROGRESS",
  "priority": "HIGH",
  "currentStage": "Diagnosis completed, repair in progress",
  "lastUpdated": "2026-01-31T14:30:00"
}
```

**Possible Status Values:**
- `REQUESTED` - Waiting for admin review
- `ASSIGNED` - Technician assigned
- `PENDING_APPROVAL` - Waiting for cost approval
- `APPROVED` - Customer approved estimate
- `IN_PROGRESS` - Repair work ongoing
- `WAITING_FOR_PARTS` - Parts being ordered
- `COMPLETED` - Repair finished
- `CANCELLED` - Cancelled by customer

---

### 19. Get Repair Timeline

**Endpoint:** `GET /customer/repairs/{id}/timeline`

**URL:** `http://localhost:8080/customer/repairs/1/timeline`

**Headers:**
```
Authorization: Bearer {your_login_token}
```

**Expected Response (200 OK):**
```json
{
  "repairId": 1,
  "timeline": [
    {
      "stage": "REQUESTED",
      "timestamp": "2026-01-31T10:30:00",
      "description": "Repair request created"
    },
    {
      "stage": "ASSIGNED",
      "timestamp": "2026-01-31T11:00:00",
      "description": "Assigned to Mike Technician"
    },
    {
      "stage": "IN_PROGRESS",
      "timestamp": "2026-01-31T14:00:00",
      "description": "Repair work started"
    }
  ]
}
```

---

### 20. Get Assigned Technician

**Endpoint:** `GET /customer/repairs/{id}/technician`

**URL:** `http://localhost:8080/customer/repairs/1/technician`

**Headers:**
```
Authorization: Bearer {your_login_token}
```

**Expected Response (200 OK):**
```json
{
  "repairId": 1,
  "technicianId": 5,
  "technicianName": "Mike Technician",
  "specialization": "Engine Specialist",
  "phoneNumber": "0771234568",
  "assignedAt": "2026-01-31T11:00:00"
}
```

**Note:** Returns null if no technician is assigned yet!

---

## Repair History

### 21. Get Complete Repair History

**Endpoint:** `GET /customer/repairs/history`

**URL:** `http://localhost:8080/customer/repairs/history`

**Headers:**
```
Authorization: Bearer {your_login_token}
```

**Expected Response (200 OK):**
```json
[
  {
    "id": 1,
    "requestNumber": "REQ-20260131-0001",
    "vehicleNumber": "ABC-1234",
    "serviceType": "REPAIR",
    "status": "COMPLETED",
    "finalCost": 14500.00,
    "completedAt": "2026-02-01T16:00:00"
  },
  {
    "id": 2,
    "requestNumber": "REQ-20260115-0045",
    "vehicleNumber": "ABC-1234",
    "serviceType": "MAINTENANCE",
    "status": "COMPLETED",
    "finalCost": 5000.00,
    "completedAt": "2026-01-16T10:00:00"
  }
]
```

---

### 22. Get Repair History for Specific Vehicle

**Endpoint:** `GET /customer/vehicles/{vehicleId}/repairs`

**URL:** `http://localhost:8080/customer/vehicles/1/repairs`

**Headers:**
```
Authorization: Bearer {your_login_token}
```

**Expected Response (200 OK):**
```json
[
  {
    "id": 1,
    "requestNumber": "REQ-20260131-0001",
    "vehicleNumber": "ABC-1234",
    "serviceType": "REPAIR",
    "status": "COMPLETED",
    "issueDescription": "Engine noise",
    "finalCost": 14500.00,
    "createdAt": "2026-01-31T10:30:00",
    "completedAt": "2026-02-01T16:00:00"
  }
]
```

---

## Cancellation

### 23. Cancel Repair Request

**Endpoint:** `DELETE /customer/repairs/{id}`

**URL:** `http://localhost:8080/customer/repairs/1?cancellationReason=Changed%20my%20mind`

**Headers:**
```
Authorization: Bearer {your_login_token}
```

**Query Parameters:**
- `cancellationReason` (required): Reason for cancellation

**Expected Response (200 OK):**
```json
{
  "id": 1,
  "requestNumber": "REQ-20260131-0001",
  "status": "CANCELLED",
  "cancellationReason": "Changed my mind",
  "cancelledAt": "2026-01-31T15:00:00"
}
```

**Important Rules:**
- Can only cancel when status is `REQUESTED` or `ASSIGNED`
- Cannot cancel repairs that are `IN_PROGRESS`, `COMPLETED`, or `CANCELLED`
- Cancellation reason is mandatory!

---

## Troubleshooting

### Error 401: Unauthorized

**Response:**
```json
{
  "path": "/customer/dashboard",
  "error": "Unauthorized",
  "message": "Full authentication is required to access this resource",
  "status": 401
}
```

**Solutions:**
1. Make sure you added the Authorization header
2. Use the token from **LOGIN** endpoint (not register)
3. Check token format: `Bearer {token}` (with space after Bearer)
4. Token might be expired - login again to get a new token

---

### Error 403: Forbidden / Access Denied

**Response:**
```json
{
  "message": "Access denied: Access Denied"
}
```

**Solutions:**
1. Make sure you're logged in as a CUSTOMER (not ADMIN or TECHNICIAN)
2. Check that your token has the correct role
3. Login again to refresh permissions

---

### Error 404: Not Found

**Response:**
```json
{
  "message": "Vehicle not found"
}
```

**Solutions:**
1. Check that the ID in the URL exists
2. Make sure you're trying to access your own resources
3. Verify the vehicle/repair belongs to you

---

### Error 400: Bad Request

**Response:**
```json
{
  "message": "Invalid input data"
}
```

**Solutions:**
1. Check all required fields are provided
2. Verify data types (numbers, strings, etc.)
3. Check enum values (serviceType, vehicleType, etc.)
4. Validate JSON syntax

---

## Testing Workflow Example

### Complete Flow: Register → Add Vehicle → Create Repair → Track Status

**Step 1:** Register
```
POST http://localhost:8080/auth/register
Body: { username, email, password, fullName, phoneNumber }
→ Save token
```

**Step 2:** Login
```
POST http://localhost:8080/auth/login
Body: { username, password }
→ Use this token for all requests
```

**Step 3:** Access Dashboard
```
GET http://localhost:8080/customer/dashboard
Header: Authorization: Bearer {token}
→ Verify authentication works
```

**Step 4:** Add Vehicle
```
POST http://localhost:8080/customer/vehicles
Header: Authorization: Bearer {token}
Body: { vehicleNumber, make, model, year, vehicleType }
→ Save vehicle ID
```

**Step 5:** Create Repair Request
```
POST http://localhost:8080/customer/repairs
Header: Authorization: Bearer {token}
Body: { vehicleId, issueDescription, serviceType }
→ Save repair ID
```

**Step 6:** Check Repair Status
```
GET http://localhost:8080/customer/repairs/{repairId}/status
Header: Authorization: Bearer {token}
→ Monitor progress
```

**Step 7:** Get Cost Estimate (when available)
```
GET http://localhost:8080/customer/repairs/{repairId}/estimate
Header: Authorization: Bearer {token}
```

**Step 8:** Approve Estimate
```
POST http://localhost:8080/customer/repairs/{repairId}/approve
Header: Authorization: Bearer {token}
```

**Step 9:** Track Progress
```
GET http://localhost:8080/customer/repairs/{repairId}/timeline
Header: Authorization: Bearer {token}
```

**Step 10:** View Final Cost (when completed)
```
GET http://localhost:8080/customer/repairs/{repairId}/final-cost
Header: Authorization: Bearer {token}
```

---

## Quick Reference: All Endpoints

| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/auth/register` | Register new customer |
| POST | `/auth/login` | Login and get token |
| GET | `/customer/dashboard` | Verify authentication |
| GET | `/customer/profile` | Get profile |
| PUT | `/customer/profile` | Update profile |
| PUT | `/customer/profile/change-password` | Change password |
| POST | `/customer/vehicles` | Add vehicle |
| GET | `/customer/vehicles` | Get all vehicles |
| GET | `/customer/vehicles/{id}` | Get specific vehicle |
| PUT | `/customer/vehicles/{id}` | Update vehicle |
| DELETE | `/customer/vehicles/{id}` | Delete vehicle |
| POST | `/customer/repairs` | Create repair request |
| GET | `/customer/repairs` | Get all repairs |
| GET | `/customer/repairs/{id}` | Get specific repair |
| GET | `/customer/repairs/{id}/status` | Get repair status |
| GET | `/customer/repairs/{id}/timeline` | Get repair timeline |
| GET | `/customer/repairs/{id}/technician` | Get assigned technician |
| GET | `/customer/repairs/{id}/estimate` | Get cost estimate |
| POST | `/customer/repairs/{id}/approve` | Approve estimate |
| POST | `/customer/repairs/{id}/reject` | Reject estimate |
| PUT | `/customer/repairs/{id}/approve-estimate?approved={boolean}` | Approve/Reject estimate |
| GET | `/customer/repairs/{id}/final-cost` | Get final cost |
| DELETE | `/customer/repairs/{id}?cancellationReason={reason}` | Cancel repair |
| GET | `/customer/repairs/history` | Get complete history |
| GET | `/customer/vehicles/{vehicleId}/repairs` | Get vehicle repair history |

---

## Notes

- **Always use the LOGIN token** for protected endpoints
- **Save IDs** (vehicle ID, repair ID) for future operations
- **Check status codes** to understand what happened
- **Read error messages** carefully for debugging
- **Test in order** - create vehicle before creating repair
- **Cannot delete vehicles** with active repairs
- **Cannot cancel repairs** that are in progress or completed

---

**Happy Testing! 🚀**

For any issues, check the application logs or contact the development team.

