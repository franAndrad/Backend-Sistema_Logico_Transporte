# 🔐 Keycloak: Single Source of Truth para Usuarios

## ¿Qué es Keycloak?

**Keycloak es un servidor de identidad y autenticación externo** que gestiona TODOS los datos de usuarios en nuestro sistema de logística. Es un componente independiente que NO forma parte de nuestras bases de datos de microservicios.

---

## 🎯 Responsabilidades de Keycloak

### ✅ Almacena TODOS los datos personales del usuario:

- `id` (UUID): Identificador único generado por Keycloak
- `username`: Nombre de usuario para login
- `password`: Contraseña (hasheada y protegida con bcrypt/argon2)
- `email`: Email del usuario
- `firstName` (nombre): Nombre de pila
- `lastName` (apellido): Apellido
- `enabled`: Si el usuario está activo
- `emailVerified`: Si verificó su email
- `roles`: Roles asignados (cliente, operador, transportista, admin)
- Atributos personalizados (teléfono, dirección, etc.)

### ✅ Gestiona la autenticación:

- Login/logout
- Generación de tokens JWT
- Refresh tokens
- Sesiones de usuario
- Single Sign-On (SSO)
- Multi-factor authentication (MFA)

### ✅ Gestiona la autorización:

- Roles y permisos
- Groups
- Realm roles y client roles
- Claims personalizados en JWT

---

## 📊 ¿Dónde vive cada dato?

| Dato | ¿Dónde se guarda? | ¿Por qué? |
|------|-------------------|-----------|
| **username** | 🔐 **Keycloak** | Credencial de autenticación |
| **password** | 🔐 **Keycloak** | Credencial de autenticación (hasheada) |
| **email** | 🔐 **Keycloak** | Identidad del usuario |
| **nombre** | 🔐 **Keycloak** | Dato personal básico |
| **apellido** | 🔐 **Keycloak** | Dato personal básico |
| **telefono** | 🔐 **Keycloak** | Atributo personalizado |
| **rol** | 🔐 **Keycloak** | Autorización y permisos |
| **activo** | 🔐 **Keycloak** | Estado de la cuenta |
| **keyCloakId** | 📦 **ms-cliente** | Referencia al usuario en Keycloak |
| **direccionFacturacion** | 📦 **ms-cliente** | Dato específico del negocio |
| **direccionEnvio** | 📦 **ms-cliente** | Dato específico del negocio |
| **razonSocial** | 📦 **ms-cliente** | Dato específico del negocio (empresa) |
| **cuit** | 📦 **ms-cliente** | Dato específico del negocio (empresa) |

### 🔑 Regla de oro:

- **Keycloak**: Datos de identidad y autenticación
- **Microservicios**: Datos específicos del dominio de negocio

---

## 🏗️ Arquitectura del Sistema

```
┌─────────────────────────────────────────────────────────────┐
│                         Frontend                             │
│                    (React/Angular/Vue)                       │
└────────────────────────┬────────────────────────────────────┘
                         │
                         │ 1. Login Request
                         ↓
              ┌──────────────────────┐
              │      Keycloak        │
              │   (Identity Server)  │
              │                      │
              │  - Valida credenciales
              │  - Genera JWT token  │
              │  - Retorna token     │
              └──────────┬───────────┘
                         │
                         │ 2. JWT Token
                         ↓
              ┌──────────────────────┐
              │    API Gateway       │
              │  (Spring Cloud)      │
              │                      │
              │  - Valida JWT        │
              │  - Enruta requests   │
              └──────────┬───────────┘
                         │
         ┌───────────────┴───────────────┐
         │                               │
         ↓                               ↓
┌────────────────┐            ┌────────────────┐
│  ms-cliente    │            │ ms-transporte  │
│                │            │                │
│ DB: clientedb  │            │ DB: transportedb│
│                │            │                │
│ Guarda solo:   │            │ Guarda solo:   │
│ - keyCloakId   │            │ - keyCloakId   │
│ - direcciones  │            │   (en Tramo)   │
│ - razonSocial  │            │                │
│ - cuit         │            │                │
└────────┬───────┘            └────────┬───────┘
         │                             │
         │ 3. Consulta datos usuario   │
         └──────────────┬──────────────┘
                        │
                        ↓
              ┌──────────────────────┐
              │  Keycloak Admin API  │
              │                      │
              │  GET /users/{uuid}   │
              │                      │
              │  Retorna:            │
              │  - nombre, apellido  │
              │  - email, teléfono   │
              │  - roles             │
              └──────────────────────┘
```

---

## 🔄 Flujos Completos

### 1️⃣ Registro de Usuario

