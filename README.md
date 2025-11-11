# Sistema Logístico de Transporte de Contenedores

## Descripción del proyecto
Este proyecto implementa una solución backend basada en microservicios para gestionar un sistema de logística de transporte terrestre de contenedores, desarrollado como Trabajo Práctico Integrador para la asignatura Backend de Aplicaciones (2025).

![Arquitectura del Sistema](./docs/images/arquitectura.png)

## Estructura del proyecto

```
├── microservices/              # Microservicios del sistema
│   ├── api-gateway/            # API Gateway para enrutamiento y seguridad
│   ├── ms-cliente/             # Microservicio de gestión de clientes y contenedores
│   └── ms-transporte/          # Microservicio de gestión de transporte (rutas, camiones, depósitos, seguimiento por estados)
├── docker/                     # Archivos de configuración Docker
│   └── docker-compose.yml      # Configuración para despliegue de todos los servicios
├── docs/                       # Documentación del proyecto
│   ├── images/                 # Imágenes y diagramas
│   └── postman/                # Colecciones de Postman para pruebas
└── ER*.plantuml               # Diagramas de Entidad-Relación
```

## Tecnologías utilizadas

### Backend
- **Java 21**: Lenguaje de programación principal
- **Spring Boot 3.2**: Framework para desarrollo de microservicios
- **Spring Security**: Para seguridad y autenticación
- **Spring Data JPA**: Para acceso a datos y ORM
- **Spring Cloud Gateway**: Para implementación del API Gateway

### Seguridad
- **Keycloak**: Servidor de autenticación y autorización
- **JWT**: Tokens para autenticación entre servicios

### Base de datos
- **PostgreSQL 14**: Sistema de gestión de bases de datos relacional
- **Flyway**: Para migraciones de base de datos

### Documentación
- **Swagger/OpenAPI 3**: Documentación de API
- **SpringDoc**: Generación automática de documentación OpenAPI

### Testing
- **JUnit 5**: Framework de testing
- **Mockito**: Framework de mocking para pruebas unitarias
- **Postman**: Pruebas de integración y end-to-end

### Herramientas de desarrollo
- **Maven**: Gestión de dependencias y construcción
- **Docker**: Containerización de servicios
- **Docker Compose**: Orquestación de contenedores para desarrollo local
- **PlantUML**: Diseño de diagramas UML

### Integración externa
- **Google Maps Directions API**: Para cálculo de rutas y distancias

## Microservicios y responsabilidades

### API Gateway
- Punto único de entrada al sistema
- Enrutamiento de solicitudes a los microservicios correspondientes
- Validación de tokens JWT
- Logging de solicitudes

### Microservicio de Cliente
- Gestión de usuarios y clientes
- Gestión de contenedores
- Solicitudes de transporte
- Consultas de estado de envío

### Microservicio de Transporte
- Gestión de rutas y tramos
- Gestión de camiones y conductores
- Gestión de depósitos
- Cálculo de costos y tiempos
- Seguimiento por estados de solicitudes y tramos

## Ejecución del proyecto

### Requisitos previos
- Java 21
- Docker y Docker Compose
- Maven

### Pasos para ejecutar
1. Clonar el repositorio
2. Ejecutar `mvn clean package -DskipTests` en la carpeta raíz
3. Ejecutar `docker-compose up -d` en la carpeta `/docker`
4. Acceder a la API a través de `http://localhost:8080`
5. Acceder a la documentación de la API a través de `http://localhost:8080/swagger-ui.html`

## Autores
- [Andrade Francisco - 403499]
- [Bottero Constantino - 400892]
- [Ramirez Hernan - 83397]
- [Villaba Alex - 400249]

Trabajo desarrollado para la asignatura Backend de Aplicaciones - 2025
