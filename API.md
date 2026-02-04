# API Testing Guide - Postman (Updated for Hashed Tokens)

Complete guide for testing the E-commerce Backend REST API using Postman with secure HTTP-only cookie authentication.

## Table of Contents
1. [Setup](#setup)
2. [Environment Variables](#environment-variables)
3. [Cookie Configuration](#cookie-configuration)
4. [Authentication Endpoints](#authentication-endpoints)
5. [User Endpoints](#user-endpoints)
6. [Admin Endpoints](#admin-endpoints)
7. [Test Scenarios](#test-scenarios)

---

## Setup

Start your application: `./mvnw spring-boot:run`

### Base URL
```
http://localhost:8080/api/v1
```

### Import into Postman

1. Create a new Collection: **"E-commerce Backend API"**
2. Set Collection variables:
   - `base_url`: `http://localhost:8080/api/v1`
   - `admin_token`: (will be set after admin login)
   - `user_token`: (will be set after user login)

---

## Environment Variables

Create these variables in Postman:

| Variable | Initial Value | Description |
|----------|---------------|-------------|
| `base_url` | `http://localhost:8080/api/v1` | API base URL |
| `admin_token` | (empty) | Admin JWT access token |
| `user_token` | (empty) | User JWT access token |
| `test_user_id` | (empty) | Test user ID |

---

## Cookie Configuration

### Important: Enable Cookie Management in Postman

**⚠️ CRITICAL:** Postman must be configured to handle cookies automatically.

1. **Settings → General**
   - Enable: ✅ "Automatically follow redirects"
   - Enable: ✅ "Send cookies from cookie jar"

2. **Cookie Jar Access**
   - View cookies: Click "Cookies" button below "Send"
   - Cookies are stored per domain automatically
   - Refresh tokens are stored as HTTP-only cookies

### How Cookies Work in This API

| Cookie Name | Type | Lifetime | Security |
|-------------|------|----------|----------|
| `refresh_token` | HTTP-only | 7 days | Secure, SameSite=Strict |

**Key Points:**
- ✅ Refresh tokens are stored in HTTP-only cookies (not in response body)
- ✅ Access tokens are returned in response body (store in memory/localStorage in frontend)
- ✅ Cookies are automatically sent with requests to the same domain
- ✅ Token rotation occurs on each refresh (old token invalidated)

---

## Authentication Endpoints

### 1. Admin Login

**Request:**
```
POST {{base_url}}/auth/login
Content-Type: application/json
```

**Body:**
```json
{
  "usernameOrEmail": "admin",
  "password": "Admin123!@#"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "access_token": "eyJhbGciOiJIUzI1NiJ9...",
    "token_type": "Bearer",
    "expires_in": 900000,
    "user": {
      "id": 1,
      "username": "admin",
      "email": "admin@ecommerce.com",
      "firstName": "Admin",
      "lastName": "User",
      "role": "ADMIN",
      "enabled": true
    }
  }
}
```

**Set-Cookie Header (Automatic):**
```
Set-Cookie: refresh_token=<hashed-token>; Path=/; Max-Age=604800; HttpOnly; Secure; SameSite=Strict
```

**Postman Test Script (Auto-save token):**
```javascript
if (pm.response.code === 200) {
    const response = pm.response.json();
    pm.collectionVariables.set("admin_token", response.data.access_token);
    console.log("Admin token saved:", response.data.access_token);
}
```

---

### 2. User Registration

**Request:**
```
POST {{base_url}}/auth/register
Content-Type: application/json
```

**Body:**
```json
{
  "username": "johndoe",
  "email": "john.doe@example.com",
  "password": "SecurePass123!",
  "confirmPassword": "SecurePass123!",
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "+1234567890"
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "access_token": "eyJhbGciOiJIUzI1NiJ9...",
    "token_type": "Bearer",
    "expires_in": 900000,
    "user": {
      "id": 2,
      "username": "johndoe",
      "email": "john.doe@example.com",
      "firstName": "John",
      "lastName": "Doe",
      "role": "USER",
      "enabled": true
    }
  }
}
```

**Note:** Refresh token is automatically set in HTTP-only cookie (not in response body).

**Postman Test Script:**
```javascript
if (pm.response.code === 201) {
    const response = pm.response.json();
    pm.collectionVariables.set("user_token", response.data.access_token);
    pm.collectionVariables.set("test_user_id", response.data.user.id);
    console.log("User registered. ID:", response.data.user.id);
}
```

---

### 3. User Login

**Request:**
```
POST {{base_url}}/auth/login
Content-Type: application/json
```

**Body:**
```json
{
  "usernameOrEmail": "johndoe",
  "password": "SecurePass123!"
}
```

**Response:** Same as Admin Login

**Postman Test Script:**
```javascript
if (pm.response.code === 200) {
    const response = pm.response.json();
    pm.collectionVariables.set("user_token", response.data.access_token);
    console.log("User logged in successfully");
}
```

---

### 4. Refresh Access Token

**⭐ NEW ENDPOINT** - Rotate refresh token and get new access token

**Request:**
```
POST {{base_url}}/auth/refresh
```

**Body:** None (cookie sent automatically)

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Token refreshed successfully",
  "data": {
    "access_token": "eyJhbGciOiJIUzI1NiJ9...",
    "token_type": "Bearer",
    "expires_in": 900000
  }
}
```

**Behavior:**
- Old refresh token in cookie is invalidated (replaced_by_token_hash set)
- New refresh token is set in cookie
- New access token returned in response
- This implements **token rotation** for security

**Postman Test Script:**
```javascript
if (pm.response.code === 200) {
    const response = pm.response.json();
    pm.collectionVariables.set("user_token", response.data.access_token);
    console.log("Token refreshed successfully");
}
```

**Use Case:**
- Access token expires after 15 minutes
- Frontend calls this endpoint to get new access token without re-login
- User stays logged in for up to 7 days (refresh token lifetime)

---

### 5. Logout (Single Device)

**⭐ NEW ENDPOINT** - Logout from current device only

**Request:**
```
POST {{base_url}}/auth/logout
Authorization: Bearer {{user_token}}
```

**Body:** None

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Logged out successfully",
  "data": null
}
```

**Behavior:**
- Current refresh token is revoked in database
- Cookie is cleared (Max-Age=0)
- User must login again on this device
- Other devices remain logged in

**Postman Test Script:**
```javascript
if (pm.response.code === 200) {
    pm.collectionVariables.unset("user_token");
    console.log("Logged out. Token cleared.");
}
```

---

### 6. Logout All Devices

**⭐ NEW ENDPOINT** - Logout from all devices

**Request:**
```
POST {{base_url}}/auth/logout-all
Authorization: Bearer {{user_token}}
```

**Body:** None

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Logged out from all devices successfully",
  "data": null
}
```

**Behavior:**
- All refresh tokens for this user are revoked
- User is logged out from all devices (web, mobile, desktop)
- Must login again on every device

**Use Case:**
- Security concern (device lost/stolen)
- Password changed
- Suspicious activity detected

**Postman Test Script:**
```javascript
if (pm.response.code === 200) {
    pm.collectionVariables.unset("user_token");
    console.log("Logged out from all devices");
}
```

---

## User Endpoints

**Authorization Required:** All user endpoints require `Bearer {{user_token}}` in Authorization header.

### 7. Get Current User

**Request:**
```
GET {{base_url}}/users/me
Authorization: Bearer {{user_token}}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Current user retrieved successfully",
  "data": {
    "id": 2,
    "username": "johndoe",
    "email": "john.doe@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "phoneNumber": "+1234567890",
    "role": "USER",
    "enabled": true,
    "createdAt": "2026-01-27 10:30:00",
    "updatedAt": "2026-01-27 10:30:00"
  }
}
```
---

### 8. Get User by ID

**Request:**
```
GET {{base_url}}/users/{{test_user_id}}
Authorization: Bearer {{user_token}}
```

**Response:** Same structure as Get Current User

---

### 9. Get User by Username

**Request:**
```
GET {{base_url}}/users/username/johndoe
Authorization: Bearer {{user_token}}
```

**Response:** Same structure as Get Current User

---

### 10. Update User Profile

**Request:**
```
PUT {{base_url}}/users/{{test_user_id}}
Authorization: Bearer {{user_token}}
Content-Type: application/json
```

**Body:**
```json
{
  "email": "john.updated@example.com",
  "firstName": "Jonathan",
  "lastName": "Doe",
  "phoneNumber": "+1987654321"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "User updated successfully",
  "data": {
    "id": 2,
    "username": "johndoe",
    "email": "john.updated@example.com",
    "firstName": "Jonathan",
    "lastName": "Doe",
    "phoneNumber": "+1987654321",
    "role": "USER",
    "enabled": true
  }
}
```
---

### 11. Change Password

**Request:**
```
PATCH {{base_url}}/users/change-password
Authorization: Bearer {{user_token}}
Content-Type: application/json
```

**Body:**
```json
{
  "currentPassword": "SecurePass123!",
  "newPassword": "NewSecurePass456!",
  "confirmPassword": "NewSecurePass456!"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Password changed successfully",
  "data": null
}
```

**⚠️ Security Note:** 
After password change, it's recommended to call `/auth/logout-all` to invalidate all existing sessions.

---

### 12. Search Users

**Request:**
```
GET {{base_url}}/users/search?query=john&page=0&size=10
Authorization: Bearer {{user_token}}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Search completed successfully",
  "data": {
    "content": [
      {
        "id": 2,
        "username": "johndoe",
        "email": "john.doe@example.com",
        "firstName": "John",
        "lastName": "Doe",
        "role": "USER",
        "enabled": true
      }
    ],
    "pageNumber": 0,
    "pageSize": 10,
    "totalElements": 1,
    "totalPages": 1,
    "last": true,
    "first": true
  }
}
```

---

### 13. Delete User

**Request:**
```
DELETE {{base_url}}/users/{{test_user_id}}
Authorization: Bearer {{user_token}}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "User deleted successfully",
  "data": null
}
```

---

## Admin Endpoints

**Authorization Required:** All admin endpoints require `Bearer {{admin_token}}` in Authorization header.

### 14. Get All Users (Admin Only)

**Request:**
```
GET {{base_url}}/users?page=0&size=10&sortBy=createdAt&direction=desc
Authorization: Bearer {{admin_token}}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Users retrieved successfully",
  "data": {
    "content": [
      {
        "id": 3,
        "username": "alice",
        "email": "alice@example.com",
        "firstName": "Alice",
        "lastName": "Smith",
        "role": "USER",
        "enabled": true,
        "createdAt": "2026-01-27 11:00:00"
      },
      {
        "id": 2,
        "username": "johndoe",
        "email": "john.doe@example.com",
        "firstName": "John",
        "lastName": "Doe",
        "role": "USER",
        "enabled": true,
        "createdAt": "2026-01-27 10:30:00"
      }
    ],
    "pageNumber": 0,
    "pageSize": 10,
    "totalElements": 2,
    "totalPages": 1,
    "last": true,
    "first": true
  }
}
```
---

### 15. Enable User (Admin Only)

**Request:**
```
PATCH {{base_url}}/admin/users/{{test_user_id}}/enable
Authorization: Bearer {{admin_token}}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "User enabled successfully",
  "data": {
    "id": 2,
    "username": "johndoe",
    "email": "john.doe@example.com",
    "enabled": true
  }
}
```
---

### 16. Disable User (Admin Only)

**Request:**
```
PATCH {{base_url}}/admin/users/{{test_user_id}}/disable
Authorization: Bearer {{admin_token}}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "User disabled successfully",
  "data": {
    "id": 2,
    "username": "johndoe",
    "email": "john.doe@example.com",
    "enabled": false
  }
}
```

---

### 17. Change User Role (Admin Only)

**Request:**
```
PATCH {{base_url}}/admin/users/{{test_user_id}}/role?role=MODERATOR
Authorization: Bearer {{admin_token}}
```

**Query Parameters:**
- `role`: `USER` | `MODERATOR` | `ADMIN`

**Response (200 OK):**
```json
{
  "success": true,
  "message": "User role updated successfully",
  "data": {
    "id": 2,
    "username": "johndoe",
    "email": "john.doe@example.com",
    "role": "MODERATOR"
  }
}
```


---

### 18. Get Users by Role (Admin Only)

**Request:**
```
GET {{base_url}}/admin/users/role/ADMIN?page=0&size=10
Authorization: Bearer {{admin_token}}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Users retrieved successfully",
  "data": {
    "content": [
      {
        "id": 1,
        "username": "admin",
        "email": "admin@ecommerce.com",
        "role": "ADMIN",
        "enabled": true
      }
    ],
    "pageNumber": 0,
    "pageSize": 10,
    "totalElements": 1,
    "totalPages": 1
  }
}
```

---

## Test Scenarios

### Scenario 1: Complete User Registration & Authentication Flow

```
1. Register new user
   POST /auth/register
   → Save access_token to user_token variable
   → Refresh token automatically stored in cookie

