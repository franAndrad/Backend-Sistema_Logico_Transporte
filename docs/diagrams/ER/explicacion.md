# Explicación detallada del Diagrama Entidad-Relación (ER)

Explicación paso a paso de cómo funciona este diagrama de base de datos para el sistema logístico de transporte de contenedores, implementado con arquitectura de microservicios y Keycloak como fuente única de verdad para usuarios.

---

## 🏗️ Arquitectura: Database per Service

Este sistema implementa el patrón **Database per Service** con dos microservicios principales:

### 📦 **ms-cliente** (DB: clientedb)
Gestiona clientes, contenedores y solicitudes de transporte.

### 🚛 **ms-transporte** (DB: transportedb)
Gestiona tarifas, rutas, tramos, camiones y depósitos.

### 🔐 **Keycloak** (Sistema externo)
Fuente única de verdad para usuarios, autenticación y autorización.

---

## 🔑 UsuarioKeycloak (Sistema Externo)

**NO es una tabla en nuestras bases de datos**, sino una entidad gestionada por Keycloak.

**Campos principales (gestionados por Keycloak):**
- `idUsuario`: Identificador único (UUID en Keycloak)
- `nombre`, `apellido`: Datos personales
- `telefono`, `email`: Información de contacto
- `username`, `password`: Credenciales de acceso
- `rol`: Define el tipo de usuario (cliente, operador, transportista)
- `activo`: Indica si el usuario está habilitado

**¿Cómo se relaciona con nuestro sistema?**
- Los microservicios guardan solo el `keyCloakId` (string/UUID)
- Los datos personales se consultan via Keycloak Admin API cuando se necesitan
- La autenticación es manejada por Keycloak (JWT tokens)

**Ventajas:**
- ✅ Sin duplicación de datos de usuario
- ✅ Autenticación centralizada
- ✅ Single Sign-On (SSO)
- ✅ Gestión de roles y permisos centralizada

---

## 📊 Entidades - Microservicio Cliente (clientedb)

### 1. Cliente

Perfil específico para usuarios que solicitan servicios de transporte.

**Campos principales:**
- `idCliente`: Identificador único del cliente (PK)
- `keyCloakId`: Referencia al usuario en Keycloak (referencia lógica)
- `direccionFacturacion`: Dirección para facturación
- `direccionEnvio`: Dirección de envío por defecto
- `razonSocial`: Nombre de la empresa (si aplica)
- `cuit`: Identificación fiscal (si es empresa)

**Relaciones:**
- **Referencia lógica** a UsuarioKeycloak (no FK física)
- Puede tener múltiples Contenedores (FK física)
- Puede realizar múltiples Solicitudes (FK física)

**Nota importante:** 
- `keyCloakId` NO es una FK física, es una referencia lógica
- Los datos personales (nombre, email, etc.) se obtienen de Keycloak
- La validación de existencia del usuario se hace via API HTTP a Keycloak

---

### 2. Contenedor

### 2. Contenedor

Representa unidades de carga del cliente.

**Campos principales:**
- `idContenedor`: Identificador único (PK)
- `idCliente`: Referencia al cliente propietario (FK física)
- `peso`: Peso en kilogramos
- `volumen`: Volumen en metros cúbicos
- `estado`: Estado actual (en_origen, en_transito, en_deposito, entregado)
- `ubicacionActual`: Descripción textual del lugar actual
- `activo`: Indica si el contenedor está disponible para uso

**Relaciones:**
- Pertenece a un Cliente (FK física en la misma DB)
- Se usa en Solicitudes (FK física en la misma DB)

**Modelo de negocio:**
- ✅ Un contenedor puede usarse en múltiples solicitudes a lo largo del tiempo
- ✅ El campo `activo` permite desactivar contenedores sin eliminarlos
- ✅ `ubicacionActual` se actualiza en tiempo real durante el transporte
- ✅ Esto permite mantener el historial completo de uso

---

### 3. Solicitud

Representa una petición de transporte por parte de un cliente.

