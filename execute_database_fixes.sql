-- Script completo para corregir todas las incompatibilidades de tipos
-- Ejecutar en MySQL antes de iniciar la aplicación

USE technova_java1;

-- Corregir tabla users (asegurar que id es INT)
ALTER TABLE users MODIFY COLUMN id INT AUTO_INCREMENT PRIMARY KEY;

-- Corregir tabla notificacions (user_id debe ser INT)
ALTER TABLE notificacions MODIFY COLUMN user_id INT NOT NULL;

-- Corregir tabla atencioncliente (ID_Usuario debe ser INT)
ALTER TABLE atencioncliente MODIFY COLUMN ID_Usuario INT NOT NULL;

-- Corregir tabla reclamos (ID_Usuario debe ser INT)
ALTER TABLE reclamos MODIFY COLUMN ID_Usuario INT NOT NULL;

-- Corregir tabla ventas (ID_Usuario debe ser INT)
ALTER TABLE ventas MODIFY COLUMN ID_Usuario INT NOT NULL;

-- Corregir tabla compras (ID_Usuario debe ser INT)
ALTER TABLE compras MODIFY COLUMN ID_Usuario INT NOT NULL;

-- Corregir tabla medio_de_pago (ID_Usuario debe ser INT)
ALTER TABLE medio_de_pago MODIFY COLUMN ID_Usuario INT NOT NULL;

-- Corregir tabla carrito (ID_Usuario debe ser INT)
ALTER TABLE carrito MODIFY COLUMN ID_Usuario INT NOT NULL;

-- Si existe tabla de mensajes_directos, corregirla
-- ALTER TABLE mensajes_directos MODIFY COLUMN ID_Usuario INT NOT NULL;

-- Verificar y recrear restricciones si es necesario
-- Las restricciones FOREIGN KEY se recrearán automáticamente con los tipos correctos
