CREATE TABLE IF NOT EXISTS depositos (
  id_deposito SERIAL PRIMARY KEY,
  nombre VARCHAR(100) NOT NULL,
  direccion VARCHAR(300),
  latitud  NUMERIC(10,8) NOT NULL,
  longitud NUMERIC(11,8) NOT NULL,
  costo_estadia_diario NUMERIC(19,2) NOT NULL CHECK (costo_estadia_diario >= 0.01),
  activo BOOLEAN NOT NULL DEFAULT true
);

CREATE TABLE IF NOT EXISTS camiones (
  dominio VARCHAR(20) PRIMARY KEY,
  capacidad REAL NOT NULL,
  volumen REAL NOT NULL,
  disponibilidad BOOLEAN DEFAULT true,
  costo_base DECIMAL(10,2) NOT NULL,
  consumo_combustible REAL NOT NULL
);

CREATE TABLE IF NOT EXISTS tarifas (
  id_tarifa SERIAL PRIMARY KEY,
  nombre VARCHAR(200) NOT NULL,
  valor_base REAL NOT NULL CHECK (valor_base >= 0.01),
  valor_por_km REAL NOT NULL,
  valor_por_peso REAL NOT NULL,
  valor_por_volumen REAL NOT NULL,
  valor_por_tramo REAL NOT NULL,
  valor_litro_combustible REAL NOT NULL,
  fecha_vigencia DATE NOT NULL,
  activo BOOLEAN DEFAULT true
);

CREATE TABLE IF NOT EXISTS rutas (
  id_ruta SERIAL PRIMARY KEY,
  id_solicitud INTEGER NOT NULL,
  cantidad_tramos INTEGER NOT NULL DEFAULT 0,
  cantidad_depositos INTEGER NOT NULL DEFAULT 0,
  distancia_total NUMERIC(12,3) NOT NULL DEFAULT 0,
  estado VARCHAR(20) NOT NULL
);

CREATE TABLE IF NOT EXISTS tramos (
  id_tramo SERIAL PRIMARY KEY,
  id_ruta INTEGER NOT NULL REFERENCES rutas(id_ruta) ON DELETE CASCADE,
  id_deposito_origen INTEGER REFERENCES depositos(id_deposito),
  id_deposito_destino INTEGER REFERENCES depositos(id_deposito),
  keycloak_id_transportista VARCHAR(80),
  dominio_camion VARCHAR(20) REFERENCES camiones(dominio),
  tipo   VARCHAR(30) NOT NULL,
  estado VARCHAR(20) NOT NULL,
  distancia NUMERIC(12,3),
  costo_aproximado NUMERIC(14,2),
  costo_real NUMERIC(14,2),
  fh_inicio_estimada TIMESTAMP,
  fh_fin_estimada TIMESTAMP,
  fh_inicio TIMESTAMP,
  fh_fin TIMESTAMP,
  fh_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_tramos_ruta ON tramos (id_ruta);
CREATE INDEX IF NOT EXISTS idx_depositos_latlng ON depositos (latitud, longitud);
