#  Diseño Funcional de los Microservicios  
_Especificación técnica de recursos, endpoints, roles y estructuras de datos del Sistema Logístico de Transporte de Contenedores._

---

##  Microservicio: Cliente (`ms-cliente`)

###  Responsabilidades
- Gestión de clientes y usuarios
- Registro y mantenimiento de contenedores
- Creación y consulta del estado de solicitudes de transporte
- Consulta de tarifas y estado de solicitudes

---

### 1️⃣ Recurso: **Cliente**
> “Los clientes con solicitudes activas no se eliminan físicamente; se desactivan para mantener trazabilidad.”

| Método | Endpoint | Roles Autorizados | Descripción | Datos de Entrada | Datos de Salida |
|--------|----------|-------------------|-------------|------------------|-----------------|
| **GET** | `/api/v1/clientes` | OPERADOR, ADMIN | Lista todos los clientes registrados | — | `[ { id, nombre, email, telefono, activo } ]` |
| **GET** | `/api/v1/clientes/{id}` | CLIENTE (propio), OPERADOR, ADMIN | Obtiene los datos de un cliente específico | `id` (path) | `{ id, nombre, apellido, email, direccionFacturacion, razonSocial, cuit }` |
| **POST** | `/api/v1/clientes` | PÚBLICO (registro), OPERADOR | Crea un nuevo cliente y usuario asociado | `{ nombre, apellido, email, password, telefono, direccionFacturacion }` | `{ idCliente, mensaje: "Cliente creado correctamente" }` |
| **PUT** | `/api/v1/clientes/{id}` | CLIENTE (propio), OPERADOR | Actualiza información del cliente | `{ telefono, direccionFacturacion }` | `{ mensaje: "Datos actualizados" }` |
| **DELETE** | `/api/v1/clientes/{id}` | ADMIN | Elimina (o desactiva) un cliente | `id` | `{ mensaje: "Cliente eliminado" }` |

**Ejemplo de creación:**
```json
{
  "nombre": "Juan",
  "apellido": "Pérez",
  "email": "juan@example.com",
  "password": "12345",
  "telefono": "+54 9 341 1234567",
  "direccionFacturacion": "Av. Corrientes 1234, Buenos Aires"
}
```

---

### 2️⃣ Recurso: **Contenedor**
> 🔒 No posee DELETE físico. Se utiliza borrado lógico (`activo = false`) para mantener la trazabilidad.

| Método | Endpoint | Roles | Descripción | Datos de Entrada | Datos de Salida |
|--------|----------|-------|-------------|------------------|-----------------|
| **GET** | `/api/v1/contenedores` | OPERADOR, ADMIN | Lista todos los contenedores | — | `[ { id, identificacion, peso, volumen, estado, clienteId } ]` |
| **GET** | `/api/v1/contenedores/{id}` | CLIENTE (propio), OPERADOR | Consulta un contenedor específico | `id` | `{ id, identificacion, peso, volumen, estado, activo }` |
| **GET** | `/api/v1/contenedores/cliente/{clienteId}` | CLIENTE (propio), OPERADOR | Lista los contenedores de un cliente | `clienteId` | `[ ... ]` |
| **POST** | `/api/v1/contenedores` | CLIENTE, OPERADOR | Crea un nuevo contenedor | `{ identificacion, peso, volumen, estado, clienteId }` | `{ id, mensaje: "Contenedor registrado" }` |
| **PUT** | `/api/v1/contenedores/{id}` | CLIENTE, OPERADOR | Actualiza información del contenedor | `{ peso, volumen, estado }` | `{ mensaje: "Contenedor actualizado" }` |

**Ejemplo de creación:**
```json
{
  "identificacion": "CNT-BA-2025-01",
  "peso": 1800,
  "volumen": 12.5,
  "clienteId": 5
}
```

---

### 3️⃣ Recurso: **Solicitud de Transporte**
> ⚙️ No posee DELETE físico. Se maneja mediante cambio de estado (`BORRADOR`, `PROGRAMADA`, `ASIGNADA`, `EN_TRANSITO`, `EN_DEPOSITO`, `ENTREGADA`, `CANCELADA`).

| Método | Endpoint | Roles | Descripción | Datos de Entrada | Datos de Salida |
|--------|----------|-------|-------------|------------------|-----------------|
| **GET** | `/api/v1/solicitudes` | OPERADOR, ADMIN | Lista todas las solicitudes | — | `[ { id, clienteId, estado, costoEstimado, fechaCreacion } ]` |
| **GET** | `/api/v1/solicitudes/{id}` | CLIENTE (propio), OPERADOR | Consulta una solicitud | `id` | `{ id, origen, destino, estado, costoEstimado, tiempoEstimado, contenedor }` |
| **GET** | `/api/v1/solicitudes/cliente/{clienteId}` | CLIENTE (propio), OPERADOR | Lista las solicitudes de un cliente | `clienteId` | `[ ... ]` |
| **GET** | `/api/v1/solicitudes/{id}/estado` | CLIENTE (propio), OPERADOR, TRANSPORTISTA | Obtiene el estado actual de la solicitud | `id` | `{ idSolicitud, estado, fechaActualizacion }` |
| **POST** | `/api/v1/solicitudes` | CLIENTE | Crea una nueva solicitud | `{ idCliente, idContenedor, origenDireccion, destinoDireccion }` | `{ idSolicitud, estado: "BORRADOR", costoEstimado, tiempoEstimado }` |
| **PUT** | `/api/v1/solicitudes/{id}` | OPERADOR | Actualiza o confirma una solicitud | `{ estado, tarifaId }` | `{ mensaje: "Solicitud actualizada" }` |
| **PUT** | `/api/v1/solicitudes/{id}/estado` | OPERADOR (interno), TRANSPORTISTA (indirecto vía ms-transporte) | Actualiza el estado de una solicitud | `{ estado, descripcion }` | `{ mensaje: "Estado actualizado" }` |

