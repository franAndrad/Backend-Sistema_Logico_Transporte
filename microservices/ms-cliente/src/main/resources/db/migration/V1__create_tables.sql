CREATE TABLE IF NOT EXISTS clientes (
  id_cliente SERIAL PRIMARY KEY,
  nombre VARCHAR(255) NOT NULL,
  documento VARCHAR(50) UNIQUE,
  email VARCHAR(255),
  telefono VARCHAR(50),
  direccion VARCHAR(500),
  activo BOOLEAN DEFAULT true
);

CREATE TABLE IF NOT EXISTS contenedores (
  id_contenedor SERIAL PRIMARY KEY,
  id_cliente INT NOT NULL REFERENCES clientes(id_cliente),
  codigo VARCHAR(100) UNIQUE NOT NULL,
  tipo VARCHAR(50),
  peso_maximo REAL,
  volumen REAL,
  activo BOOLEAN DEFAULT true
);

CREATE TABLE IF NOT EXISTS solicitudes (
  id_solicitud SERIAL PRIMARY KEY,
  id_cliente INT NOT NULL REFERENCES clientes(id_cliente),
  id_contenedor INT NOT NULL REFERENCES contenedores(id_contenedor),
  id_tarifa INT,
  origen_direccion VARCHAR(500),
  origen_latitud DECIMAL(10,8),
  origen_longitud DECIMAL(11,8),
  destino_direccion VARCHAR(500),
  destino_latitud DECIMAL(10,8),
  destino_longitud DECIMAL(11,8),
  costo_estimado REAL,
  tiempo_estimado REAL,
  costo_final REAL,
  tiempo_real REAL,
  fecha_creacion TIMESTAMP DEFAULT NOW(),
  fecha_actualizacion TIMESTAMP DEFAULT NOW(),
  estado VARCHAR(30)
);

CREATE INDEX IF NOT EXISTS idx_solicitud_tarifa ON solicitudes(id_tarifa);
