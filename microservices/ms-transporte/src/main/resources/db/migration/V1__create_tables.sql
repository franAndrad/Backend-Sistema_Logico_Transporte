-- Flyway migration: crea tablas para ms_transporte
CREATE SCHEMA IF NOT EXISTS ms_transporte;

CREATE TABLE IF NOT EXISTS ms_transporte.depositos (
  id_deposito SERIAL PRIMARY KEY,
  nombre VARCHAR(80) NOT NULL,
  direccion VARCHAR(120) NOT NULL,
  latitud NUMERIC(10,8) NOT NULL,
  longitud NUMERIC(11,8) NOT NULL,
  costo_estadia_diario NUMERIC(19,2) NOT NULL CHECK (costo_estadia_diario >= 0.01),
  activo BOOLEAN NOT NULL DEFAULT true
);

CREATE TABLE IF NOT EXISTS ms_transporte.camion (
  dominio VARCHAR(20) PRIMARY KEY,
  capacidad_peso REAL NOT NULL,
  capacidad_volumen REAL NOT NULL,
  disponibilidad BOOLEAN DEFAULT true,
  costo_base_km REAL NOT NULL,
  consumo_combustible REAL NOT NULL
);

CREATE TABLE IF NOT EXISTS ms_transporte.tarifa (
  id_tarifa SERIAL PRIMARY KEY,
  nombre VARCHAR(200) NOT NULL,
  valor_por_km REAL NOT NULL,
  valor_por_peso REAL NOT NULL,
  valor_por_volumen REAL NOT NULL,
  valor_por_tramo REAL NOT NULL,
  valor_litro_combustible REAL NOT NULL,
  fecha_vigencia DATE NOT NULL,
  activo BOOLEAN DEFAULT true
);

CREATE TABLE IF NOT EXISTS ms_transporte.ruta (
  id_ruta SERIAL PRIMARY KEY,
  id_solicitud INTEGER,
  cantidad_tramos INTEGER DEFAULT 0,
  cantidad_depositos INTEGER DEFAULT 0,
  distancia_total REAL,
  costo_total REAL,
  estado VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS ms_transporte.tramo (
  id_tramo SERIAL PRIMARY KEY,
  id_ruta INTEGER NOT NULL REFERENCES ms_transporte.ruta(id_ruta),
  id_deposito_origen INTEGER REFERENCES ms_transporte.depositos(id_deposito),
  id_deposito_destino INTEGER REFERENCES ms_transporte.depositos(id_deposito),
  id_tarifa INTEGER REFERENCES ms_transporte.tarifa(id_tarifa),
  keycloak_id_transportista VARCHAR(255),
  dominio_camion VARCHAR(20) REFERENCES ms_transporte.camion(dominio),
  tipo VARCHAR(50) NOT NULL,
  estado VARCHAR(50) NOT NULL,
  distancia REAL NOT NULL,
  costo_aproximado REAL,
  costo_real REAL,
  fecha_hora_inicio_estimada TIMESTAMP,
  fecha_hora_fin_estimada TIMESTAMP,
  fecha_hora_inicio TIMESTAMP,
  fecha_hora_fin TIMESTAMP,
  fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_depositos_latlng ON ms_transporte.depositos (latitud, longitud);
CREATE INDEX IF NOT EXISTS idx_tramo_ruta ON ms_transporte.tramo (id_ruta);