**Campos principales:**
- `idSolicitud`: Identificador único (PK)
- `idCliente`: Cliente que realiza la solicitud (FK física)
- `idContenedor`: Contenedor a transportar (FK física)
- `idTarifa`: Tarifa aplicada (nullable, **referencia lógica** a ms-transporte)
- `origen_direccion`, `origen_latitud`, `origen_longitud`: Punto de origen
- `destino_direccion`, `destino_latitud`, `destino_longitud`: Punto de destino
- `costoEstimado`: Costo estimado inicial
- `tiempoEstimado`: Tiempo estimado inicial
- `costoFinal`: Costo real final
- `tiempoReal`: Tiempo real transcurrido
- `fechaCreacion`, `fechaActualizacion`: Timestamps de auditoría
- `estado`: Estado del proceso (borrador, programada, asignada, en_transito, en_deposito, entregada, cancelada)

**Relaciones:**
- Pertenece a un Cliente (FK física)
- Referencia un Contenedor (FK física)
- Tiene una Ruta asociada en ms-transporte (**referencia lógica**, sin FK física)
- Usa una Tarifa de ms-transporte (**referencia lógica**)

**⚠️ Importante - Referencias entre microservicios:**
- `idTarifa` es una **referencia lógica** (NO hay FK física porque Tarifa está en otra DB)
- La validación se hace en código: ms-cliente llama a ms-transporte via HTTP
- Si Tarifa existe en ms-transporte, se guarda el ID
- Si no existe o se elimina, la validación falla en tiempo de ejecución

**Lógica de negocio clave:**
- Estado inicial: `estado` = 'borrador', `idTarifa` puede ser null
- Al programar: ms-cliente consulta a ms-transporte para calcular `costoEstimado`
- Al confirmar: se asigna Ruta (en ms-transporte) y se guarda la referencia
- Al completar: se registra `costoFinal` y `tiempoReal`
- ✅ Las coordenadas permiten cálculos precisos de distancia y ruta

---

## 📊 Entidades - Microservicio Transporte (transportedb)

### 4. Tarifa

Define los costos de transporte vigentes.

**Campos principales:**
- `idTarifa`: Identificador único (PK)
- `concepto`: Descripción de la tarifa
- `valorBase`: Costo base del servicio
- `valorPorKm`: Costo por kilómetro recorrido
- `valorPorPeso`: Costo adicional por peso
- `valorPorVolumen`: Costo adicional por volumen
- `valorPorTramo`: Costo por cada tramo de la ruta
- `valorLitroCombustible`: Precio del combustible para cálculos
- `fechaVigencia`: Fecha desde la cual aplica
- `activo`: Indica si la tarifa está vigente

**Relaciones:**
- Es referenciada por Solicitudes en ms-cliente (**referencia lógica**)

**Modelo de versionado:**
- ✅ Múltiples tarifas pueden existir con diferentes `fechaVigencia`
- ✅ Solo una tarifa debe estar `activo=true` a la vez
- ✅ Solicitudes antiguas mantienen referencia a tarifas históricas
- ✅ Esto permite auditoría completa de cambios de precios

---

### 5. Ruta

Define el camino completo que seguirá un transporte.

**Campos principales:**
- `idRuta`: Identificador único (PK)
- `idSolicitud`: Referencia a la Solicitud en ms-cliente (**referencia lógica**)
- `cantidadTramos`: Número de segmentos de la ruta
- `cantidadDepositos`: Número de paradas en depósitos
- `distanciaTotal`: Distancia total en kilómetros
- `estado`: Estado de la ruta (estimada, asignada, en_progreso, completada)

**Relaciones:**
- **Referencia lógica** a Solicitud (en otra DB)
- Se divide en múltiples Tramos (FK física)

**⚠️ Importante - Referencia entre microservicios:**
- `idSolicitud` es una **referencia lógica** (NO hay FK física)
- La validación se hace en código: ms-transporte llama a ms-cliente via HTTP
- Antes de crear una Ruta, se verifica que la Solicitud existe
- Si la Solicitud no existe, la operación falla

**Diseño modular:**
- ✅ Una ruta se compone de varios tramos (segmentos)
- ✅ Cada tramo puede tener depósitos de origen/destino opcionales
- ✅ Permite rutas directas (sin paradas) o complejas (con múltiples paradas)

---

### 6. Tramo

Segmento individual de una ruta (de punto A a punto B).

