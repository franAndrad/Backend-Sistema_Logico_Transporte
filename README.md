# Sistema Logístico de Transporte de Contenedores

## 📑 Índice

- [Descripción del proyecto](#descripción-del-proyecto)
- [🏗️ Arquitectura del Sistema](#️-arquitectura-del-sistema)
- [🚀 Inicio Rápido](#-inicio-rápido)
- [🔐 Usuarios de Prueba](#-usuarios-de-prueba)
- [📡 Endpoints Principales](#-endpoints-principales)
- [Tecnologías utilizadas](#tecnologías-utilizadas)
- [Requisitos](#ejecución-proyecto)

---

## Descripción del proyecto
Este proyecto implementa una solución backend basada en microservicios para gestionar un sistema de logística de transporte terrestre de contenedores, desarrollado como Trabajo Práctico Integrador para la asignatura Backend de Aplicaciones (2025).

---

## 🏗️ Arquitectura del Sistema

<p align="center">
  <img src="./docs/diagrams/VDG/out/arquitectura/arquitectura.relese.png" alt="Arquitectura del Sistema" width="80%">
</p>


---

### Diagramas

- [📊 Diagrama Entidad-Relación](docs/diagrams/ER/entidad_relacion.plantuml) - Modelo de datos completo
- [🏛️ Vista de Despliegue General](docs/diagrams/VDG/arquitectura.relese.puml) - Arquitectura de despliegue


## 🚀 Inicio Rápido

### 2. Crear Imagenes

```powershell
# Terminal 1 - MS Cliente
cd microservices/ms-cliente
mvn package

# Terminal 2 - MS Transporte
cd microservices/ms-transporte
mvn package

# Terminal 3 - API Gateway
cd microservices/api-gateway
mvn package
```
### 1. Levantar Docker

```powershell
docker compose up -d --build
```


## 🔐 Usuarios de Prueba

| Usuario | Password | Rol | Descripción |
|---------|----------|-----|-------------|
| `cliente1` | `cliente123` | **CLIENTE** | Gestiona sus contenedores y solicitudes |
| `operador1` | `operador123` | **OPERADOR** | Gestiona clientes, rutas, camiones |
| `transportista1` | `transportista123` | **TRANSPORTISTA** | Inicia/finaliza tramos |
| `admin` | `admin123` | **ADMIN** | Acceso total + eliminaciones |

---

## 📡 Endpoints Principales

### 🌐 API Gateway
- **URL Base:** `https://localhost:8443`

📖 **[Ver documentación completa de endpoints →](docs/microservicios.md)**

### Postman
1. Importa la colección: `./postman/tpi_postman_config`
2. Prueba los endpoints con los roles correspondientes

---

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

## Autores
- [Andrade Francisco - 403499]
- [Bottero Constantino - 400892]
- [Ramirez Hernan - 83397]
- [Villaba Alex - 400249]

Trabajo desarrollado para la asignatura Backend de Aplicaciones - 2025
