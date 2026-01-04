# Repair Request API Documentation

## Overview
Simple repair request system with two core business logic requirements:
1. Create repair request for customer's vehicle
2. Validate vehicle ownership

---

## Endpoint

### Create Repair Request
**POST** `/api/repairs`

**Access:** CUSTOMER role only

**Description:** Create a new repair request for a vehicle

**Request Body:**
```json
{
  "vehicleId": 1,
  "serviceType": "BREAKDOWN",
  "issueDescription": "Engine not starting, needs immediate attention",
  "priority": "HIGH"
}
```

**Request Fields:**
- `vehicleId` (Long, required): ID of the vehicle requiring repair
- `serviceType` (ServiceType enum, required): Type of service needed
  - BREAKDOWN
  - REGULAR_SERVICE
  - INSPECTION
  - BODY_REPAIR
  - ENGINE_REPAIR
  - ELECTRICAL
  - TIRE_SERVICE
  - OTHER
- `issueDescription` (String, required): Detailed description of the issue (min 10, max 1000 characters)
- `priority` (RepairPriority enum, optional): Priority level (defaults to NORMAL if not specified)
  - URGENT
  - HIGH
  - NORMAL
  - LOW

**Response (201 Created):**
```json
{
  "id": 1,
  "vehicleId": 1,
  "vehicleNumber": "ABC-1234",
  "issueDescription": "Engine not starting, needs immediate attention",
  "serviceType": "BREAKDOWN",
  "status": "REQUESTED",
  "priority": "HIGH",
  "estimatedCost": null,
  "actualCost": null,
  "createdAt": "2026-01-04T10:30:00",
  "updatedAt": "2026-01-04T10:30:00"
}
```

**Error Responses:**

400 Bad Request - Vehicle not found:
```json
{
  "message": "Vehicle not found with id: 1"
}
```

403 Forbidden - Not vehicle owner:
```json
{
  "message": "Access denied. You can only create repair requests for your own vehicles"
}
```

---

## Business Logic

### 1. Create Repair Request for Customer's Vehicle
- Creates a new repair record in the database
- Links the repair to the specified vehicle
- Links the repair to the authenticated customer
- Sets initial status to REQUESTED
- Sets payment status to PENDING
- Auto-assigns priority to NORMAL if not specified

### 2. Validate Vehicle Ownership
**Critical Security Check:** Before creating the repair request, the system validates that:
- The vehicle exists in the database
- The vehicle belongs to the authenticated customer
- This prevents customers from creating repair requests for vehicles they don't own

---

## Testing with Postman

### Step 1: Login
```
POST http://localhost:8080/api/auth/login
Body:
{
  "username": "customer@example.com",
  "password": "password123"
}
```
Copy the JWT token from response.

### Step 2: Create Repair Request
```
POST http://localhost:8080/api/repairs
Headers:
- Authorization: Bearer YOUR_JWT_TOKEN
Body:
{
  "vehicleId": 1,
  "serviceType": "BREAKDOWN",
  "issueDescription": "Engine not starting, needs immediate attention",
  "priority": "HIGH"
}
```

---

## Files Created/Modified

### DTOs
- `RepairRequestDto.java` - Input for creating repair request
- `RepairResponseDto.java` - Output response with repair details

### Service
- `RepairRequestService.java` - Contains the two business logic implementations

### Controller
- `RepairRequestController.java` - REST endpoint for creating repair requests

---

## Security
- Endpoint protected with `@PreAuthorize("hasAuthority('CUSTOMER')")`
- Only authenticated users with CUSTOMER role can access
- Vehicle ownership validated in service layer
- Uses JWT token for authentication

