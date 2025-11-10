-- Flyway migration: crea tablas para ms_cliente
CREATE SCHEMA IF NOT EXISTS ms_cliente;

CREATE TABLE IF NOT EXISTS ms_cliente.cliente (
  id_cliente SERIAL PRIMARY KEY,
  nombre VARCHAR(255) NOT NULL,
  documento VARCHAR(50) UNIQUE,
  email VARCHAR(255),
  telefono VARCHAR(50),
  direccion VARCHAR(500),
  activo BOOLEAN DEFAULT true
);

CREATE TABLE IF NOT EXISTS ms_cliente.contenedor (
  id_contenedor SERIAL PRIMARY KEY,
  codigo VARCHAR(100) UNIQUE NOT NULL,
  tipo VARCHAR(50),
  peso_maximo REAL,
  volumen REAL,
  activo BOOLEAN DEFAULT true
);

CREATE TABLE IF NOT EXISTS ms_cliente.solicitud (
  id_solicitud SERIAL PRIMARY KEY,
  id_cliente INTEGER NOT NULL REFERENCES ms_cliente.cliente(id_cliente),
  origen VARCHAR(500),
  destino VARCHAR(500),
  fecha_solicitud TIMESTAMP,
  estado VARCHAR(50)
);
