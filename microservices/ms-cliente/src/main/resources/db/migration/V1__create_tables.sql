CREATE TABLE IF NOT EXISTS clientes (
    id_cliente SERIAL PRIMARY KEY,
    keycloak_id VARCHAR(255) NOT NULL UNIQUE,
    direccion_facturacion VARCHAR(500) NOT NULL,
    direccion_envio VARCHAR(500),
    razon_social VARCHAR(255),
    cuit VARCHAR(11) UNIQUE,
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS contenedores (
    id_contenedor SERIAL PRIMARY KEY,
    id_cliente INTEGER NOT NULL,
    identificacion VARCHAR(100) NOT NULL UNIQUE,
    peso DOUBLE PRECISION NOT NULL,
    volumen DOUBLE PRECISION NOT NULL,
    estado VARCHAR(50) NOT NULL DEFAULT 'EN_ORIGEN',
    ubicacion_actual VARCHAR(255),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_contenedores_clientes
    FOREIGN KEY (id_cliente) REFERENCES clientes(id_cliente)
);

CREATE TABLE IF NOT EXISTS solicitudes (
    id_solicitud SERIAL PRIMARY KEY,
    id_cliente INTEGER NOT NULL REFERENCES clientes(id_cliente),
    id_contenedor INTEGER NOT NULL REFERENCES contenedores(id_contenedor),
    id_tarifa INTEGER,
    origen_direccion  VARCHAR(500) NOT NULL,
    origen_latitud    DOUBLE PRECISION,
    origen_longitud   DOUBLE PRECISION,
    destino_direccion VARCHAR(500) NOT NULL,
    destino_latitud   DOUBLE PRECISION,
    destino_longitud  DOUBLE PRECISION,
    costo_estimado    DOUBLE PRECISION,
    tiempo_estimado   INTEGER,
    costo_final       DOUBLE PRECISION,
    tiempo_real       INTEGER,
    estado            VARCHAR(30) NOT NULL DEFAULT 'BORRADOR',
    descripcion_estado VARCHAR(500),
    fecha_creacion     TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_solicitud_tarifa ON solicitudes(id_tarifa);
