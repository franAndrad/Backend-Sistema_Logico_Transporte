# Explicación detallada del Diagrama Entidad-Relación (ER)

Explicación paso a paso de cómo funciona este diagrama de base de datos para el sistema logístico de transporte de contenedores.

---

## 📊 Entidades Principales

### 1. Usuario

Sistema de autenticación y autorización centralizado para todos los tipos de usuarios.

**Campos principales:**
- `idUsuario`: Identificador único
- `nombre`, `apellido`: Datos personales completos
- `telefono`, `email`: Información de contacto
- `username`, `password`: Credenciales de acceso
- `rol`: Define el tipo de usuario (cliente, operador, transportista)
- `activo`: Indica si el usuario está habilitado

**Relaciones:**
- Puede tener un perfil de Cliente asociado (rol: cliente)
- Puede realizar Tramos de transporte (rol: transportista)

**Diseño clave:** 
- ✅ Todos los datos personales están centralizados en Usuario
- ✅ No hay duplicación de credenciales
- ✅ Los roles definen el comportamiento sin necesidad de tablas adicionales para operador/transportista

---

### 2. Cliente

Perfil específico para usuarios que solicitan servicios de transporte.

**Campos principales:**
- `idCliente`: Identificador único del cliente
- `idUsuario`: Referencia al Usuario (para autenticación y datos personales)
- `direccionFacturacion`: Dirección para facturación
- `direccionEnvio`: Dirección de envío por defecto
- `razonSocial`: Nombre de la empresa (si aplica)
- `cuit`: Identificación fiscal (si es empresa)

**Relaciones:**
- Está vinculado a un Usuario del sistema
- Puede tener múltiples contenedores
- Puede realizar múltiples solicitudes de transporte

**Nota importante:** Cliente **NO** tiene username/password propios, usa los de Usuario

---

### 3. Contenedor

Representa unidades de carga reutilizables del cliente.

**Campos principales:**
- `idContenedor`: Identificador único
- `idCliente`: Referencia al cliente propietario
- `tipo`: Tipo de contenedor (estándar, refrigerado, etc.)
- `capacidad`: Capacidad en metros cúbicos o toneladas
- `activo`: Indica si el contenedor está disponible para uso

**Relaciones:**
- Pertenece a un Cliente específico
- Puede usarse en múltiples Solicitudes (reutilizable)

**Modelo de reutilización:**
- ✅ Un contenedor puede usarse en múltiples solicitudes a lo largo del tiempo
- ✅ El campo `activo` permite desactivar contenedores dañados sin eliminarlos
- ✅ Esto permite mantener el historial completo de uso

---

### 4. Solicitud

Representa una petición de transporte por parte de un cliente.

**Campos principales:**
- `idSolicitud`: Identificador único
- `idCliente`: Cliente que realiza la solicitud
- `idContenedor`: Contenedor a transportar
- `idRuta`: Ruta asignada (puede ser null si aún no está asignada)
- `idTarifa`: Tarifa aplicada (**puede ser null hasta que se confirme**)
- `estadoSolicitud`: Estado del proceso (pendiente, en_proceso, completada, cancelada)
- `estadoContenedor`: Estado físico (cargado, en_transito, entregado)
- `fechaSolicitud`: Fecha de creación
- `fechaEntregaEstimada`: Fecha estimada de entrega
- `costoTotal`: Costo calculado (se congela cuando se asigna idTarifa)

**Relaciones:**
- Pertenece a un Cliente
- Referencia un Contenedor específico
- Se le asigna una Ruta
- Usa una versión específica de Tarifa (versionado)

**Lógica de negocio clave:**
- Estado inicial: `idTarifa` es **NULL**, `estadoSolicitud` = 'pendiente'
- Al asignar ruta: Se calcula `costoTotal` usando la Tarifa vigente actual
- Se guarda `idTarifa` para mantener el precio histórico
- ✅ Esto asegura que cambios futuros en tarifas NO afecten solicitudes ya confirmadas

---

### 5. Ruta

Define el camino completo que seguirá un transporte.

**Campos principales:**
- `idRuta`: Identificador único
- `origen`: Dirección o punto de inicio
- `destino`: Dirección o punto final
- `distanciaTotal`: Distancia calculada en kilómetros

