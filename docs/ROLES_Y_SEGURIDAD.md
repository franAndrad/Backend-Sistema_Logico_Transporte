# Guía de Roles y Protección de Rutas - Sistema Logístico

## 📑 Índice

- [Roles del Sistema](#-roles-del-sistema)
- [Cómo Funciona la Protección de Rutas](#-cómo-funciona-la-protección-de-rutas)
- [Matriz de Permisos Completa](#-matriz-de-permisos-completa)
  - [MS-Cliente: Clientes](#ms-cliente-clientes)
  - [MS-Cliente: Contenedores](#ms-cliente-contenedores)
  - [MS-Cliente: Solicitudes](#ms-cliente-solicitudes)
  - [MS-Transporte: Rutas](#ms-transporte-rutas)
  - [MS-Transporte: Tramos](#ms-transporte-tramos)
  - [MS-Transporte: Camiones](#ms-transporte-camiones)
  - [MS-Transporte: Depósitos](#ms-transporte-depósitos)
  - [MS-Transporte: Tarifas](#ms-transporte-tarifas)
- [Cómo Probar con PowerShell](#-cómo-probar-con-powershell)
- [Cómo Probar con Postman](#-cómo-probar-con-postman)
- [Casos de Uso por Rol](#-casos-de-uso-por-rol)
- [Resumen Visual](#-resumen-visual)

---

## 📋 Roles del Sistema

Basado en la documentación de `microservicios.md`, el sistema tiene los siguientes roles:

| Rol | Usuario de Prueba | Password | Descripción |
|-----|-------------------|----------|-------------|
| **CLIENTE** | `cliente1` | `cliente123` | Cliente registrado que gestiona sus contenedores y solicitudes de transporte |
| **OPERADOR** | `operador1` | `operador123` | Personal operativo que gestiona rutas, tramos, camiones, depósitos y tarifas |
| **TRANSPORTISTA** | `transportista1` | `transportista123` | Conductor que inicia/finaliza tramos y consulta rutas asignadas |
| **ADMIN** | `admin` | `admin123` | Administrador con acceso total incluyendo eliminaciones físicas |

---

## 🔐 Cómo Funciona la Protección de Rutas

### 1. Spring Security intercepta TODAS las peticiones

Antes de que una petición llegue a tu controlador, pasa por el `SecurityConfig.java`:

```java
@Bean
public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
    http.authorizeExchange(exchanges -> exchanges
        .pathMatchers("/api/v1/clientes").hasAnyRole("OPERADOR", "ADMIN")  // Solo OPERADOR o ADMIN
        .pathMatchers("/api/v1/clientes/{id}").hasAnyRole("CLIENTE", "OPERADOR", "ADMIN")  // CLIENTE puede ver su propio perfil
        // ... más reglas
        .anyExchange().authenticated()  // Cualquier otra ruta requiere estar autenticado
    )
}
```

### 2. Orden de evaluación (IMPORTANTE)

Las reglas se evalúan **de arriba hacia abajo**. La primera que coincida se aplica:

```java
// ❌ MAL (la segunda regla nunca se ejecuta)
.pathMatchers("/api/v1/**").permitAll()
.pathMatchers("/api/v1/admin/**").hasRole("ADMIN")  // Esta regla nunca se alcanza

// ✅ BIEN (más específico primero)
.pathMatchers("/api/v1/admin/**").hasRole("ADMIN")
.pathMatchers("/api/v1/**").permitAll()
```

### 3. Validación del Token JWT

1. Cliente envía petición con header: `Authorization: Bearer <token>`
2. Gateway extrae el token y lo valida contra Keycloak
3. Gateway extrae los roles del claim `realm_access` del JWT
4. Los roles se convierten a formato Spring Security: `cliente` → `ROLE_CLIENTE`
5. Spring Security compara los roles del usuario con los requeridos por la ruta
6. Si coincide → **permite acceso**
7. Si no coincide → **403 Forbidden**
8. Si no hay token → **401 Unauthorized**

---

## 🛣️ Matriz de Permisos por Endpoint

### MS-CLIENTE: Clientes

| Endpoint | Método | Públic | CLIENTE | OPERADOR | TRANSPORTISTA | ADMIN |
|----------|--------|--------|---------|----------|---------------|-------|
| `/api/v1/clientes` | POST | ✅ | ❌ | ✅ | ❌ | ✅ |
| `/api/v1/clientes` | GET | ❌ | ❌ | ✅ | ❌ | ✅ |
| `/api/v1/clientes/{id}` | GET | ❌ | ✅ (propio) | ✅ | ❌ | ✅ |
| `/api/v1/clientes/{id}` | PUT | ❌ | ✅ (propio) | ✅ | ❌ | ✅ |
| `/api/v1/clientes/{id}` | DELETE | ❌ | ❌ | ❌ | ❌ | ✅ |

### MS-CLIENTE: Contenedores

| Endpoint | Método | CLIENTE | OPERADOR | ADMIN |
|----------|--------|---------|----------|-------|
| `/api/v1/contenedores` | GET | ❌ | ✅ | ✅ |
| `/api/v1/contenedores/**` | GET | ✅ | ✅ | ✅ |
| `/api/v1/contenedores` | POST | ✅ | ✅ | ✅ |
| `/api/v1/contenedores/**` | PUT | ✅ | ✅ | ✅ |

### MS-CLIENTE: Solicitudes

| Endpoint | Método | CLIENTE | OPERADOR | TRANSPORTISTA | ADMIN |
|----------|--------|---------|----------|---------------|-------|
| `/api/v1/solicitudes` | GET | ❌ | ✅ | ❌ | ✅ |
| `/api/v1/solicitudes/**` | GET | ✅ (propias) | ✅ | ✅ | ✅ |
| `/api/v1/solicitudes` | POST | ✅ | ❌ | ❌ | ❌ |
| `/api/v1/solicitudes/**` | PUT | ❌ | ✅ | ✅ | ✅ |

### MS-TRANSPORTE: Rutas

| Endpoint | Método | CLIENTE | OPERADOR | ADMIN |
|----------|--------|---------|----------|-------|
| `/api/v1/rutas` | GET | ❌ | ✅ | ✅ |
| `/api/v1/rutas/**` | GET | ✅ (consulta estado) | ✅ | ✅ |
| `/api/v1/rutas` | POST | ❌ | ✅ | ❌ |

### MS-TRANSPORTE: Tramos

| Endpoint | Método | OPERADOR | TRANSPORTISTA | ADMIN |
|----------|--------|----------|---------------|-------|
| `/api/v1/tramos` | GET | ✅ | ❌ | ✅ |
| `/api/v1/tramos/**` | GET | ✅ | ✅ | ✅ |
| `/api/v1/tramos` | POST | ✅ | ❌ | ❌ |
| `/api/v1/tramos/{id}` | PUT | ✅ | ❌ | ❌ |
| `/api/v1/tramos/{id}` | DELETE | ✅ | ❌ | ❌ |
| `/api/v1/tramos/*/iniciar` | POST | ❌ | ✅ | ❌ |
| `/api/v1/tramos/*/finalizar` | POST | ❌ | ✅ | ❌ |

### MS-TRANSPORTE: Camiones

| Endpoint | Método | OPERADOR | ADMIN |
|----------|--------|----------|-------|
| `/api/v1/camiones/**` | GET | ✅ | ✅ |
| `/api/v1/camiones` | POST | ✅ | ❌ |
| `/api/v1/camiones/**` | PUT | ✅ | ✅ |
| `/api/v1/camiones/**` | DELETE | ❌ | ✅ |

### MS-TRANSPORTE: Depósitos

| Endpoint | Método | OPERADOR | ADMIN |
|----------|--------|----------|-------|
| `/api/v1/depositos/**` | GET | ✅ | ✅ |
| `/api/v1/depositos` | POST | ✅ | ❌ |
| `/api/v1/depositos/**` | PUT | ✅ | ✅ |
| `/api/v1/depositos/**` | DELETE | ❌ | ✅ |

### MS-TRANSPORTE: Tarifas

| Endpoint | Método | CLIENTE | OPERADOR | ADMIN |
|----------|--------|---------|----------|-------|
| `/api/v1/tarifas/**` | GET | ✅ | ✅ | ✅ |
| `/api/v1/tarifas` | POST | ❌ | ❌ | ✅ |
| `/api/v1/tarifas/**` | PUT | ❌ | ❌ | ✅ |
| `/api/v1/tarifas/**` | DELETE | ❌ | ❌ | ✅ |

---

## 🧪 Pruebas en Postman

### Obtener Token por Rol

```javascript
// En Pre-request Script de Postman
pm.sendRequest({
    url: 'http://localhost:8180/realms/logistica/protocol/openid-connect/token',
    method: 'POST',
    header: 'Content-Type: application/x-www-form-urlencoded',
    body: {
        mode: 'urlencoded',
        urlencoded: [
            {key: 'grant_type', value: 'password'},
            {key: 'client_id', value: 'api-gateway'},
            {key: 'client_secret', value: 'gateway-secret'},
            {key: 'username', value: 'cliente1'},  // Cambiar según rol
            {key: 'password', value: 'cliente123'}
        ]
    }
}, function (err, res) {
    pm.environment.set("access_token", res.json().access_token);
});
```

### Probar Endpoints con Diferentes Roles

1. **Cliente consulta sus contenedores (✅ Permitido)**
```http
GET http://localhost:8080/api/v1/contenedores/cliente/1
Authorization: Bearer <token_cliente1>
```

2. **Cliente intenta listar todos los contenedores (❌ Forbidden)**
```http
GET http://localhost:8080/api/v1/contenedores
Authorization: Bearer <token_cliente1>
→ 403 Forbidden (requiere OPERADOR o ADMIN)
```

3. **Operador crea una ruta (✅ Permitido)**
```http
POST http://localhost:8080/api/v1/rutas
Authorization: Bearer <token_operador1>
{
  "idSolicitud": 5,
  "origenLat": -31.4201,
  "origenLon": -64.1888,
  "destinoLat": -34.6037,
  "destinoLon": -58.3816
}
```

4. **Transportista inicia un tramo (✅ Permitido)**
```http
POST http://localhost:8080/api/v1/tramos/10/iniciar
Authorization: Bearer <token_transportista1>
{
  "fechaHoraInicio": "2025-10-30T10:00:00"
}
```

5. **Cliente intenta eliminar un camión (❌ Forbidden)**
```http
DELETE http://localhost:8080/api/v1/camiones/ABC123
Authorization: Bearer <token_cliente1>
→ 403 Forbidden (requiere ADMIN)
```

---

## 🔧 Comandos PowerShell para Probar

### Obtener tokens de todos los roles

```powershell
# CLIENTE
$bodyCliente = @{ grant_type = "password"; client_id = "api-gateway"; client_secret = "gateway-secret"; username = "cliente1"; password = "cliente123" }
$tokenCliente = (Invoke-RestMethod -Uri "http://localhost:8180/realms/logistica/protocol/openid-connect/token" -Method Post -Body $bodyCliente -ContentType "application/x-www-form-urlencoded").access_token

# OPERADOR
$bodyOperador = @{ grant_type = "password"; client_id = "api-gateway"; client_secret = "gateway-secret"; username = "operador1"; password = "operador123" }
$tokenOperador = (Invoke-RestMethod -Uri "http://localhost:8180/realms/logistica/protocol/openid-connect/token" -Method Post -Body $bodyOperador -ContentType "application/x-www-form-urlencoded").access_token

# TRANSPORTISTA
$bodyTransportista = @{ grant_type = "password"; client_id = "api-gateway"; client_secret = "gateway-secret"; username = "transportista1"; password = "transportista123" }
$tokenTransportista = (Invoke-RestMethod -Uri "http://localhost:8180/realms/logistica/protocol/openid-connect/token" -Method Post -Body $bodyTransportista -ContentType "application/x-www-form-urlencoded").access_token

# ADMIN
$bodyAdmin = @{ grant_type = "password"; client_id = "api-gateway"; client_secret = "gateway-secret"; username = "admin"; password = "admin123" }
$tokenAdmin = (Invoke-RestMethod -Uri "http://localhost:8180/realms/logistica/protocol/openid-connect/token" -Method Post -Body $bodyAdmin -ContentType "application/x-www-form-urlencoded").access_token

Write-Host "Tokens obtenidos correctamente" -ForegroundColor Green
```

### Probar acceso por rol

```powershell
# Cliente intenta acceder a endpoint de clientes (✅)
$headersCliente = @{ Authorization = "Bearer $tokenCliente" }
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/clientes/1" -Headers $headersCliente

# Operador lista todos los camiones (✅)
$headersOperador = @{ Authorization = "Bearer $tokenOperador" }
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/camiones" -Headers $headersOperador

# Admin elimina un camión (✅)
$headersAdmin = @{ Authorization = "Bearer $tokenAdmin" }
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/camiones/ABC123" -Headers $headersAdmin -Method Delete
```

---

## 📝 Resumen

### ¿Cómo sabe el sistema qué rutas están protegidas?

1. **Configuración explícita en `SecurityConfig.java`**
   - Cada ruta tiene reglas definidas con `.pathMatchers()`
   - Se especifica qué roles pueden acceder con `.hasRole()` o `.hasAnyRole()`

2. **Interceptor de Spring Security**
   - Intercepta TODAS las peticiones antes del controlador
   - Extrae y valida el token JWT
   - Compara roles del token vs. roles requeridos
   - Permite o deniega acceso antes de enrutar

3. **Respuestas HTTP**
   - **200 OK**: Rol correcto, acceso permitido
   - **401 Unauthorized**: No hay token o token inválido
   - **403 Forbidden**: Token válido pero rol insuficiente

### Ventajas de este enfoque

✅ **Seguridad centralizada**: Todo en el Gateway  
✅ **Microservicios simples**: Sin lógica de autenticación  
✅ **Fácil auditoría**: Todas las reglas en un solo lugar  
✅ **Testeable**: Fácil probar con diferentes tokens/roles  

---

## 🚀 Próximos Pasos

1. Reiniciar Keycloak para aplicar nuevos roles
2. Reiniciar API Gateway para aplicar nueva configuración
3. Probar cada endpoint con diferentes roles
4. Documentar en Postman con ejemplos por rol