**Campos principales:**
- `idTramo`: Identificador único (PK)
- `idRuta`: Ruta a la que pertenece (FK física)
- `idDepositoOrigen`: Depósito de inicio (FK física, **nullable**)
- `idDepositoDestino`: Depósito de fin (FK física, **nullable**)
- `keyCloakIdTransportista`: Usuario transportista (referencia lógica a Keycloak)
- `dominioCamion`: Camión asignado (FK física, string)
- `tipo`: Tipo de tramo (origen-deposito, deposito-deposito, deposito-destino, origen-destino)
- `estado`: Progreso (planificado, asignado, iniciado, finalizado, cancelado)
- `distancia`: Kilómetros del segmento
- `costoAproximado`, `costoReal`: Costos estimado y final
- `fechaHoraInicioEstimada`, `fechaHoraFinEstimada`: Tiempos planificados
- `fechaHoraInicio`, `fechaHoraFin`: Tiempos reales (nullable)
- `fechaActualizacion`: Timestamp de última modificación

**Relaciones:**
- Pertenece a una Ruta (FK física)
- **Opcionalmente** tiene origen/destino en Depositos (FK física, nullable)
- Es realizado por un Camion (FK física, string)
- Es operado por un Usuario transportista (referencia lógica a Keycloak)

**⚠️ Referencias mixtas:**
- `keyCloakIdTransportista`: **Referencia lógica** a Keycloak (validación via API HTTP)
- `idRuta`, `dominioCamion`, `idDepositoOrigen`, `idDepositoDestino`: **FK físicas** (validadas por PostgreSQL)

**Modelo de asignación dinámica:**
- ✅ `dominioCamion` es **string** (FK a `Camion.dominio`)
- ✅ Depósitos son opcionales (nullable) para permitir rutas directas
- ✅ `tipo` define automáticamente qué campos son obligatorios
- ✅ Seguimiento temporal completo: estimado vs real

**Tipos de tramo:**
1. **origen-deposito**: Desde dirección de origen hasta un depósito intermedio
2. **deposito-deposito**: Entre dos depósitos
3. **deposito-destino**: Desde último depósito hasta dirección de destino
4. **origen-destino**: Ruta directa sin paradas en depósitos

---

### 7. Camion

Vehículos de transporte disponibles.

**Campos principales:**
- `dominio`: Patente/matrícula del camión (PK, string)
- `capacidadPeso`: Capacidad máxima en kilogramos
- `capacidadVolumen`: Capacidad máxima en metros cúbicos
- `disponibilidad`: Si está disponible para asignación
- `costoBaseKm`: Costo operativo por kilómetro
- `consumoCombustible`: Litros por 100 km

**Relaciones:**
- Es usado por Tramos (FK física)
- Opcionalmente asociado a un transportista en Keycloak (referencia lógica, no mostrada en diagrama)

**Modelo de asignación:**
- ✅ `dominio` como PK permite identificación única y natural
- ✅ `disponibilidad` permite gestionar qué camiones están en uso
- ✅ Información de costos permite cálculos automáticos de presupuestos

---

### 8. Deposito

Puntos intermedios de almacenamiento en las rutas.

**Campos principales:**
- `idDeposito`: Identificador único (PK)
- `nombre`: Nombre del depósito
- `direccion`: Dirección completa
- `latitud`, `longitud`: Coordenadas GPS precisas
- `costoEstadiaDiario`: Costo de almacenamiento por día

**Relaciones:**
- Puede ser origen de Tramos (FK física, nullable)
- Puede ser destino de Tramos (FK física, nullable)

**Diseño de logística:**
- ✅ Coordenadas GPS permiten cálculo automático de rutas óptimas
- ✅ `costoEstadiaDiario` permite calcular costos de almacenamiento temporal
- ✅ Un contenedor puede pasar por múltiples depósitos en su ruta
- ✅ Permite optimización: dividir rutas largas en segmentos con descansos

---

## 🔗 Tipos de Relaciones

### 🟢 FK Físicas (dentro de la misma DB)

**Validadas por PostgreSQL con FOREIGN KEY constraints:**

