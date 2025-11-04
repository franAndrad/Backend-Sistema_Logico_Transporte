# 📋 Explicación del archivo realm-logistica.json

Este archivo configura el realm de Keycloak para el sistema logístico. Aquí te explico cada sección para que puedas activar/desactivar lo que necesites.

---

## 🌍 **CONFIGURACIÓN GENERAL DEL REALM**

```json
"realm": "logistica",
"enabled": true,
```
- **realm**: Nombre del realm (NO cambiar)
- **enabled**: `true` = Realm activo | `false` = Realm deshabilitado
  - ✅ **Recomendación**: Dejar en `true`

---

## 🔒 **CONFIGURACIÓN DE SEGURIDAD**

```json
"sslRequired": "none",
```
- **sslRequired**: Requiere HTTPS
  - `"none"` = No requiere SSL (desarrollo)
  - `"external"` = Solo requiere SSL en conexiones externas
  - `"all"` = Requiere SSL siempre (producción)
  - ✅ **Recomendación**: `"none"` para desarrollo, `"all"` para producción

---

## 👥 **REGISTRO Y AUTENTICACIÓN DE USUARIOS**

```json
"registrationAllowed": false,
"loginWithEmailAllowed": true,
"duplicateEmailsAllowed": false,
"resetPasswordAllowed": false,
"editUsernameAllowed": false,
```

### **registrationAllowed**
- `false` = Los usuarios NO pueden auto-registrarse
- `true` = Permite que cualquiera se registre desde la página de login
- ✅ **Recomendación**: `false` (solo admins crean usuarios)
- ❌ **Desactivar si**: Quieres que los clientes se registren solos → cambiar a `true`

### **loginWithEmailAllowed**
- `true` = Permite login con email (además de username)
- `false` = Solo permite login con username
- ✅ **Recomendación**: `true` (más flexible)

### **duplicateEmailsAllowed**
- `false` = Emails únicos (un email = un usuario)
- `true` = Permite emails duplicados
- ✅ **Recomendación**: `false` (evita confusión)

### **resetPasswordAllowed**
- `false` = Usuarios NO pueden resetear su contraseña
- `true` = Permite "Olvidé mi contraseña"
- ❌ **Activar si**: Quieres que usuarios recuperen contraseñas → cambiar a `true`

### **editUsernameAllowed**
- `false` = Username inmutable después de crear cuenta
- `true` = Usuarios pueden cambiar su username
- ✅ **Recomendación**: `false` (evita problemas de identificación)

---

## 🛡️ **PROTECCIÓN CONTRA ATAQUES**

```json
"bruteForceProtected": false,
```
- `false` = Sin protección contra intentos de login repetidos
- `true` = Bloquea cuentas después de X intentos fallidos
- ❌ **Activar si**: Quieres protección contra ataques de fuerza bruta → cambiar a `true`

---

## 🔌 **CONFIGURACIÓN DEL CLIENTE (API Gateway)**

```json
"clientId": "api-gateway",
"name": "API Gateway Client",
"enabled": true,
"protocol": "openid-connect",
```
- **clientId**: Identificador único del cliente (NO cambiar)
- **enabled**: `true` = Cliente activo | `false` = Cliente deshabilitado
- **protocol**: Siempre `"openid-connect"` para OAuth2/OIDC

---

## 🔑 **AUTENTICACIÓN DEL CLIENTE**

```json
"publicClient": false,
"secret": "gateway-secret",
"clientAuthenticatorType": "client-secret",
```

### **publicClient**
- `false` = Cliente confidencial (requiere secret)
- `true` = Cliente público (sin secret, ej: apps móviles)
- ✅ **Recomendación**: `false` para backend

### **secret**
- Contraseña del cliente para autenticarse en Keycloak
- ❌ **CAMBIAR EN PRODUCCIÓN** a algo más seguro

### **clientAuthenticatorType**
- `"client-secret"` = Autentica con secret
- `"client-jwt"` = Autentica con JWT firmado
- ✅ **Recomendación**: `"client-secret"` (más simple)

---

## 🔗 **URLs DE REDIRECCIÓN Y CORS**

```json
"redirectUris": [
  "http://localhost:8080/*",
  "*"
],
"webOrigins": [
  "http://localhost:8080",
  "*"
],
```

### **redirectUris**
- Lista de URLs válidas para redirección después del login
- `"*"` = Permite cualquier URL (⚠️ **INSEGURO EN PRODUCCIÓN**)
- ❌ **En producción**: Remover `"*"` y listar URLs específicas

### **webOrigins**
- Lista de orígenes permitidos para CORS
- `"*"` = Permite cualquier origen (⚠️ **INSEGURO EN PRODUCCIÓN**)
- ❌ **En producción**: Remover `"*"` y listar dominios específicos

---

## 🔄 **FLUJOS DE AUTENTICACIÓN (OAuth2 Flows)**

```json
"standardFlowEnabled": true,
"implicitFlowEnabled": false,
"directAccessGrantsEnabled": true,
"serviceAccountsEnabled": true,
```

### **standardFlowEnabled** (Authorization Code Flow)
- `true` = Habilita el flujo estándar OAuth2 (redirección al login)
- **Uso**: Login desde navegador con redirección automática a Keycloak
- ✅ **Mantener en `true` si**: Quieres login automático desde el navegador
- ❌ **Cambiar a `false` si**: Solo usarás tokens JWT (Postman/APIs)

### **implicitFlowEnabled** (Implicit Flow)
- `false` = Flujo implícito deshabilitado
- **Uso**: Apps JavaScript SPA (obsoleto, inseguro)
- ✅ **Recomendación**: Dejar en `false` (usar Authorization Code + PKCE)

