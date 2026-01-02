# Vehicle Management API Guide

## Overview
This guide provides endpoints for managing vehicles in the Smart Repair & Service Management System.

## Base URL
```
http://localhost:8080/api/vehicles
```

## Authentication
All endpoints require JWT authentication. Include the JWT token in the Authorization header:
```
Authorization: Bearer <your-jwt-token>
```

---

## Endpoints

### 1. Add a New Vehicle (Customer Only)
**POST** `/api/vehicles`

**Description:** Allows a customer to add a new vehicle to their account.

**Authorization:** CUSTOMER role required

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

**Response (201 Created):**
```json
{
  "id": 1,
  "vehicleNumber": "ABC-1234",
  "make": "Toyota",
  "model": "Corolla",
  "year": 2022,
  "vehicleType": "CAR",
  "customerId": 5,
  "customerName": "John Doe",
  "createdAt": "2026-01-02T10:30:00",
  "updatedAt": "2026-01-02T10:30:00"
}
```

---

### 2. Get My Vehicles (Customer Only)
**GET** `/api/vehicles`

**Description:** Retrieves all vehicles belonging to the authenticated customer.

**Authorization:** CUSTOMER role required

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "vehicleNumber": "ABC-1234",
    "make": "Toyota",
    "model": "Corolla",
    "year": 2022,
    "vehicleType": "CAR",
    "customerId": 5,
    "customerName": "John Doe",
    "createdAt": "2026-01-02T10:30:00",
    "updatedAt": "2026-01-02T10:30:00"
  },
  {
    "id": 2,
    "vehicleNumber": "XYZ-5678",
    "make": "Honda",
    "model": "Civic",
    "year": 2021,
    "vehicleType": "CAR",
    "customerId": 5,
    "customerName": "John Doe",
    "createdAt": "2026-01-01T09:15:00",
    "updatedAt": "2026-01-01T09:15:00"
  }
]
```

---

### 3. Get All Vehicles (Admin/Technician Only)
**GET** `/api/vehicles/all`

**Description:** Retrieves all vehicles in the system.

**Authorization:** ADMIN or TECHNICIAN role required

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "vehicleNumber": "ABC-1234",
    "make": "Toyota",
    "model": "Corolla",
    "year": 2022,
    "vehicleType": "CAR",
    "customerId": 5,
    "customerName": "John Doe",
    "createdAt": "2026-01-02T10:30:00",
    "updatedAt": "2026-01-02T10:30:00"
  }
]
```

---

### 4. Get Vehicle by ID
**GET** `/api/vehicles/{id}`

**Description:** Retrieves a specific vehicle by its ID.

**Authorization:** CUSTOMER, ADMIN, or TECHNICIAN role required

**Path Parameters:**
- `id` (Long) - Vehicle ID

**Example:** `/api/vehicles/1`

**Response (200 OK):**
```json
{
  "id": 1,
  "vehicleNumber": "ABC-1234",
  "make": "Toyota",
  "model": "Corolla",
  "year": 2022,
  "vehicleType": "CAR",
  "customerId": 5,
  "customerName": "John Doe",
  "createdAt": "2026-01-02T10:30:00",
  "updatedAt": "2026-01-02T10:30:00"
}
```

---

### 5. Update Vehicle (Customer Only)
**PUT** `/api/vehicles/{id}`

**Description:** Updates vehicle details. Customers can only update their own vehicles.

**Authorization:** CUSTOMER role required

**Path Parameters:**
- `id` (Long) - Vehicle ID

**Example:** `/api/vehicles/1`

**Request Body:**
```json
{
  "vehicleNumber": "ABC-1234",
  "make": "Toyota",
  "model": "Corolla GLX",
  "year": 2022,
  "vehicleType": "CAR"
}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "vehicleNumber": "ABC-1234",
  "make": "Toyota",
  "model": "Corolla GLX",
  "year": 2022,
  "vehicleType": "CAR",
  "customerId": 5,
  "customerName": "John Doe",
  "createdAt": "2026-01-02T10:30:00",
  "updatedAt": "2026-01-02T11:45:00"
}
```

---

### 6. Delete Vehicle (Customer Only)
**DELETE** `/api/vehicles/{id}`

**Description:** Deletes a vehicle. Customers can only delete their own vehicles.

**Authorization:** CUSTOMER role required

**Path Parameters:**
- `id` (Long) - Vehicle ID

**Example:** `/api/vehicles/1`

**Response (200 OK):**
```json
{
  "message": "Vehicle deleted successfully"
}
```

---

### 7. Get Vehicles by Customer ID (Admin/Technician Only)
**GET** `/api/vehicles/customer/{customerId}`

**Description:** Retrieves all vehicles belonging to a specific customer.

**Authorization:** ADMIN or TECHNICIAN role required

**Path Parameters:**
- `customerId` (Long) - Customer ID

**Example:** `/api/vehicles/customer/5`

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "vehicleNumber": "ABC-1234",
    "make": "Toyota",
    "model": "Corolla",
    "year": 2022,
    "vehicleType": "CAR",
    "customerId": 5,
    "customerName": "John Doe",
    "createdAt": "2026-01-02T10:30:00",
    "updatedAt": "2026-01-02T10:30:00"
  }
]
```

---

## Error Responses

### 400 Bad Request
```json
{
  "timestamp": "2026-01-02T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "errors": [
    {
      "field": "vehicleNumber",
      "message": "Vehicle number is required"
    }
  ]
}
```

### 401 Unauthorized
```json
{
  "error": "Unauthorized",
  "message": "Full authentication is required to access this resource"
}
```

### 403 Forbidden
```json
{
  "error": "Forbidden",
  "message": "Access Denied"
}
```

### 404 Not Found
```json
{
  "message": "Vehicle not found with id: 1"
}
```

### 409 Conflict
```json
{
  "message": "Vehicle with number ABC-1234 already exists for this customer"
}
```

---

## Postman Collection Setup

### 1. Create Environment Variables
- `base_url`: `http://localhost:8080`
- `token`: `<your-jwt-token>` (will be set automatically after login)

### 2. Set Authorization Header
For all vehicle endpoints, add:
```
Authorization: Bearer {{token}}
```

### 3. Test Flow
1. Register a user → `/auth/register`
2. Login → `/auth/login` (save token)
3. Add vehicle → `POST /api/vehicles`
4. Get my vehicles → `GET /api/vehicles`
5. Update vehicle → `PUT /api/vehicles/1`
6. Delete vehicle → `DELETE /api/vehicles/1`

---

## Business Rules

1. **Vehicle Number Uniqueness**: Each vehicle number must be unique per customer (the same vehicle number can exist for different customers).

2. **Authorization**: 
   - Customers can only add, view, update, and delete their own vehicles.
   - Admins and Technicians can view all vehicles in the system.

3. **Required Fields**: 
   - Vehicle number
   - Make
   - Model
   - Year

4. **Optional Fields**:
   - Vehicle type (default: null)

5. **Timestamps**: 
   - `createdAt` is set when the vehicle is first added.
   - `updatedAt` is updated whenever the vehicle is modified.

---

## Common Vehicle Types
- CAR
- MOTORCYCLE
- TRUCK
- VAN
- SUV
- BUS

---

## Notes
- All responses are in JSON format.
- All dates/times are in ISO 8601 format.
- Vehicle numbers are case-sensitive.
- The system prevents duplicate vehicle numbers for the same customer.