2. Get current user
   GET /users/me
   → Verify user details

3. Update profile
   PUT /users/{id}
   → Verify updates

4. Wait 16 minutes (access token expires after 15 min)

5. Try to access protected endpoint
   GET /users/me
   → Expect 401 Unauthorized (token expired)

6. Refresh access token
   POST /auth/refresh
   → New access token received
   → Old refresh token rotated (invalidated)
   → New refresh token set in cookie

7. Access protected endpoint with new token
   GET /users/me
   → Expect 200 Success

8. Logout from current device
   POST /auth/logout
   → Current refresh token revoked
   → Cookie cleared

9. Try to refresh token after logout
   POST /auth/refresh
   → Expect 401 Unauthorized (token revoked)
```

---

### Scenario 2: Multi-Device Login & Logout

```
1. Login on Device 1 (Postman)
   POST /auth/login
   → Save token as device1_token
   → Cookie: refresh_token_device1

2. Login on Device 2 (Browser/Another Postman tab)
   POST /auth/login (same user)
   → Save token as device2_token
   → Cookie: refresh_token_device2

3. Verify both devices can access API
   GET /users/me (with device1_token)
   → Expect 200
   GET /users/me (with device2_token)
   → Expect 200

4. Logout from Device 1 only
   POST /auth/logout (with device1_token)
   → Device 1 refresh token revoked