**En ms-cliente (clientedb):**
- `Contenedor.idCliente` → `Cliente.idCliente`
- `Solicitud.idCliente` → `Cliente.idCliente`
- `Solicitud.idContenedor` → `Contenedor.idContenedor`

**En ms-transporte (transportedb):**
- `Tramo.idRuta` → `Ruta.idRuta`
- `Tramo.idDepositoOrigen` → `Deposito.idDeposito` (nullable)
- `Tramo.idDepositoDestino` → `Deposito.idDeposito` (nullable)
- `Tramo.dominioCamion` → `Camion.dominio`

### ⚠️ Referencias Lógicas (entre diferentes sistemas)

**NO hay FK física, validación en código de aplicación:**

**Entre microservicios:**
- `Ruta.idSolicitud` → `Solicitud.idSolicitud` (ms-transporte → ms-cliente)
- `Solicitud.idTarifa` → `Tarifa.idTarifa` (ms-cliente → ms-transporte)

**A Keycloak:**
- `Cliente.keyCloakId` → Usuario en Keycloak
- `Tramo.keyCloakIdTransportista` → Usuario en Keycloak

**¿Cómo se validan?**
1. Antes de guardar, el microservicio hace una llamada HTTP al otro servicio
2. Verifica que el ID existe
3. Si existe, guarda el ID como número/string
4. Si no existe, retorna error de validación

**Ejemplo:**
```java
// En ms-transporte, antes de crear Ruta
@Service
public class RutaService {
    @Autowired
    private SolicitudClient solicitudClient; // Feign Client
    
    public Ruta crearRuta(RutaDTO dto) {
        // Validar que la solicitud existe en ms-cliente
        if (!solicitudClient.existeSolicitud(dto.getIdSolicitud())) {
            throw new ValidationException("Solicitud no encontrada");
        }
        // Crear ruta guardando solo el ID
        Ruta ruta = new Ruta();
        ruta.setIdSolicitud(dto.getIdSolicitud());
        return rutaRepository.save(ruta);
    }
}
```

---

## 🔄 Flujos de Negocio Completos

### 📋 Flujo Completo: Cliente solicita transporte de contenedor

#### **Fase 1: Registro y Autenticación (Keycloak)**

1. **Usuario se registra en Keycloak:**
   - Nombre, apellido, email, username, password
   - Se le asigna rol: "cliente"
   - Keycloak genera un UUID único (keyCloakId)

2. **Se crea perfil en ms-cliente:**
   - Se guarda `Cliente` con:
     - `keyCloakId` = UUID de Keycloak
     - `direccionFacturacion`, `direccionEnvio`
     - `razonSocial`, `cuit` (si es empresa)

3. **Cliente registra su Contenedor:**
   - Se crea `Contenedor` en ms-cliente:
     - `idCliente` (FK física a Cliente)
     - `peso`, `volumen`, `estado` = 'en_origen'
     - `activo` = true

#### **Fase 2: Solicitud de Transporte (ms-cliente)**

4. **Cliente crea Solicitud:**
   ```
   POST /api/solicitudes
   {
     "idCliente": 123,
     "idContenedor": 456,
     "origen": { "direccion": "...", "lat": -34.6, "lon": -58.4 },
     "destino": { "direccion": "...", "lat": -31.4, "lon": -64.2 },
     "estado": "borrador"
   }
   ```
   - Se guarda en tabla `Solicitud` (clientedb)
   - `idTarifa` = NULL (aún no calculado)
   - `costoEstimado` = NULL

5. **ms-cliente solicita cotización a ms-transporte:**
   ```
   GET /api/tarifas/calcular?distancia=500&peso=1000&volumen=20
   ```
   - ms-transporte calcula costo con Tarifa vigente (activo=true)
   - Retorna: `{ "costoEstimado": 5000, "idTarifa": 10 }`

6. **ms-cliente actualiza Solicitud:**
   - `costoEstimado` = 5000
   - `idTarifa` = 10 (referencia lógica)
   - `estado` = "programada"

#### **Fase 3: Planificación de Ruta (ms-transporte)**

7. **ms-transporte recibe solicitud de crear Ruta:**
   ```
   POST /api/rutas
   {
     "idSolicitud": 789 // ID de Solicitud en ms-cliente
   }
   ```

