# Fix Guide for Repair Request Number Migration Error

## Problem Description
When running the application, you encountered this error:
```
Hibernate: alter table if exists repairs add column request_number varchar(50) not null
ERROR: column "request_number" of relation "repairs" contains null values
```

This happened because:
1. There was a duplicate field in the Repair entity (`requestNumber` and `repairRequestNumber`)
2. Existing repair records in the database don't have values for the new column
3. Hibernate tried to add a NOT NULL column to a table with existing data

---

## What Was Fixed

### 1. Code Changes

#### Repair.java
- **Removed**: Duplicate `requestNumber` field
- **Changed**: Made `repairRequestNumber` nullable to allow migration
- **Status**: ✅ Fixed

#### RepairRequestService.java
- **Fixed**: Changed `setRequestNumber()` to `setRepairRequestNumber()`
- **Status**: ✅ Fixed

---

## Database Migration Steps

### Option 1: Run SQL Script (Recommended)

I've created a SQL script that will:
1. Drop duplicate columns
2. Add the correct column
3. Populate existing records with auto-generated numbers
4. Apply constraints

**Steps:**

1. **Open pgAdmin 4** or your PostgreSQL client

2. **Connect to the `srsmsystem` database**

3. **Run the SQL script**: `fix_repair_request_number.sql`

   Or copy and run this SQL directly:

```sql
-- Drop duplicate columns
ALTER TABLE repairs DROP COLUMN IF EXISTS request_number CASCADE;
ALTER TABLE repairs DROP COLUMN IF EXISTS repair_request_number CASCADE;

-- Add new column as nullable
ALTER TABLE repairs ADD COLUMN repair_request_number VARCHAR(255);

-- Populate existing records
UPDATE repairs 
SET repair_request_number = 'RR-' || 
    TO_CHAR(COALESCE(created_at, NOW()), 'YYYYMMDD') || '-' || 
    LPAD(id::TEXT, 4, '0')
WHERE repair_request_number IS NULL;

-- Verify all records have values (should return 0)
SELECT COUNT(*) FROM repairs WHERE repair_request_number IS NULL;

-- Make column NOT NULL
ALTER TABLE repairs ALTER COLUMN repair_request_number SET NOT NULL;

-- Add unique constraint
ALTER TABLE repairs ADD CONSTRAINT uk_repair_request_number UNIQUE (repair_request_number);

-- Verify the result
SELECT id, repair_request_number, status, created_at FROM repairs ORDER BY id;
```

4. **Verify**: Check that all repairs now have repair_request_number values

---

### Option 2: Delete and Recreate (If Safe)

If you don't have important repair data, you can simply drop and recreate:

```sql
-- WARNING: This deletes all repair data!
DROP TABLE IF EXISTS repairs CASCADE;
```

Then restart the application, and Hibernate will create the table with the correct schema.

---

### Option 3: Manual Update in pgAdmin

1. Open pgAdmin 4
2. Navigate to: Databases → srsmsystem → Schemas → public → Tables → repairs
3. Right-click on `repairs` table → View/Edit Data → All Rows
4. Check if there's a column called `request_number` or `repair_request_number`
5. If `request_number` exists, delete it:
   ```sql
   ALTER TABLE repairs DROP COLUMN request_number;
   ```
6. Follow the steps in Option 1

---

## After Database Fix

Once the database is fixed:

1. **Restart your Spring Boot application**
   ```
   mvn spring-boot:run
   ```

2. **Check the logs** - you should see:
   ```
   Hibernate: [no errors about repair_request_number]
   Started SrsmsystemApplication in X seconds
   ```

3. **Test the API** using Postman:
   - Create a new repair request
   - Verify the response includes `repairRequestNumber` field
   - Format should be: `RR-20260105-0001`

---

## Verification Checklist

After completing the fix, verify:

- [ ] No compilation errors
- [ ] Application starts without Hibernate errors
- [ ] Existing repairs have `repair_request_number` values
- [ ] New repairs get auto-generated `repair_request_number`
- [ ] Repair request numbers are unique
- [ ] Format is correct: `RR-YYYYMMDD-XXXX`

---

## Testing

### Test 1: Check Existing Repairs
```sql
SELECT id, repair_request_number, created_at FROM repairs ORDER BY id;
```
**Expected**: All records should have repair_request_number values like `RR-20260105-0001`

### Test 2: Create New Repair
Use Postman to create a new repair request (see POSTMAN_REPAIR_REQUEST_TESTING_GUIDE_UPDATED.md)

**Expected Response**:
```json
{
  "id": 5,
  "repairRequestNumber": "RR-20260105-0005",
  "vehicleNumber": "ABC-1234",
  ...
}
```

---

## Common Issues

### Issue 1: "Column already exists"
**Solution**: Run `ALTER TABLE repairs DROP COLUMN repair_request_number CASCADE;` first

### Issue 2: "Cannot add NOT NULL column"
**Solution**: Make sure all existing records have values before making it NOT NULL

### Issue 3: "Duplicate key value violates unique constraint"
**Solution**: Check for duplicate repair_request_number values:
```sql
SELECT repair_request_number, COUNT(*) 
FROM repairs 
GROUP BY repair_request_number 
HAVING COUNT(*) > 1;
```

---

## Summary

**What was the problem?**
- Duplicate field in entity class
- Hibernate trying to add NOT NULL column to table with existing data

**What was fixed?**
- ✅ Removed duplicate `requestNumber` field from Repair.java
- ✅ Changed `repairRequestNumber` to nullable
- ✅ Fixed method call in RepairRequestService.java
- ✅ Created SQL migration script

**What you need to do?**
1. Run the SQL script to fix the database
2. Restart the application
3. Verify it works

---

## Need Help?

If you still encounter errors:

1. **Check the database connection**:
   ```
   spring.datasource.url=jdbc:postgresql://localhost:5432/srsmsystem
   spring.datasource.username=postgres
   spring.datasource.password=0000
   ```

2. **Check PostgreSQL is running**:
   - Open Services (Windows)
   - Look for "postgresql" service
   - Status should be "Running"

3. **Check the table exists**:
   ```sql
   SELECT * FROM information_schema.tables WHERE table_name = 'repairs';
   ```

4. **View Hibernate DDL logs**:
   The application logs show exactly what Hibernate is trying to do

---

**Good luck! The issue should be resolved after running the SQL migration script.** 🚀

