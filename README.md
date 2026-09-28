# E-Commerce Order & Inventory Management REST Service

A backend RESTful API built with **Java** and **Spring Boot** for managing products, customers, inventory stock levels, order processing, and transactional stock synchronization.

---

## 🔑 Key Features

* **Product Catalog Management**: Complete CRUD operations for managing products with categories and prices.
* **Customer Profile Management**: Customer registration and management with unique email constraint validation.
* **Inventory Stock Management**: Thread-safe stock tracking with pessimistic locking (`PESSIMISTIC_WRITE`) to handle concurrent operations safely.
* **Transactional Order Processing**: Atomic order creation with real-time stock validation, item subtotal & order total calculations, and automatic inventory deduction.
* **Order Lifecycle & Cancellation Handling**: Order status state machine (`PENDING` → `CONFIRMED` → `PROCESSING` → `SHIPPED` → `DELIVERED`). Cancelling an order automatically restocks item quantities back to inventory.
* **Centralized Exception Handling**: Global `@RestControllerAdvice` returning structured error responses with HTTP status codes (200, 201, 204, 400, 404, 409, 500).
* **Interactive API Documentation**: Embedded Swagger UI / OpenAPI 3 specification for testing endpoints live.
* **Docker Ready**: Multi-stage `Dockerfile` and `docker-compose.yml` orchestrating Spring Boot and MySQL containers.

---

## 🛠️ Technology Stack

* **Language**: Java 8
* **Framework**: Spring Boot 2.7.18
* **Web**: Spring MVC / REST
* **Persistence & ORM**: Spring Data JPA, Hibernate 5.6
* **Database**: MySQL 8.0 (H2 for in-memory unit/integration tests)
* **Validation**: Hibernate Validator / Bean Validation (`javax.validation`)
* **Documentation**: Springdoc OpenAPI 1.7.0 (Swagger UI)
* **Testing**: JUnit 5, Mockito, MockMvc, Spring Boot Test
* **Containerization**: Docker & Docker Compose
* **Build Tool**: Apache Maven 3.9

---

## 🏗️ Architecture & Package Structure

The application follows a clean layered backend architecture:

```text
Controller Layer (REST Endpoints)
       ↓
Service Layer (Business Logic & Transactions)
       ↓
Repository Layer (Spring Data JPA)
       ↓
Database (MySQL / H2)
```

```text
src/main/java/com/ecommerce
├── config          # OpenAPI & Application Configurations
├── controller      # REST Controllers
├── dto             # Data Transfer Objects (Request/Response)
├── entity          # JPA Domain Entities & Enums
├── exception       # Custom Exceptions & Global Exception Handler
├── repository     # Spring Data JPA Repositories
├── service         # Business Logic & Transaction Management
└── EcommerceApplication.java
```

---

## 🗄️ Database Schema & Relationships

```text
+-------------------+       1:N       +-----------------+
|     CUSTOMERS     |----------------<|     ORDERS      |
+-------------------+                 +-----------------+
| id (PK)           |                         | 1
| name              |                         |
| email (UNIQUE)    |                         | N
| phone             |                 +-----------------+
| created_at        |                 |   ORDER_ITEMS   |
+-------------------+                 +-----------------+
                                      | id (PK)         |
                                      | order_id (FK)   |
+-------------------+       1:1       | product_id (FK) |
|     PRODUCTS      |-----------------| quantity        |
+-------------------+                 | price           |
| id (PK)           |                 +-----------------+
| name              |                         | N
| description       |                         |
| price             |                         | 1
| category          |                 +-----------------+
| created_at        |                 |   INVENTORIES   |
| updated_at        |                 +-----------------+
+-------------------+                 | id (PK)         |
                                      | product_id (FK) |
                                      | quantity        |
                                      | updated_at      |
                                      +-----------------+
```

---

## 📡 API Endpoints

