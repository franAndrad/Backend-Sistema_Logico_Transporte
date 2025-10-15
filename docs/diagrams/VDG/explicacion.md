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
└─────┬──────────────┬──────────────┬──────────────┬────────────┘
      │              │              │              │
      │              │              │              │
      ↓              ↓              ↓              ↓
┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐
│    MS    │  │    MS    │  │    MS    │  │    MS    │
│ Cliente  │  │Transport │  │Seguimien │  │   Maps   │
└────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘
     │             │             │             │
     ↓             ↓             ↓             ↓
┌─────────┐   ┌─────────┐   ┌─────────┐      ↓
│   DB    │   │   DB    │   │   DB    │   Google
│Cliente  │   │Transport│   │Seguim.  │   Maps API
└─────────┘   └─────────┘   └─────────┘
```

---

## 👥 Actores del Sistema

### 1. **Cliente**
**Rol:** Usuario final que solicita servicios de transporte

**Capacidades:**
- 📦 Registrar contenedores
- 📋 Crear solicitudes de transporte
- 📍 Consultar estado de solicitudes
- 🔍 Hacer seguimiento en tiempo real de sus contenedores
- 💰 Ver costos estimados y finales

**Endpoints principales:**
- `POST /api/v1/clientes` - Registrarse
- `POST /api/v1/contenedores` - Registrar contenedor
- `POST /api/v1/solicitudes` - Crear solicitud de transporte
- `GET /api/v1/solicitudes/{id}/seguimiento` - Ver seguimiento

---

### 2. **Operador/Administrador**
**Rol:** Personal interno que gestiona el sistema

**Capacidades:**
- 🚛 Asignar camiones a tramos
- 📊 Gestionar rutas y depósitos
- 💵 Configurar tarifas
- 📈 Ver reportes y estadísticas
- ⚙️ Administrar usuarios del sistema

**Endpoints principales:**
- `POST /api/v1/tramos/{id}/asignar-camion` - Asignar recursos
- `PUT /api/v1/tarifas/{id}` - Actualizar tarifas
- `GET /api/v1/reportes/solicitudes` - Ver reportes
- `POST /api/v1/depositos` - Gestionar depósitos

---

### 3. **Transportista**
**Rol:** Conductor que realiza el transporte físico

**Capacidades:**
- 🚚 Ver tramos asignados
- ✅ Iniciar y finalizar tramos
- 📍 Actualizar ubicación en tiempo real
- 📝 Reportar incidencias
- ⛽ Registrar consumos

**Endpoints principales:**
- `GET /api/v1/tramos/mis-asignaciones` - Ver tramos asignados
- `POST /api/v1/tramos/{id}/iniciar` - Iniciar tramo
- `POST /api/v1/tramos/{id}/finalizar` - Finalizar tramo
- `POST /api/v1/seguimiento` - Actualizar ubicación

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
            
        - id: ms-seguimiento
          uri: lb://ms-seguimiento
          predicates:
            - Path=/api/seguimiento/**
          filters:
            - TokenRelay
```

**Puerto:** `8080`

---

### 🔐 Keycloak (Authentication Server)

**Responsabilidad:** Gestión centralizada de autenticación y autorización

**Características:**
- **Single Sign-On (SSO)**: Una sola autenticación para todos los servicios
- **OAuth 2.0 / OpenID Connect**: Estándares de autenticación
- **JWT Tokens**: Tokens seguros y auto-contenidos
- **Roles y permisos**: Control de acceso basado en roles (RBAC)
- **Gestión de usuarios**: CRUD de usuarios y credenciales
- **Federación de identidades**: Integración con proveedores externos (Google, Facebook, etc.)

