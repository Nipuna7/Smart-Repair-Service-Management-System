# Repair Request Service - Business Logic Update Summary

## Date: January 5, 2026

---

## Overview
Updated the `RepairRequestService` and `RepairRequestController` to implement three additional business logic requirements:

1. **Prevent multiple active repair requests for the same vehicle**
2. **Validate input (issue description, service type)**
3. **Auto-generate repair request number**

---

## Files Modified

### 1. Repair.java (Entity)
**Location**: `src/main/java/com/nipuna/demo/entity/Repair.java`

**Changes**:
- Added new field: `repairRequestNumber` (String)
- Column properties: `unique = true`, `nullable = false`

```java
@Column(name = "repair_request_number", unique = true, nullable = false)
private String repairRequestNumber;
```

---

### 2. RepairResponseDto.java
**Location**: `src/main/java/com/nipuna/demo/dto/repair/RepairResponseDto.java`

**Changes**:
- Added new field: `repairRequestNumber` (String)

```java
// Auto-generated repair request number (e.g., RR-20260105-0001)
private String repairRequestNumber;
```

---

### 3. RepairRequestService.java
**Location**: `src/main/java/com/nipuna/demo/service/repair/RepairRequestService.java`

**Changes**:
- Added import for `LocalDate` and `DateTimeFormatter`
- Added three new business logic methods
- Updated `createRepairRequest()` method to use the new business logic
- Updated `convertToResponseDto()` to include repair request number

#### New Methods Added:

##### Method 1: Check For Active Repair Requests
```java
@Transactional(readOnly = true)
public void checkForActiveRepairRequests(Long vehicleId)
```
- **Purpose**: Prevent multiple active repair requests for the same vehicle
- **Logic**: Checks if vehicle has repair with active status (REQUESTED, ASSIGNED, IN_PROGRESS, ESTIMATE_SUBMITTED, APPROVED)
- **Error**: Throws RuntimeException if active repair exists

##### Method 2: Validate Repair Request Input
```java
@Transactional(readOnly = true)
public void validateRepairRequestInput(RepairRequestDto requestDto)
```
- **Purpose**: Validate input data before creating repair request
- **Validations**:
  - Service type not null
  - Issue description not null or empty
  - Issue description length: 10-1000 characters
- **Error**: Throws RuntimeException with specific validation message

##### Method 3: Generate Repair Request Number
```java
@Transactional
public String generateRepairRequestNumber()
```
- **Purpose**: Auto-generate unique repair request number
- **Format**: `RR-YYYYMMDD-XXXX`
  - `RR`: Prefix for Repair Request
  - `YYYYMMDD`: Current date (e.g., 20260105)
  - `XXXX`: Sequential 4-digit number padded with zeros
- **Example Output**: `RR-20260105-0001`, `RR-20260105-0002`

#### Updated Method:

##### createRepairRequest()
Now includes all validations in the correct order:
1. Validate input (issue description, service type)
2. Validate vehicle ownership
3. Check for active repair requests
4. Auto-generate repair request number
5. Create and save repair entity

```java
@Transactional
public RepairResponseDto createRepairRequest(RepairRequestDto requestDto, Long customerId) {
    // Step 1: Validate input
    validateRepairRequestInput(requestDto);
    
    // Step 2: Validate vehicle ownership
    validateVehicleOwnership(requestDto.getVehicleId(), customerId);
    
    // Step 3: Prevent multiple active repairs
    checkForActiveRepairRequests(requestDto.getVehicleId());
    
    // ... create repair entity
    
    // Step 4: Auto-generate repair request number
    repair.setRepairRequestNumber(generateRepairRequestNumber());
    
    // ... save and return
}
```

---

### 4. RepairRequestController.java
**Location**: `src/main/java/com/nipuna/demo/controller/repair/RepairRequestController.java`

**No Changes Required** - Controller already has the correct endpoints:
- `GET /api/repairs/validate-ownership/{vehicleId}` - Validate ownership
- `POST /api/repairs` - Create repair request (now with enhanced validations)

---

## Business Logic Flow

### When Creating a Repair Request:

```
1. Customer sends POST request to /api/repairs
   ↓
2. JWT Authentication validates user
   ↓
3. validateRepairRequestInput() 
   → Validates service type and issue description
   ↓
4. validateVehicleOwnership()
   → Ensures vehicle belongs to customer
   ↓
5. checkForActiveRepairRequests()
   → Prevents multiple active repairs for same vehicle
   ↓
6. generateRepairRequestNumber()
   → Creates unique repair request number (RR-YYYYMMDD-XXXX)
   ↓
7. Create Repair entity
   ↓
8. Save to database
   ↓
9. Return RepairResponseDto with repair request number
```

