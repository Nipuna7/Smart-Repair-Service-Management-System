# Customer API Quick Reference

## Base URL: `http://localhost:8080`

---

## 🔐 Authentication (No Token Required)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/auth/register` | Register new customer |
| POST | `/auth/login` | Login and get JWT token |

---

## 👤 Profile Management (Token Required)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/customer/dashboard` | Get dashboard info |
| GET | `/customer/profile` | Get customer profile |
| PUT | `/customer/profile` | Update profile |
| PUT | `/customer/profile/change-password` | Change password |

---

## 🚗 Vehicle Management (Token Required)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/customer/vehicles` | Add new vehicle |
| GET | `/customer/vehicles` | Get all my vehicles |
| GET | `/customer/vehicles/{id}` | Get vehicle by ID |
| PUT | `/customer/vehicles/{id}` | Update vehicle |
| DELETE | `/customer/vehicles/{id}` | Delete vehicle |

---

## 🔧 Repair Management (Token Required)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/customer/repairs` | Create repair request |
| GET | `/customer/repairs` | Get all my repairs |
| GET | `/customer/repairs/{id}` | Get repair by ID |
| PUT | `/customer/repairs/{id}/approve-estimate?approved={true/false}` | Approve/Reject estimate |
| DELETE | `/customer/repairs/{id}?cancellationReason={reason}` | Cancel repair |

---

## 📊 History (Token Required)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/customer/repairs/history` | Get complete repair history |
| GET | `/customer/vehicles/{vehicleId}/repairs` | Get vehicle repair history |

---

## Service Types

- `BREAKDOWN`
- `ENGINE_REPAIR`
- `ELECTRICAL`
- `BODY_REPAIR`
- `TIRE_SERVICE`
- `INSPECTION`
- `REGULAR_SERVICE`
- `OTHER`

---

## Repair Status Flow

```
REQUESTED → ASSIGNED → IN_PROGRESS → ESTIMATE_SUBMITTED → APPROVED → COMPLETED → DELIVERED
                                                        ↓
                                                   CANCELLED
```

---

## Priority Levels

- `URGENT` - Critical, immediate attention
- `HIGH` - Important, high priority
- `NORMAL` - Standard priority
- `LOW` - Can wait, routine maintenance

---

## Quick Test (Postman)

1. **Register:** POST `/auth/register`
2. **Login:** POST `/auth/login` → Copy token
3. **Set Token:** Authorization → Bearer Token → Paste token
4. **Add Vehicle:** POST `/customer/vehicles`
5. **Create Repair:** POST `/customer/repairs`
6. **View History:** GET `/customer/repairs/history`

---

## Common Headers

```
Authorization: Bearer {your_jwt_token}
Content-Type: application/json
```

---

For detailed examples, see `CUSTOMER_API_TESTING_GUIDE.md`