---

## 🚛 Microservicio: Transporte (`ms-transporte`)

### 🧭 Responsabilidades
- Planificación de rutas y tramos
- Asignación de camiones y transportistas
- Gestión de depósitos y tarifas
- Cálculo de costos y tiempos (integración con Google Maps API)
- Actualización de estado de solicitudes (cuando corresponde)

---

### 1️⃣ Recurso: **Ruta**
> 🧠 No posee PUT manual: las rutas se recalculan automáticamente por cambios de estado en tramos y solicitudes.

| Método | Endpoint | Roles | Descripción | Entrada | Salida |
|--------|----------|-------|-------------|---------|--------|
| **GET** | `/api/v1/rutas` | OPERADOR, ADMIN | Lista todas las rutas registradas | — | `[ { id, solicitudId, estado, distanciaTotal, cantidadTramos } ]` |
| **GET** | `/api/v1/rutas/{id}` | OPERADOR | Consulta una ruta específica | `id` | `{ id, solicitudId, tramos, estado }` |
| **GET** | `/api/v1/rutas/solicitud/{solicitudId}` | OPERADOR, CLIENTE | Obtiene la ruta asociada a una solicitud | `solicitudId` | `{ idRuta, distanciaTotal, estado, tramos }` |
| **POST** | `/api/v1/rutas` | OPERADOR | Crea una nueva ruta | `{ idSolicitud, origenLat, origenLon, destinoLat, destinoLon }` | `{ idRuta, distanciaTotal, tiempoEstimado, estado: "ESTIMADA" }` |

---

### 2️⃣ Recurso: **Tramo**
> ⚙️ Las operaciones de actualización y eliminación están permitidas solo si el tramo no ha sido iniciado.

| Método | Endpoint | Roles | Descripción | Entrada | Salida |
|--------|----------|-------|-------------|---------|--------|
| **GET** | `/api/v1/tramos` | OPERADOR | Lista todos los tramos | — | `[ { id, rutaId, estado, distancia, camion } ]` |
| **GET** | `/api/v1/tramos/{id}` | OPERADOR, TRANSPORTISTA | Obtiene tramo específico | `id` | `{ id, rutaId, tipo, distancia, estado, costoAproximado }` |
| **GET** | `/api/v1/tramos/ruta/{rutaId}` | OPERADOR | Lista tramos de una ruta | `rutaId` | `[ { id, tipo, estado, distancia, camionAsignado } ]` |
| **POST** | `/api/v1/tramos` | OPERADOR | Crea un nuevo tramo | `{ rutaId, depositoOrigenId, depositoDestinoId, tipo }` | `{ idTramo, estado: "PLANIFICADO" }` |
| **PUT** | `/api/v1/tramos/{id}` | OPERADOR | Actualiza datos del tramo (si no ha sido iniciado) | `{ depositoOrigenId, depositoDestinoId, distancia, camion }` | `{ mensaje: "Tramo actualizado" }` |
| **DELETE** | `/api/v1/tramos/{id}` | OPERADOR | Elimina tramo (si no ha sido iniciado) | `id` | `{ mensaje: "Tramo eliminado" }` |
| **POST** | `/api/v1/tramos/{id}/iniciar` | TRANSPORTISTA | Marca inicio de tramo (cambia estado a `INICIADO`) | `{ fechaHoraInicio }` | `{ mensaje: "Tramo iniciado" }` |
| **POST** | `/api/v1/tramos/{id}/finalizar` | TRANSPORTISTA | Marca fin de tramo (cambia estado a `FINALIZADO`) y calcula costo real | `{ fechaHoraFin, kmRecorridos, litrosCombustible }` | `{ mensaje: "Tramo finalizado", costoReal }` |

---

### 3️⃣ Recurso: **Camión**
> “Solo se permite si el camión no está asignado a tramos activos.”

