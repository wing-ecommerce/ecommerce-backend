# 🛒 E-commerce Backend API – Category & Product Modules

This document explains **how to test and use the Category and Product APIs** in your E-commerce backend built with Spring Boot.

---

## 🌐 Base URL

```
http://localhost:8080/api/v1
```

---

## 🔐 Authentication (ADMIN)

Creating, updating, or deleting categories or products requires an **ADMIN JWT token**.

### Login Endpoint

```
POST /api/v1/auth/login
```

#### Request Body

```json
{
  "email": "admin@example.com",
  "password": "admin123"
}
```

#### Response

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

📌 Use this token in the `Authorization` header for secured requests:

```
Authorization: Bearer <ADMIN_TOKEN>
```

---

# 📦 Category API Endpoints & Workflow

### 1️⃣ Create Category (ADMIN)

```
POST /api/v1/categories
```

**Headers**

```
Authorization: Bearer <ADMIN_TOKEN>
Content-Type: application/json
```

**Request Body**

```json
{
  "name": "Electronics",
  "slug": "electronics"
}
```

**Response**

* Status: `200 OK`
* Category created successfully with `createdAt` and `updatedAt`.

---

### 2️⃣ Get All Categories (Public, with Pagination)

```
GET /api/v1/categories?page=0&size=10
```

**Query Parameters**

| Parameter | Type | Description |
|-----------|------|-------------|
| page      | int  | Page number (0-based index) |
| size      | int  | Number of categories per page |

**Response**

* Status: `200 OK`
* Paginated list of categories with timestamps.

---

### 3️⃣ Get Category by ID (Public)

```
GET /api/v1/categories/{id}
```

---

### 4️⃣ Update Category (ADMIN)

```
PUT /api/v1/categories/{id}
```

**Headers**

```
Authorization: Bearer <ADMIN_TOKEN>
```

**Request Body**

```json
{
  "name": "Electronic Devices",
  "slug": "electronic-devices"
}
```

---

### 5️⃣ Delete Category (ADMIN)

```
DELETE /api/v1/categories/{id}
```

**Headers**

```
Authorization: Bearer <ADMIN_TOKEN>
```

---

### ⚠️ Category Error Scenarios

| Scenario            | Expected Result |
| ------------------- | --------------- |
| No token provided   | 403 Forbidden   |
| USER role token     | 403 Forbidden   |
| Invalid category ID | 404 Not Found   |
| Duplicate slug      | 400 Bad Request |

---

# 📦 Product API Endpoints & Workflow

### 1️⃣ Create Product (ADMIN)

```
POST /api/v1/products
```

**Headers**

```
Authorization: Bearer <ADMIN_TOKEN>
Content-Type: application/json
```

**Request Body**

```json
{
  "name": "iPhone 15",
  "slug": "iphone-15",
  "price": 1200.0,
  "originalPrice": 1400.0,
  "discount": 200,
  "image": "iphone_main.jpg",
  "additionalPhotos": ["photo1.jpg", "photo2.jpg"],
  "description": "Latest Apple iPhone 15",
  "inStock": true,
  "sizes": ["128GB", "256GB"],
  "categoryId": "c123-uuid"
}
```

---

### 2️⃣ Update Product (ADMIN)

```
PUT /api/v1/products/{id}
```

**Headers**

```
Authorization: Bearer <ADMIN_TOKEN>
```

**Request Body**

```json
{
  "name": "iPhone 15 Pro",
  "slug": "iphone-15-pro",
  "price": 1300.0,
  "originalPrice": 1500.0,
  "discount": 200,
  "image": "iphone_pro.jpg",
  "additionalPhotos": ["photo1.jpg", "photo2.jpg"],
  "description": "Apple iPhone 15 Pro",
  "inStock": true,
  "sizes": ["256GB", "512GB"],
  "categoryId": "c123-uuid"
}
```

---

### 3️⃣ Delete Product (ADMIN)

```
DELETE /api/v1/products/{id}
```

---

### 4️⃣ Get Product by ID (Public)

```
GET /api/v1/products/{id}
```

---

### 5️⃣ Get All Products (Public)

```
GET /api/v1/products
```

**Optional Pagination**

```
GET /api/v1/products?page=0&size=10
```

---

### 6️⃣ Filter Products by Category

**With Pagination**

```
GET /api/v1/products/category/{categoryId}?page=0&size=10
```

**See All Products (No Pagination)**

```
GET /api/v1/products/category/{categoryId}/all
```

---

### ⚠️ Product Error Scenarios

| Scenario                    | Expected Result          |
|-------------------------------|-------------------------|
| No token provided (ADMIN)     | 403 Forbidden           |
| USER role token for admin API | 403 Forbidden           |
| Invalid product ID            | 404 Not Found           |
| Duplicate slug                | 400 Bad Request         |
| Invalid category ID           | 404 Not Found           |

---

### ✅ Testing Checklist

* [ ] Login as ADMIN
* [ ] Create / Update / Delete category
* [ ] Get category by ID
* [ ] Get all categories (with pagination)
* [ ] Create / Update / Delete product
* [ ] Get product by ID
* [ ] Get all products (with and without pagination)
* [ ] Filter products by category (paged & all)

---

### 📘 Notes

* `createdAt` and `updatedAt` are handled automatically.
* Pagination defaults: `page=0` and `size=10`.
* Public endpoints do not require authentication.
* Admin endpoints require JWT token with `ROLE_ADMIN`.

