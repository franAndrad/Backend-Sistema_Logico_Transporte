# Arquitectura del Sistema Logístico de Transporte de Contenedores

Este documento describe en detalle la arquitectura de microservicios del sistema, sus componentes, relaciones y flujos de comunicación.

---

## 📋 Tabla de Contenidos

- [Visión General](#visión-general)
- [Arquitectura de Alto Nivel](#arquitectura-de-alto-nivel)
- [Componentes del Sistema](#componentes-del-sistema)
- [Actores del Sistema](#actores-del-sistema)
- [Microservicios](#microservicios)
- [Bases de Datos](#bases-de-datos)
- [Seguridad y Autenticación](#seguridad-y-autenticación)
- [Comunicación entre Servicios](#comunicación-entre-servicios)
- [Integraciones Externas](#integraciones-externas)
- [Flujos de Trabajo](#flujos-de-trabajo)
- [Patrones de Arquitectura](#patrones-de-arquitectura)
- [Tecnologías Utilizadas](#tecnologías-utilizadas)

---

## 🎯 Visión General

El Sistema Logístico de Transporte de Contenedores está diseñado como una **arquitectura de microservicios** que permite:

- ✅ **Escalabilidad independiente** de cada componente
- ✅ **Despliegue independiente** de servicios
- ✅ **Tolerancia a fallos** mediante aislamiento de servicios
- ✅ **Mantenibilidad** con responsabilidades bien definidas
- ✅ **Flexibilidad tecnológica** para cada microservicio

### Principios de Diseño

1. **Single Responsibility Principle**: Cada microservicio tiene una responsabilidad única y bien definida
2. **API First**: Todos los servicios exponen APIs REST documentadas
3. **Database per Service**: Cada microservicio gestiona su propia base de datos
4. **Stateless Services**: Los servicios no mantienen estado de sesión
5. **Centralized Authentication**: Autenticación centralizada con Keycloak

---

## 🏗️ Arquitectura de Alto Nivel

```
┌──────────────────────────────────────────────────────────────┐
│                         USUARIOS                              │
│  (Cliente, Operador, Transportista)                          │
└────────────────────┬─────────────────────────────────────────┘
                     │ HTTPS
                     ↓
┌────────────────────────────────────────────────────────────────┐
│                      API GATEWAY                               │
│              (Spring Cloud Gateway)                            │
│  • Enrutamiento                                               │
│  • Balanceo de carga                                          │
│  • Rate limiting                                              │
│  • Validación de tokens JWT                                   │
└─────┬──────────────┬──────────────────────────────────────────┘
      │              │
      │              │
      ↓              ↓
┌──────────┐  ┌──────────────────┐
│    MS    │  │       MS         │
│ Cliente  │  │   Transporte     │
└────┬─────┘  └────┬─────────────┘
     │             │ ↓
     ↓             │ Google Maps API
┌─────────┐   ┌───┴─────┐
│   DB    │   │   DB    │
│Cliente  │   │Transport│
└─────────┘   └─────────┘
                  │
                  │ Incluye:
                  │ • Rutas y Tramos
                  │ • Camiones y Depósitos
                  │ • Seguimiento por estados
                  ↓
              Maps API
```

---

## 👥 Actores del Sistema

### 1. **Cliente**
**Rol:** Usuario final que solicita servicios de transporte

**Capacidades:**
- 📦 Registrar contenedores
- 📋 Crear solicitudes de transporte
- 📍 Consultar estado actual de solicitudes
- 🔍 Ver estado actual de sus contenedores
- 💰 Ver costos estimados y finales

**Endpoints principales:**
- `POST /api/v1/clientes` - Registrarse
- `POST /api/v1/contenedores` - Registrar contenedor
- `POST /api/v1/solicitudes` - Crear solicitud de transporte
- `GET /api/v1/solicitudes/{id}/estado` - Ver estado actual

---

### 2. **Operador/Administrador**
**Rol:** Personal interno que gestiona el sistema

**Capacidades:**
- 🚛 Asignar camiones a tramos
- 📊 Gestionar rutas y depósitos
- 💵 Configurar tarifas
- 📈 Ver reportes y estadísticas
- ⚙️ Administrar usuarios del sistema
- 🔍 Monitorear estado de todas las solicitudes

**Endpoints principales:**
- `POST /api/v1/tramos/{id}/asignar-camion` - Asignar recursos
- `PUT /api/v1/tarifas/{id}` - Actualizar tarifas
- `GET /api/v1/reportes/solicitudes` - Ver reportes
- `POST /api/v1/depositos` - Gestionar depósitos
- `GET /api/v1/solicitudes` - Ver todas las solicitudes y sus estados

---

### 3. **Transportista**
**Rol:** Conductor que realiza el transporte físico

**Capacidades:**
- 🚚 Ver tramos asignados
- ✅ Iniciar y finalizar tramos
-  Reportar incidencias
- ⛽ Registrar consumos

**Endpoints principales:**
- `GET /api/v1/tramos/mis-asignaciones` - Ver tramos asignados
- `POST /api/v1/tramos/{id}/iniciar` - Iniciar tramo
- `POST /api/v1/tramos/{id}/finalizar` - Finalizar tramo

---

## 🔧 Componentes del Sistema

### 🌐 API Gateway (Spring Cloud Gateway)

**Responsabilidad:** Punto único de entrada al sistema

**Funciones principales:**
- **Enrutamiento inteligente**: Dirige las peticiones al microservicio correcto
- **Seguridad**: Valida tokens JWT con Keycloak
- **Load Balancing**: Distribuye carga entre instancias
- **Rate Limiting**: Previene abuso del API
- **CORS**: Gestiona políticas de acceso cross-origin
- **Circuit Breaker**: Implementa tolerancia a fallos

**Configuración de rutas:**
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: ms-cliente
          uri: lb://ms-cliente
          predicates:
            - Path=/api/clientes/**,/api/contenedores/**,/api/solicitudes/**
          filters:
            - TokenRelay
            
        - id: ms-transporte
          uri: lb://ms-transporte
          predicates:
            - Path=/api/rutas/**,/api/tramos/**,/api/camiones/**,/api/depositos/**
          filters:
            - TokenRelay
```

**Puerto:** `8080`

---

### 🔐 Keycloak (Sistema Externo - Servidor de Identidad)

**Responsabilidad:** Gestión centralizada de identidad, autenticación y autorización

**⚠️ IMPORTANTE:** Keycloak es un **sistema externo independiente** que **almacena TODOS los datos de usuarios**. NO forma parte de nuestras bases de datos de microservicios.

**Datos que almacena Keycloak:**
- `id` (UUID): Identificador único del usuario
- `username`: Nombre de usuario para login
- `password`: Contraseña (hasheada con bcrypt/argon2)
- `email`: Email del usuario
- `firstName` (nombre): Nombre de pila
- `lastName` (apellido): Apellido
- `enabled`: Si el usuario está activo
- `emailVerified`: Si verificó su email
- `roles`: Roles asignados (cliente, operador, transportista, admin)
- Atributos personalizados (teléfono, etc.)

**Características:**
- **Single Sign-On (SSO)**: Una sola autenticación para todos los servicios
- **OAuth 2.0 / OpenID Connect**: Estándares de autenticación
- **JWT Tokens**: Tokens seguros y auto-contenidos
- **Roles y permisos**: Control de acceso basado en roles (RBAC)
- **Keycloak Admin API**: API REST para gestionar usuarios programáticamente
- **Federación de identidades**: Integración con proveedores externos (Google, Facebook, etc.)

**Roles definidos:**
- `cliente`: Acceso a funcionalidades de cliente
- `operador`: Acceso a gestión administrativa
- `transportista`: Acceso a operaciones de transporte
- `admin`: Acceso completo al sistema

**¿Cómo se integra con nuestros microservicios?**

1. **Nuestros microservicios NO duplican datos de usuario**
   - Solo guardan `keyCloakId` (UUID) como referencia
   - Consultan Keycloak Admin API cuando necesitan datos personales

2. **Validación de tokens JWT**
   - API Gateway valida tokens con la clave pública de Keycloak
   - Los microservicios confían en los tokens ya validados

3. **Consulta de datos de usuario**
   ```java
   // Ejemplo en ms-cliente
   UserRepresentation user = keycloakClient
       .realm("logistica")
       .users()
       .get(keyCloakId)
       .toRepresentation();
   
   String nombre = user.getFirstName();
   String email = user.getEmail();
   List<String> roles = user.getRealmRoles();
   ```

**Flujo de autenticación:**
```
1. Usuario → Login Request → Keycloak
2. Keycloak → Valida credenciales → Genera JWT
3. Keycloak → JWT Token → Usuario
4. Usuario → Request + JWT → API Gateway
5. API Gateway → Valida JWT con Keycloak
6. API Gateway → Enruta a Microservicio
```

**Puerto:** `8180`

---

## 🎯 Microservicios

### 1. 📦 Microservicio Cliente (ms-cliente)

**Responsabilidad:** Gestión de clientes, contenedores y solicitudes de transporte

#### Endpoints principales:

##### Clientes
```
GET    /api/v1/clientes              - Listar todos los clientes
GET    /api/v1/clientes/{id}         - Obtener cliente por ID
POST   /api/v1/clientes              - Crear nuevo cliente
PUT    /api/v1/clientes/{id}         - Actualizar cliente
DELETE /api/v1/clientes/{id}         - Eliminar cliente
```

##### Contenedores
```
GET    /api/v1/contenedores                    - Listar contenedores
GET    /api/v1/contenedores/{id}               - Obtener contenedor
POST   /api/v1/contenedores                    - Crear contenedor
PUT    /api/v1/contenedores/{id}               - Actualizar contenedor
GET    /api/v1/contenedores/cliente/{id}       - Contenedores de un cliente
```

##### Solicitudes
```
GET    /api/v1/solicitudes                     - Listar solicitudes
GET    /api/v1/solicitudes/{id}                - Obtener solicitud
GET    /api/v1/solicitudes/{id}/estado         - Obtener estado actual de solicitud
POST   /api/v1/solicitudes                     - Crear solicitud
PUT    /api/v1/solicitudes/{id}                - Actualizar solicitud
PUT    /api/v1/solicitudes/{id}/estado         - Actualizar estado (uso interno)
GET    /api/v1/solicitudes/cliente/{id}        - Solicitudes de un cliente
POST   /api/v1/solicitudes/{id}/confirmar      - Confirmar solicitud
POST   /api/v1/solicitudes/{id}/cancelar       - Cancelar solicitud
```

#### Comunicación con otros servicios:
- **→ ms-transporte**: Solicita cálculo de costos, creación de rutas y asignación de recursos (Feign Client)
- **← ms-transporte**: Recibe notificaciones de cambio de estado (Feign Client)
- **→ Keycloak**: Consulta datos de usuario via Admin API
- **← API Gateway**: Recibe peticiones de usuarios autenticados

#### Base de datos: **clientedb**

**Entidades gestionadas:**
- `Cliente` (keyCloakId, direccionFacturacion, direccionEnvio, razonSocial, cuit)
- `Contenedor` (peso, volumen, estado, ubicacionActual, activo)
- `Solicitud` (origen, destino, costos, tiempos, estado)

**Referencias lógicas (sin FK física):**
- `Cliente.keyCloakId` → Usuario en Keycloak
- `Solicitud.idTarifa` → Tarifa en ms-transporte

**Puerto:** `8081`

---

### 2. 🚛 Microservicio Transporte (ms-transporte)

**Responsabilidad:** Gestión de tarifas, rutas, tramos, camiones, depósitos e integración con Google Maps API

#### Endpoints principales:

##### Rutas
```
GET    /api/v1/rutas                    - Listar rutas
GET    /api/v1/rutas/{id}               - Obtener ruta
POST   /api/v1/rutas                    - Crear ruta (calculada automáticamente)
GET    /api/v1/rutas/solicitud/{id}     - Ruta de una solicitud
```

##### Tramos
```
GET    /api/v1/tramos                          - Listar tramos
GET    /api/v1/tramos/{id}                     - Obtener tramo
GET    /api/v1/tramos/ruta/{id}                - Tramos de una ruta
POST   /api/v1/tramos/{id}/asignar-camion      - Asignar camión a tramo
POST   /api/v1/tramos/{id}/asignar-transportista - Asignar transportista
POST   /api/v1/tramos/{id}/iniciar             - Iniciar tramo
POST   /api/v1/tramos/{id}/finalizar           - Finalizar tramo
GET    /api/v1/tramos/transportista/{id}       - Tramos de un transportista
```

##### Camiones
```
GET    /api/v1/camiones                        - Listar camiones
GET    /api/v1/camiones/{dominio}              - Obtener camión
POST   /api/v1/camiones                        - Registrar camión
PUT    /api/v1/camiones/{dominio}              - Actualizar camión
GET    /api/v1/camiones/disponibles            - Camiones disponibles
PUT    /api/v1/camiones/{dominio}/disponibilidad - Cambiar disponibilidad
```

##### Depósitos
```
GET    /api/v1/depositos                       - Listar depósitos
GET    /api/v1/depositos/{id}                  - Obtener depósito
POST   /api/v1/depositos                       - Crear depósito
PUT    /api/v1/depositos/{id}                  - Actualizar depósito
GET    /api/v1/depositos/cercanos              - Depósitos cercanos a coordenadas
```

#### Integración con Google Maps API:

Este microservicio incluye la **integración directa con Google Maps API** para:

##### 1. Geocodificación
Convierte direcciones en coordenadas y viceversa.

```java
// Servicio interno de Geocodificación
@Service
public class GeocodingService {
    
    @Value("${google.maps.api-key}")
    private String apiKey;
    
    public CoordenadasDTO geocodificar(String direccion) {
        // Llama a Google Geocoding API
        // Retorna latitud y longitud
    }
    
    public DireccionDTO reverseGeocode(Double lat, Double lng) {
        // Llama a Google Reverse Geocoding API
        // Retorna dirección formateada
    }
}
```

##### 2. Cálculo de Distancias
Calcula distancias y tiempos entre puntos usando Distance Matrix API.

```java
@Service
public class DistanceService {
    
    @Cacheable(value = "distancias", key = "#origen + '-' + #destino")
    public DistanciaDTO calcularDistancia(
            CoordenadasDTO origen, 
            CoordenadasDTO destino) {
        // Llama a Google Distance Matrix API
        // Retorna distancia en km y duración en minutos
    }
    
    public List<DistanciaDTO> calcularDistanciaMultiple(
            List<CoordenadasDTO> origenes,
            List<CoordenadasDTO> destinos) {
        // Calcula matriz de distancias
        // Útil para optimizar selección de depósitos
    }
}
```

##### 3. Cálculo de Rutas
Genera rutas optimizadas usando Directions API.

```java
@Service
public class RouteService {
    
    public RutaOptimizadaDTO calcularRutaOptimizada(
            CoordenadasDTO origen,
            CoordenadasDTO destino,
            List<CoordenadasDTO> puntosIntermedios) {
        // Llama a Google Directions API
        // Retorna ruta optimizada con waypoints
    }
}
```

##### 4. Resiliencia y Optimización

**Caché de consultas:**
```yaml
spring:
  cache:
    cache-names: distancias, geocodificacion
    caffeine:
      spec: maximumSize=1000,expireAfterWrite=24h
```

**Circuit Breaker:**
```java
@CircuitBreaker(name = "googleMaps", fallbackMethod = "calcularDistanciaFallback")
public DistanciaDTO calcularDistancia(CoordenadasDTO origen, CoordenadasDTO destino) {
    // Llama a Google Maps API
}

private DistanciaDTO calcularDistanciaFallback(
        CoordenadasDTO origen, 
        CoordenadasDTO destino, 
        Exception e) {
    // Cálculo usando fórmula de Haversine (distancia en línea recta)
    return calcularDistanciaHaversine(origen, destino);
}
```

**Rate Limiting:**
```yaml
resilience4j:
  ratelimiter:
    instances:
      googleMaps:
        limitForPeriod: 50
        limitRefreshPeriod: 1s
        timeoutDuration: 5s
```

#### Lógica de negocio:

##### Cálculo de rutas:
```java
1. Recibe solicitud con origen y destino
2. Geocodifica direcciones usando Google Maps API interna
3. Consulta depósitos intermedios óptimos
4. Calcula distancias entre puntos usando Google Distance Matrix API
5. Divide ruta en tramos según depósitos
6. Asigna tipo a cada tramo:
   - origen-deposito
   - deposito-deposito
   - deposito-destino
   - origen-destino (directo, sin depósitos)
7. Calcula costos estimados usando Tarifa
8. Retorna ruta completa con tramos
```

##### Asignación de recursos:
```java
1. Buscar camiones disponibles con capacidad suficiente
2. Buscar transportistas disponibles (sin tramos activos)
3. Asignar camión y transportista al tramo
4. Marcar camión como no disponible
5. Actualizar estado del tramo a "asignado"
```

#### Gestión de Estados:

Los estados se gestionan directamente en las entidades sin necesidad de tablas de historial:

**Estados de Solicitud:**
- `BORRADOR` → Solicitud creada, no confirmada
- `PROGRAMADA` → Ruta calculada y confirmada por cliente
- `ASIGNADA` → Recursos asignados (camión y transportista)
- `EN_TRANSITO` → Contenedor en viaje
- `EN_DEPOSITO` → Contenedor en depósito intermedio
- `ENTREGADA` → Contenedor entregado en destino
- `CANCELADA` → Solicitud cancelada

**Estados de Tramo:**
- `PLANIFICADO` → Tramo calculado por el sistema
- `ASIGNADO` → Camión y transportista asignados
- `INICIADO` → Transportista inició el tramo
- `FINALIZADO` → Tramo completado
- `CANCELADO` → Tramo cancelado

**Estados de Contenedor:**
- `EN_ORIGEN` → Contenedor en ubicación de origen
- `EN_TRANSITO` → Contenedor en viaje
- `EN_DEPOSITO` → Contenedor en depósito intermedio
- `ENTREGADO` → Contenedor entregado

#### Comunicación con otros servicios:
- **→ Google Maps API**: Geocodificación, distancias y rutas (integración directa)
- **→ ms-cliente**: Notifica cambios de estado de solicitudes (Feign Client)
- **← ms-cliente**: Recibe solicitudes de cálculo de costos y creación de rutas (Feign Client)
- **→ Keycloak**: Consulta datos de transportistas via Admin API
- **← API Gateway**: Recibe peticiones de operadores, transportistas y clientes autenticados

#### Base de datos: **transportedb**

**Entidades gestionadas:**
- `Tarifa` (concepto, valores, vigencia, activo)
- `Ruta` (idSolicitud, cantidadTramos, distanciaTotal, estado)
- `Tramo` (keyCloakIdTransportista, camión, depósitos, costos, tiempos, estado)
- `Camion` (dominio, capacidades, disponibilidad, costos)
- `Deposito` (ubicación, coordenadas, costos)

**Referencias lógicas (sin FK física):**
- `Ruta.idSolicitud` → Solicitud en ms-cliente
- `Tramo.keyCloakIdTransportista` → Usuario en Keycloak

#### Configuración:
```yaml
# application.yml
google:
  maps:
    api-key: ${GOOGLE_MAPS_API_KEY}
    base-url: https://maps.googleapis.com/maps/api
    timeout: 5s
    max-retries: 3
    
spring:
  cache:
    type: caffeine
    cache-names: distancias, geocodificacion, rutas
    
resilience4j:
  circuitbreaker:
    instances:
      googleMaps:
        slidingWindowSize: 10
        failureRateThreshold: 50
        waitDurationInOpenState: 10s
  
  ratelimiter:
    instances:
      googleMaps:
        limitForPeriod: 50
        limitRefreshPeriod: 1s
```

**Puerto:** `8082`

---

## 🗄️ Bases de Datos

### Arquitectura: Database per Service

Cada microservicio tiene su propia base de datos PostgreSQL, siguiendo el patrón **Database per Service**.

#### Ventajas de este enfoque:

✅ **Independencia**: Cada servicio puede evolucionar su esquema sin afectar otros
✅ **Escalabilidad**: Cada BD puede escalarse independientemente
✅ **Aislamiento de fallos**: Un problema en una BD no afecta a las demás
✅ **Flexibilidad tecnológica**: Cada servicio puede usar la BD más adecuada

#### Desventajas y soluciones:

❌ **Transacciones distribuidas**: No hay ACID entre servicios
✅ **Solución**: Patrón Saga para transacciones distribuidas

❌ **Consultas join entre servicios**: No se puede hacer JOIN entre BDs
✅ **Solución**: API Composition o CQRS con Event Sourcing

---

### 📊 DB Cliente (PostgreSQL - clientedb)

**Nombre de la base de datos:** `clientedb`

**Entidades:**
- `cliente`: Información del cliente (keyCloakId, direcciones, razonSocial, cuit)
- `contenedor`: Contenedores registrados (peso, volumen, estado, ubicación)
- `solicitud`: Solicitudes de transporte (origen, destino, costos, tiempos, estado)

**Referencias lógicas (sin FK física):**
- `cliente.keycloak_id` → Usuario en Keycloak (validación via API HTTP)
- `solicitud.id_tarifa` → Tarifa en ms-transporte (validación via API HTTP)

**Relaciones principales (FK físicas):**
```
cliente 1:N contenedor
cliente 1:N solicitud
contenedor 1:1 solicitud
```

**Índices importantes:**
```sql
CREATE INDEX idx_cliente_keycloak ON cliente(keycloak_id);
CREATE INDEX idx_contenedor_cliente ON contenedor(id_cliente);
CREATE INDEX idx_contenedor_estado ON contenedor(estado);
CREATE INDEX idx_solicitud_cliente ON solicitud(id_cliente);
CREATE INDEX idx_solicitud_contenedor ON solicitud(id_contenedor);
CREATE INDEX idx_solicitud_estado ON solicitud(estado);
CREATE INDEX idx_solicitud_fecha_creacion ON solicitud(fecha_creacion DESC);
```

**Esquema simplificado:**
```sql
CREATE TABLE cliente (
    id_cliente SERIAL PRIMARY KEY,
    keycloak_id VARCHAR(255) UNIQUE NOT NULL, -- UUID de Keycloak
    direccion_facturacion VARCHAR(500),
    direccion_envio VARCHAR(500),
    razon_social VARCHAR(255),
    cuit VARCHAR(20)
);

CREATE TABLE contenedor (
    id_contenedor SERIAL PRIMARY KEY,
    id_cliente INTEGER NOT NULL REFERENCES cliente(id_cliente),
    peso FLOAT NOT NULL,
    volumen FLOAT NOT NULL,
    estado VARCHAR(50) NOT NULL, -- en_origen, en_transito, en_deposito, entregado
    ubicacion_actual VARCHAR(500),
    activo BOOLEAN DEFAULT true
);

CREATE TABLE solicitud (
    id_solicitud SERIAL PRIMARY KEY,
    id_contenedor INTEGER NOT NULL REFERENCES contenedor(id_contenedor),
    id_cliente INTEGER NOT NULL REFERENCES cliente(id_cliente),
    id_tarifa INTEGER, -- Referencia lógica a ms-transporte (nullable)
    origen_direccion VARCHAR(500) NOT NULL,
    origen_latitud DECIMAL(10,8) NOT NULL,
    origen_longitud DECIMAL(11,8) NOT NULL,
    destino_direccion VARCHAR(500) NOT NULL,
    destino_latitud DECIMAL(10,8) NOT NULL,
    destino_longitud DECIMAL(11,8) NOT NULL,
    costo_estimado FLOAT,
    tiempo_estimado FLOAT,
    costo_final FLOAT,
    tiempo_real FLOAT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    estado VARCHAR(50) NOT NULL -- borrador, programada, asignada, en_transito, en_deposito, entregada, cancelada
);
```

**Puerto:** `5432`

---

### 🚛 DB Transporte (PostgreSQL - transportedb)

**Nombre de la base de datos:** `transportedb`

**Entidades:**
- `tarifa`: Configuración de precios (valores, vigencia, activo)
- `ruta`: Rutas completas (idSolicitud, distanciaTotal, estado)
- `tramo`: Segmentos de ruta (keyCloakIdTransportista, camión, depósitos, costos, tiempos)
- `camion`: Vehículos de transporte (dominio, capacidades, disponibilidad)
- `deposito`: Ubicaciones de almacenamiento intermedio (ubicación, coordenadas, costos)

**Referencias lógicas (sin FK física):**
- `ruta.id_solicitud` → Solicitud en ms-cliente (validación via API HTTP)
- `tramo.keycloak_id_transportista` → Usuario en Keycloak (validación via API HTTP)

**Relaciones principales (FK físicas):**
```
ruta 1:N tramo
camion 1:N tramo
deposito 1:N tramo (origen, nullable)
deposito 1:N tramo (destino, nullable)
tarifa 1:N solicitud (referencia lógica desde ms-cliente)
```

**Índices importantes:**
```sql
CREATE INDEX idx_tarifa_activo ON tarifa(activo) WHERE activo = true;
CREATE INDEX idx_tarifa_vigencia ON tarifa(fecha_vigencia DESC);
CREATE INDEX idx_ruta_solicitud ON ruta(id_solicitud);
CREATE INDEX idx_ruta_estado ON ruta(estado);
CREATE INDEX idx_tramo_ruta ON tramo(id_ruta);
CREATE INDEX idx_tramo_estado ON tramo(estado);
CREATE INDEX idx_tramo_camion ON tramo(dominio_camion);
CREATE INDEX idx_tramo_keycloak_transportista ON tramo(keycloak_id_transportista);
CREATE INDEX idx_tramo_fecha_actualizacion ON tramo(fecha_actualizacion DESC);
CREATE INDEX idx_camion_disponibilidad ON camion(disponibilidad) WHERE disponibilidad = true;
CREATE INDEX idx_deposito_ubicacion ON deposito(latitud, longitud);
```

**Esquema simplificado:**
```sql
CREATE TABLE tarifa (
    id_tarifa SERIAL PRIMARY KEY,
    concepto VARCHAR(255) NOT NULL,
    valor_base FLOAT NOT NULL,
    valor_por_km FLOAT NOT NULL,
    valor_por_peso FLOAT NOT NULL,
    valor_por_volumen FLOAT NOT NULL,
    valor_por_tramo FLOAT NOT NULL,
    valor_litro_combustible FLOAT NOT NULL,
    fecha_vigencia DATE NOT NULL,
    activo BOOLEAN DEFAULT true
);

CREATE TABLE ruta (
    id_ruta SERIAL PRIMARY KEY,
    id_solicitud INTEGER NOT NULL, -- Referencia lógica a ms-cliente
    cantidad_tramos INTEGER DEFAULT 0,
    cantidad_depositos INTEGER DEFAULT 0,
    distancia_total FLOAT,
    costo_total FLOAT,
    estado VARCHAR(50) NOT NULL -- estimada, asignada, en_progreso, completada
);

CREATE TABLE camion (
    dominio VARCHAR(20) PRIMARY KEY,
    capacidad_peso FLOAT NOT NULL,
    capacidad_volumen FLOAT NOT NULL,
    disponibilidad BOOLEAN DEFAULT true,
    costo_base_km FLOAT NOT NULL,
    consumo_combustible FLOAT NOT NULL
);

CREATE TABLE deposito (
    id_deposito SERIAL PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    direccion VARCHAR(500) NOT NULL,
    latitud DECIMAL(10,8) NOT NULL,
    longitud DECIMAL(11,8) NOT NULL,
    costo_estadia_diario FLOAT NOT NULL
);

CREATE TABLE tramo (
    id_tramo SERIAL PRIMARY KEY,
    id_ruta INTEGER NOT NULL REFERENCES ruta(id_ruta),
    id_deposito_origen INTEGER REFERENCES deposito(id_deposito),
    id_deposito_destino INTEGER REFERENCES deposito(id_deposito),
    id_tarifa INTEGER NOT NULL REFERENCES tarifa(id_tarifa),
    keycloak_id_transportista VARCHAR(255), -- UUID de Keycloak (nullable)
    dominio_camion VARCHAR(20) REFERENCES camion(dominio),
    tipo VARCHAR(50) NOT NULL, -- origen-deposito, deposito-deposito, deposito-destino, origen-destino
    estado VARCHAR(50) NOT NULL, -- planificado, asignado, iniciado, finalizado, cancelado
    distancia FLOAT NOT NULL,
    costo_aproximado FLOAT,
    costo_real FLOAT,
    fecha_hora_inicio_estimada TIMESTAMP,
    fecha_hora_fin_estimada TIMESTAMP,
    fecha_hora_inicio TIMESTAMP,
    fecha_hora_fin TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**Consideraciones de rendimiento:**
- Estados actuales en columnas directas (rápido acceso)
- Índices en campos de estado para consultas frecuentes
- Índices geoespaciales para búsqueda de depósitos cercanos
- Caché de tarifas activas

**Puerto:** `5433`

---

## 🔐 Seguridad y Autenticación

### Flujo de Autenticación Completo

```
1. REGISTRO
   Usuario → Frontend → Keycloak
   Keycloak → Valida datos
   Keycloak → Crea usuario (username, password, email, nombre, apellido)
   Keycloak → Genera UUID único
   Keycloak → Asigna rol (cliente/operador/transportista)
   Keycloak → Retorna UUID
   
   Frontend → ms-cliente (si rol = cliente)
   ms-cliente → Crea Cliente en BD (solo keyCloakId + datos de negocio)
   
2. LOGIN
   Usuario → Login Form → Keycloak
   Keycloak → Valida credenciales
   Keycloak → Genera JWT Token (incluye roles, email, nombre, UUID)
   Keycloak → Retorna Token + Refresh Token
   Frontend → Guarda token en localStorage/sessionStorage
   
3. ACCESO A RECURSOS
   Usuario → Request + JWT → API Gateway
   API Gateway → Valida JWT con clave pública de Keycloak
   API Gateway → Extrae roles del token
   API Gateway → Verifica permisos
   API Gateway → Enruta a Microservicio
   Microservicio → Confía en token (ya validado por Gateway)
   Microservicio → Procesa request
   Microservicio → Response → Usuario
   
4. CONSULTA DE DATOS DE USUARIO
   Microservicio → Necesita nombre/email de usuario
   Microservicio → Extrae keyCloakId de la BD
   Microservicio → Llama a Keycloak Admin API
   Keycloak → Retorna datos del usuario
   Microservicio → Combina con datos de negocio
   Microservicio → Response completo → Usuario
   
5. TOKEN EXPIRADO
   Usuario → Request + JWT expirado → API Gateway
   API Gateway → Detecta token expirado (401)
   Frontend → Refresh Token → Keycloak
   Keycloak → Genera nuevo JWT
   Frontend → Reintenta request con nuevo JWT
```

### Estructura del JWT Token

```json
{
  "header": {
    "alg": "RS256",
    "typ": "JWT",
    "kid": "keycloak-key-id"
  },
  "payload": {
    "exp": 1697388000,
    "iat": 1697384400,
    "jti": "uuid-token-id",
    "iss": "http://localhost:8180/realms/logistica",
    "sub": "user-uuid",
    "typ": "Bearer",
    "azp": "logistica-app",
    "session_state": "session-uuid",
    "preferred_username": "juanperez",
    "email": "juan@example.com",
    "email_verified": true,
    "name": "Juan Pérez",
    "given_name": "Juan",
    "family_name": "Pérez",
    "realm_access": {
      "roles": ["cliente", "user"]
    },
    "resource_access": {
      "logistica-app": {
        "roles": ["cliente"]
      }
    }
  },
  "signature": "..."
}
```

### Matriz de Permisos

| Endpoint | Cliente | Operador | Transportista | Público |
|----------|---------|----------|---------------|---------|
| POST /api/v1/clientes | ✅ | ✅ | ❌ | ✅ (registro) |
| GET /api/v1/clientes | ❌ | ✅ | ❌ | ❌ |
| POST /api/v1/solicitudes | ✅ | ✅ | ❌ | ❌ |
| GET /api/v1/solicitudes | ✅ (sus solicitudes) | ✅ (todas) | ❌ | ❌ |
| GET /api/v1/solicitudes/{id}/estado | ✅ (si es suya) | ✅ | ✅ (si asignado) | ❌ |
| POST /api/v1/tramos/{id}/asignar-camion | ❌ | ✅ | ❌ | ❌ |
| POST /api/v1/tramos/{id}/iniciar | ❌ | ❌ | ✅ | ❌ |
| POST /api/v1/tramos/{id}/finalizar | ❌ | ❌ | ✅ | ❌ |

## 🔄 Comunicación entre Servicios

### Patrón de Comunicación: HTTP REST Síncrono

Los microservicios se comunican mediante llamadas HTTP REST síncronas usando **RestTemplate** o **WebClient** (Spring WebFlux).

#### Ventajas:
✅ Simplicidad de implementación
✅ Fácil debugging
✅ Request-response inmediato
✅ Ideal para operaciones que requieren respuesta inmediata

#### Desventajas:
❌ Acoplamiento temporal (ambos servicios deben estar disponibles)
❌ Aumento de latencia por llamadas en cadena
❌ Propagación de fallos

### Implementación de Comunicación

#### Ejemplo: ms-cliente llama a ms-transporte

```java
@Component
@RequiredArgsConstructor
public class TransporteClient {
    
    private final RestTemplate restTemplate;
    
    @Value("${microservices.transporte.url}")
    private String transporteUrl;
    
    @CircuitBreaker(name = "transporte", fallbackMethod = "crearRutaFallback")
    @Retry(name = "transporte")
    public RutaDTO crearRuta(SolicitudDTO solicitud) {
        String url = transporteUrl + "/api/v1/rutas";
        
        RutaRequestDTO request = RutaRequestDTO.builder()
            .idSolicitud(solicitud.getIdSolicitud())
            .origenDireccion(solicitud.getOrigenDireccion())
            .destinoDireccion(solicitud.getDestinoDireccion())
            .pesoContenedor(solicitud.getPesoContenedor())
            .volumenContenedor(solicitud.getVolumenContenedor())
            .build();
        
        ResponseEntity<RutaDTO> response = restTemplate.postForEntity(
            url, 
            request, 
            RutaDTO.class
        );
        
        return response.getBody();
    }
    
    private RutaDTO crearRutaFallback(SolicitudDTO solicitud, Exception e) {
        log.error("Error al crear ruta para solicitud {}: {}", 
            solicitud.getIdSolicitud(), e.getMessage());
        
        // Retornar ruta estimada sin cálculo exacto
        return RutaDTO.builder()
            .idSolicitud(solicitud.getIdSolicitud())
            .estado("ESTIMADA_PENDIENTE")
            .distanciaTotal(0.0)
            .build();
    }
}
```

### Resiliencia con Resilience4j

#### Circuit Breaker Pattern

Previene cascadas de fallos cortando llamadas a servicios que fallan repetidamente.

```yaml
resilience4j:
  circuitbreaker:
    instances:
      transporte:
        slidingWindowSize: 10
        failureRateThreshold: 50
        waitDurationInOpenState: 10s
        permittedNumberOfCallsInHalfOpenState: 3
        automaticTransitionFromOpenToHalfOpenEnabled: true
```

**Estados del Circuit Breaker:**
- **CLOSED**: Funcionamiento normal, todas las llamadas se ejecutan
- **OPEN**: Demasiados fallos, se cortan las llamadas y se ejecuta fallback
- **HALF_OPEN**: Prueba si el servicio se recuperó

#### Retry Pattern

Reintenta operaciones que fallan temporalmente.

```yaml
resilience4j:
  retry:
    instances:
      transporte:
        maxAttempts: 3
        waitDuration: 1s
        exponentialBackoffMultiplier: 2
        retryExceptions:
          - org.springframework.web.client.ResourceAccessException
          - java.net.SocketTimeoutException
```

---

## 🌍 Integraciones Externas

### Google Maps API (Integrada en ms-transporte)

La integración con Google Maps API está implementada **directamente en el microservicio de Transporte**, no como un microservicio separado.

**Razones de esta decisión:**
- ✅ Reduce latencia al eliminar un salto de red
- ✅ Simplifica la arquitectura
- ✅ Solo ms-transporte requiere funcionalidades de mapas
- ✅ Facilita el manejo de caché y rate limiting en un solo lugar

#### Servicios de Google Maps utilizados:

##### 1. Geocoding API
Convierte direcciones en coordenadas y viceversa.

**Uso:**
- Cuando el cliente ingresa direcciones de origen/destino
- Para validar y estandarizar direcciones
- Para obtener coordenadas exactas de depósitos

**Ejemplo:**
```http
GET https://maps.googleapis.com/maps/api/geocode/json?
    address=Av.+Corrientes+1234,+Buenos+Aires
    &key=YOUR_API_KEY

Response:
{
  "results": [{
    "formatted_address": "Av. Corrientes 1234, Buenos Aires, Argentina",
    "geometry": {
      "location": {
        "lat": -34.603722,
        "lng": -58.381592
      }
    }
  }]
}
```

##### 2. Distance Matrix API
Calcula distancias y tiempos entre múltiples puntos.

**Uso:**
- Calcular distancias entre depósitos
- Estimar tiempos de viaje
- Optimizar selección de depósitos intermedios

**Ejemplo:**
```http
GET https://maps.googleapis.com/maps/api/distancematrix/json?
    origins=-34.603722,-58.381592|-31.416668,-64.183334
    &destinations=-32.889459,-68.845839|-34.920883,-57.954526
    &key=YOUR_API_KEY

Response:
{
  "rows": [
    {
      "elements": [
        {
          "distance": { "value": 1045267, "text": "1,045 km" },
          "duration": { "value": 38160, "text": "10 hours 36 mins" },
          "status": "OK"
        }
      ]
    }
  ]
}
```

##### 3. Directions API
Genera rutas optimizadas con waypoints.

**Uso:**
- Generar rutas optimizadas entre puntos
- Calcular rutas con paradas intermedias
- Obtener instrucciones de navegación

**Ejemplo:**
```http
GET https://maps.googleapis.com/maps/api/directions/json?
    origin=Buenos+Aires
    &destination=Mendoza
    &waypoints=optimize:true|Rosario|Córdoba
    &key=YOUR_API_KEY

Response:
{
  "routes": [{
    "legs": [
      {
        "distance": { "value": 298000, "text": "298 km" },
        "duration": { "value": 10800, "text": "3 hours" },
        "start_address": "Buenos Aires, Argentina",
        "end_address": "Rosario, Santa Fe, Argentina"
      }
    ],
    "waypoint_order": [0, 1]
  }]
}
```

#### Estrategias de Optimización:

##### 1. Caché Agresivo
```java
@Cacheable(value = "distancias", key = "#origen + '-' + #destino")
public DistanciaDTO calcularDistancia(String origen, String destino) {
    // Solo consulta Google Maps si no está en caché
    // Caché válido por 24 horas
}
```

##### 2. Circuit Breaker y Fallback
```java
@CircuitBreaker(name = "googleMaps", fallbackMethod = "calcularDistanciaFallback")
public DistanciaDTO calcularDistancia(CoordenadasDTO origen, CoordenadasDTO destino) {
    // Llama a Google Maps API
}

private DistanciaDTO calcularDistanciaFallback(
        CoordenadasDTO origen, 
        CoordenadasDTO destino, 
        Exception e) {
    // Cálculo aproximado usando fórmula de Haversine
    return calcularDistanciaHaversine(origen, destino);
}
```

##### 3. Rate Limiting
```yaml
resilience4j:
  ratelimiter:
    instances:
      googleMaps:
        limitForPeriod: 50    # 50 requests
        limitRefreshPeriod: 1s # por segundo
        timeoutDuration: 5s
```

##### 4. Agrupación de Requests
```java
// En lugar de múltiples llamadas individuales, usar Distance Matrix
List<DistanciaDTO> distancias = distanceService.calcularDistanciaMultiple(
    origenes,  // Lista de puntos de origen
    destinos   // Lista de puntos de destino
);
// Una sola llamada para calcular múltiples distancias
```

#### Gestión de Costos:

**Precios aproximados (2025):**
- Geocoding: $5 por 1,000 requests
- Distance Matrix: $5-10 por 1,000 elements
- Directions: $5 por 1,000 requests

**Cuota gratuita:** $200/mes de crédito

**Estrategias de ahorro:**
1. Caché de 24 horas para geocodificaciones
2. Caché de 12 horas para distancias
3. Agrupación de requests (Distance Matrix)
4. Fallback a cálculos locales cuando sea posible
5. Rate limiting interno para evitar sobrecostos

---

## 🔄 Flujos de Trabajo

### Flujo 1: Registro y Solicitud de Transporte

```
┌─────────┐
│ Cliente │
└────┬────┘
     │
     │ 1. POST /api/v1/clientes (registro)
     ↓
┌────────────────┐      ┌──────────┐
│  API Gateway   │─────→│ Keycloak │
└────────┬───────┘      └──────────┘
         │                    │
         │                    │ 2. Crear usuario + rol
         │                    │
         │ 3. POST /clientes  │
         ↓                    ↓
    ┌─────────────┐    ┌──────────┐
    │ ms-cliente  │    │ Usuario  │
    └─────┬───────┘    └──────────┘
          │
          │ 4. Guardar cliente en BD
          ↓
    ┌────────────┐
    │ DB Cliente │
    └────────────┘
    
    ... Cliente inicia sesión ...
    
┌─────────┐
│ Cliente │ (autenticado)
└────┬────┘
     │
     │ 5. POST /api/v1/contenedores
     ↓
┌────────────────┐
│  API Gateway   │ (valida JWT)
└────────┬───────┘
         │
         │ 6. POST /contenedores
         ↓
    ┌─────────────┐
    │ ms-cliente  │
    └─────┬───────┘
          │
          │ 7. Guardar contenedor
          ↓
    ┌────────────┐
    │ DB Cliente │
    └────────────┘
    
    ... Cliente crea solicitud ...
    
┌─────────┐
│ Cliente │
└────┬────┘
     │
     │ 8. POST /api/v1/solicitudes
     │    {
     │      "idContenedor": 1,
     │      "origenDireccion": "Av. Corrientes 1234, Buenos Aires",
     │      "destinoDireccion": "Av. San Martín 567, Mendoza"
     │    }
     ↓
┌────────────────┐
│  API Gateway   │
└────────┬───────┘
         │
         │ 9. POST /solicitudes
         ↓
    ┌─────────────┐
    │ ms-cliente  │
    └─────┬───────┘
          │
          │ 10. POST /api/v1/rutas (solicitar cálculo)
          ↓
    ┌──────────────┐      ┌──────────┐
    │ ms-transporte│─────→│  Google  │
    └──────┬───────┘      │   Maps   │
           │              └──────────┘
           │ 11. Geocodifica direcciones (interno)
           │ 12. Busca depósitos cercanos
           │ 13. Calcula distancias con Google Maps
           │ 14. Crea tramos óptimos
           │ 15. Calcula costos con Tarifa
           ↓
    ┌──────────────┐
    │ DB Transporte│
    └──────────────┘
           │
           │ 16. Retorna ruta con costo estimado
           │
    ┌─────────────┐
    │ ms-cliente  │
    └─────┬───────┘
          │
          │ 17. Actualiza solicitud con costo
          │     (estado: PROGRAMADA)
          ↓
    ┌────────────┐
    │ DB Cliente │
    └────────────┘
```

---

### Flujo 2: Asignación y Transporte

```
┌──────────┐
│ Operador │
└────┬─────┘
     │
     │ 1. POST /api/v1/tramos/{id}/asignar-camion
     ↓
┌────────────────┐
│  API Gateway   │
└────────┬───────┘
         │
         ↓
    ┌──────────────┐
    │ ms-transporte│
    └──────┬───────┘
           │
           │ 2. Asignar camión y transportista
           │ 3. Marcar camión como NO disponible
           │ 4. Estado tramo = ASIGNADO
           │ 5. Registrar evento de seguimiento
           ↓
    ┌──────────────┐
    │ DB Transporte│
    └──────────────┘

┌──────────────┐
│ Transportista│
└──────┬───────┘
       │
       │ 6. POST /api/v1/tramos/{id}/iniciar
       ↓
    ┌──────────────┐
    │ ms-transporte│
    └──────┬───────┘
           │
           │ 7. Tramo.estado = INICIADO
           │ 8. Actualizar Solicitud.estado = EN_TRANSITO
           ↓
    ┌──────────────┐
    │ DB Transporte│
    └──────────────┘

┌──────────────┐
│ Transportista│
└──────┬───────┘
       │
       │ 10. POST /api/v1/tramos/{id}/finalizar
       ↓
    ┌──────────────┐
    │ ms-transporte│
    └──────┬───────┘
           │
           │ 11. Tramo.estado = FINALIZADO
           │ 12. Calcular costo real
           │ 13. Liberar camión (disponible = true)
           │ 14. Actualizar estado de Solicitud según lógica
           ↓
    ┌──────────────┐
    │ DB Transporte│
    └──────────────┘
           │
           │ 15. Si es último tramo:
           │     PUT /api/v1/solicitudes/{id}/estado (ENTREGADA)
           ↓
    ┌─────────────┐
    │ ms-cliente  │
    └─────┬───────┘
          │
          │ 16. Estado = ENTREGADA
          │ 17. Calcular costo final
          ↓
    ┌────────────┐
    │ DB Cliente │
    └────────────┘
```

---

## 🏛️ Patrones de Arquitectura

### 1. API Gateway Pattern

**Propósito:** Punto único de entrada

**Beneficios:**
- Simplifica el cliente
- Centraliza autenticación
- Facilita monitoreo
- Rate limiting global

**Implementación:** Spring Cloud Gateway

---

### 2. Database per Service

**Propósito:** Cada microservicio con su BD

**Beneficios:**
- Independencia de esquemas
- Escalabilidad independiente
- Aislamiento de fallos

**Desafíos:**
- Consultas distribuidas (API Composition)
- Transacciones distribuidas (Saga Pattern)

---

### 3. Circuit Breaker Pattern

**Propósito:** Prevenir cascadas de fallos

**Estados:**
- **Closed**: Normal
- **Open**: Servicio caído, ejecuta fallback
- **Half-Open**: Prueba recuperación

**Implementación:** Resilience4j

---

### 4. Retry Pattern

**Propósito:** Reintentar operaciones transitorias

**Configuración:**
- Reintentos exponenciales
- Límite de intentos
- Solo para errores temporales

---

### 5. Caché Pattern

**Propósito:** Reducir latencia y costos

**Aplicación en el sistema:**
- Geocodificación (24h)
- Distancias (12h)
- Configuraciones (1h)

**Implementación:** Caffeine Cache

---

## 🛠️ Tecnologías Utilizadas

### Backend

| Tecnología | Versión | Propósito |
|------------|---------|-----------|
| Java | 21 | Lenguaje de programación |
| Spring Boot | 3.2+ | Framework de microservicios |
| Spring Cloud Gateway | 4.x | API Gateway |
| Spring Data JPA | 3.x | Persistencia de datos |
| Spring Security | 6.x | Seguridad y autenticación |
| Keycloak | 22.x | Servidor de autenticación |
| PostgreSQL | 14+ | Base de datos relacional |
| Flyway | 9.x | Migraciones de BD |
| Resilience4j | 2.x | Circuit Breaker, Retry, Rate Limiting |
| Caffeine | 3.x | Caché en memoria |
| Lombok | 1.18+ | Reducción de boilerplate |
| MapStruct | 1.5+ | Mapeo de objetos |
| SpringDoc OpenAPI | 2.x | Documentación API (Swagger) |

### Herramientas de Desarrollo

| Herramienta | Propósito |
|-------------|-----------|
| Maven | Gestión de dependencias |
| Git | Control de versiones |
| Docker | Containerización |
| Docker Compose | Orquestación local |
| Postman | Testing de APIs |
| IntelliJ IDEA / VS Code | IDEs |
| DBeaver / pgAdmin | Gestión de BD |

### Integraciones Externas

| Servicio | Propósito | Integrado en |
|----------|-----------|--------------|
| Google Maps Geocoding API | Direcciones ↔ Coordenadas | ms-transporte |
| Google Maps Distance Matrix API | Calcular distancias | ms-transporte |
| Google Maps Directions API | Rutas optimizadas | ms-transporte |

---

## 📈 Métricas y Monitoreo (Futuro)

### Herramientas recomendadas:

| Herramienta | Propósito |
|-------------|-----------|
| Prometheus | Recolección de métricas |
| Grafana | Dashboards visuales |
| ELK Stack | Logging centralizado |
| Zipkin / Jaeger | Tracing distribuido |

### Métricas clave a monitorear:

- **API Gateway**: Requests/s, latencia, errores
- **ms-cliente**: Solicitudes creadas, clientes activos
- **ms-transporte**: Rutas calculadas, uso de Google Maps API, caché hit ratio, eventos de seguimiento
- **Bases de datos**: Conexiones, queries lentas, uso de disco

---

## 🚀 Próximos Pasos

### Mejoras planificadas:

1. **Service Discovery**: Implementar Eureka o Consul
2. **Event-Driven**: Kafka/RabbitMQ para comunicación asíncrona
3. **CQRS**: Separar lecturas y escrituras en módulo de seguimiento
4. **Saga Pattern**: Transacciones distribuidas
5. **API Versioning**: Versionado de APIs (v1, v2)
6. **Rate Limiting**: Límites por usuario/plan
7. **Health Checks**: Endpoints de salud avanzados
8. **Blue-Green Deployment**: Despliegues sin downtime

---

## 📚 Referencias

- [Spring Cloud Gateway Documentation](https://spring.io/projects/spring-cloud-gateway)
- [Keycloak Documentation](https://www.keycloak.org/documentation)
- [Resilience4j Documentation](https://resilience4j.readme.io/)
- [Google Maps Platform Documentation](https://developers.google.com/maps/documentation)
- [Microservices Patterns (Chris Richardson)](https://microservices.io/patterns/index.html)
- [Spring Boot Best Practices](https://spring.io/guides)

---

## 👥 Autores

- [Andrade Francisco - 403499]
- []
- []
- []

**Trabajo Práctico Integrador - Backend de Aplicaciones 2025**
