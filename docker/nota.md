# 🐳 Guía de Comandos Docker - Keycloak

## 📑 Índice

- [Ubicación](#-ubicación)
- [Comandos Básicos](#-comandos-básicos)
  - [Levantar Keycloak](#levantar-keycloak)
  - [Ver logs en tiempo real](#ver-logs-en-tiempo-real)
  - [Detener Keycloak](#detener-keycloak-sin-eliminar)
  - [Detener y eliminar](#detener-y-eliminar-keycloak)
  - [Dar de baja el compose completo](#dar-de-baja-el-compose-completo)
- [Comandos de Diagnóstico](#-comandos-de-diagnóstico)
- [Comandos de Reinicio](#-comandos-de-reinicio)
- [Limpieza](#-limpieza)
- [Verificación de Estado](#-verificación-de-estado)
- [Acceso al Contenedor](#-acceso-al-contenedor)
- [Backup y Restore](#-backup-y-restore-solo-con-postgresql)
- [Troubleshooting](#-troubleshooting)
- [Notas Importantes](#-notas-importantes)
- [Enlaces Útiles](#-enlaces-útiles)
- [Comandos Rápidos (Resumen)](#-comandos-rápidos-resumen)

---

Esta guía contiene todos los comandos necesarios para gestionar el contenedor de Keycloak en el proyecto.

---

## 📍 Ubicación

Todos los comandos deben ejecutarse desde la carpeta `docker/`:

```powershell
cd "F:\Archivos\Facultad\Sistemas\Tercer Año\Backend de aplicaciones\Trabajo Practico\docker"
```

---

## 🚀 Comandos Básicos

### Levantar Keycloak

```powershell
docker compose -f docker-compose.keycloak.yml up -d
```

**¿Qué hace?**
- `-f docker-compose.keycloak.yml`: especifica el archivo compose
- `up`: crea e inicia los contenedores
- `-d`: modo detached (en segundo plano)

---

### Ver logs en tiempo real

```powershell
docker logs -f keycloak
```

**Salir de los logs:** `Ctrl + C`

---

### Detener Keycloak (sin eliminar)

```powershell
docker stop keycloak
```

**El contenedor queda guardado, podés reiniciarlo con:**

```powershell
docker start keycloak
```

---

### Detener y eliminar Keycloak

```powershell
docker stop keycloak
docker rm keycloak
```

**O en una sola línea:**

```powershell
docker stop keycloak ; docker rm keycloak
```

⚠️ **Importante:** Con H2 en memoria, esto **borra todos los datos** (usuarios, configuraciones).

---

### Dar de baja el compose completo

```powershell
docker compose -f docker-compose.keycloak.yml down
```

**¿Qué hace?**
- Detiene todos los contenedores del compose
- Elimina contenedores
- Elimina redes creadas
- **NO elimina volúmenes** (datos persisten si usas PostgreSQL)

**Para eliminar TODO (incluyendo volúmenes):**

```powershell
docker compose -f docker-compose.keycloak.yml down -v
```

---

## 🔍 Comandos de Diagnóstico

### Ver estado del contenedor

```powershell
docker ps --filter "name=keycloak"
```

**Mostrar también contenedores detenidos:**

```powershell
docker ps -a --filter "name=keycloak"
```

---

### Ver información completa del contenedor

```powershell
docker inspect keycloak
```

---

### Ver uso de recursos

```powershell
docker stats keycloak
```

**Salir:** `Ctrl + C`

---

### Ver puertos mapeados

```powershell
docker port keycloak
```

**Respuesta esperada:**
```
8080/tcp -> 0.0.0.0:8180
```

---

## 🔄 Comandos de Reinicio

### Reiniciar Keycloak (mantiene datos si usa PostgreSQL)

```powershell
docker restart keycloak
```

---

### Reinicio completo (recrear contenedor)

```powershell
docker compose -f docker-compose.keycloak.yml down
docker compose -f docker-compose.keycloak.yml up -d
```

---

### Forzar recreación (sin usar caché)

```powershell
docker compose -f docker-compose.keycloak.yml up -d --force-recreate
```

---

## 🧹 Limpieza

### Eliminar contenedor y red

```powershell
docker compose -f docker-compose.keycloak.yml down
```

---

### Eliminar TODO (contenedor, red, volúmenes, datos)

```powershell
docker compose -f docker-compose.keycloak.yml down -v
```

⚠️ **Cuidado:** Esto borra TODOS los datos de Keycloak.

---

### Limpiar imágenes no usadas

```powershell
docker image prune -a
```

---

### Limpiar todo Docker (contenedores, redes, volúmenes, imágenes)

```powershell
docker system prune -a --volumes
```

⚠️ **Muy peligroso:** Solo usar si querés limpiar TODO Docker.

---

## 📊 Verificación de Estado

### ¿Está corriendo?

```powershell
docker ps --filter "name=keycloak" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
```

---

### ¿Responde el endpoint?

```powershell
Invoke-WebRequest -Uri "http://localhost:8180/realms/logistica" -UseBasicParsing
```

**Respuesta esperada:** Status 200

---

### Ver últimas 50 líneas de logs

```powershell
docker logs --tail 50 keycloak
```

---

### Ver logs desde una hora específica

```powershell
docker logs --since 1h keycloak
```

---

## 🔐 Acceso al Contenedor

### Abrir shell interactivo dentro del contenedor

```powershell
docker exec -it keycloak bash
```

**Salir:** `exit`

---

### Ejecutar un comando dentro del contenedor

```powershell
docker exec keycloak ls -la /opt/keycloak/data/import
```

---

## 💾 Backup y Restore (solo con PostgreSQL)

### Backup de la BD de Keycloak

```powershell
docker exec postgres pg_dump -U keycloak keycloak > backup_keycloak.sql
```

---

### Restore desde backup

```powershell
docker exec -i postgres psql -U keycloak keycloak < backup_keycloak.sql
```

---

## 🆘 Troubleshooting

### El contenedor se detiene solo

Ver logs para identificar el error:

```powershell
docker logs keycloak
```

Problemas comunes:
- Puerto 8180 ocupado → cambiar en docker-compose
- Archivo `realm-logistica.json` mal formateado → revisar JSON
- Falta memoria → revisar Docker Desktop settings

---

### No puedo acceder a http://localhost:8180

1. **Verificar que está corriendo:**
   ```powershell
   docker ps --filter "name=keycloak"
   ```

2. **Ver logs de arranque:**
   ```powershell
   docker logs keycloak
   ```

3. **Verificar puerto:**
   ```powershell
   netstat -ano | findstr :8180
   ```

4. **Probar desde el contenedor:**
   ```powershell
   docker exec keycloak curl -I http://localhost:8080
   ```

---

### Resetear completamente Keycloak

```powershell
# 1. Detener y eliminar
docker compose -f docker-compose.keycloak.yml down -v

# 2. Eliminar la imagen (fuerza re-descarga)
docker rmi quay.io/keycloak/keycloak:21.1.1

# 3. Levantar de nuevo
docker compose -f docker-compose.keycloak.yml up -d
```

---

## 📝 Notas Importantes

### Diferencias H2 vs PostgreSQL

| Comando | Con H2 (modo dev) | Con PostgreSQL |
|---------|-------------------|----------------|
| `docker restart keycloak` | ❌ Pierde datos | ✅ Mantiene datos |
| `docker rm keycloak` | ❌ Pierde TODO | ✅ Datos en volumen |
| `down -v` | ❌ Pierde TODO | ❌ Pierde TODO |

### Variables de Entorno Importantes

En `docker-compose.keycloak.yml`:

- `KEYCLOAK_ADMIN`: usuario admin (default: `admin`)
- `KEYCLOAK_ADMIN_PASSWORD`: password admin (default: `admin`)
- `KC_DB`: tipo de BD (`postgres` o usar H2 por defecto)
- `KC_DB_URL_HOST`: host de PostgreSQL

---

## 🔗 Enlaces Útiles

- **Consola Admin:** http://localhost:8180
- **Realm Logistica:** http://localhost:8180/realms/logistica
- **Token Endpoint:** http://localhost:8180/realms/logistica/protocol/openid-connect/token
- **Docs Oficiales:** https://www.keycloak.org/docs/latest/server_admin/

---

## 📌 Comandos Rápidos (Resumen)

```powershell
# Levantar
docker compose -f docker-compose.keycloak.yml up -d

# Ver logs
docker logs -f keycloak

# Detener
docker stop keycloak

# Reiniciar
docker restart keycloak

# Eliminar TODO
docker compose -f docker-compose.keycloak.yml down -v

# Estado
docker ps --filter "name=keycloak"
```

---

**Última actualización:** 30 de octubre de 2025