```
┌─────────┐
│ Usuario │
│ Frontend│
└────┬────┘
     │ 1. POST /api/clientes/registro
     │    { username, password, email, firstName, lastName, direccion }
     ↓
┌────────────┐
│ ms-cliente │
└────┬───────┘
     │ 2. POST /admin/realms/logistica/users (Keycloak Admin API)
     │    { username, password, email, firstName, lastName, roles: ["cliente"] }
     ↓
┌──────────┐
│ Keycloak │
│          │
│ ✅ Guarda TODOS los datos personales
│ ✅ Hashea password
│ ✅ Asigna rol "cliente"
│ ✅ Genera UUID: "a1b2c3d4-..."
└────┬─────┘
     │ 3. Responde: { "id": "a1b2c3d4-..." }
     ↓
┌────────────┐
│ ms-cliente │
│            │
│ INSERT INTO cliente (keycloak_id, direccion_facturacion)
│ VALUES ('a1b2c3d4-...', 'Av. Corrientes 1234');
└────┬───────┘
     │ 4. Responde: { "idCliente": 123, "mensaje": "Registrado" }
     ↓
┌─────────┐
│ Usuario │
│ Frontend│
└─────────┘
```

**Código Java (ms-cliente):**

```java
@Service
public class ClienteService {
    
    @Autowired
    private KeycloakAdminClient keycloakClient;
    
    @Autowired
    private ClienteRepository clienteRepository;
    
    @Transactional
    public ClienteDTO registrarCliente(RegistroClienteDTO dto) {
        // 1. Crear usuario en Keycloak
        UserRepresentation keycloakUser = new UserRepresentation();
        keycloakUser.setUsername(dto.getUsername());
        keycloakUser.setEmail(dto.getEmail());
        keycloakUser.setFirstName(dto.getFirstName());
        keycloakUser.setLastName(dto.getLastName());
        keycloakUser.setEnabled(true);
        keycloakUser.setEmailVerified(false);
        
        // Configurar password
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(dto.getPassword());
        credential.setTemporary(false);
        keycloakUser.setCredentials(List.of(credential));
        
        // Asignar rol "cliente"
        keycloakUser.setRealmRoles(List.of("cliente"));
        
        // Crear en Keycloak
        Response response = keycloakClient
            .realm("logistica")
            .users()
            .create(keycloakUser);
        
        if (response.getStatus() != 201) {
            throw new KeycloakException("Error al crear usuario en Keycloak");
        }
        
        // Obtener el UUID generado por Keycloak
        String keycloakId = CreatedResponseUtil.getCreatedId(response);
        
        // 2. Crear cliente en nuestra DB (SOLO guardamos el UUID)
        Cliente cliente = new Cliente();
        cliente.setKeyCloakId(keycloakId); // 👈 SOLO el UUID
        cliente.setDireccionFacturacion(dto.getDireccionFacturacion());
        cliente.setDireccionEnvio(dto.getDireccionEnvio());
        cliente.setRazonSocial(dto.getRazonSocial());
        cliente.setCuit(dto.getCuit());
        
        cliente = clienteRepository.save(cliente);
        
        return ClienteDTO.fromEntity(cliente);
    }
}
```

---

### 2️⃣ Login de Usuario

```
┌─────────┐
│ Usuario │
└────┬────┘
     │ 1. POST /auth/realms/logistica/protocol/openid-connect/token
     │    { username: "juan.perez", password: "Pass123!", grant_type: "password" }
     ↓
┌──────────┐
│ Keycloak │
│          │
│ ✅ Valida credenciales
│ ✅ Genera JWT token
│ ✅ Incluye roles en el token
└────┬─────┘
     │ 2. Responde: { 
     │      "access_token": "eyJhbGciOiJSUzI1NiIs...",
     │      "refresh_token": "eyJhbGciOiJIUzI1NiIs...",
     │      "expires_in": 3600
     │    }
     ↓
┌─────────┐
│ Usuario │
│ Frontend│
│          │
│ ✅ Guarda token en localStorage/sessionStorage
│ ✅ Incluye token en headers de todos los requests:
│    Authorization: Bearer eyJhbGciOiJSUzI1NiIs...
└─────────┘
```

**Estructura del JWT Token:**

```json
{
  "header": {
    "alg": "RS256",
    "typ": "JWT"
  },
  "payload": {
    "sub": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "email": "juan@example.com",
    "name": "Juan Pérez",
    "preferred_username": "juan.perez",
    "realm_access": {
      "roles": ["cliente"]
    },
    "exp": 1729105200,
    "iat": 1729101600
  },
  "signature": "..."
}
```

---