### **directAccessGrantsEnabled** (Resource Owner Password Credentials)
- `true` = Permite obtener tokens con username/password directamente
- **Uso**: Postman, cURL, scripts - enviar credenciales directamente
- ✅ **Mantener en `true` si**: Usas Postman para testing
- ❌ **Cambiar a `false` en producción**: Es menos seguro

### **serviceAccountsEnabled**
- `true` = Permite que el cliente actúe como service account
- **Uso**: Autenticación servidor-a-servidor sin usuario
- ✅ **Recomendación**: `true` si necesitas machine-to-machine auth

---

## 📦 **SCOPES (Alcances de Información)**

```json
"defaultClientScopes": [
  "web-origins",
  "acr",
  "profile",
  "roles",
  "email"
],
"optionalClientScopes": [
  "address",
  "phone",
  "offline_access",
  "microprofile-jwt"
]
```

### **defaultClientScopes** (Siempre incluidos en el token)
- `web-origins` → CORS origins
- `acr` → Authentication Context Class Reference
- `profile` → Nombre, apellido, username
- `roles` → **⭐ CRÍTICO** - Roles del usuario (OPERADOR, CLIENTE, etc.)
- `email` → Email del usuario

❌ **NO REMOVER** `roles` - Lo necesitas para autorización

### **optionalClientScopes** (Se piden explícitamente)
- `address` → Dirección física
- `phone` → Número de teléfono
- `offline_access` → Refresh tokens (tokens de larga duración)
- `microprofile-jwt` → Claims adicionales para MicroProfile

---

## 👤 **USUARIOS PRECARGADOS**

```json
"users": [
  {
    "username": "cliente1",
    "enabled": true,
    "emailVerified": true,
    "email": "cliente1@logistica.com",
    "credentials": [...],
    "realmRoles": ["cliente"]
  }
]
```

### Campos importantes:
- **username**: Identificador único
- **enabled**: `true` = Usuario activo | `false` = Usuario bloqueado
- **emailVerified**: `true` = Email ya verificado (sin confirmación)
- **credentials.value**: Contraseña del usuario
- **credentials.temporary**: `false` = No forzar cambio de contraseña
- **realmRoles**: Roles asignados (CLIENTE, OPERADOR, etc.)

✅ **Personaliza**: Agrega, elimina o modifica usuarios según necesites

---

## 🎭 **ROLES DEL SISTEMA**

```json
"roles": {
  "realm": [
    {
      "name": "cliente",
      "description": "Cliente registrado - gestiona sus contenedores y solicitudes"
    },
    {
      "name": "operador",
      "description": "Personal operativo - gestiona rutas, tramos, camiones"
    },
    {
      "name": "transportista",
      "description": "Conductor - inicia/finaliza tramos"
    },
    {
      "name": "admin",
      "description": "Administrador - acceso total"
    }
  ]
}
```

✅ **Personaliza**: Agrega o elimina roles según tu sistema

---

## 🎯 **CONFIGURACIONES RECOMENDADAS POR ESCENARIO**

### 📱 **Escenario 1: Solo APIs con Postman/cURL (Sin login de navegador)**
```json
"standardFlowEnabled": false,           // ❌ No necesitas redirección
"directAccessGrantsEnabled": true,      // ✅ Postman obtiene tokens
"registrationAllowed": false,           // ❌ Admin crea usuarios
"bruteForceProtected": true,            // ✅ Protección
```

### 🌐 **Escenario 2: Login desde navegador + APIs**
```json
"standardFlowEnabled": true,            // ✅ Redirección automática
"directAccessGrantsEnabled": true,      // ✅ También permite Postman
"registrationAllowed": false,           // ❌ Solo admin crea usuarios
"bruteForceProtected": true,            // ✅ Protección
```

### 🚀 **Escenario 3: Registro público (Clientes se registran solos)**
```json
"standardFlowEnabled": true,            // ✅ Login desde navegador
"directAccessGrantsEnabled": false,     // ❌ Solo OAuth2 flow
"registrationAllowed": true,            // ✅ Auto-registro
"resetPasswordAllowed": true,           // ✅ Recuperar contraseña
"bruteForceProtected": true,            // ✅ Protección
```

---

## ⚠️ **SEGURIDAD EN PRODUCCIÓN - CAMBIOS OBLIGATORIOS**

1. **Cambiar el secret del cliente**:
   ```json
   "secret": "TU-SECRET-SEGURO-AQUI-MIN-32-CARACTERES"
   ```

2. **Eliminar wildcards de URLs**:
   ```json
   "redirectUris": [
     "https://tu-dominio.com/login/oauth2/code/keycloak"
   ],
   "webOrigins": [
     "https://tu-dominio.com"
   ]
   ```

3. **Habilitar SSL**:
   ```json
   "sslRequired": "all"
   ```

4. **Habilitar protección contra brute force**:
   ```json
   "bruteForceProtected": true
   ```

5. **Deshabilitar direct access grants**:
   ```json
   "directAccessGrantsEnabled": false
   ```

---

## 📝 **CÓMO USAR ESTE ARCHIVO**

1. **Editar**: Modifica `realm-logistica.json` según esta guía
2. **Reiniciar Keycloak**: `docker-compose down && docker-compose up -d`
3. **Verificar**: Accede a http://localhost:9090 y verifica los cambios

---

## 🔧 **COMANDOS ÚTILES**

```bash
# Reiniciar Keycloak con nueva configuración
docker-compose -f docker-compose.keycloak.yml down
docker-compose -f docker-compose.keycloak.yml up -d

# Ver logs de Keycloak
docker-compose -f docker-compose.keycloak.yml logs -f

# Acceder a Keycloak Admin
http://localhost:9090
Usuario: admin
Contraseña: admin
```

---

**¿Dudas?** Revisa esta guía cada vez que quieras modificar la configuración de Keycloak. 🎯
