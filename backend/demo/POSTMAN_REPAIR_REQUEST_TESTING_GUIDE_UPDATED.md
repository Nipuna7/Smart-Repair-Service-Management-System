# Postman Testing Guide for RepairRequestController (Updated)

## Overview
This guide provides step-by-step instructions to test the RepairRequestController endpoints using Postman.
The controller now includes three important business logic validations:
1. **Prevent multiple active repair requests** for the same vehicle
2. **Validate input** (issue description, service type)
3. **Auto-generate repair request number** (format: RR-YYYYMMDD-XXXX)

---

## Prerequisites

Before testing repair endpoints, you must:
1. **Register** a customer account
2. **Login** to get JWT token
3. **Add a vehicle** for the customer

---

## Step 1: Register a Customer

### Endpoint
```
POST http://localhost:8080/auth/register
```

### Headers
```
Content-Type: application/json
```

### Request Body
```json
{
  "fullName": "John Doe",
  "email": "john.doe@example.com",
  "password": "Password123",
  "phoneNumber": "0771234567"
}
```

### Expected Response (200 OK)
```json
{
  "message": "User registered successfully. Default role: CUSTOMER"
}
```

---

## Step 2: Login to Get JWT Token

### Endpoint
```
POST http://localhost:8080/auth/login
```

### Headers
```
Content-Type: application/json
```

### Request Body
```json
{
  "email": "john.doe@example.com",
  "password": "Password123"
}
```

### Expected Response (200 OK)
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "userId": 1,
  "email": "john.doe@example.com",
  "fullName": "John Doe",
  "roles": ["CUSTOMER"]
}
```

### Important
**Copy the token** from the response. You will use this in the `Authorization` header for all subsequent requests.

---

## Step 3: Add a Vehicle

### Endpoint
```
POST http://localhost:8080/api/vehicles
```

### Headers
```
Content-Type: application/json
Authorization: Bearer <your_jwt_token_from_login>
```

### Request Body
```json
{
  "vehicleNumber": "ABC-1234",
  "make": "Toyota",
  "model": "Corolla",
  "year": 2022,
  "vehicleType": "CAR"
}
```

### Expected Response (201 CREATED)
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
  "createdAt": "2026-01-05T10:30:00",
  "updatedAt": "2026-01-05T10:30:00"
}
```

### Important
**Copy the vehicle id** (in this example: 1). You will use this when creating repair requests.

---

## Step 4: Validate Vehicle Ownership

This endpoint validates that the vehicle belongs to the authenticated customer.

### Endpoint
```
GET http://localhost:8080/api/repairs/validate-ownership/{vehicleId}
```

Example:
```
GET http://localhost:8080/api/repairs/validate-ownership/1
```

### Headers
```
Authorization: Bearer <your_jwt_token_from_login>
```

### Expected Response (200 OK)
```json
{
  "message": "Vehicle ownership validated successfully"
}
```

### Error Cases

#### Case 1: Vehicle Not Found
**Response (500 Internal Server Error)**
```json
{
  "message": "Vehicle not found with id: 999"
}
```

#### Case 2: Access Denied (Vehicle belongs to another customer)
**Response (500 Internal Server Error)**
```json
{
  "message": "Access denied. You can only access your own vehicles"
}
```

#### Case 3: Unauthorized (No token or invalid token)
**Response (401 Unauthorized)**
```json
{
  "path": "/api/repairs/validate-ownership/1",
  "error": "Unauthorized",
  "message": "Full authentication is required to access this resource",
  "status": 401
}
```

---

## Step 5: Create Repair Request

This endpoint creates a new repair request with the following validations:
- **Input validation**: Issue description (10-1000 characters), service type required
- **Vehicle ownership validation**: Ensures the vehicle belongs to the customer
- **Active repair check**: Prevents multiple active repairs for the same vehicle
- **Auto-generates** repair request number (format: RR-20260105-0001)

### Endpoint
```
POST http://localhost:8080/api/repairs
```

### Headers
```
Content-Type: application/json
Authorization: Bearer <your_jwt_token_from_login>
```