5. Verify Device 1 cannot refresh
   POST /auth/refresh (Device 1 cookie)
   → Expect 401 Unauthorized

6. Verify Device 2 still works
   POST /auth/refresh (Device 2 cookie)
   → Expect 200 Success

7. Logout from all devices (Device 2)
   POST /auth/logout-all (with device2_token)
   → All refresh tokens revoked

8. Verify Device 2 cannot refresh
   POST /auth/refresh (Device 2 cookie)
   → Expect 401 Unauthorized
```

---

### Scenario 3: Admin User Management

```
1. Login as admin
   POST /auth/login (admin credentials)
   → Save admin_token

2. Get all users
   GET /users
   → View all users

3. Disable a user
   PATCH /admin/users/{id}/disable
   → Verify user disabled

4. Try user login (should fail)
   POST /auth/login (disabled user)
   → Expect 401 or 403

5. Enable user
   PATCH /admin/users/{id}/enable
   → Verify user enabled

6. User can login again
   POST /auth/login (enabled user)
   → Expect 200

7. Change user role to MODERATOR
   PATCH /admin/users/{id}/role?role=MODERATOR
   → Verify role changed
```

---

### Scenario 4: Token Security & Rotation

```
1. Login and capture refresh token
   POST /auth/login
   → Copy refresh_token from Cookie Jar