**Roles definidos:**
- `cliente`: Acceso a funcionalidades de cliente
- `operador`: Acceso a gestión administrativa
- `transportista`: Acceso a operaciones de transporte

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
POST   /api/v1/solicitudes                     - Crear solicitud
PUT    /api/v1/solicitudes/{id}                - Actualizar solicitud
GET    /api/v1/solicitudes/cliente/{id}        - Solicitudes de un cliente
POST   /api/v1/solicitudes/{id}/confirmar      - Confirmar solicitud
POST   /api/v1/solicitudes/{id}/cancelar       - Cancelar solicitud
```

#### Comunicación con otros servicios:
- **→ ms-transporte**: Solicita creación de rutas y asignación de recursos
- **→ ms-seguimiento**: Notifica cambios de estado en solicitudes
- **← API Gateway**: Recibe peticiones de usuarios

#### Base de datos:
**Entidades gestionadas:**
- `Usuario`
- `Cliente`
- `Contenedor`
- `Solicitud`
- `Tarifa`

**Puerto:** `8081`

---

### 2. 🚛 Microservicio Transporte (ms-transporte)

**Responsabilidad:** Gestión de rutas, tramos, camiones y depósitos

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

#### Lógica de negocio:

##### Cálculo de rutas:
```java
1. Recibe solicitud con origen y destino
2. Consulta ms-maps para geocodificar direcciones
3. Consulta depósitos intermedios óptimos
4. Calcula distancias entre puntos con ms-maps
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
6. Notificar a ms-seguimiento del cambio
```

#### Comunicación con otros servicios:
- **→ ms-maps**: Obtiene distancias, rutas y geocodificación
- **→ ms-seguimiento**: Notifica cambios en tramos
- **← ms-cliente**: Recibe solicitudes de creación de rutas
- **← API Gateway**: Recibe peticiones de operadores y transportistas

#### Base de datos:
**Entidades gestionadas:**
- `Ruta`
- `Tramo`
- `Camion`
- `Deposito`

**Puerto:** `8082`

---

### 3. 📍 Microservicio Seguimiento (ms-seguimiento)

**Responsabilidad:** Tracking en tiempo real de contenedores

#### Endpoints principales:

##### Seguimiento
```
GET    /api/v1/seguimiento/solicitud/{id}      - Historial de seguimiento
POST   /api/v1/seguimiento                     - Registrar evento de seguimiento
GET    /api/v1/seguimiento/contenedor/{id}     - Seguimiento de contenedor
GET    /api/v1/seguimiento/actual/{id}         - Ubicación actual
GET    /api/v1/seguimiento/ruta/{id}           - Puntos de seguimiento en mapa
```

#### Funcionalidades:

##### Registro de eventos:
```java
{
  "idSolicitud": 123,
  "estado": "EN_VIAJE",
  "descripcion": "Contenedor en tránsito hacia depósito central",
  "latitud": -34.603722,
  "longitud": -58.381592,
  "fechaHora": "2025-10-15T14:30:00"
}
```

##### Estados rastreables:
- `PENDIENTE`: Contenedor en espera de retiro
- `RETIRADO`: Contenedor retirado del origen
- `EN_VIAJE`: En tránsito
- `EN_DEPOSITO`: Almacenado en depósito intermedio
- `ENTREGADO`: Entregado en destino final

##### Integración con mapas:
```java
// Obtener ruta visual con todos los puntos de seguimiento
GET /api/v1/seguimiento/ruta/{idSolicitud}

Response:
{
  "solicitudId": 123,
  "puntos": [
    {
      "latitud": -34.603722,
      "longitud": -58.381592,
      "fechaHora": "2025-10-15T10:00:00",
      "descripcion": "Origen - Buenos Aires"
    },
    {
      "latitud": -34.620000,
      "longitud": -58.390000,
      "fechaHora": "2025-10-15T11:30:00",
      "descripcion": "En tránsito"
    },
    ...
  ],
  "rutaOptima": {...}  // Obtenida de ms-maps
}
```

#### Comunicación con otros servicios:
- **→ ms-maps**: Obtiene rutas visuales y direcciones
- **← ms-cliente**: Recibe notificaciones de cambios en solicitudes
- **← ms-transporte**: Recibe notificaciones de cambios en tramos
- **← API Gateway**: Recibe consultas de seguimiento

#### Base de datos:
**Entidades gestionadas:**
- `Seguimiento`

**Puerto:** `8083`

---

### 4. 🗺️ Microservicio Maps (ms-maps)

**Responsabilidad:** Interfaz centralizada con Google Maps API

#### Endpoints principales:

##### Geocodificación
```
GET    /api/v1/maps/geocode                    - Dirección → Coordenadas
GET    /api/v1/maps/reverse-geocode            - Coordenadas → Dirección
```

**Ejemplo:**
```
GET /api/v1/maps/geocode?direccion=Av. Corrientes 1234, Buenos Aires

