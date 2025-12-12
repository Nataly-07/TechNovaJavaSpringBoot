-- Script SQL para crear la tabla de reclamos
-- Ejecutar este script en la base de datos MySQL

USE technova_java;

CREATE TABLE IF NOT EXISTS reclamos (
    ID_Reclamo INT AUTO_INCREMENT PRIMARY KEY,
    ID_Usuario BIGINT NOT NULL,
    Fecha_Reclamo DATETIME NOT NULL,
    Titulo VARCHAR(200) NOT NULL,
    Descripcion TEXT NOT NULL,
    Estado VARCHAR(50) NOT NULL DEFAULT 'pendiente',
    Respuesta TEXT NULL,
    Prioridad VARCHAR(20) NULL DEFAULT 'normal',
    FOREIGN KEY (ID_Usuario) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_usuario (ID_Usuario),
    INDEX idx_estado (Estado),
    INDEX idx_fecha (Fecha_Reclamo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

