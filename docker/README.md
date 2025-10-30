# 🐳 Docker - Keycloak

## 📑 Índice

- [🚀 Inicio Rápido](#-inicio-rápido)
- [⚙️ Configuración con Variables de Entorno](#️-configuración-con-variables-de-entorno)
- [🔧 Variables Disponibles](#-variables-disponibles)
- [📋 Comandos Útiles](#-comandos-útiles)
- [🔒 Seguridad](#-seguridad)
- [🐛 Troubleshooting](#-troubleshooting)

---

## 🚀 Inicio Rápido

### Opción 1: Usar valores por defecto

```powershell
# Levantar Keycloak con valores por defecto
docker-compose -f docker-compose.keycloak.yml up -d
```

### Opción 2: Usar archivo .env (Recomendado)

```powershell
# 1. Crear archivo .env desde la plantilla
Copy-Item .env.example .env

# 2. Editar el archivo .env con tus valores
notepad .env

# 3. Levantar Keycloak
docker-compose -f docker-compose.keycloak.yml up -d
```

---

## ⚙️ Configuración con Variables de Entorno

### ¿Por qué usar variables de entorno?

✅ **Seguridad**: No commiteas credenciales reales al repositorio  
✅ **Flexibilidad**: Diferentes configuraciones para dev/test/prod  
✅ **Portabilidad**: Fácil de adaptar en diferentes entornos  
✅ **CI/CD**: Integración sencilla con pipelines automatizados

### Estructura de archivos

```
docker/
├── .env.example          # 📄 Plantilla con valores por defecto
├── .env                  # 🔒 Tu configuración (NO commitear)
└── docker-compose.keycloak.yml
```

### Crear tu archivo .env

**Windows (PowerShell):**
```powershell
Copy-Item .env.example .env
notepad .env
```

**Linux/macOS:**
```bash
cp .env.example .env
nano .env
```

---

## 🔧 Variables Disponibles

### 📦 KEYCLOAK_VERSION
**Descripción**: Versión de la imagen de Keycloak  
**Valor por defecto**: `21.1.1`  
**Ejemplo**: `KEYCLOAK_VERSION=22.0.0`

```yaml
# En docker-compose.keycloak.yml:
image: quay.io/keycloak/keycloak:${KEYCLOAK_VERSION:-21.1.1}
```

---

### 🏷️ KEYCLOAK_CONTAINER_NAME
**Descripción**: Nombre personalizado para el contenedor  
**Valor por defecto**: `keycloak`  
**Ejemplo**: `KEYCLOAK_CONTAINER_NAME=keycloak-dev`

```yaml
# En docker-compose.keycloak.yml:
container_name: ${KEYCLOAK_CONTAINER_NAME:-keycloak}
```

---

### 👤 KEYCLOAK_ADMIN
**Descripción**: Usuario administrador de Keycloak  
**Valor por defecto**: `admin`  
**⚠️ Recomendación**: Cambiar en producción

```yaml
# En docker-compose.keycloak.yml:
KEYCLOAK_ADMIN: ${KEYCLOAK_ADMIN:-admin}
```

**Acceso a la consola**:
- URL: `http://localhost:8180/admin`
- Usuario: Valor de `KEYCLOAK_ADMIN`
- Password: Valor de `KEYCLOAK_ADMIN_PASSWORD`

---

### 🔐 KEYCLOAK_ADMIN_PASSWORD
**Descripción**: Contraseña del administrador  
**Valor por defecto**: `admin`  
**⚠️ CRÍTICO**: Usa una contraseña fuerte en producción

**Recomendaciones de seguridad**:
- Mínimo 16 caracteres
- Combinar mayúsculas, minúsculas, números y símbolos
- No usar palabras del diccionario
- Cambiar periódicamente

**Ejemplo de contraseña fuerte**:
```env
KEYCLOAK_ADMIN_PASSWORD=K3ycl0@k#2025$Pr0d!Secur3
```

---

### 🌐 KEYCLOAK_PORT
**Descripción**: Puerto externo para acceder a Keycloak  
**Valor por defecto**: `8180`  
**Formato**: `PUERTO_HOST:8080`

```yaml
# En docker-compose.keycloak.yml:
ports:
  - "${KEYCLOAK_PORT:-8180}:8080"
```

**Ejemplos**:
```env
# Desarrollo
KEYCLOAK_PORT=8180

# Producción (detrás de reverse proxy)
KEYCLOAK_PORT=8080

# Múltiples instancias
KEYCLOAK_PORT=8181
```

---

### 📁 KEYCLOAK_IMPORT_PATH
**Descripción**: Ruta local a los archivos de importación del realm  
**Valor por defecto**: `./keycloak/import`  
**Contenido esperado**: `realm-logistica.json`

```yaml
# En docker-compose.keycloak.yml:
volumes:
  - ${KEYCLOAK_IMPORT_PATH:-./keycloak/import}:/opt/keycloak/data/import:ro
```

**Estructura esperada**:
```
docker/
└── keycloak/
    └── import/
        └── realm-logistica.json  # ← Configuración del realm
```

---

## 📋 Comandos Útiles

### Levantar Keycloak

```powershell
# Con valores por defecto
docker-compose -f docker-compose.keycloak.yml up -d

# Con archivo .env personalizado
docker-compose -f docker-compose.keycloak.yml --env-file .env up -d

# Ver logs
docker-compose -f docker-compose.keycloak.yml logs -f

# Solo crear (sin iniciar)
docker-compose -f docker-compose.keycloak.yml up --no-start
```

### Detener Keycloak

```powershell
# Detener contenedor
docker-compose -f docker-compose.keycloak.yml stop

# Detener y eliminar contenedor
docker-compose -f docker-compose.keycloak.yml down

# Detener y eliminar volúmenes
docker-compose -f docker-compose.keycloak.yml down -v
```

### Reiniciar Keycloak

```powershell
# Reiniciar contenedor
docker-compose -f docker-compose.keycloak.yml restart

# Recrear contenedor (aplica cambios en docker-compose.yml)
docker-compose -f docker-compose.keycloak.yml up -d --force-recreate
```

### Verificar configuración

```powershell
# Ver variables de entorno del contenedor
docker exec keycloak env | Select-String "KEYCLOAK"

# Ver configuración de docker-compose con variables resueltas
docker-compose -f docker-compose.keycloak.yml config

# Ver puertos abiertos
docker ps --format "table {{.Names}}\t{{.Ports}}" | Select-String "keycloak"

# Verificar que el realm se importó correctamente
docker logs keycloak | Select-String "import"
```

### Acceder al contenedor

```powershell
# Shell interactivo
docker exec -it keycloak bash

# Ver configuración interna de Keycloak
docker exec keycloak cat /opt/keycloak/conf/keycloak.conf

# Ver realms importados
docker exec keycloak ls -la /opt/keycloak/data/import/
```

---

## 🔒 Seguridad

### ⚠️ Configuración Actual (Desarrollo)

El `docker-compose.keycloak.yml` está configurado para **DESARROLLO**:

- ❌ Usa `start-dev` (modo desarrollo)
- ❌ No tiene HTTPS
- ❌ Base de datos H2 en memoria (datos se pierden)
- ❌ Credenciales por defecto débiles
- ❌ Puerto expuesto directamente

### ✅ Checklist de Seguridad para Producción

#### 1. Variables de Entorno

```env
# ❌ NO HACER (desarrollo)
KEYCLOAK_ADMIN=admin
KEYCLOAK_ADMIN_PASSWORD=admin

# ✅ HACER (producción)
KEYCLOAK_ADMIN=admin_$(openssl rand -hex 4)
KEYCLOAK_ADMIN_PASSWORD=$(openssl rand -base64 32)
```

#### 2. Cambiar a modo producción

Modificar `docker-compose.keycloak.yml`:

```yaml
command: 
  - start  # ← Cambiar de 'start-dev' a 'start'
  - --import-realm
  - --hostname=auth.tudominio.com
  - --https-certificate-file=/opt/keycloak/certs/cert.pem
  - --https-certificate-key-file=/opt/keycloak/certs/key.pem
```

#### 3. Base de datos externa

Agregar servicio PostgreSQL:

```yaml
services:
  keycloak-db:
    image: postgres:15
    environment:
      POSTGRES_DB: ${KEYCLOAK_DB_NAME:-keycloak}
      POSTGRES_USER: ${KEYCLOAK_DB_USER:-keycloak}
      POSTGRES_PASSWORD: ${KEYCLOAK_DB_PASSWORD}
    volumes:
      - keycloak-db-data:/var/lib/postgresql/data

  keycloak:
    depends_on:
      - keycloak-db
    environment:
      KC_DB: postgres
      KC_DB_URL: jdbc:postgresql://keycloak-db:5432/${KEYCLOAK_DB_NAME:-keycloak}
      KC_DB_USERNAME: ${KEYCLOAK_DB_USER:-keycloak}
      KC_DB_PASSWORD: ${KEYCLOAK_DB_PASSWORD}

volumes:
  keycloak-db-data:
```

#### 4. Reverse Proxy

Usar Nginx/Traefik delante de Keycloak:

```yaml
services:
  nginx:
    image: nginx:alpine
    ports:
      - "443:443"
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf:ro
      - ./certs:/etc/nginx/certs:ro
    depends_on:
      - keycloak

  keycloak:
    # No exponer puerto directamente
    expose:
      - "8080"
```

#### 5. Proteger archivo .env

```powershell
# Asegurar que .env está en .gitignore
git check-ignore .env

# Resultado esperado: .env

# Si no está ignorado, agregarlo
Add-Content ../.gitignore "`n# Variables de entorno`n.env`ndocker/.env"

# Verificar permisos (Linux/macOS)
chmod 600 .env
```

---

## 🐛 Troubleshooting

### Problema: Variables no se aplican

**Síntoma**: Keycloak usa valores por defecto aunque tengas .env

**Solución**:
```powershell
# 1. Verificar que .env existe en el directorio correcto
Test-Path .env

# 2. Ver contenido del .env
Get-Content .env

# 3. Recrear contenedor
docker-compose -f docker-compose.keycloak.yml down
docker-compose -f docker-compose.keycloak.yml up -d

# 4. Verificar que se aplicaron
docker-compose -f docker-compose.keycloak.yml config
```

---

### Problema: Puerto ya en uso

**Síntoma**: `Error: Bind for 0.0.0.0:8180 failed: port is already allocated`

**Solución**:
```powershell
# Ver qué proceso usa el puerto
netstat -ano | findstr ":8180"

# Cambiar el puerto en .env
KEYCLOAK_PORT=8181

# O detener el proceso conflictivo
Stop-Process -Id <PID>
```

---

### Problema: Realm no se importa

**Síntoma**: Al acceder a Keycloak no existe el realm "logistica"

**Solución**:
```powershell
# 1. Verificar que existe el archivo
Test-Path ./keycloak/import/realm-logistica.json

# 2. Verificar logs de importación
docker logs keycloak | Select-String "import"

# 3. Verificar que el volumen está montado correctamente
docker inspect keycloak | Select-String "Mounts" -Context 0,10

# 4. Verificar permisos del archivo (Linux/macOS)
ls -la ./keycloak/import/realm-logistica.json

# 5. Forzar reimportación (eliminar contenedor y volúmenes)
docker-compose -f docker-compose.keycloak.yml down -v
docker-compose -f docker-compose.keycloak.yml up -d
```

---

### Problema: No puedo acceder a la consola admin

**Síntoma**: Error 401 o credenciales inválidas

**Solución**:
```powershell
# 1. Verificar credenciales en .env
Get-Content .env | Select-String "KEYCLOAK_ADMIN"

# 2. Ver variables en el contenedor
docker exec keycloak env | Select-String "KEYCLOAK_ADMIN"

# 3. Verificar que Keycloak está listo
docker logs keycloak | Select-String "Listening"

# 4. Acceder a la URL correcta
# http://localhost:${KEYCLOAK_PORT}/admin

# 5. Reiniciar contenedor con nuevas credenciales
docker-compose -f docker-compose.keycloak.yml down
docker-compose -f docker-compose.keycloak.yml up -d
```

---

### Problema: Cambios en .env no se aplican

**Síntoma**: Modifico .env pero Keycloak sigue usando valores antiguos

**Explicación**: Docker Compose cachea algunos valores

**Solución**:
```powershell
# 1. Detener y eliminar contenedor
docker-compose -f docker-compose.keycloak.yml down

# 2. Limpiar caché de Docker Compose
docker-compose -f docker-compose.keycloak.yml rm -f

# 3. Recrear desde cero
docker-compose -f docker-compose.keycloak.yml up -d --force-recreate

# 4. Verificar configuración resultante
docker-compose -f docker-compose.keycloak.yml config
```

---

## 📚 Referencias

- [Documentación oficial de Keycloak](https://www.keycloak.org/documentation)
- [Keycloak en Docker](https://www.keycloak.org/server/containers)
- [Variables de entorno en Docker Compose](https://docs.docker.com/compose/environment-variables/)
- [Guía de seguridad de Keycloak](https://www.keycloak.org/server/configuration-production)

---

## 📝 Ejemplo Completo

### Archivo .env para desarrollo

```env
KEYCLOAK_VERSION=21.1.1
KEYCLOAK_CONTAINER_NAME=keycloak-dev
KEYCLOAK_ADMIN=admin
KEYCLOAK_ADMIN_PASSWORD=admin
KEYCLOAK_PORT=8180
KEYCLOAK_IMPORT_PATH=./keycloak/import
```

### Comando para levantar

```powershell
docker-compose -f docker-compose.keycloak.yml up -d
```

### Verificación

```powershell
# Estado del contenedor
docker ps | Select-String "keycloak"

# Logs
docker logs -f keycloak

# Acceder a la consola
start http://localhost:8180/admin
```

---

<div align="center">

**¿Necesitas ayuda?** Revisa la sección [🐛 Troubleshooting](#-troubleshooting)

[⬆️ Volver arriba](#-docker---keycloak)

</div>