**Relaciones:**
- Se divide en múltiples Tramos
- Puede tener múltiples Paradas intermedias (Depositos)
- Es usada por Solicitudes

**Diseño modular:**
- ✅ Una ruta se compone de varios tramos (segmentos)
- ✅ Cada tramo puede tener depósitos de origen/destino opcionales
- ✅ Permite rutas directas (sin paradas) o complejas (con múltiples paradas)

---

### 6. Tramo

Segmento individual de una ruta (de punto A a punto B).

**Campos principales:**
- `idTramo`: Identificador único
- `idRuta`: Ruta a la que pertenece
- `idDepositoOrigen`: Depósito de inicio (**nullable** - puede ser dirección de solicitud)
- `idDepositoDestino`: Depósito de fin (**nullable** - puede ser dirección de solicitud)
- `dominioCamion`: Camión asignado (**string FK**, no int) - asignación dinámica
- `idUsuarioTransportista`: Usuario transportista que realiza el tramo
- `orden`: Posición del tramo en la ruta (1, 2, 3...)
- `distancia`: Kilómetros del segmento
- `tiempoEstimado`: Duración estimada del viaje
- `estado`: Progreso (pendiente → asignado → en_transito → completado)

**Relaciones:**
- Pertenece a una Ruta
- **Opcionalmente** tiene origen/destino en Depositos (nullable)
- Es realizado por un Camion (FK: dominioCamion string)
- Es operado por un Usuario transportista (FK: idUsuarioTransportista)

**Modelo de asignación dinámica:**
- ✅ `dominioCamion` es **string** (no int) para coincidir con `Camion.dominio`
- ✅ Los camiones se asignan dinámicamente según disponibilidad
- ✅ Un camión puede hacer múltiples tramos (pero no simultáneamente)
- ✅ Depositos son opcionales: permite rutas directas origen→destino sin paradas

---

### 7. Camion

Vehículos que realizan el transporte.

**Campos principales:**
- `dominio`: Placa/patente (**clave primaria string**)
- `capacidadPeso`: Peso máximo en kg
- `capacidadVolumen`: Volumen máximo en m³
- `disponibilidad`: Si está libre o en uso
- `costoBaseKm`: Precio por kilómetro recorrido

**Relaciones:**
- Puede ser asignado a múltiples Tramos (en momentos diferentes)
- Es operado por Usuarios con rol transportista

**Nota crítica:**
- ✅ `dominio` es **string** (ej: "ABC123"), no int
- ✅ Este es el campo referenciado por `Tramo.dominioCamion`
- ✅ La disponibilidad se actualiza dinámicamente según asignaciones activas


---

### 8. Deposito

Ubicaciones intermedias de almacenamiento opcionales.

**Campos principales:**
- `idDeposito`: Identificador único
- `nombre`: Nombre del depósito
- `direccion`: Ubicación completa
- `latitud`: Coordenada GPS (decimal(10,8) para precisión)
- `longitud`: Coordenada GPS (decimal(11,8) para precisión)
- `costoEstadiaDiario`: Tarifa por día de almacenamiento

**Relaciones:**
- **Opcionalmente** puede ser origen/destino de Tramos

**Uso opcional:**
- ✅ No todos los tramos requieren depósitos
- ✅ Rutas directas origen→destino no usan depósitos
- ✅ Rutas complejas pueden tener múltiples paradas en depósitos

---

### 9. Tarifa

Sistema de versionado de precios para mantener consistencia histórica.

**Campos principales:**
- `idTarifa`: Identificador único (autoincremental)
- `concepto`: Descripción de la tarifa (ej: "Tarifa estándar Q1 2024")
- **Componentes de costo:**
  - `valorBase`: Cargo fijo por solicitud
  - `valorPorKm`: Costo por kilómetro recorrido
  - `valorPorPeso`: Costo por tonelada transportada
  - `valorPorVolumen`: Costo por metro cúbico
  - `valorPorTramo`: Costo fijo por cada segmento
  - `valorLitroCombustible`: Precio del combustible (para cálculos)
- `fechaVigencia`: Fecha desde la cual aplica
- `activo`: Si es la tarifa actual vigente