| Método | Endpoint | Roles | Descripción | Entrada | Salida |
|--------|----------|-------|-------------|---------|--------|
| **GET** | `/api/v1/camiones` | OPERADOR, ADMIN | Lista camiones registrados | — | `[ { dominio, capacidadPeso, capacidadVolumen, disponibilidad } ]` |
| **GET** | `/api/v1/camiones/{dominio}` | OPERADOR | Obtiene un camión específico | `dominio` | `{ dominio, capacidadPeso, capacidadVolumen, disponibilidad, costoBaseKm }` |
| **GET** | `/api/v1/camiones/disponibles` | OPERADOR | Lista camiones libres | — | `[ { dominio, capacidadPeso, costoBaseKm } ]` |
| **POST** | `/api/v1/camiones` | OPERADOR | Registra un nuevo camión | `{ dominio, capacidadPeso, capacidadVolumen, costoBaseKm, consumoCombustible }` | `{ dominio, mensaje: "Camión registrado" }` |
| **PUT** | `/api/v1/camiones/{dominio}` | OPERADOR, ADMIN | Actualiza información del camión | `{ capacidadPeso, capacidadVolumen, costoBaseKm, disponibilidad }` | `{ mensaje: "Camión actualizado correctamente" }` |
| **DELETE** | `/api/v1/camiones/{dominio}` | ADMIN | Elimina o desactiva un camión (si no está asignado a tramos activos) | `dominio` | `{ mensaje: "Camión eliminado" }` |

---

### 4️⃣ Recurso: **Depósito**
> ⚙️ Las actualizaciones están permitidas solo para operadores y administradores. Los depósitos no se eliminan físicamente; se desactivan para mantener la integridad de rutas y tramos.

| Método | Endpoint | Roles | Descripción | Entrada | Salida |
|--------|----------|-------|-------------|---------|--------|
| **GET** | `/api/v1/depositos` | OPERADOR, ADMIN | Lista depósitos activos | — | `[ { id, nombre, direccion, latitud, longitud, costoEstadiaDiario } ]` |
| **GET** | `/api/v1/depositos/{id}` | OPERADOR, ADMIN | Consulta un depósito específico | `id` | `{ id, nombre, direccion, latitud, longitud, costoEstadiaDiario }` |
| **GET** | `/api/v1/depositos/cercanos` | OPERADOR | Busca depósitos cercanos a coordenadas dadas | `lat, lon` | `[ { id, nombre, distanciaKm } ]` |
| **POST** | `/api/v1/depositos` | OPERADOR | Crea un nuevo depósito | `{ nombre, direccion, latitud, longitud, costoEstadiaDiario }` | `{ id, mensaje: "Depósito creado correctamente" }` |
| **PUT** | `/api/v1/depositos/{id}` | OPERADOR, ADMIN | Actualiza datos del depósito | `{ nombre, direccion, latitud, longitud, costoEstadiaDiario }` | `{ mensaje: "Depósito actualizado correctamente" }` |
| **DELETE** | `/api/v1/depositos/{id}` | ADMIN | Desactiva depósito si no está en uso | `id` | `{ mensaje: "Depósito desactivado" }` |

---

### 5️⃣ Recurso: **Tarifa**
> ⚙️ Las tarifas se actualizan o desactivan, no se eliminan físicamente. Solo los administradores pueden crear o modificar valores económicos.

| Método | Endpoint | Roles | Descripción | Entrada | Salida |
|--------|----------|-------|-------------|---------|--------|
| **GET** | `/api/v1/tarifas` | OPERADOR, ADMIN | Lista tarifas vigentes | — | `[ { id, concepto, valorBase, valorPorKm, valorPorPeso, valorPorVolumen, fechaVigencia, activo } ]` |
| **GET** | `/api/v1/tarifas/{id}` | OPERADOR, ADMIN | Obtiene una tarifa específica | `id` | `{ id, concepto, valores..., fechaVigencia }` |
| **POST** | `/api/v1/tarifas` | ADMIN | Crea nueva tarifa | `{ concepto, valorBase, valorPorKm, valorPorPeso, valorPorVolumen, valorLitroCombustible, fechaVigencia }` | `{ idTarifa, mensaje: "Tarifa creada correctamente" }` |
| **PUT** | `/api/v1/tarifas/{id}` | ADMIN | Modifica los valores o la vigencia de una tarifa existente | `{ valorBase, valorPorKm, valorPorPeso, valorPorVolumen, valorLitroCombustible, fechaVigencia, activo }` | `{ mensaje: "Tarifa actualizada correctamente" }` |

---

##  Roles y Accesos Globales
> “El acceso se controla mediante Keycloak utilizando JWT y roles RBAC (cliente, operador, transportista, admin).”

| Rol | Descripción | Permisos principales |
|-----|-------------|----------------------|
| **CLIENTE** | Usuario final que crea solicitudes y consulta su estado | CRUD limitado de cliente/solicitudes propias, consulta de estado de sus contenedores |
| **OPERADOR** | Personal interno que gestiona rutas, camiones y solicitudes | CRUD total en cliente, contenedor, solicitud, rutas, tramos y depósitos |
| **TRANSPORTISTA** | Conductor que ejecuta tramos asignados | Lectura de tramos propios, inicio y finalización de tramos |
| **ADMIN** | Administrador del sistema | Acceso total a todos los recursos y configuraciones |
