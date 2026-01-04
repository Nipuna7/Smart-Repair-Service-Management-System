# Repair Request API Documentation - Two Separate Endpoints

## Overview
Two separate endpoints for two separate business logics:
1. **Validate Vehicle Ownership** - Check if a vehicle belongs to the customer
2. **Create Repair Request** - Create a new repair request for a vehicle

---

## Endpoints

### 1. Validate Vehicle Ownership (Business Logic 1)
**GET** `/api/repairs/validate-ownership/{vehicleId}`

**Access:** CUSTOMER role only

**Description:** Validates if the vehicle belongs to the authenticated customer

**Path Parameter:**
- `vehicleId` (Long): ID of the vehicle to validate

**Response (200 OK):**
```json
{
  "message": "Vehicle ownership validated successfully"
}
```

**Error Responses:**

404 Not Found - Vehicle not found:
```json
{
  "message": "Vehicle not found with id: 1"
}
```

403 Forbidden - Not vehicle owner:
```json
{
  "message": "Access denied. You can only access your own vehicles"
}
```

**Postman Example:**
```
GET http://localhost:8080/api/repairs/validate-ownership/1
Headers:
- Authorization: Bearer YOUR_JWT_TOKEN
```

---

### 2. Create Repair Request (Business Logic 2)
**POST** `/api/repairs`

**Access:** CUSTOMER role only

**Description:** Creates a new repair request for a vehicle (automatically validates ownership first)

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
- `issueDescription` (String, required): Detailed description (min 10, max 1000 characters)
- `priority` (RepairPriority enum, optional): Priority level (defaults to NORMAL)
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

404 Not Found:
```json
{
  "message": "Vehicle not found with id: 1"
}
```

403 Forbidden - Not vehicle owner:
```json
{
  "message": "Access denied. You can only access your own vehicles"
}
```

**Postman Example:**
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

## Business Logic Implementation

### Service Layer (RepairRequestService.java)

#### Method 1: validateVehicleOwnership()
```java
// BUSINESS LOGIC 1: Validate vehicle ownership
// This method checks if the vehicle belongs to the customer
@Transactional(readOnly = true)
public boolean validateVehicleOwnership(Long vehicleId, Long customerId)
```
**What it does:**
- Finds the vehicle by ID
- Checks if vehicle belongs to the customer
- Throws exception if ownership validation fails
- Returns true if validation passes

#### Method 2: createRepairRequest()
```java
// BUSINESS LOGIC 2: Create repair request for customer's vehicle
// This method creates a new repair request after validation
@Transactional
public RepairResponseDto createRepairRequest(RepairRequestDto requestDto, Long customerId)
```
**What it does:**
- Calls validateVehicleOwnership() first
- Finds the vehicle and customer
- Creates new repair entity
- Sets status to REQUESTED
- Sets priority (provided or defaults to NORMAL)
- Sets payment status to PENDING
- Saves to database
- Returns repair details as DTO

---

## Testing with Postman

### Step 1: Login to get JWT token
```
POST http://localhost:8080/api/auth/login
Body:
{
  "username": "customer@example.com",
  "password": "password123"
}
```
Copy the JWT token from response.

### Step 2: Test Endpoint 1 - Validate Vehicle Ownership
```
GET http://localhost:8080/api/repairs/validate-ownership/1
Headers:
- Authorization: Bearer YOUR_JWT_TOKEN
```
Expected Response:
```json
{
  "message": "Vehicle ownership validated successfully"
}
```

### Step 3: Test Endpoint 2 - Create Repair Request
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
Expected Response:
```json
{
  "id": 1,
  "vehicleId": 1,
  "vehicleNumber": "ABC-1234",
  ...
}
```

---

## Architecture

### Controller (RepairRequestController.java)
- **Endpoint 1:** `GET /api/repairs/validate-ownership/{vehicleId}`
  - Calls `repairRequestService.validateVehicleOwnership()`
- **Endpoint 2:** `POST /api/repairs`
  - Calls `repairRequestService.createRepairRequest()`

### Service (RepairRequestService.java)
- **Method 1:** `validateVehicleOwnership()` - Business Logic 1
- **Method 2:** `createRepairRequest()` - Business Logic 2 (calls Method 1)

### Flow Diagram
```
Client Request
     |
     v
Controller (Endpoint 1 or 2)
     |
     v
Service Layer (Method 1 or 2)
     |
     v
Repository Layer
     |
     v
Database
```

---

## Files Structure

```
controller/repair/
  └── RepairRequestController.java (2 endpoints)

service/repair/
  └── RepairRequestService.java (2 methods)

dto/repair/
  ├── RepairRequestDto.java
  └── RepairResponseDto.java

entity/
  ├── Repair.java
  ├── Vehicle.java
  └── User.java
```

---

## Security
- Both endpoints protected with `@PreAuthorize("hasAuthority('CUSTOMER')")`
- Only authenticated CUSTOMER users can access
- Vehicle ownership validated in service layer
- Uses JWT token for authentication
- Customer can only access their own vehicles

---

## Key Benefits of Separate Endpoints

1. **Clear Separation of Concerns** - Each endpoint does one thing
2. **Easy to Understand** - Clear which endpoint does what
3. **Testable** - Can test each business logic independently
4. **Reusable** - Validation endpoint can be called independently
5. **Better Error Handling** - Can identify which step failed

