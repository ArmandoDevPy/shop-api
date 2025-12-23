# 🛒 Shop API – Sistema de Gestión de Órdenes de Compra

API REST desarrollada con **Java 21 y Spring Boot 3.x** para la gestión de usuarios, productos y órdenes de compra en una tienda online.  
Incluye autenticación con **JWT**, control de roles, auditoría automática y pruebas unitarias.

---

## Tecnologías usadas

- Java 21  
- Spring Boot 3.x  
- Spring Security + JWT  
- Spring Data JPA (Hibernate)  
- MySQL 8  
- Maven  
- JUnit 5 + Mockito  

---

## Requisitos previos

- Java **17+** (recomendado Java 21)
- Maven 3.9+
- MySQL 8.x
- Git

**Verificar versiones:**

```bash
java -version
mvn -version
mysql --version
```

---

## Base de datos

**Crear base de datos:**

```sql
CREATE DATABASE shop_db;
```

---

## Configuración

Editar `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/shop_db
    username: root
    password: root
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true

jwt:
  secret: my-super-secret-key
  expiration: 3600000
```

---

## Levantar el proyecto

Desde la raíz del proyecto:

```bash
./mvnw clean spring-boot:run
```

La API estará disponible en:

```
http://localhost:8080
```

---

## Autenticación y roles

- **USER** → puede crear y gestionar sus propias órdenes
- **ADMIN** → puede gestionar productos

---

## Endpoints

### Registro de usuario

```http
POST /auth/register
```

```json
{
  "fullName": "Pedro User",
  "email": "pedro@mail.com",
  "password": "123456"
}
```

### Login

```http
POST /auth/login
```

```json
{
  "email": "pedro@mail.com",
  "password": "123456"
}
```

**Respuesta:**

```json
{
  "token": "JWT_TOKEN"
}
```

### Usuario autenticado

```http
GET /me
Authorization: Bearer <TOKEN>
```

---

## Productos (ADMIN)

### Crear producto

```http
POST /products
Authorization: Bearer <ADMIN_TOKEN>
```

```json
{
  "name": "Laptop",
  "price": 2500,
  "stock": 5
}
```

### Listar productos

```http
GET /products
```

---

## 🧾 Órdenes de compra (USER)

### Crear orden

```http
POST /orders
Authorization: Bearer <USER_TOKEN>
```

```json
{
  "items": [
    { "productId": 3, "quantity": 1 },
    { "productId": 2, "quantity": 1 }
  ]
}
```

### Listar mis órdenes

```http
GET /orders
Authorization: Bearer <USER_TOKEN>
```

### Obtener una orden

```http
GET /orders/{id}
Authorization: Bearer <USER_TOKEN>
```

### Actualizar orden

```http
PUT /orders/{id}
Authorization: Bearer <USER_TOKEN>
```

```json
{
  "items": [
    { "productId": 3, "quantity": 2 }
  ]
}
```

### Eliminar orden

```http
DELETE /orders/{id}
Authorization: Bearer <USER_TOKEN>
```

---

## Reglas de negocio

- Cálculo automático del total de la orden
- Validación de stock
- Devolución de stock al actualizar o eliminar órdenes
- Auditoría automática:
  - `createdAt`, `createdBy`
  - `updatedAt`, `updatedBy`
- Manejo global de errores con `@ControllerAdvice`

---

## Pruebas

**Ejecutar pruebas:**

```bash
./mvnw clean test
```

**Incluye:**

- `OrderServiceImplTest` (Mockito)
- `ShopApiApplicationTests`

---

## Arquitectura

```
Controller → Service → Repository → Database
```
