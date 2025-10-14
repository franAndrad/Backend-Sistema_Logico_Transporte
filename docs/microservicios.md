# Configuración de los Microservicios

Este documento describe la arquitectura y configuración de los microservicios que componen el Sistema Logístico de Transporte de Contenedores.

## Microservicio: Cliente (ms-cliente)

### Responsabilidades:
- Gestión de clientes
- Gestión de contenedores
- Registro de solicitudes de transporte
- Consulta de estado de solicitudes

### Endpoints principales:

#### Clientes
- `GET /api/v1/clientes` - Listar todos los clientes
- `GET /api/v1/clientes/{id}` - Obtener un cliente por ID
- `POST /api/v1/clientes` - Crear un nuevo cliente
- `PUT /api/v1/clientes/{id}` - Actualizar un cliente
- `DELETE /api/v1/clientes/{id}` - Eliminar un cliente

#### Contenedores
- `GET /api/v1/contenedores` - Listar todos los contenedores
- `GET /api/v1/contenedores/{id}` - Obtener un contenedor por ID
- `GET /api/v1/contenedores/cliente/{clienteId}` - Obtener contenedores por cliente
- `POST /api/v1/contenedores` - Crear un nuevo contenedor
- `PUT /api/v1/contenedores/{id}` - Actualizar un contenedor
- `DELETE /api/v1/contenedores/{id}` - Eliminar un contenedor

#### Solicitudes
- `GET /api/v1/solicitudes` - Listar todas las solicitudes
- `GET /api/v1/solicitudes/{id}` - Obtener una solicitud por ID
- `GET /api/v1/solicitudes/cliente/{clienteId}` - Obtener solicitudes por cliente
- `POST /api/v1/solicitudes` - Crear una nueva solicitud
- `PUT /api/v1/solicitudes/{id}` - Actualizar una solicitud
- `DELETE /api/v1/solicitudes/{id}` - Eliminar una solicitud

### Roles y accesos:
- `CLIENTE`: Acceso a sus propios datos, contenedores y solicitudes
- `OPERADOR`: Acceso a todos los clientes, contenedores y solicitudes
- `ADMIN`: Acceso completo

## Microservicio: Transporte (ms-transporte)

### Responsabilidades:
- Gestión de rutas y tramos
- Gestión de depósitos
- Gestión de camiones
- Cálculo de costos y tiempos

### Endpoints principales:

#### Rutas
- `GET /api/v1/rutas` - Listar todas las rutas
- `GET /api/v1/rutas/{id}` - Obtener una ruta por ID
- `GET /api/v1/rutas/solicitud/{solicitudId}` - Obtener rutas por solicitud
- `POST /api/v1/rutas` - Crear una nueva ruta
- `PUT /api/v1/rutas/{id}` - Actualizar una ruta
- `DELETE /api/v1/rutas/{id}` - Eliminar una ruta

#### Tramos
- `GET /api/v1/tramos` - Listar todos los tramos
- `GET /api/v1/tramos/{id}` - Obtener un tramo por ID
- `GET /api/v1/tramos/ruta/{rutaId}` - Obtener tramos por ruta
- `POST /api/v1/tramos` - Crear un nuevo tramo
- `PUT /api/v1/tramos/{id}` - Actualizar un tramo
- `DELETE /api/v1/tramos/{id}` - Eliminar un tramo
- `PUT /api/v1/tramos/{id}/iniciar` - Registrar inicio de un tramo
- `PUT /api/v1/tramos/{id}/finalizar` - Registrar fin de un tramo

#### Camiones
- `GET /api/v1/camiones` - Listar todos los camiones
- `GET /api/v1/camiones/{id}` - Obtener un camión por ID
- `GET /api/v1/camiones/disponibles` - Obtener camiones disponibles
- `POST /api/v1/camiones` - Crear un nuevo camión
- `PUT /api/v1/camiones/{id}` - Actualizar un camión
- `DELETE /api/v1/camiones/{id}` - Eliminar un camión

#### Depósitos
- `GET /api/v1/depositos` - Listar todos los depósitos
- `GET /api/v1/depositos/{id}` - Obtener un depósito por ID
- `GET /api/v1/depositos/cercanos` - Obtener depósitos cercanos a coordenadas
- `POST /api/v1/depositos` - Crear un nuevo depósito
- `PUT /api/v1/depositos/{id}` - Actualizar un depósito
- `DELETE /api/v1/depositos/{id}` - Eliminar un depósito

#### Tarifas
- `GET /api/v1/tarifas` - Listar todas las tarifas
- `GET /api/v1/tarifas/{id}` - Obtener una tarifa por ID
- `POST /api/v1/tarifas` - Crear una nueva tarifa
- `PUT /api/v1/tarifas/{id}` - Actualizar una tarifa
- `DELETE /api/v1/tarifas/{id}` - Eliminar una tarifa

### Roles y accesos:
- `OPERADOR`: Acceso a todas las operaciones de gestión
- `TRANSPORTISTA`: Acceso a consultas y actualización de tramos asignados
- `ADMIN`: Acceso completo

## Microservicio: Seguimiento (ms-seguimiento)

### Responsabilidades:
- Registro de eventos de seguimiento
- Consulta de estado de contenedores
- Generación de informes de seguimiento

### Endpoints principales:

#### Seguimiento
- `GET /api/v1/seguimientos` - Listar todos los seguimientos
- `GET /api/v1/seguimientos/solicitud/{solicitudId}` - Obtener seguimientos por solicitud
- `POST /api/v1/seguimientos` - Registrar un nuevo evento de seguimiento
- `GET /api/v1/seguimientos/contenedor/{contenedorId}` - Obtener seguimientos por contenedor

#### Reportes
- `GET /api/v1/reportes/contenedor/{contenedorId}` - Generar reporte de seguimiento de un contenedor
- `GET /api/v1/reportes/cliente/{clienteId}` - Generar reporte de contenedores de un cliente
- `GET /api/v1/reportes/deposito/{depositoId}` - Generar reporte de contenedores en un depósito

### Roles y accesos:
- `CLIENTE`: Acceso a seguimiento de sus contenedores
- `OPERADOR`: Acceso a todos los seguimientos
- `TRANSPORTISTA`: Acceso a seguimientos de tramos asignados
- `ADMIN`: Acceso completo

## API Gateway

### Configuración de rutas:
- `/api/v1/clientes/**` -> ms-cliente
- `/api/v1/contenedores/**` -> ms-cliente
- `/api/v1/solicitudes/**` -> ms-cliente
- `/api/v1/rutas/**` -> ms-transporte
- `/api/v1/tramos/**` -> ms-transporte
- `/api/v1/camiones/**` -> ms-transporte
- `/api/v1/depositos/**` -> ms-transporte
- `/api/v1/tarifas/**` -> ms-transporte
- `/api/v1/seguimientos/**` -> ms-seguimiento
- `/api/v1/reportes/**` -> ms-seguimiento

### Funcionalidades adicionales:
- Validación de tokens JWT
- Logging de solicitudes
- Rate limiting
- Timeout management
- Circuit breaker