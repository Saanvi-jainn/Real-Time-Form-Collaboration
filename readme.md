# Project Summary
The project aims to develop a collaborative form filling application that allows multiple users to edit forms in real-time. This system is designed for teams and organizations that require efficient and synchronized data entry, leveraging technologies such as Spring Boot and WebSocket for seamless interaction and data management.

# Project Module Description
The application consists of several functional modules:
- **Backend**: Built with Spring Boot, it handles API requests, user authentication, and real-time collaboration features.
- **WebSocket Integration**: Enables real-time updates and field-level locking to prevent edit conflicts.
- **Frontend Interface**: A basic user interface for users to interact with the forms.

# Directory Tree
```
.
├── docs
│   ├── collab_form_class_diagram.mermaid
│   ├── collab_form_sequence_diagram.mermaid
│   ├── prd.md
│   └── system_design.md
└── java_template
    ├── pom.xml
    ├── src
    │   └── main
    │       └── java
    │           └── com
    │               └── collabform
    │                   ├── CollabFormApplication.java
    │                   ├── dto
    │                   │   ├── UserDto.java
    │                   │   ├── auth
    │                   │   │   ├── AuthResponse.java
    │                   │   │   ├── LoginRequest.java
    │                   │   │   └── RegisterRequest.java
    │                   │   ├── collaboration
    │                   │   │   └── JoinFormRequest.java
    │                   │   └── form
    │                   │       ├── FormCreateRequest.java
    │                   │       ├── FormFieldRequest.java
    │                   │       ├── FormFieldResponse.java
    │                   │       ├── FormResponse.java
    │                   │       ├── FormShareRequest.java
    │                   │       ├── FormShareResponse.java
    │                   │       └── FormUpdateRequest.java
    │                   ├── model
    │                   │   ├── EditLock.java
    │                   │   ├── FieldType.java
    │                   │   ├── FieldValue.java
    │                   │   ├── Form.java
    │                   │   ├── FormAccess.java
    │                   │   ├── FormField.java
    │                   │   ├── FormResponse.java
    │                   │   ├── ResponseStatus.java
    │                   │   ├── User.java
    │                   │   └── UserRole.java
    │                   ├── repository
    │                   │   ├── EditLockRepository.java
    │                   │   ├── FieldValueRepository.java
    │                   │   ├── FormAccessRepository.java
    │                   │   ├── FormFieldRepository.java
    │                   │   ├── FormRepository.java
    │                   │   └── FormResponseRepository.java
    │                   ├── security
    │                   │   ├── CustomUserDetailsService.java
    │                   │   ├── JwtAuthenticationFilter.java
    │                   │   ├── JwtTokenProvider.java
    │                   │   └── WebSecurityConfig.java
    │                   └── example
    │                       └── Main.java
    └── resources
        └── application.properties
```

# File Description Inventory
- **docs/**: Contains design documents and diagrams.
- **java_template/**: Main application source code and configuration.
  - **pom.xml**: Maven project configuration file.
  - **CollabFormApplication.java**: Main application entry point.
  - **dto/**: Data Transfer Objects for handling requests and responses.
  - **model/**: Domain models representing application data.
  - **repository/**: Interfaces for database access.
  - **security/**: Security configuration and JWT handling.
  - **resources/**: Application properties and configurations.

# Technology Stack
- **Backend**: Spring Boot
- **Real-time Communication**: WebSocket
- **Database**: (Not specified, assumed to be a relational database)
- **Authentication**: JWT (JSON Web Tokens)

# Usage
To set up the project:
1. Install dependencies using Maven.
2. Build the project with the appropriate build command.
3. Run the application using the command to start the Spring Boot application.