Response:
{
  "direccion": "Av. Corrientes 1234, Buenos Aires, Argentina",
  "latitud": -34.603722,
  "longitud": -58.381592,
  "tipo": "street_address"
}
```

##### Distancias
```
GET    /api/v1/maps/distancia                  - Distancia entre dos puntos
POST   /api/v1/maps/distancia/multiple         - Matriz de distancias
```

**Ejemplo:**
```
GET /api/v1/maps/distancia?origen=-34.603722,-58.381592&destino=-31.416668,-64.183334

Response:
{
  "distanciaKm": 698.5,
  "duracionMinutos": 510,
  "origen": {
    "latitud": -34.603722,
    "longitud": -58.381592,
    "direccion": "Buenos Aires"
  },
  "destino": {
    "latitud": -31.416668,
    "longitud": -64.183334,
    "direccion": "Córdoba"
  }
}
```

##### Rutas
```
GET    /api/v1/maps/ruta                       - Ruta óptima entre dos puntos
GET    /api/v1/maps/ruta/optimizada            - Ruta optimizada (múltiples puntos)
```

**Ejemplo:**
```
GET /api/v1/maps/ruta/optimizada?puntos=Buenos Aires|Rosario|Córdoba|Mendoza

Response:
{
  "ordenOptimo": ["Buenos Aires", "Rosario", "Córdoba", "Mendoza"],
  "distanciaTotal": 1320.5,
  "duracionTotal": 840,
  "tramos": [
    {
      "desde": "Buenos Aires",
      "hasta": "Rosario",
      "distanciaKm": 298.0,
      "duracionMinutos": 180
    },
    ...
  ]
}
```

#### Funcionalidades:

##### 1. Abstracción de Google Maps API
- Centraliza todas las llamadas a Google Maps
- Maneja la API Key de forma segura
- Convierte respuestas de Google Maps a formato interno

##### 2. Caché de consultas frecuentes
```java
@Cacheable(value = "distancias", key = "#origen + '-' + #destino")
public DistanciaDTO calcularDistancia(String origen, String destino) {
    // Solo consulta Google Maps si no está en caché
}
```

Beneficios:
- ✅ Reduce costos de API de Google Maps
- ✅ Mejora tiempos de respuesta
- ✅ Reduce latencia en consultas repetidas

##### 3. Rate Limiting
```java
@RateLimiter(name = "googleMapsApi", fallbackMethod = "fallbackDistancia")
public DistanciaDTO calcularDistancia(String origen, String destino) {
    // Limita llamadas a Google Maps
}
```

##### 4. Fallback y Circuit Breaker
```java
@CircuitBreaker(name = "googleMapsApi", fallbackMethod = "calcularDistanciaAproximada")
public DistanciaDTO calcularDistancia(String origen, String destino) {
    // Si Google Maps falla, usa cálculo aproximado
}

private DistanciaDTO calcularDistanciaAproximada(String origen, String destino, Exception e) {
    // Cálculo usando fórmula de Haversine (distancia en línea recta)
    return calcularDistanciaHaversine(origen, destino);
}
```

#### Ventajas de este microservicio:

✅ **Centralización**: Un solo punto de integración con Google Maps
✅ **Reutilización**: Todos los microservicios usan el mismo servicio
✅ **Caché**: Optimiza costos y rendimiento
✅ **Abstracción**: Facilita cambiar de proveedor de mapas (Google → Mapbox, HERE, etc.)
✅ **Monitoreo**: Un solo lugar para logs y métricas de llamadas a mapas
✅ **Seguridad**: API Key protegida en un solo servicio

#### Comunicación con otros servicios:
- **← ms-transporte**: Solicita cálculo de rutas y distancias
- **← ms-seguimiento**: Solicita geocodificación y rutas visuales
- **→ Google Maps API**: Consume servicios de mapas

#### Configuración:
```yaml
google:
  maps:
    api-key: ${GOOGLE_MAPS_API_KEY}
    base-url: https://maps.googleapis.com/maps/api
    timeout: 5s
    max-retries: 3
    
resilience4j:
  circuitbreaker:
    instances:
      googleMapsApi:
        slidingWindowSize: 10
        failureRateThreshold: 50
        waitDurationInOpenState: 10s
  
  ratelimiter:
    instances:
      googleMapsApi:
        limitForPeriod: 50
        limitRefreshPeriod: 1s
