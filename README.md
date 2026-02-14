# Teespace E-commerce Backend API

📖 **Project Description**  
• RESTful API for an e-commerce system  
• Handles authentication, products, orders, users, roles, categories, addresses, and payment cards  
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
 ├── controller       # REST controllers (Auth, Users, Products, Orders, Categories, Address, Card)
 ├── service          # Business logic and service layer
 ├── repository       # JPA repositories for DB access
 ├── entity           # Database entities (User, Product, Order, Role, Category, Address, Card)
 ├── dto              # Data Transfer Objects for requests/responses
 ├── config           # Security and JWT configuration
 ├── exception        # Custom exceptions and handlers
 └── util             # Utility classes (JWT utils, validation, etc.)
```

---

🔗 **Database Table Relationships**

| Entity     | Relationship                           | Related Entity |
|-----------|----------------------------------------|----------------|
| User      | One-to-Many                             | Order          |
| User      | Many-to-Many                            | Role           |
| User      | One-to-Many                             | Address        |
| User      | One-to-Many                             | Card           |
| Product   | Many-to-Many (via OrderItem)            | Order          |
| Product   | Many-to-One                             | Category       |
| Category  | One-to-Many                             | Product        |
| Order     | Many-to-One                             | User           |
| Order     | One-to-Many (Order contains OrderItems) | OrderItem      |
| OrderItem | Many-to-One                             | Product        |

> Users can have multiple Orders, Addresses, and Cards. Orders contain multiple Products via OrderItem junction table. Products belong to Categories, and Users can have multiple Roles.

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

**Categories**  
• GET `/api/categories` – List all categories  
• GET `/api/categories/{id}` – Get category by ID  
• POST `/api/categories` – Create category (Admin)  
• PUT `/api/categories/{id}` – Update category (Admin)  
• DELETE `/api/categories/{id}` – Delete category (Admin)  

**Orders**  
• GET `/api/orders` – List all orders (Admin) / User-specific orders  
• POST `/api/orders` – Create new order  
• GET `/api/orders/{id}` – Get order by ID  

**Addresses**  
• GET `/api/addresses` – List all addresses  
• GET `/api/addresses/{id}` – Get address by ID  
• POST `/api/addresses` – Create address  
• PUT `/api/addresses/{id}` – Update address  
• DELETE `/api/addresses/{id}` – Delete address  

**Cards**  
• GET `/api/cards` – List all cards  
• GET `/api/cards/{id}` – Get card by ID  
• POST `/api/cards` – Add new card  
• PUT `/api/cards/{id}` – Update card  
• DELETE `/api/cards/{id}` – Delete card  

---

👥 **Roles**  
• **USER** – Can browse products, create orders, manage addresses and cards, view own profile/orders  
• **ADMIN** – Full access: manage products, categories, users, orders, addresses, and cards