2. Use refresh token to get new access token
   POST /auth/refresh
   → New access token received
   → OLD refresh token is now invalid

3. Try to reuse old refresh token
   POST /auth/refresh (with old cookie value)
   → Expect 401 Unauthorized
   → Error: "Invalid or expired refresh token"

4. Verify token is hashed in database
   → Check database: SELECT token_hash FROM refresh_tokens
   → Should see SHA-256 hash (64 hex chars), NOT the raw token
   → Even with database access, cannot use the hash to authenticate

5. Test concurrent logins (max 5 devices)
   → Login from 5 different devices
   → Try 6th login
   → Should succeed (oldest token automatically revoked)
```

---

### Scenario 5: Authorization Testing

```
1. Try admin endpoint without token
   GET /users
   → Expect 401 Unauthorized

2. Try admin endpoint with user token
   PATCH /admin/users/{id}/enable
   → Expect 403 Forbidden

3. Try to update another user's profile (as user)
   PUT /users/{other_user_id}
   → Expect 403 or 400

4. Access admin endpoint with admin token
   PATCH /admin/users/{id}/enable
   → Expect 200 Success
```

---

### Scenario 6: Validation Testing

**Invalid Email:**
```json
{
  "username": "testuser",
  "email": "invalid-email",
  "password": "Test123!@#",
  "confirmPassword": "Test123!@#"
}
```
→ Expect 400 with validation errors

**Weak Password:**
```json
{
  "username": "testuser",
  "email": "test@example.com",
  "password": "weak",
  "confirmPassword": "weak"
}
```
→ Expect 400 with password validation error

**Password Mismatch:**
```json
{
  "username": "testuser",
  "email": "test@example.com",
  "password": "Test123!@#",
  "confirmPassword": "Different123!@#"
}
```
→ Expect 400 with mismatch error

---

## Error Response Examples

### 400 Bad Request - Validation Error
```json
{
  "success": false,
  "message": "Validation failed",
  "data": {
    "email": "Email should be valid",
    "password": "Password must be between 8 and 100 characters"
  },
  "timestamp": "2026-02-04 10:30:00",
  "path": "/api/v1/auth/register"
}
```

### 401 Unauthorized - Invalid Credentials
```json
{
  "success": false,
  "message": "Invalid username or password",
  "timestamp": "2026-02-04 10:30:00",
  "path": "/api/v1/auth/login"
}
```

### 401 Unauthorized - Token Expired
```json
{
  "success": false,
  "message": "JWT token has expired",
  "timestamp": "2026-02-04 10:30:00",
  "path": "/api/v1/users/me"
}
```

### 401 Unauthorized - Invalid Refresh Token
```json
{
  "success": false,
  "message": "Invalid or expired refresh token",
  "timestamp": "2026-02-04 10:30:00",
  "path": "/api/v1/auth/refresh"
}
```

### 403 Forbidden - Insufficient Permissions
```json
{
  "success": false,
  "message": "Access Denied",
  "timestamp": "2026-02-04 10:30:00",
  "path": "/api/v1/admin/users/2/enable"
}
```

### 404 Not Found
```json
{
  "success": false,
  "message": "User not found with id: 999",
  "timestamp": "2026-02-04 10:30:00",
  "path": "/api/v1/users/999"
}
```

### 409 Conflict - Duplicate Resource
```json
{
  "success": false,
  "message": "Username already exists",
  "timestamp": "2026-02-04 10:30:00",
  "path": "/api/v1/auth/register"
}
```

### 429 Too Many Requests - Rate Limit
```json
{
  "success": false,
  "message": "Rate limit exceeded. Try again in 45 seconds.",
  "timestamp": "2026-02-04 10:30:00",
  "path": "/api/v1/users/me"
}
```

---

## Quick Test Commands (cURL)

### Register User
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "Test123!@#",
    "confirmPassword": "Test123!@#",
    "firstName": "Test",
    "lastName": "User"
  }' \
  -c cookies.txt
```

