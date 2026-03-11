-- Script para corregir el esquema de la base de datos
-- Cambiar todas las columnas ID_Usuario de BIGINT a INT para compatibilidad

USE technova_java;

-- Corregir tabla de notificaciones (user_id es INT)
ALTER TABLE notificacions 
MODIFY COLUMN user_id INT NOT NULL;

-- Corregir tabla de mensajes (si existe)
-- ALTER TABLE mensajes_directos 
-- MODIFY COLUMN ID_Usuario INT NOT NULL;

-- Corregir tabla de atencioncliente
ALTER TABLE atencioncliente 
MODIFY COLUMN ID_Usuario INT NOT NULL;

-- Corregir tabla de reclamos (ya corregido en el archivo)
-- ALTER TABLE reclamos 
-- MODIFY COLUMN ID_Usuario INT NOT NULL;

-- Corregir tabla de ventas
ALTER TABLE ventas 
MODIFY COLUMN ID_Usuario INT NOT NULL;

-- Corregir tabla de compras
ALTER TABLE compras 
MODIFY COLUMN ID_Usuario INT NOT NULL;

-- Corregir tabla de medio_de_pago
ALTER TABLE medio_de_pago 
MODIFY COLUMN ID_Usuario INT NOT NULL;

-- Corregir tabla de carrito
ALTER TABLE carrito 
MODIFY COLUMN ID_Usuario INT NOT NULL;