---

## Validation Rules

### Input Validation:
- ✅ Service type: Required (BREAKDOWN, REGULAR_SERVICE, INSPECTION, etc.)
- ✅ Issue description: Required
- ✅ Issue description length: 10-1000 characters
- ✅ Priority: Optional (defaults to NORMAL)

### Business Logic Validation:
- ✅ Vehicle must exist
- ✅ Vehicle must belong to authenticated customer
- ✅ No active repair requests for the vehicle
  - Active statuses: REQUESTED, ASSIGNED, IN_PROGRESS, ESTIMATE_SUBMITTED, APPROVED

### Auto-Generated Fields:
- ✅ Repair request number: Format `RR-YYYYMMDD-XXXX`
- ✅ Status: Defaults to REQUESTED
- ✅ Payment status: Defaults to PENDING
- ✅ Priority: Defaults to NORMAL if not provided

---

## Error Messages

| Scenario | Error Message |
|----------|---------------|
| Service type missing | "Service type is required" |
| Issue description missing | "Issue description is required" |
| Issue description too short | "Issue description must be at least 10 characters long" |
| Issue description too long | "Issue description must not exceed 1000 characters" |
| Vehicle not found | "Vehicle not found with id: {id}" |
| Access denied | "Access denied. You can only access your own vehicles" |
| Active repair exists | "This vehicle already has an active repair request. Please wait until the current repair is completed." |

---

## Testing

### Test Scenarios:

1. ✅ **Valid repair request** - Should create successfully with auto-generated number
2. ✅ **Missing service type** - Should return validation error
3. ✅ **Missing issue description** - Should return validation error
4. ✅ **Issue description too short** - Should return validation error
5. ✅ **Issue description too long** - Should return validation error
6. ✅ **Vehicle not found** - Should return error
7. ✅ **Access denied (vehicle belongs to another customer)** - Should return error
8. ✅ **Multiple active repairs** - Should prevent and return error
9. ✅ **Create second repair after first completed** - Should succeed with new number

### Test Guide:
See `POSTMAN_REPAIR_REQUEST_TESTING_GUIDE_UPDATED.md` for detailed testing instructions.

---

## Database Impact

### New Column Added:
```sql
ALTER TABLE repairs 
ADD COLUMN repair_request_number VARCHAR(255) UNIQUE NOT NULL;
```

**Note**: You may need to run this SQL manually or let JPA auto-update the schema.

---

## Example API Response

### Successful Repair Request Creation:

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
  "serviceType": "BREAKDOWN",
  "issueDescription": "Engine overheating and making unusual noise. Needs immediate attention.",
  "status": "REQUESTED",
  "priority": "URGENT",
  "paymentStatus": "PENDING",
  "createdAt": "2026-01-05T11:00:00",
  "updatedAt": "2026-01-05T11:00:00"
}
```

---

## Benefits of These Updates

1. **Data Integrity**: Prevents duplicate active repairs for the same vehicle
2. **Better UX**: Clear validation messages for users
3. **Traceability**: Unique repair request numbers for tracking
4. **Professional**: Standardized numbering system (RR-YYYYMMDD-XXXX)
5. **Maintainability**: Separate methods for each business logic (Single Responsibility Principle)

---

## Summary

### Three Separate Business Logic Methods:

| # | Method Name | Purpose | Type |
|---|-------------|---------|------|
| 1 | `checkForActiveRepairRequests()` | Prevent multiple active repairs | Validation |
| 2 | `validateRepairRequestInput()` | Validate input data | Validation |
| 3 | `generateRepairRequestNumber()` | Auto-generate unique number | Generation |

### All Methods Are:
- ✅ Well-commented with single-line comments
- ✅ Follow Java naming conventions
- ✅ Use transactions appropriately
- ✅ Handle errors with meaningful messages
- ✅ Integrated into the main create flow

---

## Next Steps

1. **Run the application** to ensure no compilation errors
2. **Test with Postman** using the provided guide
3. **Verify database schema** updated with new column
4. **Test all validation scenarios**
5. **Verify repair request number generation** is working correctly

---

## Contact

If you encounter any issues with these updates, please check:
- Compilation errors using `mvn clean compile`
- Database schema is updated
- JWT token is valid and not expired
- All dependencies are properly imported

---

**Update completed successfully! 🎉**