### Admin Login
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "admin",
    "password": "Admin123!@#"
  }' \
  -c cookies.txt
```

### Get Current User
```bash
curl -X GET http://localhost:8080/api/v1/users/me \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -b cookies.txt
```

### Refresh Token
```bash
curl -X POST http://localhost:8080/api/v1/auth/refresh \
  -b cookies.txt \
  -c cookies.txt
```

### Logout
```bash
curl -X POST http://localhost:8080/api/v1/auth/logout \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -b cookies.txt
```

### Logout All Devices
```bash
curl -X POST http://localhost:8080/api/v1/auth/logout-all \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -b cookies.txt
```

### Enable User (Admin)
```bash
curl -X PATCH http://localhost:8080/api/v1/admin/users/2/enable \
  -H "Authorization: Bearer ADMIN_ACCESS_TOKEN" \
  -b cookies.txt
```

**Note:** The `-c cookies.txt` flag saves cookies, and `-b cookies.txt` sends cookies with the request.

---

## Security Features Summary

### 🔐 Token Security Architecture

| Feature | Implementation | Benefit |
|---------|----------------|---------|
| **HTTP-Only Cookies** | Refresh tokens stored in HTTP-only cookies | Prevents XSS attacks (JavaScript cannot access) |
| **Token Hashing** | SHA-256 hash stored in database | Database breach doesn't expose working tokens |
| **Token Rotation** | New refresh token on each use | Detects token theft (old token becomes invalid) |
| **Secure Flag** | Cookies only sent over HTTPS | Prevents man-in-the-middle attacks |
| **SameSite=Strict** | Cookie only sent to same origin | Prevents CSRF attacks |
| **Short Access Token** | 15 minutes lifetime | Limits damage from token theft |
| **Long Refresh Token** | 7 days lifetime | User convenience (don't re-login daily) |
| **Device Limits** | Max 5 concurrent sessions | Prevents unlimited token proliferation |
| **Revocation Support** | Database tracks revoked tokens | Instant logout capability |
| **Auto Cleanup** | Daily scheduled task (2 AM) | Removes expired tokens from database |

### 🔍 Database Token Storage

**What's stored in database:**
```sql
CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    token_hash VARCHAR(64) UNIQUE NOT NULL,  -- SHA-256 hash of token
    user_id BIGINT REFERENCES users(id),
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    revoked BOOLEAN DEFAULT FALSE,
    revoked_at TIMESTAMP,
    replaced_by_token_hash VARCHAR(64)
);
```

**Example row:**
```
token_hash: "a3f5b8c9d2e1f4a7b6c8d9e0f1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a0"
user_id: 123
expires_at: 2026-02-11 10:30:00
revoked: false
```

**Key point:** The actual refresh token is NEVER stored. Only the SHA-256 hash is stored. Even if the database is compromised, the attacker cannot use the hash to authenticate.

---

## Default Test Accounts

| Username | Password | Email | Role |
|----------|----------|-------|------|
| `admin` | `Admin123!@#` | admin@ecommerce.com | ADMIN |

