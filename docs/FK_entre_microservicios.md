# Manejo de Foreign Keys en Arquitectura de Microservicios

## 📑 Índice

- [Problema: Database per Service](#-problema-database-per-service)
- [Solución: Referencias Lógicas + Validación en Código](#-solución-referencias-lógicas--validación-en-código)
  - [FK Físicas (Dentro de la Misma BD)](#1️⃣-fk-físicas-dentro-de-la-misma-bd)
  - [Referencias Lógicas (Entre Microservicios)](#2️⃣-referencias-lógicas-entre-microservicios)
- [Implementación en el Sistema Logístico](#-implementación-en-el-sistema-logístico)
  - [DB Cliente (ms-cliente)](#db-cliente-ms-cliente)
  - [DB Transporte (ms-transporte)](#db-transporte-ms-transporte)
- [Validaciones en Código](#-validaciones-en-código)
- [Patrones de Comunicación](#-patrones-de-comunicación-entre-microservicios)
- [Manejo de Inconsistencias](#-manejo-de-inconsistencias)
- [Estrategias Avanzadas](#-estrategias-avanzadas-opcional)
- [Decisiones de Diseño](#-decisiones-de-diseño-para-este-proyecto)
- [Resumen Ejecutivo](#-resumen-ejecutivo)

---

## 🎯 Problema: Database per Service

En una arquitectura de microservicios con **Database per Service**, cada microservicio tiene su propia base de datos independiente. Esto genera un desafío:

**¿Cómo manejamos las relaciones entre entidades que están en bases de datos diferentes?**

---

## ✅ Solución: Referencias Lógicas + Validación en Código

### 1️⃣ FK Físicas (Dentro de la Misma BD)

Cuando las entidades están en la **misma base de datos**, usamos **FK físicas normales**:

#### DB Cliente (ms-cliente):
```sql
CREATE TABLE cliente (
    id_cliente BIGINT PRIMARY KEY,
    keycloak_id VARCHAR(255) UNIQUE NOT NULL,
    direccion_facturacion VARCHAR(255),
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE contenedor (
    id_contenedor BIGINT PRIMARY KEY,
    id_cliente BIGINT NOT NULL,
    peso DECIMAL(10,2),
    volumen DECIMAL(10,2),
    estado VARCHAR(50),
    FOREIGN KEY (id_cliente) REFERENCES cliente(id_cliente) -- ✅ FK física
);

CREATE TABLE solicitud (
    id_solicitud BIGINT PRIMARY KEY,
    id_cliente BIGINT NOT NULL,
    id_contenedor BIGINT NOT NULL,
    estado VARCHAR(50),
    costo_estimado DECIMAL(10,2),
    FOREIGN KEY (id_cliente) REFERENCES cliente(id_cliente),     -- ✅ FK física
    FOREIGN KEY (id_contenedor) REFERENCES contenedor(id_contenedor) -- ✅ FK física
);
```

**Ventajas:**
- ✅ PostgreSQL valida automáticamente
- ✅ No se pueden crear solicitudes para clientes inexistentes
- ✅ No se pueden eliminar clientes con contenedores

---

### 2️⃣ Referencias Lógicas (Entre BDs Diferentes)

Cuando las entidades están en **bases de datos diferentes**, usamos **referencias lógicas**:

#### DB Transporte (ms-transporte):
```sql
CREATE TABLE tarifa (
    id_tarifa BIGINT PRIMARY KEY,
    concepto VARCHAR(100),
    valor_por_km DECIMAL(10,2),
    valor_por_peso DECIMAL(10,2),
    activo BOOLEAN DEFAULT true
);

CREATE TABLE ruta (
    id_ruta BIGINT PRIMARY KEY,
    id_solicitud BIGINT NOT NULL,  -- ⚠️ REFERENCIA LÓGICA (sin FK física)
    distancia_total_km DECIMAL(10,2),
    costo_total DECIMAL(10,2),
    estado VARCHAR(50)
    -- NO HAY: FOREIGN KEY (id_solicitud) REFERENCES solicitud(id_solicitud)
    -- Porque solicitud está en otra base de datos
);

CREATE TABLE tramo (
    id_tramo BIGINT PRIMARY KEY,
    id_ruta BIGINT NOT NULL,
    id_tarifa BIGINT NOT NULL,
    id_deposito_origen BIGINT,
    id_deposito_destino BIGINT,
    dominio_camion VARCHAR(10),
    keycloak_id_transportista VARCHAR(255),  -- ⚠️ Referencia a Keycloak
    costo_aproximado DECIMAL(10,2),
    costo_real DECIMAL(10,2),
    FOREIGN KEY (id_ruta) REFERENCES ruta(id_ruta),         -- ✅ FK física (misma DB)
    FOREIGN KEY (id_tarifa) REFERENCES tarifa(id_tarifa),   -- ✅ FK física (misma DB)
    FOREIGN KEY (id_deposito_origen) REFERENCES deposito(id_deposito), -- ✅ FK física
    FOREIGN KEY (id_deposito_destino) REFERENCES deposito(id_deposito), -- ✅ FK física
    FOREIGN KEY (dominio_camion) REFERENCES camion(dominio) -- ✅ FK física (misma DB)
);
```

---

## 🔄 Validación en la Capa de Aplicación

La integridad referencial se valida en **código Java** mediante llamadas HTTP:

### Ejemplo: Crear una Ruta

```java
@Service
public class RutaService {
    
    @Autowired
    private RutaRepository rutaRepository;
    
    @Autowired
    private SolicitudClient solicitudClient; // Feign Client a ms-cliente
    
    @Transactional
    public RutaDTO crearRuta(CrearRutaRequest request) {
        // 1. ⚠️ VALIDACIÓN: Verificar que la solicitud existe (en otra DB)
        SolicitudDTO solicitud = solicitudClient.obtenerSolicitud(request.getIdSolicitud());
        
        if (solicitud == null) {
            throw new RecursoNoEncontradoException(
                "Solicitud no encontrada: " + request.getIdSolicitud()
            );
        }
        
        // 2. Validar estado de la solicitud
        if (!solicitud.getEstado().equals("PROGRAMADA")) {
            throw new EstadoInvalidoException(
                "La solicitud debe estar en estado PROGRAMADA para crear una ruta"
            );
        }
        
        // 3. Crear la ruta (guarda solo el ID, sin FK física)
        Ruta ruta = new Ruta();
        ruta.setIdSolicitud(request.getIdSolicitud()); // Referencia lógica
        ruta.setDistanciaTotalKm(request.getDistanciaTotal());
        ruta.setCostoTotal(request.getCostoTotal());
        ruta.setEstado("ESTIMADA");
        
        return mapToDTO(rutaRepository.save(ruta));
    }
}
```

### Configuración del Feign Client

```java
@FeignClient(name = "ms-cliente", url = "${microservices.cliente.url}")
public interface SolicitudClient {
    
    @GetMapping("/api/v1/solicitudes/{id}")
    SolicitudDTO obtenerSolicitud(@PathVariable("id") Long idSolicitud);
    
    @PutMapping("/api/v1/solicitudes/{id}/estado")
    void actualizarEstado(
        @PathVariable("id") Long idSolicitud,
        @RequestBody ActualizarEstadoRequest request
    );
}
```

---

## 🔄 Consistencia Eventual con Eventos

Si se elimina o modifica una solicitud, debes notificar a ms-transporte:

### En ms-cliente (publica evento):

```java
@Service
public class SolicitudService {
    
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    @Autowired
    private SolicitudRepository solicitudRepository;
    
    public void cancelarSolicitud(Long idSolicitud) {
        Solicitud solicitud = solicitudRepository.findById(idSolicitud)
            .orElseThrow(() -> new RecursoNoEncontradoException("Solicitud no encontrada"));
        
        // Cambiar estado
        solicitud.setEstado("CANCELADA");
        solicitudRepository.save(solicitud);
        
        // 📡 Publicar evento para otros microservicios
        eventPublisher.publishEvent(
            new SolicitudCanceladaEvent(idSolicitud, solicitud.getIdCliente())
        );
    }
}
```

### En ms-transporte (escucha evento):

```java
@Component
public class SolicitudEventListener {
    
    @Autowired
    private RutaRepository rutaRepository;
    
    @EventListener
    @Async
    public void onSolicitudCancelada(SolicitudCanceladaEvent event) {
        log.info("Recibido evento: Solicitud {} cancelada", event.getIdSolicitud());
        
        // Actualizar rutas relacionadas
        List<Ruta> rutas = rutaRepository.findByIdSolicitud(event.getIdSolicitud());
        
        rutas.forEach(ruta -> {
            ruta.setEstado("CANCELADA");
            rutaRepository.save(ruta);
            log.info("Ruta {} cancelada por cancelación de solicitud {}", 
                     ruta.getIdRuta(), event.getIdSolicitud());
        });
    }
}
```

---

## 🛡️ Soft Delete (Eliminación Lógica)

En lugar de eliminar físicamente registros, desactívalos:

```sql
-- ❌ NO hacer esto:
DELETE FROM solicitud WHERE id_solicitud = 123;

-- ✅ Hacer esto:
ALTER TABLE solicitud ADD COLUMN activo BOOLEAN DEFAULT true;
UPDATE solicitud SET activo = false WHERE id_solicitud = 123;
```

**Ventajas:**
- ✅ Las referencias lógicas siguen válidas
- ✅ Auditoría completa
- ✅ Posibilidad de "restaurar" datos
- ✅ No rompe la integridad entre microservicios

---

## 📊 Resumen Visual

```
┌─────────────────────────────────────┐
│  ms-cliente (DB: clientedb)         │
│                                     │
│  Cliente (PK: id_cliente)           │
│     ↓ FK física                     │
│  Contenedor (FK: id_cliente)        │
│     ↓ FK física                     │
│  Solicitud (FK: id_cliente,         │
│             FK: id_contenedor)      │
│     id_solicitud = 123              │
└─────────────────┬───────────────────┘
                  │
                  │ Referencia lógica
                  │ (sin FK física)
                  │
                  ↓ HTTP/REST
┌─────────────────┴───────────────────┐
│  ms-transporte (DB: transportedb)   │
│                                     │
│  Ruta (ref lógica: id_solicitud=123)│
│     ↓ FK física                     │
│  Tramo (FK: id_ruta,                │
│         FK: id_tarifa,              │
│         FK: dominio_camion)         │
│     ↓ FK física                     │
│  Tarifa (PK: id_tarifa)             │
└─────────────────────────────────────┘
```

---

## 📋 Checklist de Implementación

### Cuando tienes una referencia entre BDs:

- [ ] **No crear FK física** en SQL
- [ ] **Guardar solo el ID** como número
- [ ] **Validar en código** antes de insertar
- [ ] **Usar Feign Client** para comunicación
- [ ] **Implementar circuit breaker** (resiliencia)
- [ ] **Publicar eventos** para cambios importantes
- [ ] **Usar soft delete** en lugar de DELETE físico
- [ ] **Documentar** qué son referencias lógicas en el DER
- [ ] **Agregar índices** en campos de referencia

---

## 🎯 Ejemplo Completo: Crear Solicitud y Ruta

### 1. Cliente crea solicitud (en ms-cliente):

```java
POST /api/v1/solicitudes
{
  "idCliente": 1,
  "idContenedor": 5,
  "origenDireccion": "Av. Córdoba 1000, CABA",
  "destinoDireccion": "Av. Rivadavia 500, Mendoza"
}

// ms-cliente valida con FK físicas:
// - Cliente existe (FK física)
// - Contenedor existe (FK física)
// - Contenedor pertenece al cliente

Response: 201 Created
{
  "idSolicitud": 123,
  "estado": "BORRADOR",
  "costoEstimado": null
}
```

### 2. ms-cliente pide calcular costo (llama a ms-transporte):

```java
// ms-cliente → ms-transporte
POST /api/v1/calculos/costo-estimado
{
  "idSolicitud": 123,
  "origenLatitud": -34.603722,
  "origenLongitud": -58.381592,
  "destinoLatitud": -32.889458,
  "destinoLongitud": -68.845839,
  "peso": 1500,
  "volumen": 20
}

// ms-transporte calcula usando Tarifa y Google Maps
Response: 200 OK
{
  "costoEstimado": 45000.00,
  "tiempoEstimadoHoras": 12,
  "distanciaKm": 1050.5
}

// ms-cliente actualiza la solicitud
UPDATE solicitud 
SET costo_estimado = 45000.00, 
    tiempo_estimado = 12
WHERE id_solicitud = 123;
```

### 3. Operador crea ruta (en ms-transporte):

```java
POST /api/v1/rutas
{
  "idSolicitud": 123,
  "tipo": "DIRECTA"
}

// ms-transporte VALIDA llamando a ms-cliente:
// 1. ¿Existe solicitud 123?
GET http://ms-cliente:8081/api/v1/solicitudes/123
// 2. ¿Está en estado correcto?
if (solicitud.estado != "PROGRAMADA") throw error;

// 3. Crear ruta (referencia lógica)
INSERT INTO ruta (id_solicitud, distancia_total_km, costo_total, estado)
VALUES (123, 1050.5, 45000.00, 'ESTIMADA');

Response: 201 Created
{
  "idRuta": 456,
  "idSolicitud": 123,  // ← Referencia lógica
  "estado": "ESTIMADA"
}
```

---

## ✅ Beneficios de este Enfoque

1. **Independencia**: Cada microservicio puede evolucionar su esquema
2. **Escalabilidad**: Cada BD puede escalarse independientemente
3. **Aislamiento**: Fallo en una BD no afecta a otras
4. **Flexibilidad**: Puedes cambiar el motor de BD de cada servicio
5. **Claridad**: El DER muestra claramente las fronteras de cada servicio

---

## ⚠️ Desventajas y Mitigaciones

| Desventaja | Mitigación |
|------------|------------|
| Sin validación automática de FK | Validación en código + tests |
| Posibilidad de inconsistencias | Eventos + soft delete + idempotencia |
| Más complejidad en el código | Feign Clients + Circuit Breakers |
| No hay transacciones ACID entre servicios | Patrón Saga + compensación |
| Latencia adicional (llamadas HTTP) | Caché + async cuando sea posible |

---

## 🚀 Conclusión

En arquitecturas de microservicios:

- ✅ **Usa FK físicas** dentro de cada BD
- ⚠️ **Usa referencias lógicas** entre BDs diferentes
- 🔄 **Valida en código** con llamadas HTTP
- 📡 **Usa eventos** para mantener consistencia
- 🛡️ **Implementa soft delete** para evitar romper referencias

Este enfoque mantiene la **independencia de los microservicios** mientras asegura la **integridad de los datos**.
