# Address Management API Documentation

## Overview

Complete API for managing user shipping/billing addresses with support for:
- Multiple addresses per user
- Default address selection
- Full CRUD operations
- User-specific access control
- Admin management capabilities

---

## 📋 Table of Contents

1. [Endpoints Summary](#endpoints-summary)
2. [Authentication](#authentication)
3. [Data Models](#data-models)
4. [API Endpoints](#api-endpoints)
5. [Error Handling](#error-handling)
6. [Testing with Postman](#testing-with-postman)

---

## Endpoints Summary

| Method | Endpoint | Description | Auth Required | Role |
|--------|----------|-------------|---------------|------|
| GET | `/api/v1/addresses` | Get current user's addresses | ✅ | USER |
| GET | `/api/v1/addresses/{id}` | Get address by ID | ✅ | USER (own) |
| GET | `/api/v1/addresses/default` | Get default address | ✅ | USER |
| GET | `/api/v1/addresses/user/{userId}` | Get user addresses | ✅ | ADMIN |
| POST | `/api/v1/addresses` | Create new address | ✅ | USER |
| PUT | `/api/v1/addresses/{id}` | Update address | ✅ | USER (own) |
| PATCH | `/api/v1/addresses/{id}/set-default` | Set as default | ✅ | USER (own) |
| DELETE | `/api/v1/addresses/{id}` | Delete address | ✅ | USER (own) |
| DELETE | `/api/v1/addresses/user/{userId}` | Delete all user addresses | ✅ | ADMIN |

---

## Authentication

All endpoints require authentication via:
- **HTTP-only cookie**: `access_token` (JWT, automatically sent)
- **Alternative**: `Authorization: Bearer <token>` header

```bash
# Cookies sent automatically with credentials: 'include'
Cookie: access_token=eyJhbGc...

# Or use Authorization header
Authorization: Bearer eyJhbGc...
```

---

## Data Models

### AddressRequest (Input)

```json
{
  "fullName": "John Doe",
  "email": "john@example.com",
  "phone": "+1234567890",
  "address": "123 Main Street, Apt 4B",
  "city": "New York",
  "stateProvince": "NY",
  "postalCode": "10001",
  "country": "USA",
  "isDefault": true
}
```

**Validation Rules:**
- `fullName`: Required, 2-100 characters
- `email`: Required, valid email format
- `phone`: Required, 10-20 digits, pattern: `^[+]?[0-9]{10,20}$`
- `address`: Required, 5-255 characters
- `city`: Required, 2-100 characters
- `stateProvince`: Optional, max 100 characters
- `postalCode`: Optional, max 20 characters
- `country`: Optional, max 100 characters
- `isDefault`: Optional, boolean

### AddressResponse (Output)

```json
{
  "id": 1,
  "userId": 10,
  "fullName": "John Doe",
  "email": "john@example.com",
  "phone": "+1234567890",
  "address": "123 Main Street, Apt 4B",
  "city": "New York",
  "isDefault": true,
  "createdAt": "2026-02-05 10:30:00",
  "updatedAt": "2026-02-05 10:30:00"
}
```

---

## API Endpoints

### 1. Get Current User's Addresses

```http
GET /api/v1/addresses
```

**Description**: Get all addresses for the authenticated user (default address first)

**Response**:
```json
{
  "success": true,
  "message": "Addresses retrieved successfully",
  "data": [
    {
      "id": 1,
      "userId": 10,
      "fullName": "John Doe",
      "email": "john@example.com",
      "phone": "+1234567890",
      "address": "123 Main Street",
      "city": "New York",
      "isDefault": true,
      "createdAt": "2026-02-05 10:30:00",
      "updatedAt": "2026-02-05 10:30:00"
    },
    {
      "id": 2,
      "userId": 10,
      "fullName": "John Doe",
      "email": "john@work.com",
      "phone": "+1234567890",
      "address": "456 Office Blvd",
      "city": "New York",
      "isDefault": false,
      "createdAt": "2026-02-05 11:00:00",
      "updatedAt": "2026-02-05 11:00:00"
    }
  ],
  "timestamp": "2026-02-05 12:00:00",
  "path": "/api/v1/addresses"
}
```

---

### 2. Get Address by ID

```http
GET /api/v1/addresses/{id}
```

**Path Parameters**:
- `id` (Long): Address ID

**Response**:
```json
{
  "success": true,
  "message": "Address retrieved successfully",
  "data": {
    "id": 1,
    "userId": 10,
    "fullName": "John Doe",
    "email": "john@example.com",
    "phone": "+1234567890",
    "address": "123 Main Street",
    "city": "New York",
    "isDefault": true,
    "createdAt": "2026-02-05 10:30:00",
    "updatedAt": "2026-02-05 10:30:00"
  },
  "timestamp": "2026-02-05 12:00:00",
  "path": "/api/v1/addresses/1"
}
```

**Error Response** (404):
```json
{
  "success": false,
  "message": "Address not found with id: 999",
  "timestamp": "2026-02-05 12:00:00",
  "path": "/api/v1/addresses/999"
}
```

---

### 3. Get Default Address

```http
GET /api/v1/addresses/default
```

**Description**: Get the default shipping address for current user

**Response**:
```json
{
  "success": true,
  "message": "Default address retrieved successfully",
  "data": {
    "id": 1,
    "userId": 10,
    "fullName": "John Doe",
    "email": "john@example.com",
    "phone": "+1234567890",
    "address": "123 Main Street",
    "city": "New York",
    "isDefault": true,
    "createdAt": "2026-02-05 10:30:00",
    "updatedAt": "2026-02-05 10:30:00"
  },
  "timestamp": "2026-02-05 12:00:00",
  "path": "/api/v1/addresses/default"
}
```

---

### 4. Get User Addresses (Admin Only)

```http
GET /api/v1/addresses/user/{userId}
```

**Path Parameters**:
- `userId` (Long): User ID

**Authorization**: ADMIN role required

**Response**:
```json
{
  "success": true,
  "message": "User addresses retrieved successfully",
  "data": [
    // Array of AddressResponse objects
  ],
  "timestamp": "2026-02-05 12:00:00",
  "path": "/api/v1/addresses/user/10"
}
```

---

### 5. Create Address

```http
POST /api/v1/addresses
Content-Type: application/json
```

**Request Body**:
```json
{
  "fullName": "John Doe",
  "email": "john@example.com",
  "phone": "+1234567890",
  "address": "123 Main Street, Apt 4B",
  "city": "New York",
  "isDefault": true
}
```

**Response** (201 Created):
```json
{
  "success": true,
  "message": "Address created successfully",
  "data": {
    "id": 1,
    "userId": 10,
    "fullName": "John Doe",
    "email": "john@example.com",
    "phone": "+1234567890",
    "address": "123 Main Street, Apt 4B",
    "city": "New York",
    "isDefault": true,
    "createdAt": "2026-02-05 12:00:00",
    "updatedAt": "2026-02-05 12:00:00"
  },
  "timestamp": "2026-02-05 12:00:00",
  "path": "/api/v1/addresses"
}
```

**Behavior**:
- First address is automatically set as default
- Setting `isDefault: true` will unset all other addresses
- If `isDefault` is not provided, defaults to `false` (unless first address)

---

### 6. Update Address

```http
PUT /api/v1/addresses/{id}
Content-Type: application/json
```

**Path Parameters**:
- `id` (Long): Address ID

**Request Body**:
```json
{
  "fullName": "John Doe Jr.",
  "email": "john.jr@example.com",
  "phone": "+1234567890",
  "address": "123 Main Street, Apt 5C",
  "city": "New York",
  "isDefault": false
}
```

**Response**:
```json
{
  "success": true,
  "message": "Address updated successfully",
  "data": {
    // Updated AddressResponse
  },
  "timestamp": "2026-02-05 12:00:00",
  "path": "/api/v1/addresses/1"
}
```

---

### 7. Set Address as Default

```http
PATCH /api/v1/addresses/{id}/set-default
```

**Path Parameters**:
- `id` (Long): Address ID

**Description**: Set this address as default (unsets all other defaults)

**Response**:
```json
{
  "success": true,
  "message": "Default address set successfully",
  "data": {
    "id": 2,
    "userId": 10,
    "fullName": "John Doe",
    "email": "john@work.com",
    "phone": "+1234567890",
    "address": "456 Office Blvd",
    "city": "New York",
    "isDefault": true,
    "createdAt": "2026-02-05 11:00:00",
    "updatedAt": "2026-02-05 12:00:00"
  },
  "timestamp": "2026-02-05 12:00:00",
  "path": "/api/v1/addresses/2/set-default"
}
```

---

### 8. Delete Address

```http
DELETE /api/v1/addresses/{id}
```

**Path Parameters**:
- `id` (Long): Address ID

**Response**:
```json
{
  "success": true,
  "message": "Address deleted successfully",
  "data": null,
  "timestamp": "2026-02-05 12:00:00",
  "path": "/api/v1/addresses/1"
}
```

**Behavior**:
- If deleted address was default, the next address (by creation date) becomes default
- If last address is deleted, user has no addresses (valid state)

---

### 9. Delete All User Addresses (Admin Only)

```http
DELETE /api/v1/addresses/user/{userId}
```

**Path Parameters**:
- `userId` (Long): User ID

**Authorization**: ADMIN role required

**Response**:
```json
{
  "success": true,
  "message": "All user addresses deleted successfully",
  "data": null,
  "timestamp": "2026-02-05 12:00:00",
  "path": "/api/v1/addresses/user/10"
}
```

---

## Error Handling

### Common Error Responses

**400 Bad Request** (Validation Error):
```json
{
  "success": false,
  "message": "Validation failed",
  "errors": {
    "fullName": "Full name is required",
    "phone": "Phone number should be valid (10-20 digits)"
  },
  "timestamp": "2026-02-05 12:00:00",
  "path": "/api/v1/addresses"
}
```

**401 Unauthorized**:
```json
{
  "success": false,
  "message": "Authentication required",
  "timestamp": "2026-02-05 12:00:00",
  "path": "/api/v1/addresses"
}
```

**403 Forbidden**:
```json
{
  "success": false,
  "message": "You don't have permission to access this resource",
  "timestamp": "2026-02-05 12:00:00",
  "path": "/api/v1/addresses/user/10"
}
```

**404 Not Found**:
```json
{
  "success": false,
  "message": "Address not found with id: 999",
  "timestamp": "2026-02-05 12:00:00",
  "path": "/api/v1/addresses/999"
}
```

---

## Testing with Postman

### Setup

1. **Import Environment**:
```json
{
  "name": "E-commerce API",
  "values": [
    {
      "key": "BASE_URL",
      "value": "http://localhost:8080/api/v1",
      "enabled": true
    },
    {
      "key": "ACCESS_TOKEN",
      "value": "",
      "enabled": true
    }
  ]
}
```

2. **Enable Cookies** in Postman:
   - Settings → General → Cookies
   - Enable "Automatically follow redirects"
   - Enable "Interceptor cookies"

### Test Scenarios

#### Scenario 1: Create First Address (Becomes Default)

```http
POST {{BASE_URL}}/addresses
Content-Type: application/json

{
  "fullName": "John Doe",
  "email": "john@example.com",
  "phone": "+1234567890",
  "address": "123 Main Street",
  "city": "New York",
  "stateProvince": "NY",
  "postalCode": "10001",
  "country": "USA"
}
```

**Expected**: Address created with `isDefault: true` automatically

#### Scenario 2: Create Second Address

```http
POST {{BASE_URL}}/addresses
Content-Type: application/json

{
  "fullName": "John Doe",
  "email": "john@work.com",
  "phone": "+1234567890",
  "address": "456 Office Blvd",
  "city": "New York",
  "isDefault": false
}
```

**Expected**: Address created with `isDefault: false`

#### Scenario 3: Change Default Address

```http
PATCH {{BASE_URL}}/addresses/2/set-default
```

**Expected**: 
- Address #2 becomes default (`isDefault: true`)
- Address #1 becomes non-default (`isDefault: false`)

#### Scenario 4: Get All Addresses

```http
GET {{BASE_URL}}/addresses
```

**Expected**: List of addresses with default first

#### Scenario 5: Delete Default Address

```http
DELETE {{BASE_URL}}/addresses/2
```

**Expected**:
- Address #2 deleted
- Address #1 automatically becomes default

---

## Business Logic

### Default Address Handling

1. **First Address**: Automatically set as default
2. **Multiple Addresses**: User can have only ONE default
3. **Set Default**: All other addresses become non-default
4. **Delete Default**: Next address (by created_at) becomes default
5. **No Addresses**: Valid state, user can create new ones

### Security & Authorization

1. **User Can**:
   - View their own addresses
   - Create new addresses
   - Update their own addresses
   - Delete their own addresses
   - Set default address

2. **Admin Can**:
   - View any user's addresses
   - Delete all addresses for a user

3. **Prevented**:
   - User accessing another user's addresses
   - Unauthorized address modifications

### Validation Rules

- **Phone**: Must match pattern `^[+]?[0-9]{10,20}$`
- **Email**: Must be valid email format
- **Required Fields**: fullName, email, phone, address, city
- **Optional Fields**: stateProvince, postalCode, country
- **Length Limits**: Enforced per field (see AddressRequest)

---

## Frontend Integration Examples

### Fetch All Addresses

```typescript
import { api } from '@/services/api';

const addresses = await api.get<AddressResponse[]>('/addresses');
```

### Create Address

```typescript
const newAddress = await api.post<AddressResponse>('/addresses', {
  fullName: 'John Doe',
  email: 'john@example.com',
  phone: '+1234567890',
  address: '123 Main Street',
  city: 'New York',
  isDefault: true
});
```

### Set Default Address

```typescript
const updated = await api.patch<AddressResponse>(`/addresses/${id}/set-default`);
```

### Delete Address

```typescript
await api.delete(`/addresses/${id}`);
```

---

## Database Schema

```sql
CREATE TABLE addresses (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    address VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    
    CONSTRAINT fk_address_user FOREIGN KEY (user_id) 
        REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_address_user_id ON addresses(user_id);
CREATE INDEX idx_address_user_default ON addresses(user_id, is_default);
```

---

## Summary

✅ **Complete address management system**
✅ **Secure user-specific access**
✅ **Default address handling**
✅ **Full CRUD operations**
✅ **Admin capabilities**
✅ **Validation & error handling**
✅ **HTTP-only cookie authentication**
✅ **Production-ready**

Your Address API is ready to use! 🎉