### Request Body (Valid Request)
```json
{
  "vehicleId": 1,
  "serviceType": "BREAKDOWN",
  "issueDescription": "Engine overheating and making unusual noise. Needs immediate attention.",
  "priority": "URGENT"
}
```

### Service Type Options
- `BREAKDOWN`
- `REGULAR_SERVICE`
- `INSPECTION`
- `BODY_REPAIR`
- `ENGINE_REPAIR`
- `ELECTRICAL`
- `TIRE_SERVICE`
- `OTHER`

### Priority Options (Optional - defaults to NORMAL)
- `URGENT`
- `HIGH`
- `NORMAL`
- `LOW`

### Expected Response (201 CREATED)
```json
{
  "id": 1,
  "repairRequestNumber": "RR-20260105-0001",
  "vehicleId": 1,
  "vehicleNumber": "ABC-1234",
  "vehicleMake": "Toyota",
  "vehicleModel": "Corolla",
  "customerId": 1,
  "customerName": "John Doe",
  "customerEmail": "john.doe@example.com",
  "technicianId": null,
  "technicianName": null,
  "serviceType": "BREAKDOWN",
  "issueDescription": "Engine overheating and making unusual noise. Needs immediate attention.",
  "status": "REQUESTED",
  "priority": "URGENT",
  "estimatedCost": null,
  "finalCost": null,
  "paymentStatus": "PENDING",
  "estimateApproved": null,
  "createdAt": "2026-01-05T11:00:00",
  "assignedAt": null,
  "inProgressAt": null,
  "completedAt": null,
  "cancelledAt": null,
  "updatedAt": "2026-01-05T11:00:00",
  "cancellationReason": null
}
```

---

## Error Test Cases

### Test Case 1: Invalid Input - Missing Service Type

**Request Body**
```json
{
  "vehicleId": 1,
  "issueDescription": "Engine overheating and making unusual noise. Needs immediate attention."
}
```

**Expected Response (400 Bad Request)**
```json
{
  "serviceType": "Service type is required"
}
```

---

### Test Case 2: Invalid Input - Missing Issue Description

**Request Body**
```json
{
  "vehicleId": 1,
  "serviceType": "BREAKDOWN"
}
```

**Expected Response (400 Bad Request)**
```json
{
  "issueDescription": "Issue description is required"
}
```

---

### Test Case 3: Invalid Input - Issue Description Too Short

**Request Body**
```json
{
  "vehicleId": 1,
  "serviceType": "BREAKDOWN",
  "issueDescription": "Problem"
}
```

**Expected Response (400 Bad Request)**
```json
{
  "issueDescription": "Description must be between 10 and 1000 characters"
}
```

---

### Test Case 4: Multiple Active Repair Requests

**Scenario**: Try to create a second repair request for the same vehicle while the first one is still active (status: REQUESTED, ASSIGNED, IN_PROGRESS, ESTIMATE_SUBMITTED, or APPROVED).

**Request Body**
```json
{
  "vehicleId": 1,
  "serviceType": "REGULAR_SERVICE",
  "issueDescription": "Need routine maintenance check for the vehicle."
}
```

**Expected Response (500 Internal Server Error)**
```json
{
  "message": "This vehicle already has an active repair request. Please wait until the current repair is completed."
}
```

---

### Test Case 5: Unauthorized Access (No Token)

**Headers**
```
Content-Type: application/json
(No Authorization header)
```

**Expected Response (401 Unauthorized)**
```json
{
  "path": "/api/repairs",
  "error": "Unauthorized",
  "message": "Full authentication is required to access this resource",
  "status": 401
}
```

---

### Test Case 6: Invalid Token

**Headers**
```
Content-Type: application/json
Authorization: Bearer invalid_token_here
```

**Expected Response (403 Forbidden)**
```json
{
  "message": "Access denied: Access Denied"
}
```

---

### Test Case 7: Vehicle Not Found

**Request Body**
```json
{
  "vehicleId": 999,
  "serviceType": "BREAKDOWN",
  "issueDescription": "Engine overheating and making unusual noise. Needs immediate attention."
}
```

**Expected Response (500 Internal Server Error)**
```json
{
  "message": "Vehicle not found with id: 999"
}
```