8. **ms-transporte valida Solicitud:**
   - Llama a ms-cliente via Feign Client:
     ```java
     boolean existe = solicitudClient.existeSolicitud(789);
     ```
   - Si NO existe → Error 400: "Solicitud no encontrada"
   - Si existe → continúa

9. **ms-transporte crea Ruta:**
   - Se guarda en tabla `Ruta` (transportedb):
     - `idSolicitud` = 789 (referencia lógica, solo el número)
     - `cantidadTramos` = 0 (se actualizará)
     - `estado` = "estimada"

10. **ms-transporte divide en Tramos:**
    - **Opción A: Ruta directa** (sin depósitos):
      ```
      Tramo 1:
        - tipo: "origen-destino"
        - idDepositoOrigen: NULL
        - idDepositoDestino: NULL
        - distancia: 500 km
      ```
    
    - **Opción B: Ruta con paradas** (con depósitos):
      ```
      Tramo 1:
        - tipo: "origen-deposito"
        - idDepositoOrigen: NULL
        - idDepositoDestino: 5 (FK física)
        - distancia: 300 km
      
      Tramo 2:
        - tipo: "deposito-destino"
        - idDepositoOrigen: 5 (FK física)
        - idDepositoDestino: NULL
        - distancia: 200 km
      ```

#### **Fase 4: Asignación de Recursos (ms-transporte)**

11. **Para cada Tramo, buscar Camion disponible:**
    ```sql
    SELECT * FROM Camion 
    WHERE disponibilidad = true 
      AND capacidadPeso >= 1000 
      AND capacidadVolumen >= 20
    LIMIT 1;
    ```

12. **Buscar Transportista disponible en Keycloak:**
    - ms-transporte consulta Keycloak Admin API:
      ```java
      List<UserRepresentation> transportistas = keycloakClient
          .getUsers(realm)
          .stream()
          .filter(u -> u.getRealmRoles().contains("transportista"))
          .toList();
      ```

13. **Asignar Tramo:**
    ```java
    Tramo tramo = new Tramo();
    tramo.setIdRuta(ruta.getId());
    tramo.setDominioCamion("ABC123"); // FK física a Camion
    tramo.setKeyCloakIdTransportista("uuid-keycloak"); // Ref lógica
    tramo.setEstado("asignado");
    tramoRepository.save(tramo);
    ```

14. **Actualizar disponibilidad:**
    ```sql
    UPDATE Camion SET disponibilidad = false WHERE dominio = 'ABC123';
    ```

15. **Notificar a ms-cliente:**
    - ms-transporte llama a ms-cliente:
      ```java
      solicitudClient.actualizarEstado(789, "asignada");
      ```
    - ms-cliente actualiza `Solicitud.estado` = "asignada"

#### **Fase 5: Ejecución del Transporte (ms-transporte + Keycloak)**

16. **Transportista inicia el viaje:**
    - Se autentica con Keycloak (JWT token)
    - Actualiza Tramo:
      ```sql
      UPDATE Tramo 
      SET estado = 'iniciado',
          fechaHoraInicio = NOW()
      WHERE idTramo = 1;
      ```

17. **Tracking en tiempo real:**
    - ms-transporte puede enviar actualizaciones a ms-cliente:
      ```java
      solicitudClient.actualizarUbicacion(789, lat, lon);
      ```
    - ms-cliente actualiza `Contenedor.ubicacionActual`

18. **Llegada a destino:**
    ```sql
    UPDATE Tramo 
    SET estado = 'finalizado',
        fechaHoraFin = NOW(),
        costoReal = 4800
    WHERE idTramo = 1;
    ```

19. **Liberar Camion:**
    ```sql
    UPDATE Camion SET disponibilidad = true WHERE dominio = 'ABC123';
    ```

20. **Finalizar Ruta:**
    ```sql
    UPDATE Ruta SET estado = 'completada' WHERE idRuta = 1;
    ```

21. **ms-transporte notifica a ms-cliente:**
    ```java
    solicitudClient.completarSolicitud(789, costoFinal);
    ```

