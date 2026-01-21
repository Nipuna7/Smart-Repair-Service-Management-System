# Admin Repair Request Review Service - API Testing Guide

## Overview
This guide covers testing the **Repair Request Review Service** for the Admin role.

## Prerequisites
1. Start the Spring Boot application
2. Have Postman installed
3. Have at least one ADMIN user registered
4. Have some repair requests created by customers

---

## Step 1: Register an Admin User (if not already done)

**Endpoint:** `POST http://localhost:8080/auth/register`

**Request Body:**
```json
{
  "fullName": "Admin User",
  "email": "admin@example.com",
  "password": "admin123",
  "phoneNumber": "0771234567",
  "address": "Admin Office"
}
```

**Response:**
```json
{
  "message": "User registered successfully with role: CUSTOMER"
}
```

**Note:** By default, users are registered as CUSTOMER. You need to manually update the database to change the role to ADMIN.

### Update User Role to ADMIN (SQL)
Connect to your PostgreSQL database and run:
```sql
UPDATE users SET role_id = (SELECT id FROM roles WHERE name = 'ROLE_ADMIN') 
WHERE email = 'admin@example.com';
```

---

## Step 2: Login as Admin

**Endpoint:** `POST http://localhost:8080/auth/login`

**Request Body:**
```json
{
  "email": "admin@example.com",
  "password": "admin123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "email": "admin@example.com",
  "roles": ["ROLE_ADMIN"]
}
```

**Important:** Copy the `token` value. You'll need it for all subsequent requests.

---

## Step 3: Set Up Authorization in Postman

For all Admin endpoints, you need to include the JWT token:

1. In Postman, go to the **Authorization** tab
2. Select **Type:** `Bearer Token`
3. Paste the token from Step 2 into the **Token** field

OR

Add a header manually:
- **Key:** `Authorization`
- **Value:** `Bearer eyJhbGciOiJIUzI1NiJ9...` (your actual token)

---

## API Endpoints for Repair Request Review Service

### 1. View All Repair Requests

**Purpose:** Admin views all repair requests in the system

**Endpoint:** `GET http://localhost:8080/admin/repairs`

**Authorization:** Bearer Token (from Step 2)

**Response:**
```json
[
  {
    "id": 1,
    "repairRequestNumber": "RR-20260121-0001",
    "vehicleId": 1,
    "vehicleNumber": "ABC-1234",
    "vehicleMake": "Toyota",
    "vehicleModel": "Corolla",
    "customerId": 2,
    "customerName": "John Doe",
    "customerEmail": "john@example.com",
    "technicianId": null,
    "technicianName": null,
    "serviceType": "REGULAR_SERVICE",
    "issueDescription": "Engine making strange noise",
    "status": "REQUESTED",
    "priority": "NORMAL",
    "estimatedCost": null,
    "finalCost": null,
    "paymentStatus": "PENDING",
    "estimateApproved": null,
    "createdAt": "2026-01-21T10:30:00",
    "assignedAt": null,
    "inProgressAt": null,
    "completedAt": null,
    "cancelledAt": null,
    "updatedAt": "2026-01-21T10:30:00",
    "cancellationReason": null,
    "diagnosisDetails": null,
    "repairNotes": null
  }
]
```

---

### 2. View Repair Requests by Status

**Purpose:** Filter repair requests by their current status

**Endpoint:** `GET http://localhost:8080/admin/repairs/status/{status}`

**Available Status Values:**
- `REQUESTED` - New repair requests waiting for assignment
- `ASSIGNED` - Assigned to a technician
- `IN_PROGRESS` - Technician is working on it
- `ESTIMATE_SUBMITTED` - Cost estimate submitted
- `APPROVED` - Customer approved the estimate
- `COMPLETED` - Repair completed
- `CANCELLED` - Repair cancelled
- `DELIVERED` - Vehicle delivered back to customer

**Example 1:** Get all REQUESTED repairs
```
GET http://localhost:8080/admin/repairs/status/REQUESTED
```

**Example 2:** Get all IN_PROGRESS repairs
```
GET http://localhost:8080/admin/repairs/status/IN_PROGRESS
```

**Response:** Array of RepairResponseDto objects matching the status

---

### 3. View Repair Requests by Priority

**Purpose:** Filter repair requests by priority level

**Endpoint:** `GET http://localhost:8080/admin/repairs/priority/{priority}`

**Available Priority Values:**
- `URGENT` - Critical issues requiring immediate attention
- `HIGH` - Important repairs
- `NORMAL` - Standard priority
- `LOW` - Non-critical maintenance

**Example 1:** Get all URGENT repairs
```
GET http://localhost:8080/admin/repairs/priority/URGENT
```

**Example 2:** Get all HIGH priority repairs
```
GET http://localhost:8080/admin/repairs/priority/HIGH
```

**Response:** Array of RepairResponseDto objects matching the priority

---

### 4. View Repair Requests by Date Range

**Purpose:** Filter repair requests created within a specific date range

**Endpoint:** `GET http://localhost:8080/admin/repairs/date-range`

**Query Parameters:**
- `startDate` (required) - Start date in ISO format (YYYY-MM-DD)
- `endDate` (required) - End date in ISO format (YYYY-MM-DD)

**Example 1:** Get repairs from January 1 to January 31, 2026
```
GET http://localhost:8080/admin/repairs/date-range?startDate=2026-01-01&endDate=2026-01-31
```

**Example 2:** Get repairs from last week
```
GET http://localhost:8080/admin/repairs/date-range?startDate=2026-01-15&endDate=2026-01-21
```

**Response:** Array of RepairResponseDto objects created within the date range

---

### 5. View Specific Repair Request Details

