# 🧾 CollabForm – Real-Time Collaborative Form Filling

CollabForm is a Spring Boot-based application designed for real-time collaborative form filling. Multiple users can edit different fields simultaneously, with live typing indicators, field locks, and seamless submission. Built for scenarios like surveys, onboarding, and collaborative data entry in teams.

---

## 🚀 Features

- 🔐 **JWT-based Authentication & Authorization**
- 🧑‍🤝‍🧑 **Real-time Field Collaboration** via WebSockets
- 📝 **Field-Level Locking** to prevent data conflicts
- 🔄 **Live Typing Updates** without saving
- 📥 **Drafts, Submission & Archival** of Form Responses
- 📁 **Role-based Access Control** (Admin, User)
- 📚 **REST APIs** for CRUD and collaboration actions

---

## 🛠️ Tech Stack

- **Backend:** Spring Boot, Spring Security, JWT, WebSocket (STOMP)
- **Database:** PostgreSQL / MySQL (Configurable)
- **Messaging:** Spring Messaging (WebSocket/STOMP)
- **Validation:** Jakarta Validation API
- **Others:** Lombok, SLF4J

---

# File Description Inventory
- **docs/**: Contains design documents and diagrams.
- **data/**: Contains H2 DB files.
- **java_template/**: Main application source code and configuration.
  - **pom.xml**: Maven project configuration file.
  - **CollabFormApplication.java**: Main application entry point.
  - **dto/**: Data Transfer Objects for handling requests and responses.
  - **model/**: Domain models representing application data.
  - **repository/**: Interfaces for database access.
  - **security/**: Security configuration and JWT handling.
  - **resources/**: Application properties and configurations.

## ✅ API Endpoints

### 🔐 Authentication

- `POST /api/auth/register` – Register new user
- `POST /api/auth/login` – Login and receive JWT
- `GET /api/auth/user` – Get current logged-in user info

---

### 📄 Form Management

- `POST /api/forms` – Create new form
- `GET /api/forms` – Get all forms
- `DELETE /api/forms/{formId}` – Delete form
- `POST /api/forms/{formId}/fields` – Add fields to form

---

### ✅ Response Handling

#### Fetch & Update

- `GET /api/forms/{formId}/values` – Get field values
- `PUT /api/forms/{formId}/values` – Update a field value
  ```json
  {
    "fieldId": 101,
    "value": "Updated answer"
  }

### 🔐 Locking

- `POST /api/forms/{formId}/fields/{fieldId}/lock`  
  Acquire a lock on a specific field in the form. Optional `?force=true` param allows overriding an existing lock.

- `DELETE /api/forms/{formId}/fields/{fieldId}/lock`  
  Release the lock on a specific field.

- `DELETE /api/forms/{formId}/locks`  
  Release **all field locks** held by the current user for a given form.

---

### 📤 Submission & Archival

- `POST /api/forms/{formId}/submit`  
  Submit the form response. Marks it as finalized.

- `POST /api/forms/{formId}/archive`  
  Archive the form response. Useful for administrative record-keeping.

---

## 🌐 WebSocket Endpoints
Communicate using STOMP/WebSocket clients:

- `/app/form/{formId}/lock` – Request a field lock
- `/app/form/{formId}/typing` – Broadcast typing updates
- `/app/form/{formId}/join` – Join a form session
- `/app/form/{formId}/leave` – Leave a form session

### Example Typing Payload
```json
{
  "fieldId": 123,
  "fieldName": "email",
  "fieldType": "TEXT",
  "value": "example@example.com",
  "userId": 5
}
```
---
## ⚙️ Setup Instructions

### Prerequisites
- Java 17+, Maven, PostgreSQL/MySQL

### Steps
1. Clone the repo:
   ```bash
   git clone https://github.com/your-username/CollabForm.git
   cd CollabForm
   ```
2. Configure Application Properties:

- Open src/main/resources/application.properties or application.yml
- Set your database credentials:
```bash

spring.datasource.url=jdbc:postgresql://localhost:5432/your-db
spring.datasource.username=your-username
spring.datasource.password=your-password
````
- Set JWT and other required properties:
```bash
jwt.secret=your-secret-key
jwt.expiration=3600000
```
3. Run the Project
```bash
mvn spring-boot:run
```