### 3️⃣ Consulta de Datos Completos

```
┌─────────┐
│ Frontend│
└────┬────┘
     │ 1. GET /api/clientes/123
     │    Header: Authorization: Bearer eyJhbGciOiJSUzI1NiIs...
     ↓
┌────────────┐
│ API Gateway│
│            │
│ ✅ Valida JWT con clave pública de Keycloak
│ ✅ Extrae roles del token
│ ✅ Verifica permisos
└────┬───────┘
     │ 2. Forward request to ms-cliente
     ↓
┌────────────┐
│ ms-cliente │
│            │
│ SELECT * FROM cliente WHERE id_cliente = 123;
│ → Result: { idCliente: 123, keyCloakId: "a1b2c3d4-...", ... }
└────┬───────┘
     │ 3. GET /admin/realms/logistica/users/a1b2c3d4-...
     ↓
┌──────────┐
│ Keycloak │
│          │
│ Retorna datos del usuario:
│ { id, username, email, firstName, lastName, enabled, roles }
└────┬─────┘
     │ 4. Responde datos del usuario
     ↓
┌────────────┐
│ ms-cliente │
│            │
│ Combina datos:
│ - De Keycloak: nombre, email, username, rol
│ - De nuestra DB: direcciones, razonSocial, cuit
└────┬───────┘
     │ 5. Responde JSON completo al frontend
     ↓
┌─────────┐
│ Frontend│
└─────────┘
```

**Código Java (ms-cliente):**

```java
@Service
public class ClienteService {
    
    @Autowired
    private KeycloakAdminClient keycloakClient;
    
    @Autowired
    private ClienteRepository clienteRepository;
    
    public ClienteCompletoDTO obtenerClienteCompleto(Long idCliente) {
        // 1. Obtener datos de negocio de nuestra DB
        Cliente cliente = clienteRepository.findById(idCliente)
            .orElseThrow(() -> new NotFoundException("Cliente no encontrado"));
        
        // 2. Consultar Keycloak para obtener datos personales
        UserRepresentation keycloakUser = keycloakClient
            .realm("logistica")
            .users()
            .get(cliente.getKeyCloakId()) // 👈 UUID guardado en nuestra DB
            .toRepresentation();
        
        // 3. Combinar ambos
        return ClienteCompletoDTO.builder()
            .idCliente(cliente.getIdCliente())
            // 👇 Datos de Keycloak
            .nombre(keycloakUser.getFirstName())
            .apellido(keycloakUser.getLastName())
            .email(keycloakUser.getEmail())
            .username(keycloakUser.getUsername())
            .activo(keycloakUser.isEnabled())
            .roles(keycloakUser.getRealmRoles())
            // 👇 Datos de nuestra DB
            .direccionFacturacion(cliente.getDireccionFacturacion())
            .direccionEnvio(cliente.getDireccionEnvio())
            .razonSocial(cliente.getRazonSocial())
            .cuit(cliente.getCuit())
            .build();
    }
}
```

---

### 4️⃣ Actualización de Datos

#### **Actualizar datos en Keycloak (nombre, email, etc.):**

```java
@Service
public class ClienteService {
    
    @Autowired
    private KeycloakAdminClient keycloakClient;
    
    public void actualizarDatosPersonales(Long idCliente, ActualizarPerfilDTO dto) {
        Cliente cliente = clienteRepository.findById(idCliente)
            .orElseThrow(() -> new NotFoundException("Cliente no encontrado"));
        
        // Actualizar en Keycloak
        UserRepresentation keycloakUser = keycloakClient
            .realm("logistica")
            .users()
            .get(cliente.getKeyCloakId())
            .toRepresentation();
        
        keycloakUser.setFirstName(dto.getNombre());
        keycloakUser.setLastName(dto.getApellido());
        keycloakUser.setEmail(dto.getEmail());
        
        keycloakClient
            .realm("logistica")
            .users()
            .get(cliente.getKeyCloakId())
            .update(keycloakUser);
    }
}
```

#### **Actualizar datos de negocio (direcciones, etc.):**

```java
@Service
public class ClienteService {
    
    public void actualizarDireccion(Long idCliente, String nuevaDireccion) {
        Cliente cliente = clienteRepository.findById(idCliente)
            .orElseThrow(() -> new NotFoundException("Cliente no encontrado"));
        
        // Actualizar en nuestra DB
        cliente.setDireccionFacturacion(nuevaDireccion);
        clienteRepository.save(cliente);
    }
}
```

---

## ✅ Ventajas de este enfoque

### 1. **Single Source of Truth**
- Los datos personales SIEMPRE están actualizados
- Si el usuario cambia su email en Keycloak, todos los servicios lo ven automáticamente
- No hay sincronización entre sistemas

