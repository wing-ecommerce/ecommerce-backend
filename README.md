# Teespace E-commerce Backend API

📖 **Project Description**  
• RESTful API for an e-commerce system  
• Handles authentication, products, orders, users, and roles  
• Built with Spring Boot, Spring Security (JWT), and PostgreSQL  

---

🛠 **Tech Stack**  
• Java 17  
• Spring Boot  
• Spring Security (JWT Authentication)  
• PostgreSQL  
• JPA / Hibernate  
• Maven  

---

📂 **Project Structure**  

```
src/main/java/com/example/ecommerce_backend/
 ├── controller       # REST controllers (Auth, Users, Products, Orders)
 ├── service          # Business logic and service layer
 ├── repository       # JPA repositories for DB access
 ├── entity           # Database entities (User, Product, Order, Role)
 ├── dto              # Data Transfer Objects for requests/responses
 ├── config           # Security and JWT configuration
 ├── exception        # Custom exceptions and handlers
 └── util             # Utility classes (JWT utils, validation, etc.)
```

---

🔗 **Database Table Relationships**

| Entity   | Relationship                           | Related Entity |
|---------|----------------------------------------|----------------|
| User    | One-to-Many                             | Order          |
| User    | Many-to-Many                            | Role           |
| Product | Many-to-Many (via OrderItem)            | Order          |
| Order   | Many-to-One                             | User           |
| Order   | One-to-Many (Order contains OrderItems) | OrderItem      |
| OrderItem | Many-to-One                            | Product        |

> Users can have multiple Orders, Orders contain multiple Products via OrderItem junction table, and Users can have multiple Roles.

---

🔐 **Authentication**  
• JWT-based authentication  
• Access Token with short expiry  
• Refresh Token stored in HttpOnly cookie for session renewal  
• Roles-based access (USER / ADMIN)  

---

⚙️ **Environment Variables**  

`application.properties` example:

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

🚀 **How to Run Backend**

```bash
git clone https://github.com/wing-ecommerce/ecommerce-backend.git
cd ecommerce-backend
mvn clean install
mvn spring-boot:run
```

Runs at:  
`http://localhost:8080`

---

📌 **API Endpoints (Important!)**  

**Auth**  
• POST `/api/auth/register` – Register new user  
• POST `/api/auth/login` – Login and get JWT  
• POST `/api/auth/refresh` – Refresh access token  

**Users**  
• GET `/api/users` – Get all users (Admin)  
• GET `/api/users/{id}` – Get user by ID  
• PUT `/api/users/{id}` – Update user (Admin/User)  
• DELETE `/api/users/{id}` – Delete user (Admin)  

**Products**  
• GET `/api/products` – List all products  
• GET `/api/products/{id}` – Get product by ID  
• POST `/api/products` – Create product (Admin)  
• PUT `/api/products/{id}` – Update product (Admin)  
• DELETE `/api/products/{id}` – Delete product (Admin)  

**Orders**  
• GET `/api/orders` – List all orders (Admin) / User-specific orders  
• POST `/api/orders` – Create new order  
• GET `/api/orders/{id}` – Get order by ID  

---

👥 **Roles**  
• **USER** – Can browse products, create orders, view own profile/orders  
• **ADMIN** – Full access: manage products, manage users, view all orders

