# 🚀 Quick Reference: CustomerController Endpoints

## Base URL
```
http://localhost:8080
```

## 1️⃣ Register Customer
```
POST /auth/register
Headers: Content-Type: application/json
Body:
{
  "username": "john_customer",
  "email": "john@example.com",
  "password": "password123",
  "fullName": "John Doe",
  "phoneNumber": "1234567890"
}
```

## 2️⃣ Login
```
POST /auth/login
Headers: Content-Type: application/json
Body:
{
  "username": "john_customer",
  "password": "password123"
}
Response: Save the "token" value!
```

## 3️⃣ Dashboard
```
GET /customer/dashboard
Headers: Authorization: Bearer YOUR_TOKEN_HERE
```

## 4️⃣ Get Profile
```
GET /customer/profile
Headers: Authorization: Bearer YOUR_TOKEN_HERE
```

## 5️⃣ Update Profile
```
PUT /customer/profile
Headers: 
  Authorization: Bearer YOUR_TOKEN_HERE
  Content-Type: application/json
Body:
{
  "email": "newemail@example.com",
  "fullName": "New Name",
  "phone": "9876543210",
  "address": "New Address"
}
```

## 6️⃣ Change Password
```
PUT /customer/profile/change-password
Headers: 
  Authorization: Bearer YOUR_TOKEN_HERE
  Content-Type: application/json
Body:
{
  "currentPassword": "password123",
  "newPassword": "newPassword456",
  "confirmPassword": "newPassword456"
}
```

---

## ⚠️ Important Notes

1. **Always include "Bearer " before token** (with space)
   - ✅ Correct: `Authorization: Bearer eyJhbGc...`
   - ❌ Wrong: `Authorization: eyJhbGc...`

2. **Token expires after 24 hours** - login again if you get 401

3. **All customer endpoints require authentication** - include Authorization header

4. **Content-Type is required for POST/PUT** requests with body

---

## 📋 Testing Order

1. Register → Get token
2. Login (optional) → Verify token works
3. Dashboard → Test authentication
4. Get Profile → See your data
5. Update Profile → Change some fields
6. Get Profile again → Verify changes
7. Change Password → Update password
8. Login with new password → Verify it works

---

## 🎯 Expected Status Codes

- **200 OK** - Success
- **400 Bad Request** - Validation error (check error message)
- **401 Unauthorized** - Missing/invalid token
- **403 Forbidden** - Wrong role (need CUSTOMER role)

---

## 🔧 Quick Troubleshooting

| Error | Solution |
|-------|----------|
| Connection refused | Start Spring Boot app |
| 401 Unauthorized | Check token, might be expired |
| 403 Forbidden | User needs CUSTOMER role |
| 400 Bad Request | Check request body format |
| 500 Internal Error | Check app logs, database connection |