**Relaciones:**
- Se aplica a múltiples Solicitudes (preserva precio histórico)

**Sistema de versionado:**
- ✅ Cuando se crea/confirma una Solicitud, se guarda `idTarifa` de la versión vigente
- ✅ El `costoTotal` se calcula y congela en ese momento
- ✅ Si se cambian las tarifas futuras, las solicitudes existentes mantienen su precio original
- ✅ Esto evita problemas de inconsistencia: "¿Por qué mi factura cambió?"

**Ejemplo de uso:**
```
Solicitud #123 (Enero 2024):
  - Se crea con Tarifa #1 (valorPorKm = $10)
  - costoTotal = $500 (se guarda idTarifa = 1)

Marzo 2024: Se crea Tarifa #2 (valorPorKm = $12)

Solicitud #123 sigue costando $500 (usa Tarifa #1)
Solicitud #200 (nueva) costará más (usa Tarifa #2)
```

---

### 10. Seguimiento

Historial de eventos y ubicaciones para trazabilidad completa.

**Campos principales:**
- `idSeguimiento`: Identificador único
- `idSolicitud`: Solicitud a la que pertenece el evento
- `estado`: Estado del contenedor en este punto (cargado, en_transito, en_deposito, entregado)
- `descripcion`: Detalles del evento (ej: "Contenedor cargado en camión ABC123")
- `fechaHora`: Timestamp exacto del evento
- `latitud`: Ubicación GPS del evento (decimal(10,8))
- `longitud`: Ubicación GPS del evento (decimal(11,8))

**Relaciones:**
- Pertenece a una Solicitud específica

**Funcionalidad:**
- ✅ Permite rastreo en tiempo real
- ✅ Auditoría completa de eventos
- ✅ Coordenadas con precisión decimal para GPS exacto

---

## 🔄 Flujo de Funcionamiento

### Escenario ejemplo: Un cliente solicita transportar un contenedor

#### 1. Registro inicial

- Un **Usuario** se registra con rol "cliente" (nombre, apellido, email, username, password)
- Se crea un perfil de **Cliente** asociado (direcciones, razonSocial, CUIT)
- El cliente registra un **Contenedor** con sus características (tipo, capacidad)

#### 2. Creación de solicitud

- El cliente crea una **Solicitud** indicando:
  - Qué contenedor transportar (idContenedor)
  - Datos de origen y destino
  - Estado inicial: `estadoSolicitud` = 'pendiente', `idTarifa` = NULL

#### 3. Planificación de ruta

- Un operador (Usuario con rol "operador") revisa la solicitud
- El sistema calcula una **Ruta** óptima
- Se divide en **Tramos** según necesidad:
  - Ruta simple: 1 tramo directo (origen → destino)
  - Ruta compleja: múltiples tramos (origen → depósito A → depósito B → destino)
- Para cada tramo se calcula distancia y tiempo estimado

#### 4. Asignación de recursos

- Se obtiene la **Tarifa** vigente actual (activo = true)
- Se calcula el `costoTotal` usando los componentes de la tarifa:
  ```
  costoTotal = valorBase 
             + (distanciaTotal × valorPorKm)
             + (pesoCarga × valorPorPeso)
             + (volumenCarga × valorPorVolumen)
             + (cantidadTramos × valorPorTramo)
  ```
- Se guarda `idTarifa` en la Solicitud (congela el precio)
- Se actualiza `estadoSolicitud` = 'confirmada'

#### 5. Asignación de camiones (dinámica)

- Para cada **Tramo**, el sistema busca **Camiones** disponibles
- Criterios: capacidad suficiente, disponibilidad = true
- Se asigna:
  - `dominioCamion` (ej: "ABC123")
  - `idUsuarioTransportista` (Usuario con rol transportista)
  - Camión `disponibilidad` = false (mientras dura el tramo)
  - Estado del tramo = 'asignado'

#### 6. Ejecución del transporte

- El **Usuario transportista** inicia el tramo
- Se registra **Seguimiento**:
  - "Contenedor retirado del origen" (con coordenadas GPS)
  - "En tránsito hacia Depósito A" (con coordenadas periódicas)
  - "Llegada a Depósito A" (con timestamp)