```

**Puerto:** `8084`

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

### 📊 DB Cliente (PostgreSQL)

**Entidades:**
- `usuario`: Datos de autenticación y personales
- `cliente`: Información específica del cliente
- `contenedor`: Contenedores registrados
- `solicitud`: Solicitudes de transporte
- `tarifa`: Configuración de precios

**Relaciones principales:**
```
usuario 1:1 cliente
cliente 1:N contenedor
cliente 1:N solicitud
contenedor 1:N solicitud
tarifa 1:N solicitud
```

**Índices importantes:**
```sql
CREATE INDEX idx_cliente_usuario ON cliente(id_usuario);
CREATE INDEX idx_contenedor_cliente ON contenedor(id_cliente);
CREATE INDEX idx_solicitud_cliente ON solicitud(id_cliente);
CREATE INDEX idx_solicitud_estado ON solicitud(estado);
CREATE INDEX idx_tarifa_activo ON tarifa(activo) WHERE activo = true;
```

**Puerto:** `5432`

---

### 🚛 DB Transporte (PostgreSQL)

**Entidades:**
- `ruta`: Rutas completas
- `tramo`: Segmentos de ruta
- `camion`: Vehículos de transporte
- `deposito`: Ubicaciones de almacenamiento intermedio

**Relaciones principales:**
```
ruta 1:N tramo
camion 1:N tramo
deposito 1:N tramo (origen)
deposito 1:N tramo (destino)
usuario 1:N tramo (transportista)
```

**Índices importantes:**
```sql
CREATE INDEX idx_tramo_ruta ON tramo(id_ruta);
CREATE INDEX idx_tramo_estado ON tramo(estado);
CREATE INDEX idx_tramo_camion ON tramo(dominio_camion);
CREATE INDEX idx_tramo_transportista ON tramo(id_usuario_transportista);
CREATE INDEX idx_camion_disponibilidad ON camion(disponibilidad) WHERE disponibilidad = true;
```

**Puerto:** `5433`

---

### 📍 DB Seguimiento (PostgreSQL)

**Entidades:**
- `seguimiento`: Eventos y ubicaciones de contenedores

**Relaciones:**
```
solicitud (en ms-cliente) 1:N seguimiento
```

**Índices importantes:**
```sql
CREATE INDEX idx_seguimiento_solicitud ON seguimiento(id_solicitud);
CREATE INDEX idx_seguimiento_fecha ON seguimiento(fecha_hora DESC);
CREATE INDEX idx_seguimiento_estado ON seguimiento(estado);
CREATE INDEX idx_seguimiento_ubicacion ON seguimiento USING GIST (
    ll_to_earth(ubicacion_latitud, ubicacion_longitud)
);
```

**Consideraciones de rendimiento:**
- Tabla de alto volumen (muchos registros de tracking)
- Particionamiento por fecha recomendado para históricos
- Índices espaciales para búsquedas geográficas

**Puerto:** `5434`

---

## 🔐 Seguridad y Autenticación

### Flujo de Autenticación Completo

```
1. REGISTRO
   Usuario → API Gateway → ms-cliente
   ms-cliente → Crea Usuario en BD
   ms-cliente → Crea Usuario en Keycloak
   ms-cliente → Asigna rol según tipo
   
2. LOGIN
   Usuario → Login Form → Keycloak
   Keycloak → Valida credenciales
   Keycloak → Genera JWT Token
   Keycloak → Retorna Token + Refresh Token
   
3. ACCESO A RECURSOS
   Usuario → Request + JWT → API Gateway
   API Gateway → Valida JWT con Keycloak
   API Gateway → Extrae roles del token
   API Gateway → Verifica permisos
   API Gateway → Enruta a Microservicio
   Microservicio → Procesa request
   Microservicio → Response → Usuario
   
4. TOKEN EXPIRADO
   Usuario → Request + JWT expirado → API Gateway
   API Gateway → Detecta token expirado (401)
   Usuario → Refresh Token → Keycloak
   Keycloak → Genera nuevo JWT
   Usuario → Reintenta request con nuevo JWT
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

### Configuración de Seguridad en Microservicios