22. **ms-cliente finaliza Solicitud:**
    ```sql
    UPDATE Solicitud 
    SET estado = 'entregada',
        costoFinal = 4800,
        tiempoReal = 8.5
    WHERE idSolicitud = 789;
    
    UPDATE Contenedor 
    SET estado = 'entregado',
        ubicacionActual = 'Destino Final'
    WHERE idContenedor = 456;
    ```

#### **Fase 6: Facturación y Auditoría**

23. **Cliente consulta factura:**
    - ms-cliente retorna datos de Solicitud
    - `costoFinal` está congelado (usó `idTarifa` = 10)
    - Aunque cambien las tarifas, el precio no cambia

24. **Auditoría completa:**
    - ms-cliente: historial de Solicitudes y Contenedores
    - ms-transporte: historial de Rutas, Tramos, y uso de Camiones
    - Keycloak: logs de autenticación y acciones de usuarios

---

## 📐 Cardinalidades de las Relaciones

### **Dentro de ms-cliente (clientedb):**

- `Cliente ||--o{ Contenedor`: Un cliente tiene múltiples contenedores (1:N)
- `Cliente ||--o{ Solicitud`: Un cliente crea múltiples solicitudes (1:N)
- `Contenedor ||--|| Solicitud`: Un contenedor se usa en una solicitud a la vez (1:1)

### **Dentro de ms-transporte (transportedb):**

- `Ruta ||--o{ Tramo`: Una ruta tiene múltiples tramos (1:N)
- `Tramo }o--|| Camion`: Un camión realiza múltiples tramos en el tiempo (N:1)
- `Tramo }o--o| Deposito` (origen): Un depósito puede ser origen de múltiples tramos (N:0..1)
- `Tramo }o--o| Deposito` (destino): Un depósito puede ser destino de múltiples tramos (N:0..1)

### **Referencias Lógicas (entre sistemas):**

- `Cliente }o--|| UsuarioKeycloak`: Un usuario en Keycloak puede tener un perfil de cliente (N:1)
- `Tramo }o--|| UsuarioKeycloak`: Un transportista en Keycloak realiza múltiples tramos (N:1)
- `Solicitud ||--o| Ruta`: Una solicitud tiene una ruta en ms-transporte (1:0..1)
- `Solicitud }o--|| Tarifa`: Una tarifa se usa en múltiples solicitudes (N:1)

---

## 📝 Resumen del Modelo

### ✅ Fortalezas de esta arquitectura:

1. **Separación de responsabilidades:**
   - ms-cliente: gestiona clientes, contenedores y solicitudes
   - ms-transporte: gestiona operaciones logísticas
   - Keycloak: gestiona usuarios y autenticación

2. **Escalabilidad independiente:**
   - Cada microservicio puede escalar según su carga
   - Base de datos separada evita cuellos de botella

3. **Consistencia eventual:**
   - Referencias lógicas validadas via HTTP
   - Permite operación incluso si un servicio está temporalmente no disponible

4. **Auditoría completa:**
   - Tarifa versionada: precios históricos preservados
   - Timestamps en todas las entidades críticas
   - Rastreo de estados en Solicitud, Contenedor, Tramo

5. **Flexibilidad operativa:**
   - Depósitos opcionales (nullable)
   - Asignación dinámica de camiones
   - Rutas directas o complejas

### ⚠️ Consideraciones importantes:

1. **Consistencia entre microservicios:**
   - Usar circuit breakers para manejar fallos
   - Implementar retry logic en Feign Clients
   - Considerar eventos asíncronos para sincronización

2. **Integridad referencial:**
   - FK físicas validadas por PostgreSQL (dentro de cada DB)
   - Referencias lógicas validadas en código (entre DBs)
   - Implementar soft delete para evitar romper referencias

3. **Performance:**
   - Las validaciones cruzadas entre microservicios añaden latencia
   - Cachear datos frecuentemente consultados (ej: tarifas vigentes)
   - Usar índices en campos de referencias lógicas

4. **Seguridad:**
   - JWT tokens de Keycloak para autenticación entre servicios
   - No exponer IDs internos en APIs públicas
   - Validar permisos en cada operación (roles de Keycloak)

---

## 🎯 Próximos pasos recomendados:

