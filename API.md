# API Testing Guide - Postman

Complete guide for testing the E-commerce Backend REST API using Postman.

## Table of Contents
1. [Setup](#setup)
2. [Environment Variables](#environment-variables)
3. [Authentication Endpoints](#authentication-endpoints)
4. [User Endpoints](#user-endpoints)
5. [Admin Endpoints](#admin-endpoints)
6. [Test Scenarios](#test-scenarios)

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
| `admin_token` | (empty) | Admin JWT token |
| `user_token` | (empty) | User JWT token |
| `test_user_id` | (empty) | Test user ID |

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
    "expires_in": 86400000,
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
    "refresh_token": "eyJhbGciOiJIUzI1NiJ9...",
    "token_type": "Bearer",
    "expires_in": 86400000,
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

---

## User Endpoints

**Authorization Required:** All user endpoints require `Bearer {{user_token}}` in Authorization header.

### 4. Get Current User

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

### 5. Get User by ID

**Request:**
```
GET {{base_url}}/users/{{test_user_id}}
Authorization: Bearer {{user_token}}
```

**Response:** Same structure as Get Current User

---

### 6. Get User by Username

**Request:**
```
GET {{base_url}}/users/username/johndoe
Authorization: Bearer {{user_token}}
```

**Response:** Same structure as Get Current User

---

### 7. Update User Profile

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

### 8. Change Password

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

---

### 9. Search Users

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

### 10. Delete User

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

### 11. Get All Users (Admin Only)

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

### 12. Enable User (Admin Only)

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

### 13. Disable User (Admin Only)

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

### 14. Change User Role (Admin Only)

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

### 15. Get Users by Role (Admin Only)

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

### Scenario 1: Complete User Flow

```
1. Register new user
   POST /auth/register
   → Save user_token

2. Get current user
   GET /users/me
   → Verify user details

3. Update profile
   PUT /users/{id}
   → Verify updates

4. Change password
   PATCH /users/change-password
   → Verify success

5. Login with new password
   POST /auth/login
   → Verify login works
```

---

### Scenario 2: Admin User Management

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
   → Expect 401

5. Enable user
   PATCH /admin/users/{id}/enable
   → Verify user enabled

6. Change user role to MODERATOR
   PATCH /admin/users/{id}/role?role=MODERATOR
   → Verify role changed
```

---

### Scenario 3: Authorization Testing

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

### Scenario 4: Validation Testing

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
  "timestamp": "2026-01-27 10:30:00",
  "path": "/api/v1/auth/register"
}
```

### 401 Unauthorized
```json
{
  "success": false,
  "message": "Invalid username or password",
  "timestamp": "2026-01-27 10:30:00",
  "path": "/api/v1/auth/login"
}
```

### 403 Forbidden
```json
{
  "success": false,
  "message": "Access Denied",
  "timestamp": "2026-01-27 10:30:00",
  "path": "/api/v1/admin/users/2/enable"
}
```

### 404 Not Found
```json
{
  "success": false,
  "message": "User not found with id: 999",
  "timestamp": "2026-01-27 10:30:00",
  "path": "/api/v1/users/999"
}
```

### 409 Conflict - Duplicate Resource
```json
{
  "success": false,
  "message": "Username already exists",
  "timestamp": "2026-01-27 10:30:00",
  "path": "/api/v1/auth/register"
}
```

### 429 Too Many Requests - Rate Limit
```json
{
  "success": false,
  "message": "Rate limit exceeded. Try again in 45 seconds.",
  "timestamp": "2026-01-27 10:30:00",
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
  }'
```

### Admin Login
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "admin",
    "password": "Admin123!@#"
  }'
```

### Get Current User
```bash
curl -X GET http://localhost:8080/api/v1/users/me \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Enable User (Admin)
```bash
curl -X PATCH http://localhost:8080/api/v1/admin/users/2/enable \
  -H "Authorization: Bearer ADMIN_TOKEN"
```

---

## Default Test Accounts

| Username | Password | Email | Role |
|----------|----------|-------|------|
| `admin` | `Admin123!@#` | admin@ecommerce.com | ADMIN |

Create additional test users via registration endpoint.

---

## Tips for Testing

1. **Save tokens:** Use Postman Tests scripts to automatically save tokens
2. **Use variables:** Leverage collection variables for reusability
3. **Test order:** Run authentication tests first to get tokens
4. **Pagination:** Test different page sizes and sort orders
5. **Edge cases:** Test with invalid data, missing fields, etc.
6. **Rate limiting:** Test rapid requests to trigger rate limits
7. **Authorization:** Test each role's access to different endpoints

---

## Troubleshooting

### "401 Unauthorized" Error
- Check if token is expired (24 hours)
- Verify token is in Authorization header: `Bearer {token}`
- Re-login to get fresh token

### "403 Forbidden" Error
- Check if user has required role
- Admin endpoints require ADMIN role
- User can only modify their own data

### "404 Not Found" Error
- Verify the user ID exists
- Check the endpoint URL is correct

### "429 Too Many Requests" Error
- Wait for rate limit window to reset (default 60 seconds)
- Reduce request frequency

---