---

### Test Case 8: Access Denied - Vehicle Belongs to Another Customer

**Scenario**: Try to create a repair request for a vehicle that belongs to another customer.

**Request Body**
```json
{
  "vehicleId": 5,
  "serviceType": "BREAKDOWN",
  "issueDescription": "Engine overheating and making unusual noise. Needs immediate attention."
}
```

**Expected Response (500 Internal Server Error)**
```json
{
  "message": "Access denied. You can only access your own vehicles"
}
```

---

## Testing Multiple Repair Requests

### Test Scenario: Create Multiple Repairs After Completion

1. **Create first repair request** (follow Step 5)
2. **Manually update the repair status** in the database to `COMPLETED` or `CANCELLED`
3. **Create second repair request** - Should succeed
4. **Verify auto-generated repair request numbers** are unique and sequential
   - First: `RR-20260105-0001`
   - Second: `RR-20260105-0002`

---

## Summary of Business Logic Implemented

### 1. Prevent Multiple Active Repair Requests
- **Method**: `checkForActiveRepairRequests(Long vehicleId)`
- **Logic**: Checks if vehicle has repair with status: REQUESTED, ASSIGNED, IN_PROGRESS, ESTIMATE_SUBMITTED, or APPROVED
- **Error**: "This vehicle already has an active repair request. Please wait until the current repair is completed."

### 2. Validate Input
- **Method**: `validateRepairRequestInput(RepairRequestDto requestDto)`
- **Validations**:
  - Service type must not be null
  - Issue description must not be null or empty
  - Issue description length: 10-1000 characters
- **Errors**: Specific validation messages for each case

### 3. Auto-Generate Repair Request Number
- **Method**: `generateRepairRequestNumber()`
- **Format**: `RR-YYYYMMDD-XXXX`
  - `RR`: Prefix for Repair Request
  - `YYYYMMDD`: Current date (e.g., 20260105)
  - `XXXX`: Sequential 4-digit number (e.g., 0001, 0002)
- **Example**: `RR-20260105-0001`, `RR-20260105-0002`

---

## Notes

1. **Always use the login token** in the Authorization header
2. **Token format**: `Bearer <your_token>`
3. **Vehicle must belong to the authenticated customer**
4. **Only one active repair** per vehicle at a time
5. **Issue description** must be detailed (10-1000 characters)
6. **Repair request number** is auto-generated and unique
7. **Priority is optional** - defaults to NORMAL if not provided

---

## Troubleshooting

### Problem: 403 Forbidden
**Solution**: Make sure you're using the **login token** (not register response). The token should start with "Bearer " followed by the JWT token.

### Problem: "Access denied. You can only access your own vehicles"
**Solution**: Ensure the vehicleId in your request belongs to the authenticated customer.

### Problem: "This vehicle already has an active repair request"
**Solution**: Wait until the current repair is completed/cancelled, or manually update the status in the database.

### Problem: 401 Unauthorized
**Solution**: 
- Verify the token is valid and not expired
- Check the Authorization header format: `Bearer <token>`
- Make sure you've logged in and copied the correct token

---

## Database Schema Reference

### Repair Table Fields
- `id`: Auto-generated primary key
- `repair_request_number`: Auto-generated unique number (e.g., RR-20260105-0001)
- `vehicle_id`: Foreign key to vehicles table
- `customer_id`: Foreign key to users table
- `service_type`: Enum (BREAKDOWN, REGULAR_SERVICE, etc.)
- `issue_description`: Text (10-1000 characters)
- `status`: Enum (REQUESTED, ASSIGNED, IN_PROGRESS, etc.)
- `priority`: Enum (URGENT, HIGH, NORMAL, LOW)
- `payment_status`: Enum (PENDING, PAID, REFUNDED)
- `created_at`: Timestamp
- `updated_at`: Timestamp

---

## Conclusion

This testing guide covers all scenarios for the updated RepairRequestController with the three new business logic implementations:
1. ✅ Prevent multiple active repair requests
2. ✅ Validate input (issue description, service type)
3. ✅ Auto-generate repair request number

Follow each step carefully and verify the responses match the expected outputs.

