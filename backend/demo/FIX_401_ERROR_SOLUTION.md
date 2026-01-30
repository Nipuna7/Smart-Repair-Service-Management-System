# Fix for 401 Unauthorized Error - Complete Solution

## Problem Summary
You're getting `401 Unauthorized` error when accessing `/customer/profile/change-password` endpoint even with a valid JWT token.

Also notice strange `"FACTOR_PASSWORD"` role in your login response which should not exist.

## Root Causes Identified

### 1. **Spring Boot Version Incompatibility**
- Your project uses Spring Boot 4.0.1 which requires Java 21
- Your pom.xml specifies Java 17
- This causes Maven compiler errors

### 2. **JWT Algorithm Issue**  
- Your JWT token uses HS384 algorithm (visible in token header)
- Code was using default algorithm without explicit specification

### 3. **Strange "FACTOR_PASSWORD" Role**
- Your database might have corrupted or wrong role data
- Should only have: CUSTOMER, TECHNICIAN, ADMIN

## ✅ Solutions Applied

### Solution 1: Downgraded Spring Boot to 3.2.1 (Java 17 compatible)
**File:** `pom.xml`
```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.2.1</version>
    <relativePath/>
</parent>
```

### Solution 2: Updated Lombok to 1.18.34
**File:** `pom.xml`
```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <version>1.18.34</version>
    <optional>true</optional>
</dependency>
```

### Solution 3: Fixed SecurityConfig DaoAuthenticationProvider
**File:** `SecurityConfig.java`
```java
@Bean
public AuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(userDetailsService);
    authProvider.setPasswordEncoder(passwordEncoder());
    return authProvider;
}
```

### Solution 4: Enhanced JWT Service with HS512 Algorithm
**File:** `JwtService.java`
```java
return Jwts.builder()
        .subject(userPrincipal.getUsername())
        .issuedAt(now)
        .expiration(expiryDate)
        .signWith(getSigningKey(), Jwts.SIG.HS512)  // Explicit HS512
        .compact();
```

### Solution 5: Longer JWT Secret for HS512
**File:** `application.properties`
```properties
jwt.secret=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970337336763979244226452948404D6351655468576D5A7134743777217A25432A
```

### Solution 6: Enhanced JWT Filter with Better Logging
**File:** `JwtAuthenticationFilter.java`
- Added detailed debug logging
- Better error handling
- Shows authentication flow in logs

### Solution 7: Proper Maven Compiler Plugin Configuration
**File:** `pom.xml`
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <source>17</source>
        <target>17</target>
        <annotationProcessorPaths>
            <path>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
                <version>1.18.34</version>
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```

## 🔧 Steps to Fix Now

### Step 1: Clean Database Roles (IMPORTANT!)
Run this SQL to fix the "FACTOR_PASSWORD" issue:

```sql
-- Connect to your database
\c srsmsystem

-- Delete any wrong roles
DELETE FROM user_roles WHERE role_id NOT IN (SELECT id FROM roles WHERE name IN ('CUSTOMER', 'TECHNICIAN', 'ADMIN'));
DELETE FROM roles WHERE name NOT IN ('CUSTOMER', 'TECHNICIAN', 'ADMIN');

-- Make sure you have only these 3 roles
INSERT INTO roles (name) VALUES ('CUSTOMER') ON CONFLICT DO NOTHING;
INSERT INTO roles (name) VALUES ('TECHNICIAN') ON CONFLICT DO NOTHING;
INSERT INTO roles (name) VALUES ('ADMIN') ON CONFLICT DO NOTHING;

-- Check your user's roles
SELECT u.username, r.name as role  
FROM users u
JOIN user_roles ur ON u.id = ur.user_id
JOIN roles r ON ur.role_id = r.id
WHERE u.username = 'kusal';
```

### Step 2: Enable Lombok Annotation Processing in IDE

**For IntelliJ IDEA:**
1. File → Settings → Build, Execution, Deployment → Compiler → Annotation Processors
2. ✅ Enable annotation processing
3. Rebuild Project

**For Eclipse:**
1. Project → Properties → Java Compiler → Annotation Processing
2. ✅ Enable annotation processing
3. Clean and Build

### Step 3: Clean and Rebuild Project

```powershell
cd "C:\Users\EX BOOK\Desktop\Smart Repair & Service Management System\backend\demo"

