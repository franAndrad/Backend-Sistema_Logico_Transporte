# Sistema Logístico de Transporte de Contenedores

## 📑 Índice

- [Descripción del proyecto](#descripción-del-proyecto)
- [🏗️ Arquitectura del Sistema](#️-arquitectura-del-sistema)
- [📚 Documentación](#-documentación)
  - [Documentación Principal](#documentación-principal)
  - [Diagramas](#diagramas)
  - [Configuración y Despliegue](#configuración-y-despliegue)
- [🚀 Inicio Rápido](#-inicio-rápido)
- [🔐 Usuarios de Prueba](#-usuarios-de-prueba)
- [📡 Endpoints Principales](#-endpoints-principales)
- [🧪 Testing](#-testing)
- [Tecnologías utilizadas](#tecnologías-utilizadas)
- [Estructura del proyecto](#estructura-del-proyecto)

---

## Descripción del proyecto
Este proyecto implementa una solución backend basada en microservicios para gestionar un sistema de logística de transporte terrestre de contenedores, desarrollado como Trabajo Práctico Integrador para la asignatura Backend de Aplicaciones (2025).

![Arquitectura del Sistema](./docs/images/arquitectura.png)

---

## 🏗️ Arquitectura del Sistema



```
┌─────────────────────────────────────────────────────────────┐
│                         CLIENTE                              │
│                    (Postman / Frontend)                      │
└────────────────────────┬────────────────────────────────────┘
                         │ JWT Token
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                      API GATEWAY                             │
│                  (Puerto 8080)                               │
│   • Validación JWT                                           │
│   • Autorización por roles                                   │
│   • Enrutamiento a microservicios                           │
└───────┬─────────────────────────┬───────────────────────────┘
        │                         │
        ▼                         ▼
┌───────────────┐         ┌───────────────┐
│  MS-CLIENTE   │         │ MS-TRANSPORTE │
│  (Puerto 8081)│         │  (Puerto 8082)│
│               │         │               │
│ • Clientes    │         │ • Rutas       │
│ • Contenedores│         │ • Tramos      │
│ • Solicitudes │         │ • Camiones    │
│               │         │ • Depósitos   │
│               │         │ • Tarifas     │
└───────────────┘         └───────────────┘

┌─────────────────────────────────────────────────────────────┐
│                      KEYCLOAK                                │
│                  (Puerto 8180)                               │
│   • Proveedor de identidad OAuth2/OIDC                      │
│   • Gestión de usuarios y roles                             │
│   • Emisión de tokens JWT                                   │
└─────────────────────────────────────────────────────────────┘
```

---

## 📚 Documentación

### Documentación Principal

| Documento | Descripción |
|-----------|-------------|
| [📖 Microservicios](docs/microservicios.md) | Arquitectura completa, endpoints y funcionalidades de cada microservicio |
| [🔐 Roles y Seguridad](docs/ROLES_Y_SEGURIDAD.md) | Guía de roles, permisos y matriz de autorización |
| [🔑 Keycloak Explicación](KEYCLOAK_EXPLICACION.md) | Concepto de Single Source of Truth y manejo de usuarios |
| [🔗 Foreign Keys entre Microservicios](docs/FK_entre_microservicios.md) | Manejo de relaciones entre bases de datos separadas |

### Diagramas

- [📊 Diagrama Entidad-Relación](docs/diagrams/ER/entidad_relacion.plantuml) - Modelo de datos completo
- [🏛️ Vista de Despliegue General](docs/diagrams/VDG/arquitectura.puml) - Arquitectura de despliegue

### Configuración y Despliegue

- [🐳 Comandos Docker - Keycloak](docker/nota.md) - Referencia completa de comandos Docker
- [📮 Colección Postman](docs/postman/Sistema_Logistico_Keycloak.postman_collection.json) - Tests automatizados con roles

---

## 🚀 Inicio Rápido

### 1. Levantar Keycloak

```powershell
cd docker
docker compose -f docker-compose.keycloak.yml up -d
```

### 2. Levantar Microservicios

```powershell
# Terminal 1 - MS Cliente
cd microservices/ms-cliente
mvn spring-boot:run

# Terminal 2 - MS Transporte
cd microservices/ms-transporte
mvn spring-boot:run

# Terminal 3 - API Gateway
cd microservices/api-gateway
mvn spring-boot:run
```

### 3. Verificar que todo esté corriendo

```powershell
# Keycloak
Invoke-WebRequest -Uri "http://localhost:8180/realms/logistica" -UseBasicParsing

# Microservicios
Invoke-WebRequest -Uri "http://localhost:8081/actuator/health" -UseBasicParsing
Invoke-WebRequest -Uri "http://localhost:8082/actuator/health" -UseBasicParsing

# Gateway
Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -UseBasicParsing
```

---

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
- **URL Base:** `http://localhost:8080`
- **Health:** `GET /actuator/health`

### 👥 MS-Cliente
- **Clientes:** `/api/v1/clientes/**`
- **Contenedores:** `/api/v1/contenedores/**`
- **Solicitudes:** `/api/v1/solicitudes/**`

### 🚚 MS-Transporte
- **Rutas:** `/api/v1/rutas/**`
- **Tramos:** `/api/v1/tramos/**`
- **Camiones:** `/api/v1/camiones/**`
- **Depósitos:** `/api/v1/depositos/**`
- **Tarifas:** `/api/v1/tarifas/**`

📖 **[Ver documentación completa de endpoints →](docs/microservicios.md)**

---

## 🧪 Testing

### Postman

1. Importa la colección: `docs/postman/Sistema_Logistico_Keycloak.postman_collection.json`
2. Ejecuta los requests de login en la carpeta "🔐 Auth"
3. Los tokens se guardan automáticamente
4. Prueba los endpoints con los roles correspondientes

### PowerShell

```powershell
# Obtener token
$body = @{
    grant_type="password"
    client_id="api-gateway"
    client_secret="gateway-secret"
    username="cliente1"
    password="cliente123"
}
$token = (Invoke-RestMethod -Uri "http://localhost:8180/realms/logistica/protocol/openid-connect/token" -Method Post -Body $body -ContentType "application/x-www-form-urlencoded").access_token

# Usar token en request
$headers = @{ Authorization = "Bearer $token" }
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/clientes/1" -Headers $headers
```

📖 **[Ver guía completa de testing →](docs/ROLES_Y_SEGURIDAD.md#-cómo-probar-con-powershell)**

---

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
- [Ramirez Hernan - ]
- [Villaba Alex - 400249]

Trabajo desarrollado para la asignatura Backend de Aplicaciones - 2025

