# Ecommerce_Backend

### Secure REST API with Spring Boot
## Features

### Core Features
- **User Authentication & Authorization**
  - JWT-based authentication
  - Role-based access control (USER, ADMIN, MODERATOR)
  - Secure password encoding with BCrypt
  
- **RESTful API Design**
  - Clean REST endpoints
  - Proper HTTP status codes
  - Standardized response format
  
-  **DTO Pattern**
  - Request DTOs with comprehensive validation
  - Response DTOs for data transfer
  - Clear separation of concerns

-  **Input Validation**
  - Bean Validation (JSR 380)
  - Custom validators
  - Password strength requirements
  - Email format validation

-  **Spring Data JPA**
  - Entity-relationship mapping
  - Custom query methods
  - Pagination support
  - Automatic timestamps

-  **Global Exception Handling**
  - Centralized error handling
  - Consistent error responses
  - Custom exception types
  - Validation error formatting

### Advanced Features

-  **Rate Limiting**
  - IP-based rate limiting using Bucket4j
  - Configurable request limits
  - Automatic token refill
  - Rate limit headers in response

-  **Full-Text Search**
  - Search users by username, email, name
  - Case-insensitive search
  - Pagination support
  - PostgreSQL full-text search support
  
## Validation Rules

### Registration
- **Username:** 3-50 characters, alphanumeric with underscore and hyphen
- **Email:** Valid email format
- **Password:** 
  - 8-100 characters
  - At least one digit
  - At least one lowercase letter
  - At least one uppercase letter
  - At least one special character (@#$%^&+=!)
- **Phone:** Valid international phone format

### Update
- All fields are optional
- Same validation rules apply when provided

## Security Features

### JWT Authentication
- Access token expires in 24 hours
- Refresh token expires in 7 days
- Tokens are signed with HS256
- Secret key configured in application.yml

### Password Security
- BCrypt hashing with strength 10
- Password strength requirements enforced
- Current password verification for changes

### Role-Based Access Control
- **USER:** Basic user operations
- **MODERATOR:** Extended permissions
- **ADMIN:** Full system access

### Rate Limiting
- Default: 100 requests per 60 seconds
- Token bucket algorithm
- Per-IP tracking
- Configurable limits

## Response Format

### Success Response
```json
{
  "success": true,
  "message": "Operation successful",
  "data": { ... },
  "timestamp": "2026-01-27 10:30:00"
}
```

### Error Response
```json
{
  "success": false,
  "message": "Error description",
  "timestamp": "2026-01-27 10:30:00",
  "path": "/api/v1/users/999"
}
```

### Validation Error Response
```json
{
  "success": false,
  "message": "Validation failed",
  "data": {
    "username": "Username must be between 3 and 50 characters",
    "email": "Email should be valid"
  },
  "timestamp": "2026-01-27 10:30:00",
  "path": "/api/v1/auth/register"
}
```

### Test with Postman/cURL

**Register:**
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

**Login:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "testuser",
    "password": "Test123!@#"
  }'
```

**Get Current User:**
```bash
curl -X GET http://localhost:8080/api/v1/users/me \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```


