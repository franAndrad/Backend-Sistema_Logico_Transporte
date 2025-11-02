# 🔧 Configuración de Perfiles - MS Transporte

Este microservicio usa **perfiles de Spring Boot** para diferentes ambientes.

---

## 📁 Archivos de configuración

| Archivo | Descripción |
|---------|---------------|
| `application.yml` |  Configuración base |
| `application-dev.yml` | Desarrollo con WireMock |
| `application-local.yml` | Personal (cada desarrollador) |
| `application-deploy.yml` | Producción (usa variables de entorno de Docker) |

---

## 🚀 Uso de perfiles

### Ejecutar con perfil específico

```bash
mvn spring-boot:run "-Dspring-boot.run.profiles=local"
```

### Ejecutar con perfil por defecto (dev)

```bash
mvn spring-boot:run
```

### Ejecutar con múltiples perfiles

```bash
mvn spring-boot:run "-Dspring-boot.run.profiles=local,dev"
```

---

## 📝 Notas

- El perfil **dev** se activa por defecto si no se especifica otro
- Crear `application-local.yml` para configuraciones personales (no subir a Git)
- Para producción, usar `application-deploy.yml` con variables de entorno