### 1. Product Management (`/api/products`)
| Method | Endpoint | Description | Status Code |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/products` | Create a new product | `201 CREATED` |
| `GET` | `/api/products` | Get list of all products | `200 OK` |
| `GET` | `/api/products/{id}` | Get product details by ID | `200 OK` |
| `PUT` | `/api/products/{id}` | Update product details | `200 OK` |
| `DELETE` | `/api/products/{id}` | Delete product by ID | `204 NO CONTENT` |

### 2. Customer Management (`/api/customers`)
| Method | Endpoint | Description | Status Code |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/customers` | Register a new customer | `201 CREATED` |
| `GET` | `/api/customers` | Get list of all customers | `200 OK` |
| `GET` | `/api/customers/{id}` | Get customer by ID | `200 OK` |
| `PUT` | `/api/customers/{id}` | Update customer profile | `200 OK` |
| `DELETE` | `/api/customers/{id}` | Delete customer by ID | `204 NO CONTENT` |

### 3. Inventory Management (`/api/inventory`)
| Method | Endpoint | Description | Status Code |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/inventory` | Set initial stock quantity | `200 OK` |
| `GET` | `/api/inventory` | Get all inventory records | `200 OK` |
| `GET` | `/api/inventory/product/{productId}` | Get stock for a specific product | `200 OK` |
| `PUT` | `/api/inventory/product/{productId}/add?quantity=N` | Add stock quantity | `200 OK` |
| `PUT` | `/api/inventory/product/{productId}/reduce?quantity=N` | Reduce stock quantity | `200 OK` |

### 4. Order Management (`/api/orders`)
| Method | Endpoint | Description | Status Code |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/orders` | Create order & deduct stock | `201 CREATED` |
| `GET` | `/api/orders` | Get all orders | `200 OK` |
| `GET` | `/api/orders/{id}` | Get order details by ID | `200 OK` |
| `GET` | `/api/orders/customer/{customerId}` | Get orders by customer ID | `200 OK` |
| `PUT` | `/api/orders/{id}/status?status=STATUS` | Update order status | `200 OK` |
| `PUT` | `/api/orders/{id}/cancel` | Cancel order & restock inventory | `200 OK` |

---

## ⚙️ Environment Variables

The application configures sensible defaults for local execution while supporting environment variable overrides:

| Variable | Description | Default |
| :--- | :--- | :--- |
| `DB_HOST` | Database host name | `localhost` |
| `DB_PORT` | Database port | `3306` |
| `DB_NAME` | Database schema name | `ecommerce_db` |
| `DB_USERNAME` | Database username | `root` |
| `DB_PASSWORD` | Database password | `root` |

---

## 🚀 Running Locally

### Prerequisites
* JDK 8+ installed
* Maven installed (or Maven wrapper)
* MySQL database running on `localhost:3306` with database `ecommerce_db` created

### Steps
1. Clone repository:
   ```bash
   git clone https://github.com/skshweta02-crypto/E_commerce.git
   cd E_commerce
   ```
2. Build project:
   ```bash
   mvn clean package
   ```
3. Run Spring Boot Application:
   ```bash
   java -jar target/ecommerce-service-0.0.1-SNAPSHOT.jar
   ```

---

## 🐳 Running with Docker & Docker Compose

Run the entire application stack (Spring Boot API + MySQL 8.0) with a single command:

```bash
docker-compose up --build
```

Access:
* API Base URL: `http://localhost:8080`
* Swagger UI: `http://localhost:8080/swagger-ui.html`

To stop:
```bash
docker-compose down -v
```

---

## 🧪 Running Tests

Execute unit tests and integration tests:

```bash
mvn clean test
```

---

## 📘 Swagger / OpenAPI Documentation

Once the service is running, explore and test endpoints interactively via Swagger UI:

* **Swagger UI Page**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
* **OpenAPI 3 JSON Spec**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

---

## 💡 Example cURL Requests

### 1. Create a Product
```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Wireless Gaming Mouse",
    "description": "Ergonomic RGB mouse",
    "price": 49.99,
    "category": "Peripherals"
  }'
```

### 2. Set Inventory Stock
```bash
curl -X POST http://localhost:8080/api/inventory \
  -H "Content-Type: application/json" \
  -d '{
    "productId": 1,
    "quantity": 100
  }'
```

### 3. Create a Customer
```bash
curl -X POST http://localhost:8080/api/customers \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Alex Smith",
    "email": "alex.smith@example.com",
    "phone": "555-0199"
  }'
```

### 4. Place an Order
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 1,
    "items": [
      {
        "productId": 1,
        "quantity": 2
      }
    ]
  }'
```

---

## 📄 License

Distributed under the Apache 2.0 License.