1. **Implementar Feign Clients** para comunicación entre microservicios
2. **Configurar Resilience4j** para circuit breakers y retry
3. **Implementar eventos** (Kafka/RabbitMQ) para consistencia eventual
4. **Agregar índices** en campos de referencias lógicas
5. **Documentar APIs** con OpenAPI/Swagger
6. **Implementar tests de integración** entre microservicios
7. **Configurar monitoring** (Prometheus + Grafana) para observabilidad


   - Tramos ordenados secuencialmente

4. **Versionado de tarifas:**
   - Precio histórico congelado por solicitud
   - Cambios futuros no afectan solicitudes confirmadas
   - Transparencia comercial total

5. **Trazabilidad completa:**
   - Seguimiento con GPS preciso (decimal)
   - Auditoría de eventos
   - Estados diferenciados (solicitud vs contenedor)

6. **Reutilización de recursos:**
   - Contenedores activos/inactivos
   - Mantiene historial completo
   - Optimiza gestión de activos

---

## 🔄 Análisis del Problema de Actualización de Tarifas

### Escenario Planteado

1. Cliente crea Solicitud → Se asigna Tarifa id=1 (al confirmar)
2. Sistema actualiza precios → Se crea Tarifa id=2 (nueva versión)
3. ¿Cómo se calcula el costo de la solicitud?

### Principio de "Precio Acordado"

En un **acuerdo comercial** el precio debe respetarse. Cuando un cliente solicita un servicio, se le cotiza con una tarifa específica. Ese es un contrato que debe mantenerse.

**Ejemplo:**

❌ **INCORRECTO:** Cambiar los precios después de acordados
- Cliente: "Me dijiste que costaría $1000"
- Sistema: "Sí, pero ahora cuesta $1200 porque cambiamos la tarifa"

✅ **CORRECTO:** Respetar el precio original
- Cliente: "Me dijiste que costaría $1000"
- Sistema: "Correcto, ese es el precio que acordamos y pagarás"

### Comportamiento Correcto del Sistema

| Aspecto | Comportamiento Correcto |
|---------|-------------------------|
| **Al crear solicitud** | idTarifa = NULL, estadoSolicitud = 'pendiente' |
| **Al confirmar solicitud** | Usar Tarifa activa del momento, calcular costoTotal, guardar idTarifa |
| **Durante el servicio** | Mantener la Tarifa original (idTarifa congelado) |
| **Al finalizar** | El costoTotal YA está calculado y congelado |
| **Nuevas solicitudes** | Usar la Tarifa activa actual (puede ser diferente) |
| **Solicitudes antiguas** | NUNCA cambiar su tarifa automáticamente |

### Modelo de Datos para Versionado

```sql
-- Solicitud #100 creada el 15/01/2024
idSolicitud: 100
idTarifa: NULL          -- Aún no confirmada
costoTotal: NULL        -- No calculado aún
estadoSolicitud: 'pendiente'

-- Se confirma el 16/01/2024 con Tarifa vigente (id=1)
idTarifa: 1             -- Tarifa congelada
costoTotal: 5000        -- Calculado con Tarifa #1
estadoSolicitud: 'confirmada'

-- El 01/02/2024 se crea nueva Tarifa (id=2) con precios más altos
-- Solicitud #100 NO se ve afectada
-- Sigue usando idTarifa=1, costoTotal=5000

-- Solicitud #200 creada el 05/02/2024
idTarifa: 2             -- Usa la nueva tarifa
costoTotal: 6000        -- Precio actualizado
```

### ¿Qué Cambia entre Estimado y Real?

Lo que varía **NO son las tarifas**, sino los **valores medidos**:

**COSTO ESTIMADO (con Tarifa id=1):**
```
Componentes:
- Distancia estimada: 100 km × $50/km = $5,000
- Combustible estimado: 20 litros × $100/litro = $2,000
- Estadía estimada: 1 día × $500/día = $500
TOTAL ESTIMADO: $7,500
```

**COSTO REAL (con la MISMA Tarifa id=1):**
```
Componentes:
- Distancia real: 120 km × $50/km = $6,000 ← Más kilómetros recorridos
- Combustible real: 25 litros × $100/litro = $2,500 ← Más consumo
- Estadía real: 3 días × $500/día = $1,500 ← Más días en depósito
TOTAL REAL: $10,000
```