```java
// filepath: microservices/ms-cliente/src/main/java/com/logistica/cliente/config/SecurityConfig.java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                // Endpoints públicos
                .requestMatchers("/actuator/health", "/api-docs/**", "/swagger-ui/**").permitAll()
                
                // Endpoints de clientes
                .requestMatchers(HttpMethod.POST, "/api/v1/clientes").permitAll() // Registro
                .requestMatchers("/api/v1/clientes/**").hasAnyRole("CLIENTE", "OPERADOR")
                .requestMatchers("/api/v1/contenedores/**").hasAnyRole("CLIENTE", "OPERADOR")
                
                // Endpoints de solicitudes
                .requestMatchers(HttpMethod.GET, "/api/v1/solicitudes/**").hasAnyRole("CLIENTE", "OPERADOR")
                .requestMatchers(HttpMethod.POST, "/api/v1/solicitudes/**").hasRole("CLIENTE")
                .requestMatchers(HttpMethod.PUT, "/api/v1/solicitudes/**").hasRole("OPERADOR")
                
                // Todo lo demás requiere autenticación
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            )
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );
        
        return http.build();
    }
    
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = 
            new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthoritiesClaimName("realm_access.roles");
        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");
        
        JwtAuthenticationConverter jwtAuthenticationConverter = 
            new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(
            grantedAuthoritiesConverter
        );
        
        return jwtAuthenticationConverter;
    }
}
```

### Matriz de Permisos

| Endpoint | Cliente | Operador | Transportista | Público |
|----------|---------|----------|---------------|---------|
| POST /api/v1/clientes | ✅ | ✅ | ❌ | ✅ (registro) |
| GET /api/v1/clientes | ❌ | ✅ | ❌ | ❌ |
| POST /api/v1/solicitudes | ✅ | ✅ | ❌ | ❌ |
| GET /api/v1/solicitudes | ✅ (sus solicitudes) | ✅ (todas) | ❌ | ❌ |
| POST /api/v1/tramos/{id}/asignar-camion | ❌ | ✅ | ❌ | ❌ |
| POST /api/v1/tramos/{id}/iniciar | ❌ | ❌ | ✅ | ❌ |
| GET /api/v1/seguimiento/solicitud/{id} | ✅ (si es suya) | ✅ | ✅ (si asignado) | ❌ |
| POST /api/v1/seguimiento | ❌ | ✅ | ✅ | ❌ |

---

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
// filepath: microservices/ms-cliente/src/main/java/com/logistica/cliente/client/TransporteClient.java
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
            .origenLatitud(solicitud.getOrigen_latitud())
            .origenLongitud(solicitud.getOrigen_longitud())
            .destinoLatitud(solicitud.getDestino_latitud())
            .destinoLongitud(solicitud.getDestino_longitud())
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
# filepath: microservices/ms-cliente/src/main/resources/application.yml
resilience4j:
  circuitbreaker:
    instances:
      transporte:
        slidingWindowSize: 10
        failureRateThreshold: 50
        waitDurationInOpenState: 10s
        permittedNumberOfCallsInHalfOpenState: 3
        automaticTransitionFromOpenToHalfOpenEnabled: true
      
      seguimiento:
        slidingWindowSize: 5
        failureRateThreshold: 60
        waitDurationInOpenState: 5s
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

#### Timeout Pattern

Evita esperas indefinidas.

```yaml
resilience4j:
  timelimiter:
    instances:
      transporte:
        timeoutDuration: 5s
```

### Service Discovery (Futuro)

Para ambientes productivos, se recomienda implementar **Eureka** o **Consul** para:
- Registro automático de instancias
- Descubrimiento dinámico de servicios
- Balanceo de carga client-side
- Health checks automáticos

```java
// Con Eureka
@FeignClient(name = "ms-transporte") // Nombre lógico, no URL
public interface TransporteClient {
    @PostMapping("/api/v1/rutas")
    RutaDTO crearRuta(RutaRequestDTO request);
}
```

---

## 🌍 Integraciones Externas

### Google Maps API

**Servicios utilizados:**

#### 1. Geocoding API
Convierte direcciones en coordenadas y viceversa.

**Uso en el sistema:**
- Cuando el cliente ingresa una dirección de origen/destino
- Para validar y estandarizar direcciones
- Para obtener coordenadas exactas de depósitos

**Ejemplo de llamada:**
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

#### 2. Distance Matrix API
Calcula distancias y tiempos entre múltiples orígenes y destinos.

**Uso en el sistema:**
- Calcular distancias entre depósitos
- Estimar tiempos de viaje
- Optimizar selección de depósitos intermedios