### 2. **Sin duplicación de datos**
- No hay riesgo de inconsistencia
- No hay que mantener múltiples copias de username, password, email, etc.
- Reducción de complejidad

### 3. **Seguridad mejorada**
- Keycloak maneja passwords de forma segura (bcrypt, argon2)
- Nuestros microservicios nunca ven las contraseñas
- Tokens JWT firmados criptográficamente
- Refresh tokens para renovación automática

### 4. **Centralización de autenticación**
- Single Sign-On (SSO)
- Un solo lugar para gestionar usuarios
- Si un usuario se desactiva en Keycloak, pierde acceso a TODOS los servicios

### 5. **Separación de responsabilidades (SoC)**
- **Keycloak**: Identidad, autenticación, autorización
- **ms-cliente**: Datos de negocio específicos (direcciones, facturación)
- **ms-transporte**: Operaciones logísticas
- Cada componente tiene una responsabilidad clara

### 6. **Escalabilidad**
- Keycloak se puede escalar independientemente
- Soporte para clustering y alta disponibilidad
- Caché de tokens para mejor performance

### 7. **Estándares abiertos**
- OAuth 2.0
- OpenID Connect
- SAML 2.0
- JWT (JSON Web Tokens)

---

## ⚠️ Consideraciones importantes

### 1. **Dependencia de Keycloak**
- Si Keycloak está caído, no hay autenticación
- **Solución**: Implementar alta disponibilidad (cluster de Keycloak)

### 2. **Latencia adicional**
- Consultar Keycloak Admin API añade latencia
- **Solución**: Cachear datos de usuario que cambian raramente

```java
@Cacheable(value = "usuarios", key = "#keycloakId")
public UserRepresentation obtenerUsuarioKeycloak(String keycloakId) {
    return keycloakClient
        .realm("logistica")
        .users()
        .get(keycloakId)
        .toRepresentation();
}
```

### 3. **Gestión de tokens**
- Tokens JWT tienen tiempo de expiración
- **Solución**: Implementar refresh token automático en frontend

### 4. **Sincronización inicial**
- Si ya tienes usuarios en otra DB, hay que migrarlos
- **Solución**: Script de migración para importar usuarios a Keycloak

---

## 🔧 Configuración básica

### application.yml (ms-cliente):

```yaml
keycloak:
  realm: logistica
  auth-server-url: http://localhost:8080
  ssl-required: external
  resource: ms-cliente
  credentials:
    secret: ${KEYCLOAK_CLIENT_SECRET}
  use-resource-role-mappings: true
  bearer-only: true

# Cliente Admin para gestionar usuarios
keycloak-admin:
  server-url: http://localhost:8080
  realm: logistica
  client-id: admin-cli
  username: ${KEYCLOAK_ADMIN_USER}
  password: ${KEYCLOAK_ADMIN_PASSWORD}
```

### Dependencias (pom.xml):

```xml
<!-- Keycloak Spring Boot Adapter -->
<dependency>
    <groupId>org.keycloak</groupId>
    <artifactId>keycloak-spring-boot-starter</artifactId>
    <version>23.0.0</version>
</dependency>

<!-- Keycloak Admin Client -->
<dependency>
    <groupId>org.keycloak</groupId>
    <artifactId>keycloak-admin-client</artifactId>
    <version>23.0.0</version>
</dependency>
```

---

## 📚 Recursos adicionales

- **Documentación oficial**: https://www.keycloak.org/documentation
- **Admin API**: https://www.keycloak.org/docs-api/latest/rest-api/
- **Spring Boot Adapter**: https://www.keycloak.org/docs/latest/securing_apps/#_spring_boot_adapter

---

## 🎯 Resumen ejecutivo

| Aspecto | Descripción |
|---------|-------------|
| **¿Qué es?** | Servidor de identidad y autenticación externo |
| **¿Qué guarda?** | TODOS los datos personales (username, password, email, nombre, roles) |
| **¿Qué NO guarda?** | Datos específicos del negocio (direcciones, razonSocial, cuit) |
| **Nuestros microservicios guardan** | Solo `keyCloakId` (UUID) como referencia |
| **¿Cómo consultamos datos?** | Via Keycloak Admin API usando el UUID |
| **Ventaja principal** | Single Source of Truth, sin duplicación |
| **Desventaja** | Dependencia externa, latencia en consultas |
| **Solución a desventaja** | Alta disponibilidad + caché |

---

**¿Preguntas?** Consulta la documentación completa en `/docs/` o contacta al equipo de desarrollo. 🚀
