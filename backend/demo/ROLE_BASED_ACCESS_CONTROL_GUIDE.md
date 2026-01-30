# Role-Based Access Control (RBAC) Implementation Guide

## Overview
This Smart Repair & Service Management System implements a **3-tier Role-Based Access Control** system using **Spring Security** and **JWT tokens**.

---

## 🎭 Three Roles Defined

### 1. **CUSTOMER** 
- Default role for all new registrations
- Can manage their own vehicles
- Can create and track repair requests
- Can approve/reject cost estimates
- Limited to viewing only their own data

### 2. **TECHNICIAN**
- Assigned by admin
- Can view assigned repairs
- Can add diagnosis and repair notes
- Can submit cost estimates
- Can update repair status
- Cannot access admin or customer-specific data

### 3. **ADMIN**
- Full system access
- Can view all repairs and users
- Can assign/reassign technicians
- Can manage technician profiles
- Can override repair statuses
- Can monitor system statistics

---

## 📂 Implementation Components

### 1. **Role Entity** (`Role.java`)
```java
@Entity
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private RoleName name;
    
    public enum RoleName {
        CUSTOMER,    // Default role for new users
        TECHNICIAN,  // Assigned by admin
        ADMIN        // Full system access
    }
}
```

### 2. **User Entity** (`User.java`)
```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String username;
    private String password;
    private String email;
    private String fullName;
    
    // Many-to-Many relationship with roles
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();
}
```

