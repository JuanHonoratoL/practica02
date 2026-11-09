-- =============================================================================
-- Script de Base de Datos: Práctica 02 (API de Cursos con HATEOAS y JWT)
-- Motor: MySQL / MariaDB
-- =============================================================================

CREATE DATABASE IF NOT EXISTS practica02_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE practica02_db;

-- 1. Tabla de Usuarios
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'CONSULTA') NOT NULL
);

-- 2. Tabla de Tokens JWT emitidos
CREATE TABLE IF NOT EXISTS tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(512) UNIQUE,
    token_type ENUM('BEARER') DEFAULT 'BEARER',
    revoked BIT(1) NOT NULL DEFAULT 0,
    expired BIT(1) NOT NULL DEFAULT 0,
    user_id BIGINT,
    CONSTRAINT fk_tokens_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 3. Tabla de Cursos (Recurso principal)
CREATE TABLE IF NOT EXISTS cursos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    duracion_horas INT NOT NULL,
    nivel VARCHAR(50) NOT NULL,
    precio DECIMAL(10, 2) NOT NULL,
    fecha_inicio DATE NOT NULL
);

-- Nota: Al iniciar la aplicación Spring Boot, el bean CommandLineRunner en AppConfig
-- inserta automáticamente el usuario ADMIN por defecto si no existe:
--   Email:    admin@ipn.mx
--   Password: admin123 (codificado con BCrypt)
--   Rol:      ADMIN

-- Datos de ejemplo opcionales para la tabla de cursos:
INSERT INTO cursos (nombre, duracion_horas, nivel, precio, fecha_inicio) VALUES
('Java con Spring Boot', 40, 'medio', 1499.90, '2026-11-15'),
('Seguridad Web con Spring Security y JWT', 35, 'avanzado', 1799.00, '2026-11-20'),
('Diseño de APIs REST y HATEOAS', 25, 'básico', 999.50, '2026-12-01');