Create additional test users via registration endpoint.

---

## Tips for Testing in Postman

### 1. Auto-Save Tokens in Tests Tab

**For Login/Register endpoints, add this Test script:**
```javascript
if (pm.response.code === 200 || pm.response.code === 201) {
    const response = pm.response.json();
    pm.collectionVariables.set("user_token", response.data.access_token);
    console.log("Token saved successfully");
}
```

### 2. Monitor Cookie Jar

- Click **"Cookies"** button below "Send" button
- View all cookies for `localhost`
- You should see `refresh_token` cookie after login
- Cookie is automatically sent with subsequent requests

### 3. Test Token Expiration

**Option A: Wait 15 minutes**
- Login → Wait 16 minutes → Try to access protected endpoint → Should fail with 401

**Option B: Manually expire token** (for faster testing)
- Change `jwt.expiration-ms` in `application.properties` to `60000` (1 minute)
- Restart application
- Login → Wait 2 minutes → Test

### 4. Test Token Rotation

```javascript
// In Tests tab for /auth/refresh
const oldCookie = pm.cookies.get("refresh_token");
console.log("Old cookie:", oldCookie);

// After response
const newCookie = pm.cookies.get("refresh_token");
console.log("New cookie:", newCookie);

// They should be different
pm.test("Token rotated", function() {
    pm.expect(oldCookie).to.not.equal(newCookie);
});
```