**Ejemplo de llamada:**
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

#### 3. Directions API
Obtiene rutas detalladas con instrucciones paso a paso.

**Uso en el sistema:**
- Generar rutas optimizadas entre puntos
- Mostrar ruta visual en el mapa
- Obtener waypoints para seguimiento

**Ejemplo de llamada:**
```http
GET https://maps.googleapis.com/maps/api/directions/json?
    origin=Buenos+Aires
    &destination=Mendoza
    &waypoints=Rosario|Córdoba
    &key=YOUR_API_KEY

Response:
{
  "routes": [{
    "legs": [
      {
        "distance": { "value": 298000, "text": "298 km" },
        "duration": { "value": 10800, "text": "3 hours" },
        "start_address": "Buenos Aires, Argentina",
        "end_address": "Rosario, Santa Fe, Argentina",
        "steps": [...]
      }
    ],
    "overview_polyline": { "points": "encoded_polyline_string" }
  }]
}
```

### Gestión de Costos de Google Maps API

**Estrategias de optimización:**

1. **Caché agresivo**: Cachear consultas por 24-48 horas
2. **Agrupación de requests**: Usar Distance Matrix en lugar de múltiples consultas individuales
3. **Límites de uso**: Configurar rate limiting interno
4. **Fallback local**: Usar cálculos aproximados (Haversine) cuando sea posible

**Precios aproximados (2025):**
- Geocoding: $5 por 1,000 requests
- Distance Matrix: $5-10 por 1,000 elements
- Directions: $5 por 1,000 requests

**Cuota gratuita mensual:** $200 de crédito

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
          │ 10. Geocodificar direcciones
          ↓
    ┌─────────────┐      ┌──────────┐
    │  ms-maps    │─────→│  Google  │
    └─────────────┘      │   Maps   │
          │              └──────────┘
          │ 11. Retorna coordenadas
          │
          │ 12. Crear solicitud (estado: BORRADOR)
          ↓
    ┌────────────┐
    │ DB Cliente │
    └────────────┘
          │
          │ 13. POST /api/v1/rutas (solicitar cálculo)
          ↓
    ┌──────────────┐
    │ ms-transporte│
    └──────┬───────┘
           │
           │ 14. Buscar depósitos cercanos
           │ 15. Calcular distancias con ms-maps
           │ 16. Crear tramos óptimos
           │ 17. Calcular costos con Tarifa
           ↓
    ┌──────────────┐
    │ DB Transporte│
    └──────────────┘
           │
           │ 18. Retorna ruta con costo estimado
           │
    ┌─────────────┐
    │ ms-cliente  │
    └─────┬───────┘
          │
          │ 19. Actualiza solicitud con costo
          │     (estado: PROGRAMADA)
          ↓
    ┌────────────┐
    │ DB Cliente │
    └────────────┘
```

---

### Flujo 2: Asignación de Recursos y Transporte

```
┌──────────┐
│ Operador │
└────┬─────┘
     │
     │ 1. GET /api/v1/tramos?estado=estimado
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
           │ 2. Busca tramos sin asignar
           ↓
    ┌──────────────┐
    │ DB Transporte│
    └──────────────┘
           │
           │ 3. Retorna lista de tramos
           │
┌──────────┐
│ Operador │ (ve tramos disponibles)
└────┬─────┘
     │
     │ 4. POST /api/v1/tramos/{id}/asignar-camion
     │    {
     │      "dominioCamion": "ABC123",
     │      "idTransportista": 5
     │    }
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
           │ 5. Verificar disponibilidad camión
           │ 6. Verificar disponibilidad transportista
           │ 7. Asignar recursos al tramo
           │ 8. Marcar camión como NO disponible
           │ 9. Cambiar estado tramo a ASIGNADO
           ↓
    ┌──────────────┐
    │ DB Transporte│
    └──────────────┘
           │
           │ 10. POST /api/v1/seguimiento
           │     (notificar asignación)
           ↓
    ┌───────────────┐
    │ ms-seguimiento│
    └───────┬───────┘
            │
            │ 11. Registrar evento
            ↓
    ┌───────────────┐
    │ DB Seguimiento│
    └───────────────┘

    ... Transportista inicia tramo ...