**Purpose:** Get detailed information about a specific repair request

**Endpoint:** `GET http://localhost:8080/admin/repairs/{id}`

**Example:** Get repair request with ID 1
```
GET http://localhost:8080/admin/repairs/1
```

**Response:**
```json
{
  "id": 1,
  "repairRequestNumber": "RR-20260121-0001",
  "vehicleId": 1,
  "vehicleNumber": "ABC-1234",
  "vehicleMake": "Toyota",
  "vehicleModel": "Corolla",
  "customerId": 2,
  "customerName": "John Doe",
  "customerEmail": "john@example.com",
  "technicianId": 3,
  "technicianName": "Mike Smith",
  "serviceType": "REGULAR_SERVICE",
  "issueDescription": "Engine making strange noise",
  "status": "IN_PROGRESS",
  "priority": "HIGH",
  "estimatedCost": 15000.00,
  "finalCost": null,
  "paymentStatus": "PENDING",
  "estimateApproved": true,
  "createdAt": "2026-01-21T10:30:00",
  "assignedAt": "2026-01-21T11:00:00",
  "inProgressAt": "2026-01-21T11:30:00",
  "completedAt": null,
  "cancelledAt": null,
  "updatedAt": "2026-01-21T11:30:00",
  "cancellationReason": null,
  "diagnosisDetails": "Worn out engine belt detected",
  "repairNotes": "Replacing belt and checking oil levels"
}
```

---

### 6. View Repair Requests with Multiple Filters

**Purpose:** Filter repair requests using multiple criteria simultaneously

**Endpoint:** `GET http://localhost:8080/admin/repairs/filter`

**Query Parameters (All Optional):**
- `status` - Filter by repair status
- `priority` - Filter by repair priority
- `startDate` - Start date in ISO format (YYYY-MM-DD)
- `endDate` - End date in ISO format (YYYY-MM-DD)

**Example 1:** Get URGENT repairs that are REQUESTED
```
GET http://localhost:8080/admin/repairs/filter?status=REQUESTED&priority=URGENT
```

**Example 2:** Get HIGH priority repairs from January
```
GET http://localhost:8080/admin/repairs/filter?priority=HIGH&startDate=2026-01-01&endDate=2026-01-31
```

**Example 3:** Get IN_PROGRESS URGENT repairs from last week
```
GET http://localhost:8080/admin/repairs/filter?status=IN_PROGRESS&priority=URGENT&startDate=2026-01-15&endDate=2026-01-21
```

**Example 4:** Get all repairs from a specific date range only
```
GET http://localhost:8080/admin/repairs/filter?startDate=2026-01-01&endDate=2026-01-31
```

**Response:** Array of RepairResponseDto objects matching all specified criteria

---

## Common Response Codes

- **200 OK** - Request successful
- **401 Unauthorized** - Token missing or invalid (check if token is set)
- **403 Forbidden** - User doesn't have ADMIN role
- **404 Not Found** - Repair request not found (invalid ID)
- **500 Internal Server Error** - Server error (check application logs)

---

## Troubleshooting

### Issue: Getting 401 Unauthorized
**Solution:** 
- Ensure you're logged in as an ADMIN user
- Check that the Authorization header has the correct Bearer token
- Token might be expired - login again to get a new token

### Issue: Getting 403 Forbidden
**Solution:**
- Verify your user has the ADMIN role in the database
- Use the SQL query from Step 1 to update the role

### Issue: Empty Array Response
**Solution:**
- No repair requests match your criteria
- Create some repair requests first using customer APIs
- Check if the filter criteria are correct

### Issue: 404 Not Found
**Solution:**
- The repair ID doesn't exist
- Use "View All Repair Requests" to see available IDs

---

## Testing Workflow

### Recommended Testing Sequence:

1. **Login as Admin** (Step 2)
2. **View All Repair Requests** (Endpoint 1) - Get overview of all repairs
3. **Filter by Status** (Endpoint 2) - See repairs in specific status (e.g., REQUESTED)
4. **Filter by Priority** (Endpoint 3) - Check urgent repairs
5. **View Specific Repair** (Endpoint 5) - Get detailed information
6. **Filter by Date Range** (Endpoint 4) - See repairs from specific period
7. **Use Multiple Filters** (Endpoint 6) - Complex filtering

---

## Business Use Cases

### Use Case 1: Admin Morning Review
```
GET /admin/repairs/status/REQUESTED
```
Admin checks all new repair requests that came in overnight.

### Use Case 2: Priority Management
```
GET /admin/repairs/priority/URGENT
```
Admin identifies critical repairs that need immediate attention.

### Use Case 3: Weekly Report
```
GET /admin/repairs/date-range?startDate=2026-01-15&endDate=2026-01-21
```
Admin generates weekly report of all repair activities.

### Use Case 4: Critical Queue
```
GET /admin/repairs/filter?status=REQUESTED&priority=URGENT
```
Admin finds urgent repairs waiting for technician assignment.

### Use Case 5: In-Progress Monitoring
```
GET /admin/repairs/status/IN_PROGRESS
```
Admin monitors all repairs currently being worked on.

---

## Notes

1. **Admin can see ALL repair requests** from all customers
2. **Admin can see customer and technician information** including names and emails
3. **Admin can review damage photos** through the `issueDescription` field
4. **Date filters are inclusive** - both start and end dates are included in results
5. **Multiple filters use AND logic** - repairs must match ALL specified criteria

---

## Next Steps

After successfully reviewing repair requests, Admin typically performs these actions:
- Assign repair requests to technicians
- Monitor repair progress
- Review cost estimates
- Generate reports

These features will be covered in the next service: **Technician Assignment Service**