### 5. Create Separate Postman Tabs for Multi-Device Testing

- Right-click request → "Duplicate Request"
- Use separate tabs to simulate different devices
- Each tab maintains its own cookie jar

### 6. Clear Cookies Between Tests

```javascript
// In Pre-request Script
pm.cookies.clear();
```

Or manually: Cookies button → Delete specific cookie

### 7. Debug Cookie Issues

If cookies aren't being set:

**Check Response Headers:**
```javascript
// In Tests tab
const setCookieHeader = pm.response.headers.get("Set-Cookie");
console.log("Set-Cookie header:", setCookieHeader);
```

**Check Cookie Jar:**
```javascript
// In Tests tab
const cookie = pm.cookies.get("refresh_token");
console.log("Refresh token cookie:", cookie);
```

---

## Troubleshooting

### "401 Unauthorized" Error

**Possible causes:**

1. **Access token expired** (15 minutes)
   - Solution: Call `/auth/refresh` to get new access token
   
2. **Token missing from Authorization header**
   - Solution: Verify header format: `Authorization: Bearer <token>`
   
3. **User account disabled**
   - Solution: Admin must enable the account
   
4. **Refresh token revoked** (after logout)
   - Solution: Login again

### "403 Forbidden" Error

- Check if user has required role
- Admin endpoints require ADMIN role
- User can only modify their own data

### "Invalid or expired refresh token" Error

**Possible causes:**

1. **Token already used** (token rotation)
   - Each refresh token can only be used once
   - After use, it's replaced with a new one
   
2. **Token revoked** (logout)
   - User logged out from this device or all devices
   
3. **Token expired** (7 days)
   - User must login again
   
4. **Cookie not sent**
   - Check Postman cookie settings
   - Verify cookie jar contains `refresh_token`

### "404 Not Found" Error

- Verify the user ID exists
- Check the endpoint URL is correct
- Ensure application is running on correct port

### "429 Too Many Requests" Error

- Wait for rate limit window to reset (default 60 seconds)
- Reduce request frequency
- Rate limits: 20 requests per minute for login, 100 for other endpoints

### Cookies Not Working in Postman

1. **Check Settings:**
   - Settings → General → Enable "Send cookies from cookie jar"
   
2. **Check Domain:**
   - Cookies are domain-specific
   - Ensure all requests go to `localhost:8080`
   
3. **Clear Cookie Jar:**
   - Sometimes old cookies interfere
   - Clear all cookies and login again
   
4. **Check HTTPS (Production):**
   - Secure cookies require HTTPS
   - For localhost, HTTP is acceptable

### Testing Token Hashing

**Verify tokens are hashed in database:**

```sql
-- Check database
SELECT 
    id,
    token_hash,
    LENGTH(token_hash) as hash_length,
    user_id,
    revoked,
    expires_at
FROM refresh_tokens
ORDER BY created_at DESC
LIMIT 5;
```

**Expected result:**
- `token_hash` should be 64 characters long (SHA-256 hex)
- Should look like: `a3f5b8c9d2e1f4a7b6c8d9e0f1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a0`
- Should NOT be the actual token sent in cookie

---

## Advanced Testing Scenarios

### Testing Token Theft Detection

**Scenario: Attacker steals refresh token**