┌──────────────┐
│ Transportista│
└──────┬───────┘
       │
       │ 12. POST /api/v1/tramos/{id}/iniciar
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
           │ 13. Validar que tramo esté asignado a él
           │ 14. Cambiar estado a INICIADO
           │ 15. Registrar fecha/hora inicio real
           ↓
    ┌──────────────┐
    │ DB Transporte│
    └──────────────┘
           │
           │ 16. POST /seguimiento
           │     (contenedor en tránsito)
           ↓
    ┌───────────────┐
    │ ms-seguimiento│
    └───────┬───────┘
            │
            │ 17. Registrar evento + ubicación
            ↓
    ┌───────────────┐
    │ DB Seguimiento│
    └───────────────┘

    ... Durante el transporte ...

┌──────────────┐
│ Transportista│ (cada 15-30 min)
└──────┬───────┘
       │
       │ 18. POST /api/v1/seguimiento
       │     {
       │       "idSolicitud": 123,
       │       "latitud": -34.620,
       │       "longitud": -58.390,
       │       "estado": "EN_VIAJE"
       │     }
       ↓
┌────────────────┐
│  API Gateway   │
└────────┬───────┘
         │
         ↓
    ┌───────────────┐
    │ ms-seguimiento│
    └───────┬───────┘
            │
            │ 19. Guardar punto de tracking
            ↓
    ┌───────────────┐
    │ DB Seguimiento│
    └───────────────┘

    ... Transportista finaliza tramo ...

┌──────────────┐
│ Transportista│
└──────┬───────┘
       │
       │ 20. POST /api/v1/tramos/{id}/finalizar
       │     {
       │       "kmRecorridos": 305.5,
       │       "litrosCombustible": 28.3
       │     }
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
           │ 21. Cambiar estado a FINALIZADO
           │ 22. Registrar fecha/hora fin real
           │ 23. Calcular costo real del tramo
           │ 24. Marcar camión como DISPONIBLE
           ↓
    ┌──────────────┐
    │ DB Transporte│
    └──────────────┘
           │
           │ 25. POST /seguimiento
           │     (tramo completado)
           ↓
    ┌───────────────┐
    │ ms-seguimiento│
    └───────┬───────┘
            │
            │ 26. Registrar evento
            ↓
    ┌───────────────┐
    │ DB Seguimiento│
    └───────────────┘
           │
           │ 27. Si es el último tramo:
           │     PUT /api/v1/solicitudes/{id}
           │     (marcar como ENTREGADA)
           ↓
    ┌─────────────┐
    │ ms-cliente  │
    └─────┬───────┘
          │
          │ 28. Actualizar estado solicitud
          │ 29. Calcular costo final total
          ↓
    ┌────────────┐
    │ DB Cliente │
    └────────────┘
```

---

### Flujo 3: Consulta de Seguimiento por Cliente

```
┌─────────┐
│ Cliente │
└────┬────┘
     │
     │ 1. GET /api/v1/solicitudes/{id}/seguimiento
     ↓
┌────────────────┐
│  API Gateway   │
└────────┬───────┘
         │
         │ 2. Valida JWT + permisos
         │    (verifica que la solicitud sea del cliente)
         ↓
    ┌───────────────┐
    │ ms-seguimiento│
    └───────┬───────┘
            │
            │ 3. Buscar eventos de la solicitud
            ↓
    ┌───────────────┐
    │ DB Seguimiento│
    └───────────────┘
            │
            │ 4. Retorna historial completo
            │
            │ 5. GET /api/v1/maps/ruta/{id}
            │    (obtener ruta visual)
            ↓
    ┌─────────────┐      ┌──────────┐
    │  ms-maps    │─────→│  Google  │
    └─────────────┘      │   Maps   │
            │            └──────────┘
            │ 6. Retorna polyline de ruta
            │
    ┌───────────────┐
    │ ms-seguimiento│
    └───────┬───────┘
            │
            │ 7. Combina eventos + ruta visual
            │
┌─────────┐
│ Cliente │ (ve mapa con puntos de tracking)
└─────────┘