# Clean everything
Remove-Item -Recurse -Force target

# Rebuild from IDE or run:
.\mvnw clean install -DskipTests
```

### Step 4: Run the Application

**Option A: Run from IDE (Recommended)**
- Right-click on `SrsmsystemApplication.java`
- Click "Run"

**Option B: Run from Maven**
```powershell
.\mvnw spring-boot:run
```

### Step 5: Test Again

**1. Login (Get Fresh Token)**
```
POST http://localhost:8080/auth/login
Content-Type: application/json

{
  "username": "kusal",
  "password": "kusal123!"
}
```

**Expected Response:**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "username": "kusal",
  "email": "kusal@gmail.com",
  "roles": ["CUSTOMER"]  // Should NOT have "FACTOR_PASSWORD"
}
```

**2. Change Password (Use New Token)**
```
PUT http://localhost:8080/customer/profile/change-password
Authorization: Bearer {token_from_login}
Content-Type: application/json

{
  "currentPassword": "kusal123!",
  "newPassword": "kusal123"
}
```

**Expected Response:**
```json
{
  "message": "Password changed successfully"
}
```

## 🐛 If Still Getting 401 Error

### Check Application Logs
Look for these log messages:
```
DEBUG --- JWT Token found: ...
DEBUG --- Valid JWT token for user: kusal
DEBUG --- User loaded with authorities: [CUSTOMER]
DEBUG --- Authentication set in SecurityContext for user: kusal
```

### If You See:
- **"JWT Token validation failed"** → Token expired or wrong secret
- **"No JWT token found"** → Authorization header missing or malformed
- **"Could not set user authentication"** → Token parsing error

### Verify Authorization Header Format
```
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWI...
               ↑      ↑
           Must have  Must have space
           "Bearer"   after Bearer
```

### Check Token is Not Expired
JWT tokens expire after 24 hours. If your token is old, login again to get a fresh one.

## 📝 Quick Reference

### All Changes Made:
1. ✅ Spring Boot 4.0.1 → 3.2.1  
2. ✅ Lombok 1.18.30 → 1.18.34
3. ✅ Fixed DaoAuthenticationProvider initialization
4. ✅ JWT algorithm explicitly set to HS512
5. ✅ JWT secret lengthened for HS512
6. ✅ Enhanced JWT filter logging
7. ✅ Proper Maven compiler plugin configuration

### Files Modified:
- `pom.xml` (Spring Boot version, Lombok version, compiler plugin)
- `SecurityConfig.java` (DaoAuthenticationProvider fix)
- `JwtService.java` (HS512 algorithm)
- `JwtAuthenticationFilter.java` (enhanced logging)
- `application.properties` (longer JWT secret)

## 🎯 Expected Outcome

After applying all fixes:
1. ✅ Project compiles successfully
2. ✅ Application starts without errors
3. ✅ Login returns token with only valid roles (CUSTOMER, TECHNICIAN, or ADMIN)
4. ✅ Token works for all protected endpoints
5. ✅ Change password works correctly
6. ✅ No more 401 errors with valid token

## ⚠️ Important Notes

1. **Always use LOGIN token** - Not the register token
2. **Token expires after 24 hours** - Login again if expired
3. **Clean database roles** - Remove "FACTOR_PASSWORD" role
4. **Enable annotation processing** - Required for Lombok to work
5. **Check logs** - Debug logs show authentication flow

## 🆘 Still Having Issues?

If you still get errors after following all steps:

1. **Share application startup logs**
2. **Share the full 401 error response**
3. **Share your JWT token** (first 50 characters only)
4. **Confirm database roles are cleaned**
5. **Confirm Lombok annotation processing is enabled in IDE**

---

**Last Updated:** January 31, 2026  
**Status:** All fixes applied, waiting for rebuild and testing