### 3. **Security Configuration** (`SecurityConfig.java`)
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**").permitAll()           // Public endpoints
                .requestMatchers("/customer/**").hasAuthority("CUSTOMER")   // Customer only
                .requestMatchers("/technician/**").hasAuthority("TECHNICIAN") // Technician only
                .requestMatchers("/admin/**").hasAuthority("ADMIN")        // Admin only
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
```

### 4. **JWT Authentication Filter** (`JwtAuthenticationFilter.java`)
- Intercepts all requests
- Validates JWT token
- Extracts user roles from token
- Sets authentication in SecurityContext
- Enforces role-based access

---

## 🔐 Access Control Matrix

| Endpoint Pattern | Required Role | Access Level |
|-----------------|---------------|--------------|
| `/auth/**` | None (Public) | Registration, Login |
| `/customer/**` | CUSTOMER | Vehicle & Repair Management |
| `/technician/**` | TECHNICIAN | Assigned Repairs Management |
| `/admin/**` | ADMIN | Full System Administration |

---

## 🚀 How It Works

### Step 1: User Registration
```http
POST /auth/register
{
  "username": "john_doe",
  "password": "password123",
  "email": "john@example.com",
  "fullName": "John Doe"
}
```
- Automatically assigned **CUSTOMER** role
- Password encrypted with BCrypt
- JWT token generated and returned

### Step 2: User Login
```http
POST /auth/login
{
  "username": "john_doe",
  "password": "password123"
}
```
**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "username": "john_doe",
  "email": "john@example.com",
  "roles": ["CUSTOMER"]
}
```

### Step 3: Access Protected Endpoints
Include the JWT token in the `Authorization` header:
```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### Step 4: Role Validation
- JWT filter extracts token
- Token validated and decoded
- User roles extracted
- Spring Security checks if user has required authority
- Request allowed/denied based on role

---

## 🔒 Role Verification in Services

### Example: Admin Service
```java
@Service
public class AdminService {
    
    // Verify user is a technician before operations
    public RepairResponseDto assignTechnicianToRepair(Long repairId, Long technicianId) {
        User technician = userRepository.findById(technicianId)
            .orElseThrow(() -> new RuntimeException("Technician not found"));
        
        // Verify user has TECHNICIAN role
        boolean isTechnician = technician.getRoles().stream()
            .anyMatch(role -> role.getName() == Role.RoleName.TECHNICIAN);
        
        if (!isTechnician) {
            throw new RuntimeException("User is not a technician");
        }
        
        // Proceed with assignment
        repair.setTechnician(technician);
        return repairRepository.save(repair);
    }
}
```

---

## 🎯 Role-Based Business Logic

### CUSTOMER Role
✅ **Allowed:**
- View own dashboard
- Add/update/delete own vehicles
- Create repair requests
- View own repair history
- Approve/reject cost estimates
- Cancel own repairs (if not completed)

❌ **Not Allowed:**
- View other customers' data
- Access admin panel
- Assign technicians
- Override repair statuses

### TECHNICIAN Role
✅ **Allowed:**
- View assigned repairs only
- Add diagnosis details
- Submit cost estimates
- Update repair status (within workflow)
- Add repair notes

❌ **Not Allowed:**
- View unassigned repairs
- Access customer data
- Self-assign repairs
- Access admin functions

### ADMIN Role
✅ **Allowed:**
- View all repairs and users
- Assign/reassign technicians
- Add/update/deactivate technicians
- Override repair statuses
- Force complete repairs
- Cancel repairs on behalf of customers
- View system statistics
- Monitor SLA breaches

---

## 🔑 Key Security Features

### 1. **JWT Token-Based Authentication**
- Stateless authentication
- Token contains user ID, username, and roles
- Token expires after configured time
- Token validated on every request

### 2. **BCrypt Password Encryption**
- All passwords hashed with BCrypt
- Salt automatically generated
- One-way encryption (cannot be decrypted)

### 3. **Role-Based Authorization**
- Spring Security annotations
- Controller-level protection
- Service-level validation
- Database-level role verification

### 4. **Exception Handling**
- **401 Unauthorized**: Invalid or expired token
- **403 Forbidden**: Valid token but insufficient permissions
- Custom exception messages

---

## 📊 Database Schema

### Tables Created:
1. **`users`** - User accounts
2. **`roles`** - Role definitions (CUSTOMER, TECHNICIAN, ADMIN)
3. **`user_roles`** - Many-to-many mapping between users and roles
4. **`repairs`** - Repair requests
5. **`vehicles`** - Customer vehicles
6. **`payments`** - Payment records
7. **`feedback`** - Customer feedback

---

## 🧪 Testing Role-Based Access

### Test 1: Customer Access
```bash
# Login as customer
POST /auth/login
{ "username": "customer1", "password": "password" }

# Access customer endpoint (✅ SUCCESS)
GET /customer/dashboard
Authorization: Bearer <customer_token>

# Try to access admin endpoint (❌ FORBIDDEN 403)
GET /admin/repairs
Authorization: Bearer <customer_token>
```

### Test 2: Technician Access
```bash
# Login as technician
POST /auth/login
{ "username": "tech1", "password": "password" }

# Access technician endpoint (✅ SUCCESS)
GET /technician/assigned-repairs
Authorization: Bearer <technician_token>

# Try to access admin endpoint (❌ FORBIDDEN 403)
GET /admin/repairs
Authorization: Bearer <technician_token>
```

### Test 3: Admin Access
```bash
# Login as admin
POST /auth/login
{ "username": "admin", "password": "admin123" }

# Access admin endpoint (✅ SUCCESS)
GET /admin/repairs
Authorization: Bearer <admin_token>

# Access any endpoint (✅ SUCCESS - if needed)
GET /customer/dashboard
Authorization: Bearer <admin_token>
```

---

## 🛠️ How to Add a New Role

### Step 1: Add to Role Enum
```java
public enum RoleName {
    CUSTOMER,
    TECHNICIAN,
    ADMIN,
    MANAGER  // New role
}
```

### Step 2: Update Security Config
```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/auth/**").permitAll()
    .requestMatchers("/customer/**").hasAuthority("CUSTOMER")
    .requestMatchers("/technician/**").hasAuthority("TECHNICIAN")
    .requestMatchers("/admin/**").hasAuthority("ADMIN")
    .requestMatchers("/manager/**").hasAuthority("MANAGER")  // New
    .anyRequest().authenticated()
)
```

### Step 3: Create Controller
```java
@RestController
@RequestMapping("/manager")
@RequiredArgsConstructor
public class ManagerController {
    // Manager-specific endpoints
}
```

### Step 4: Insert Role in Database
```sql
INSERT INTO roles (name) VALUES ('MANAGER');
```

---

## 📝 Best Practices

1. **Always validate roles at service layer** - Don't rely only on controller security
2. **Use specific role checks** - Verify exact role when needed
3. **Log unauthorized access attempts** - Track security incidents
4. **Keep JWT secret secure** - Store in environment variables
5. **Set appropriate token expiration** - Balance security and UX
6. **Implement refresh tokens** - For long-lived sessions
7. **Use HTTPS in production** - Protect tokens in transit

---

## 🚨 Common Issues and Solutions

### Issue 1: 403 Forbidden with valid token
**Cause:** User doesn't have required role
**Solution:** Check user's roles in database, verify token contains correct authorities

### Issue 2: 401 Unauthorized
**Cause:** Token expired or invalid
**Solution:** Login again to get new token

### Issue 3: Role not working after database update
**Cause:** Token still contains old roles
**Solution:** User must login again to get new token with updated roles

---

## 📖 Related Files

- **Security Config:** `src/main/java/com/nipuna/demo/config/SecurityConfig.java`
- **JWT Filter:** `src/main/java/com/nipuna/demo/security/JwtAuthenticationFilter.java`
- **JWT Service:** `src/main/java/com/nipuna/demo/service/jwt/JwtService.java`
- **User Entity:** `src/main/java/com/nipuna/demo/entity/User.java`
- **Role Entity:** `src/main/java/com/nipuna/demo/entity/Role.java`
- **Auth Service:** `src/main/java/com/nipuna/demo/service/auth/AuthService.java`

---

## ✅ Summary

The system implements a **robust 3-tier RBAC** using:
- ✅ Spring Security
- ✅ JWT tokens with role claims
- ✅ Database-backed role management
- ✅ Controller-level and service-level protection
- ✅ BCrypt password encryption
- ✅ Stateless authentication
- ✅ Custom exception handling

**Result:** Secure, scalable, and maintainable access control system.