- Estado del tramo = 'en_transito'
- `estadoContenedor` en Solicitud = 'en_transito'

#### 7. Paradas en depósitos (opcional)

- Si el tramo llega a un **Deposito**:
  - Se registra fecha/hora de llegada
  - Se calculan días de estadía × `costoEstadiaDiario`
  - Se registra en Seguimiento: "Almacenado en Depósito A"
- El siguiente tramo se activa con otro camión (asignación dinámica)

#### 8. Finalización

- Cuando todos los tramos se completan:
  - Último tramo registra: "Contenedor entregado en destino"
  - `estadoSolicitud` = 'completada'
  - `estadoContenedor` = 'entregado'
  - Camiones liberados: `disponibilidad` = true
- El **Contenedor** queda `activo` = true para ser reutilizado en futuras solicitudes

#### 9. Facturación

- El `costoTotal` permanece congelado (usando `idTarifa` guardado)
- No importa si las tarifas cambiaron después
- Transparencia total: el cliente paga lo que se le cotizó


---

## 📐 Cardinalidades de las Relaciones

### Explicación de las notaciones

- `||--o{`: Uno a muchos (obligatorio a opcional múltiple)
- `}o--||`: Muchos opcionales a uno obligatorio
- `||-||`: Uno a uno obligatorio
- `||--o|`: Uno a uno opcional

### Relaciones clave corregidas

#### 1. Usuario → Cliente (`||--o|`)

- Un Usuario puede tener un perfil de Cliente asociado (si rol = 'cliente')
- Un Cliente pertenece a un Usuario específico

#### 2. Cliente → Contenedor (`||--o{`)

- Un Cliente puede tener múltiples Contenedores
- Un Contenedor pertenece a un solo Cliente

#### 3. Cliente → Solicitud (`||--o{`)

- Un Cliente puede crear múltiples Solicitudes
- Una Solicitud pertenece a un solo Cliente

#### 4. Solicitud → Ruta (`||--||`)

- Una Solicitud tiene una Ruta asignada
- Una Ruta pertenece a una Solicitud específica

#### 5. Ruta → Tramo (`||--o{`)

- Una Ruta se compone de múltiples Tramos (mínimo 1)
- Un Tramo pertenece a una sola Ruta

#### 6. Tramo → Deposito (relaciones opcionales)

- Un Tramo **puede tener** un Deposito de origen (nullable)
- Un Tramo **puede tener** un Deposito de destino (nullable)
- Un Deposito puede ser origen/destino de múltiples Tramos
- Permite rutas directas sin paradas

#### 7. Tramo → Camion (`}o--||`)

- Un Camion puede realizar múltiples Tramos (en diferentes momentos)
- Un Tramo debe tener un Camion asignado (FK: dominioCamion string)

#### 8. Tramo → Usuario Transportista (`}o--||`)

- Un Usuario transportista puede realizar múltiples Tramos
- Un Tramo debe tener un transportista asignado (FK: idUsuarioTransportista)

#### 9. Solicitud → Tarifa (`}o--||`)

- Una Tarifa se aplica a múltiples Solicitudes
- Una Solicitud usa una versión específica de Tarifa (versionado)
- `idTarifa` puede ser NULL al crear la solicitud (hasta confirmación)

#### 10. Solicitud → Seguimiento (`||--o{`)

- Una Solicitud puede tener múltiples registros de Seguimiento
- Un registro de Seguimiento pertenece a una Solicitud

---

## 📝 Resumen del Modelo Corregido

Este modelo de datos permite gestionar todo el ciclo de vida del transporte de contenedores, desde la solicitud inicial hasta la entrega final, con:

### ✅ Características principales:

1. **Autenticación centralizada:**
   - Usuario único con datos personales completos
   - Sin duplicación de credenciales
   - Roles claramente definidos (cliente, operador, transportista)

2. **Asignación dinámica de recursos:**
   - Camiones asignados por tramo según disponibilidad
   - FK correcta: `dominioCamion` (string) → `Camion.dominio` (string)
   - Transportistas asignados por tramo

3. **Flexibilidad de rutas:**
   - Depósitos opcionales (FK nullable)
   - Permite rutas directas o con múltiples paradas
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