Response:
{
  "solicitudId": 123,
  "estadoActual": "EN_VIAJE",
  "ultimaActualizacion": "2025-10-15T14:30:00",
  "progreso": 45.5,
  "ubicacionActual": {
    "latitud": -32.889459,
    "longitud": -68.845839,
    "descripcion": "A 50 km de Córdoba"
  },
  "historial": [
    {
      "fechaHora": "2025-10-15T08:00:00",
      "estado": "RETIRADO",
      "ubicacion": {...},
      "descripcion": "Contenedor retirado del origen"
    },
    {
      "fechaHora": "2025-10-15T12:30:00",
      "estado": "EN_VIAJE",
      "ubicacion": {...},
      "descripcion": "En tránsito hacia Córdoba"
    }
  ],
  "rutaVisual": {
    "polyline": "encoded_string",
    "distanciaTotal": 698.5,
    "tiempoEstimadoRestante": 180
  }
}
```

---

## 🏛️ Patrones de Arquitectura

### 1. API Gateway Pattern

**Propósito:** Punto único de entrada para todas las peticiones

**Beneficios:**
- Simplifica el cliente (una sola URL)
- Centraliza autenticación y autorización
- Facilita monitoreo y logging
- Permite rate limiting global
- Simplifica CORS

**Implementación:** Spring Cloud Gateway

---

### 2. Database per Service

**Propósito:** Cada microservicio tiene su propia base de datos

**Beneficios:**
- Independencia de esquemas
- Escalabilidad independiente
- Aislamiento de fallos
- Libertad tecnológica

**Desafíos:**
- Consultas distribuidas
- Transacciones distribuidas
- Duplicación de datos

**Soluciones:**
- API Composition para consultas
- Patrón Saga para transacciones
- Event Sourcing para sincronización

---

### 3. Circuit Breaker Pattern

**Propósito:** Prevenir cascadas de fallos

**Estados:**
- **Closed**: Funcionamiento normal
- **Open**: Servicio caído, se ejecuta fallback
- **Half-Open**: Prueba de recuperación

**Implementación:** Resilience4j

---

### 4. Service Registry & Discovery (Futuro)

**Propósito:** Registro dinámico de instancias

**Opciones:**
- Netflix Eureka
- Consul
- Kubernetes Service Discovery

---

### 5. CQRS (Command Query Responsibility Segregation)

**Aplicable a:** Seguimiento (muchas lecturas, pocas escrituras)

**Propósito:** Separar modelos de lectura y escritura

**Beneficio:** Optimización independiente de consultas y comandos

---

### 6. Event-Driven Architecture (Futuro)

**Propósito:** Comunicación asíncrona entre servicios

**Implementación sugerida:**
- Apache Kafka o RabbitMQ
- Eventos: `SolicitudCreada`, `TramoIniciado`, `ContenedorEntregado`

**Beneficios:**
- Desacoplamiento temporal
- Escalabilidad
- Auditoría completa
- Procesamiento asíncrono

---

## 🛠️ Tecnologías Utilizadas

### Backend

| Tecnología | Versión | Propósito |
|------------|---------|-----------|
| Java | 17+ | Lenguaje de programación |
| Spring Boot | 3.x | Framework de microservicios |
| Spring Cloud Gateway | 4.x | API Gateway |
| Spring Data JPA | 3.x | Persistencia de datos |
| Spring Security | 6.x | Seguridad y autenticación |
| Keycloak | 22.x | Servidor de autenticación |
| PostgreSQL | 14+ | Base de datos relacional |
| Flyway | 9.x | Migraciones de BD |
| Resilience4j | 2.x | Resiliencia (Circuit Breaker, Retry) |
| Lombok | 1.18+ | Reducción de boilerplate |
| MapStruct | 1.5+ | Mapeo de objetos |
| SpringDoc OpenAPI | 2.x | Documentación de API (Swagger) |

### Herramientas de Desarrollo

| Herramienta | Propósito |
|-------------|-----------|
| Maven | Gestión de dependencias y build |
| Git | Control de versiones |
| Docker | Containerización |
| Postman | Testing de APIs |
| IntelliJ IDEA / VS Code | IDEs |
| DBeaver / pgAdmin | Gestión de bases de datos |

### Integraciones Externas

| Servicio | Propósito |
|----------|-----------|
| Google Maps API | Geocodificación, rutas, distancias |

### Infraestructura (Futuro)

| Tecnología | Propósito |
|------------|-----------|
| Docker Compose | Orquestación local |
| Kubernetes | Orquestación en producción |
| Netflix Eureka | Service Discovery |
| Prometheus | Métricas |
| Grafana | Dashboards |
| ELK Stack | Logging centralizado |
| Jenkins / GitHub Actions | CI/CD |

---