**Diferencia:** +$2,500 (por condiciones reales, NO por cambio de tarifa)

### Casos donde SÍ se usa la nueva tarifa

La nueva tarifa (id=2) se aplicaría SOLO a:

1. **Nuevas solicitudes creadas después de la actualización**
   - Cualquier solicitud confirmada después del cambio usará la tarifa nueva automáticamente

2. **Solicitudes en estado "pendiente" (no confirmadas)**
   - Si una solicitud tiene `idTarifa = NULL` al momento de confirmarla, usará la tarifa vigente actual
   - Esto permite que borradores no confirmados usen precios actualizados

3. **Renegociación acordada con el cliente**
   - Solo si el cliente y la empresa acuerdan modificar las condiciones
   - Requiere actualización manual explícita

### Resumen del Sistema de Versionado

Esto garantiza:
- ✅ Respeto de acuerdos comerciales (precio congelado al confirmar)
- ✅ Trazabilidad de precios históricos (idTarifa guardado)
- ✅ Facturación correcta y transparente (costoTotal inmutable)
- ✅ Auditoría completa (qué tarifa se usó en cada solicitud)
- ✅ Flexibilidad para solicitudes no confirmadas (pueden usar nueva tarifa)

---

## 🔑 Aspectos Clave del Modelo

### 1. Centralización de Datos Personales

**Problema anterior:** Duplicación entre Usuario y Cliente

**Solución actual:**
- `Usuario`: Contiene TODOS los datos personales (nombre, apellido, teléfono, email, username, password)
- `Cliente`: Solo datos específicos del negocio (direcciones, razonSocial, CUIT)
- **Ventaja:** Sin redundancia, fácil mantenimiento, integridad de datos

### 2. Asignación Dinámica de Camiones

**Problema anterior:** `Tramo.idCamion` (int) no coincidía con `Camion.dominio` (string)

**Solución actual:**
- `Tramo.dominioCamion` (string) → `Camion.dominio` (string)
- `Tramo.idUsuarioTransportista` (int) → `Usuario.idUsuario` (donde rol='transportista')
- **Ventaja:** FK correcta, asignación clara de recursos, trazabilidad completa

### 3. Flexibilidad en Rutas

**Problema anterior:** Todas las rutas requerían depósitos

**Solución actual:**
- `Tramo.idDepositoOrigen` y `Tramo.idDepositoDestino` son **nullable**
- Permite rutas directas: origen solicitud → destino solicitud
- Permite rutas complejas: origen → depósito A → depósito B → destino
- **Ventaja:** Optimización de costos, rutas más eficientes

### 4. Precisión en Coordenadas GPS

**Problema anterior:** Tipos `float` con poca precisión

**Solución actual:**
- `latitud`: decimal(10,8) - 8 decimales de precisión
- `longitud`: decimal(11,8) - 8 decimales de precisión
- **Ventaja:** Precisión GPS exacta (~1cm), compatible con estándares

### 5. Gestión de Estados

**Problema anterior:** Un solo campo `estado` ambiguo

**Solución actual:**
- `Solicitud.estadoSolicitud`: Estado del proceso (pendiente, confirmada, en_proceso, completada)
- `Solicitud.estadoContenedor`: Estado físico (cargado, en_transito, en_deposito, entregado)
- `Contenedor.activo`: Disponibilidad para uso
- `Camion.disponibilidad`: Si está libre o asignado
- **Ventaja:** Claridad en el ciclo de vida, mejor seguimiento

### 6. Versionado de Tarifas

**Problema anterior:** Cambios de precios afectaban solicitudes antiguas

---

## 🎓 Conclusión

Este modelo de datos representa un sistema de logística robusto, normalizado y preparado para producción. Las correcciones realizadas aseguran:

1. **Integridad referencial:** Todas las FK apuntan a tipos correctos
2. **Normalización:** Sin duplicación de datos
3. **Flexibilidad:** Soporta múltiples escenarios de negocio
4. **Auditoría:** Trazabilidad completa de cambios y eventos
5. **Escalabilidad:** Diseño modular y extensible
6. **Precisión:** Tipos de datos adecuados para GPS, coordenadas, etc.
7. **Historico precios:** Precios históricos preservados