```
1. User logs in on Device A
   POST /auth/login
   → Save token as device_a_token
   → Cookie: refresh_token_A

2. Attacker steals refresh_token_A from cookie

3. User refreshes on Device A (legitimate)
   POST /auth/refresh (with refresh_token_A)
   → refresh_token_A invalidated
   → New refresh_token_B issued

4. Attacker tries to use stolen refresh_token_A
   POST /auth/refresh (with refresh_token_A)
   → Expect 401 Unauthorized
   → Error: "Invalid or expired refresh token"
   → Token already rotated, attacker locked out
```

**Result:** Token rotation prevents reuse of stolen tokens.

---

### Testing Database Breach Scenario

**Scenario: Database is compromised**

```
1. User logs in
   POST /auth/login
   → Refresh token sent to client in cookie: "abc123...xyz"
   → Database stores hash: "a3f5b8c9...f9a0"

2. Attacker gains database access
   SELECT * FROM refresh_tokens;
   → Sees: token_hash = "a3f5b8c9...f9a0"

3. Attacker tries to use the hash as token
   POST /auth/refresh
   Cookie: refresh_token=a3f5b8c9...f9a0
   → Backend hashes the cookie value: SHA256("a3f5b8c9...f9a0")
   → Searches database for hash of hash: "xyz789..."
   → Not found
   → Expect 401 Unauthorized

4. Attacker tries to reverse the hash
   → SHA-256 is one-way function
   → Cannot derive original token from hash
   → Attack fails
```

**Result:** Even with database access, attacker cannot authenticate.

---

## Performance Testing

### Recommended Load Tests

**Login Endpoint:**
```
Concurrent users: 100
Duration: 60 seconds
Expected: < 500ms response time
Rate limit: 20 requests/min per IP
```

**Token Refresh:**
```
Concurrent users: 500
Duration: 60 seconds
Expected: < 200ms response time
Rate limit: 100 requests/min per IP
```

**Protected Endpoints:**
```
Concurrent users: 1000
Duration: 60 seconds
Expected: < 300ms response time
Rate limit: 100 requests/min per IP
```

---

## Summary: Key Differences from Previous Version

### What Changed?

| Aspect | Old Version | New Version (Hashed Tokens) |
|--------|-------------|----------------------------|
| **Refresh Token Storage** | Returned in response body | HTTP-only cookie only |
| **Token in Database** | Plain text (insecure) | SHA-256 hash (secure) |
| **Token Lifetime** | Access: 24h, Refresh: 7d | Access: 15min, Refresh: 7d |
| **Token Rotation** | No | Yes (on each refresh) |
| **Logout Endpoint** | No | Yes (`/logout`, `/logout-all`) |
| **XSS Protection** | No | Yes (HTTP-only cookies) |
| **CSRF Protection** | No | Yes (SameSite=Strict) |
| **Database Breach** | Tokens exposed | Tokens safe (only hashes) |
| **Multi-Device Support** | Limited | Full (max 5 devices) |
| **Token Reuse** | Possible | Prevented (rotation) |

### New Endpoints

1. **POST /auth/refresh** - Get new access token (rotates refresh token)
2. **POST /auth/logout** - Logout from current device
3. **POST /auth/logout-all** - Logout from all devices

### Security Improvements

✅ **XSS Protection** - Refresh tokens in HTTP-only cookies  
✅ **Database Security** - Tokens hashed with SHA-256  
✅ **Token Rotation** - Automatic on each refresh  
✅ **CSRF Protection** - SameSite cookie attribute  
✅ **Short-lived Access Tokens** - 15 minutes instead of 24 hours  
✅ **Revocation Support** - Instant logout capability  
✅ **Device Tracking** - Max 5 concurrent sessions  
✅ **Auto Cleanup** - Scheduled removal of expired tokens  

---

**Last Updated:** February 4, 2026  
**API Version:** v1  
**Security Level:** Production-Ready ✅