# 🛍️ Teespace E-commerce Backend API
## Table of Contents
- [Project Description](#project-description)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Database Table Relationships](#database-table-relationships)
- [Entities](#entities)
- [Authentication & Security](#authentication--security)
- [Environment Variables](#environment-variables)
- [How to Run Backend](#how-to-run-backend)
- [API Endpoints](#api-endpoints)
    - [Auth](#auth)
    - [Users](#users)
    - [Products](#products)
    - [Categories](#categories)
    - [Orders](#orders)
    - [Addresses](#addresses)
    - [Cards](#cards)
---
---


##  Project Description
- git repo:https://github.com/wing-ecommerce/ecommerce-backend.git
- hosting: https://teespace.onrender.com/

RESTful API for an e-commerce system.

* Handles authentication, users, roles, products, categories, orders, addresses, and payment cards
* Backend only (no frontend)
* Built with **Spring Boot**, **Spring Security (JWT)**, and **PostgreSQL**

---

## Tech Stack

| Technology      | Version            |
| --------------- | ------------------ |
| Java            | 17                 |
| Spring Boot     | Latest             |
| Spring Security | JWT Authentication |
| Database        | PostgreSQL         |
| ORM             | JPA / Hibernate    |
| Build Tool      | Maven              |

---

## Project Structure

```
src/main/java/com/example/ecommerce_backend/
 ├── controller       # REST Controllers
 ├── service          # Business Logic
 ├── repository       # JPA Repositories
 ├── entity           # Database Entities
 ├── dto              # Request / Response Models
 ├── config           # Security & JWT Configuration
 ├── exception        # Global Exception Handling
 └── util             # Utilities
```

---
## Database Table Relationships

| Entity        | Relationship                    | Related Entity     | Description |
|---------------|----------------------------------|--------------------|-------------|
| User          | One-to-Many                      | Order              | One user can place many orders |
| User          | One-to-Many                      | Address            | One user can have multiple shipping/billing addresses |
| User          | One-to-Many                      | RefreshToken       | One user can have multiple refresh tokens |
| User          | One-to-One                       | Cart               | Each user has one active shopping cart |
| Cart          | One-to-Many                      | CartItem           | A cart contains multiple cart items |
| CartItem      | Many-to-One                      | Cart               | Each cart item belongs to one cart |
| CartItem      | Many-to-One                      | Product            | Each cart item references one product |
| CartItem      | Many-to-One                      | ProductSize        | Each cart item references one product variant (size) |
| Order         | One-to-Many                      | OrderItem          | An order contains multiple order items |
| OrderItem     | Many-to-One                      | Order              | Each order item belongs to one order |
| OrderItem     | Many-to-One                      | Product            | Each order item references one product |
| OrderItem     | Many-to-One                      | ProductSize        | Each order item references one product variant (size) |
| Product       | Many-to-One                      | Category           | Each product belongs to one category |
| Category      | One-to-Many                      | Product            | A category contains many products |
| Product       | One-to-Many                      | ProductSize        | A product has multiple size variants |
| ProductSize   | Many-to-One                      | Product            | Each size belongs to one product |


---

## Entities

### User
| Field              | Type            | Required | Description |
|--------------------|-----------------|----------|-------------|
| id                 | Long            | Yes      | Primary key |
| username           | String          | Yes      | Unique username |
| email              | String          | Yes      | Unique email |
| password           | String          | Optional | Hashed password (null for OAuth users) |
| firstName          | String          | Optional | User first name |
| lastName           | String          | Optional | User last name |
| phoneNumber        | String          | Optional | User phone number |
| role               | Role (ENUM)     | Yes      | User role (USER, ADMIN, MODERATOR) |
| oauthProvider      | OAuthProvider   | Optional | OAuth provider (LOCAL, GOOGLE) |
| oauthProviderId    | String          | Optional | OAuth provider user ID |
| profileImageUrl    | String          | Optional | Profile image URL |
| emailVerified      | Boolean         | Yes      | Email verification status |
| enabled            | Boolean         | Yes      | Account enabled |
| createdAt          | LocalDateTime   | Yes      | Account creation time |
| updatedAt          | LocalDateTime   | Yes      | Last update time |
| lastLogin          | LocalDateTime   | Optional | Last login timestamp |

---

### Category
| Field       | Type          | Required | Description |
|------------|---------------|----------|-------------|
| id         | String        | Yes      | Primary key (UUID/String) |
| name       | String        | Yes      | Category name |
| slug       | String        | Yes      | URL-friendly slug |
| createdAt  | LocalDateTime | Yes      | Creation time |
| updatedAt  | LocalDateTime | Yes      | Last update time |

---

### Product
| Field             | Type            | Required | Description |
|-------------------|-----------------|----------|-------------|
| id                | Long            | Yes      | Primary key |
| name              | String          | Yes      | Product name |
| slug              | String          | Yes      | Unique URL-friendly name |
| price             | Double          | Yes      | Base product price |
| originalPrice     | Double          | Optional | Original price before discount |
| discount          | Integer         | Optional | Discount percentage |
| image             | String          | Optional | Main image URL |
| additionalPhotos  | List<String>    | Optional | Additional product images |
| description       | String          | Optional | Product description |
| category          | Category        | Yes      | Product category |
| createdAt         | LocalDateTime   | Yes      | Creation time |
| updatedAt         | LocalDateTime   | Yes      | Last update time |

---

### ProductSize
| Field        | Type          | Required | Description |
|-------------|---------------|----------|-------------|
| id          | Long          | Yes      | Primary key |
| size        | String        | Yes      | Size (S, M, L, XL, etc.) |
| stock       | Integer       | Yes      | Stock quantity |
| priceOverride | Double     | Optional | Optional size-specific price |
| sku         | String        | Optional | Variant SKU |
| product     | Product       | Yes      | Parent product |

---

### Cart
| Field       | Type          | Required | Description |
|------------|---------------|----------|-------------|
| id         | Long          | Yes      | Primary key |
| userId     | Long          | Yes      | Linked user ID |
| createdAt  | LocalDateTime | Yes      | Creation time |
| updatedAt  | LocalDateTime | Yes      | Last update time |

---

### CartItem
| Field        | Type          | Required | Description |
|-------------|---------------|----------|-------------|
| id          | Long          | Yes      | Primary key |
| cart        | Cart          | Yes      | Parent cart |
| product     | Product       | Yes      | Linked product |
| productSize | ProductSize   | Yes      | Selected size variant |
| quantity    | Integer       | Yes      | Quantity in cart |
| price       | Double        | Yes      | Price at time of adding |
| createdAt   | LocalDateTime | Yes      | Creation time |
| updatedAt   | LocalDateTime | Yes      | Last update time |

---

### Order
| Field            | Type            | Required | Description |
|------------------|-----------------|----------|-------------|
| id               | Long            | Yes      | Primary key |
| orderNumber      | String          | Yes      | Unique order number |
| userId           | Long            | Yes      | Customer user ID |
| addressId        | Long            | Yes      | Shipping address ID |
| status           | OrderStatus     | Yes      | Order status |
| paymentMethod    | PaymentMethod   | Yes      | Payment method |
| paymentStatus    | PaymentStatus   | Yes      | Payment status |
| subtotal         | Double          | Yes      | Subtotal price |
| shipping         | Double          | Yes      | Shipping cost |
| tax              | Double          | Yes      | Tax amount |
| total            | Double          | Yes      | Final total amount |
| notes            | String          | Optional | Customer/admin notes |
| estimatedDelivery| LocalDateTime   | Optional | Estimated delivery |
| deliveredAt      | LocalDateTime   | Optional | Actual delivery time |
| createdAt        | LocalDateTime   | Yes      | Order creation time |
| updatedAt        | LocalDateTime   | Yes      | Last update time |

---

### OrderItem
| Field        | Type        | Required | Description |
|-------------|-------------|----------|-------------|
| id          | Long        | Yes      | Primary key |
| order       | Order       | Yes      | Parent order |
| product     | Product     | Yes      | Purchased product |
| productSize | ProductSize | Yes      | Purchased size |
| quantity    | Integer     | Yes      | Quantity ordered |
| price       | Double      | Yes      | Price at time of purchase |

---

### Address
| Field      | Type          | Required | Description |
|------------|---------------|----------|-------------|
| id         | Long          | Yes      | Primary key |
| user       | User          | Yes      | Address owner |
| fullName   | String        | Yes      | Recipient full name |
| email      | String        | Yes      | Recipient email |
| phone      | String        | Yes      | Contact phone |
| address    | String        | Yes      | Street / full address |
| city       | String        | Yes      | City |
| isDefault  | Boolean       | Yes      | Default shipping address |
| createdAt  | LocalDateTime | Yes      | Created time |
| updatedAt  | LocalDateTime | Yes      | Last updated time |

---

### RefreshToken
| Field               | Type          | Required | Description |
|---------------------|---------------|----------|-------------|
| id                  | Long          | Yes      | Primary key |
| tokenHash           | String        | Yes      | SHA-256 hashed refresh token |
| user                | User          | Yes      | Token owner |
| expiresAt           | LocalDateTime | Yes      | Token expiration |
| revoked             | Boolean       | Yes      | Revocation status |
| revokedAt           | LocalDateTime | Optional | Revocation timestamp |
| replacedByTokenHash | String        | Optional | Rotated token hash |
| createdAt           | LocalDateTime | Yes      | Token creation time |


##  Authentication & Security

* JWT-based Authentication
* Access Token (Short-lived)
* Refresh Token (HTTP-only Cookie)
* Role-based Authorization

### Authorization Header

```
Authorization: Bearer <access_token>
```

---

##  Environment Variables

```
spring.datasource.url=jdbc:postgresql://localhost:5432/teespace
spring.datasource.username=your_db_username
spring.datasource.password=your_db_password

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

jwt.secret=your_jwt_secret
jwt.expiration=86400000
jwt.refreshExpiration=604800000
```

---

##  How to Run Backend

```bash
git clone https://github.com/wing-ecommerce/ecommerce-backend.git
cd ecommerce-backend
mvn clean install
mvn spring-boot:run
```

Server runs at:

```
http://localhost:8080
```

---

##  Base API URL

```
http://localhost:8080/api/v1
```

---

#  API ENDPOINTS

---

#  AUTH

## Register

**POST** `/api/v1/auth/register`

### Request

```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "password123"
}
```

### Response

```json
{
  "success": true,
  "message": "User registered successfully"
}
```

---

## Login

**POST** `/api/v1/auth/login`

```json
{
  "email": "john@example.com",
  "password": "password123"
}
```

### Response

```json
{
  "accessToken": "jwt_token",
  "refreshToken": "refresh_token"
}
```

---

## Refresh Token

**POST** `/api/v1/auth/refresh`

Returns new access token using refresh cookie.

---

## Logout

**POST** `/api/v1/auth/logout`

Invalidates current refresh token.

---

#  USERS

## Get User

**GET** `/api/v1/users/{id}`

## Update User

**PUT** `/api/v1/users/{id}`

```json
{
  "firstName": "John",
  "lastName": "Updated",
  "phoneNumber": "012345678"
}
```

## Delete User

**DELETE** `/api/v1/users/{id}`

---

#  PRODUCTS

## Create Product (ADMIN)

**POST** `/api/v1/products`

```json
{
  "name": "Classic Cotton Tee",
  "slug": "classic-cotton-tee",
  "price": 25.0,
  "originalPrice": 30.0,
  "discount": 10,
  "image": "image.jpg",
  "additionalPhotos": ["photo1.jpg"],
  "description": "Premium cotton t-shirt",
  "categoryId": "cat-123",
  "sizes": [
    {
      "size": "M",
      "stock": 50,
      "priceOverride": 27.0,
      "sku": "TEE-M-001"
    }
  ]
}
```

---

## Update Product

**PUT** `/api/v1/products/{id}`

## Patch Product

**PATCH** `/api/v1/products/{id}`

## Delete Product

**DELETE** `/api/v1/products/{id}`

## Get Product

**GET** `/api/v1/products/{id}`

## Get All Products

**GET** `/api/v1/products?page=0&size=10`

## Get Products by Category

**GET** `/api/v1/products/category/{categoryId}`

---

#  CATEGORIES

## Create Category

**POST** `/api/v1/categories`

```json
{
  "name": "Hoodies",
  "slug": "hoodies"
}
```

## Update Category

**PUT** `/api/v1/categories/{id}`

## Delete Category

**DELETE** `/api/v1/categories/{id}`

## Get Categories

**GET** `/api/v1/categories`

## Get Category by ID

**GET** `/api/v1/categories/{id}`

---

#  ORDERS

## Create Order

**POST** `/api/v1/orders`

```json
{
  "addressId": 1,
  "paymentMethod": "CARD",
  "items": [
    {
      "productId": 1,
      "sizeId": 2,
      "quantity": 2
    }
  ]
}
```

## Get Orders

**GET** `/api/v1/orders`

## Get Order by ID

**GET** `/api/v1/orders/{id}`

---

#  ADDRESSES

## Create Address

**POST** `/api/v1/addresses`

```json
{
  "fullName": "John Doe",
  "phone": "012345678",
  "address": "Phnom Penh",
  "city": "Phnom Penh",
  "isDefault": true
}
```

## Update Address

**PUT** `/api/v1/addresses/{id}`

## Delete Address

**DELETE** `/api/v1/addresses/{id}`

## Get Addresses

**GET** `/api/v1/addresses`

---

#  CARDS

## Add Card

**POST** `/api/v1/cards`

```json
{
  "cardHolderName": "John Doe",
  "last4": "4242",
  "brand": "VISA"
}
```

## Get Cards

**GET** `/api/v1/cards`

## Update Card

**PUT** `/api/v1/cards/{id}`

## Delete Card

**DELETE** `/api/v1/cards/{id}`

---

#  Standard Response Format

```json
{
  "success": true,
  "data": {}
}
```


```

---

## 👮 Roles & Permissions

| Role  | Permissions                    |
| ----- | ------------------------------ |
| USER  | Browse products, create orders |
| ADMIN | Full CRUD access               |

---

## 🧪 Sample Curl

```bash
curl -X GET http://localhost:8080/api/v1/products
```

---

✔ Cleaned Structure
✔ Removed Duplicates
✔ Fixed Base Paths
✔ Matches Controller Mapping (`/api/v1/...`)
✔ Ready for GitHub README

---

If you want next, I can generate:

* ✅ Postman Collection
* ✅ Swagger/OpenAPI YAML
* ✅ Database ER Diagram
