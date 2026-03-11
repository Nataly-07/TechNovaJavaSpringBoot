-- Script completo para corregir TODAS las incompatibilidades de tipos en la base de datos
-- Ejecutar este script en MySQL antes de iniciar la aplicación

USE technova_java1;

-- Desactivar temporalmente la verificación de claves foráneas
SET FOREIGN_KEY_CHECKS = 0;

-- Corregir tabla users (asegurar que id es INT)
ALTER TABLE users MODIFY COLUMN id INT AUTO_INCREMENT PRIMARY KEY;

-- Corregir tabla producto (ID_Producto debe ser INT)
ALTER TABLE producto MODIFY COLUMN ID_Producto INT AUTO_INCREMENT PRIMARY KEY;

-- Corregir tabla notificacions (user_id debe ser INT)
ALTER TABLE notificacions MODIFY COLUMN user_id INT NOT NULL;
ALTER TABLE notificacions MODIFY COLUMN id INT AUTO_INCREMENT PRIMARY KEY;

-- Corregir tabla atencioncliente (ID_Usuario debe ser INT)
ALTER TABLE atencioncliente MODIFY COLUMN ID_Usuario INT NOT NULL;
ALTER TABLE atencioncliente MODIFY COLUMN id INT AUTO_INCREMENT PRIMARY KEY;

-- Corregir tabla reclamos (ID_Usuario debe ser INT)
ALTER TABLE reclamos MODIFY COLUMN ID_Usuario INT NOT NULL;
ALTER TABLE reclamos MODIFY COLUMN id INT AUTO_INCREMENT PRIMARY KEY;

-- Corregir tabla ventas (ID_Usuario debe ser INT)
ALTER TABLE ventas MODIFY COLUMN ID_Usuario INT NOT NULL;
ALTER TABLE ventas MODIFY COLUMN id INT AUTO_INCREMENT PRIMARY KEY;

-- Corregir tabla compras (ID_Usuario debe ser INT)
ALTER TABLE compras MODIFY COLUMN ID_Usuario INT NOT NULL;
ALTER TABLE compras MODIFY COLUMN id INT AUTO_INCREMENT PRIMARY KEY;

-- Corregir tabla medio_de_pago (ID_Usuario debe ser INT)
ALTER TABLE medio_de_pago MODIFY COLUMN ID_Usuario INT NOT NULL;
ALTER TABLE medio_de_pago MODIFY COLUMN id INT AUTO_INCREMENT PRIMARY KEY;

-- Corregir tabla carrito (ID_Usuario debe ser INT)
ALTER TABLE carrito MODIFY COLUMN ID_Usuario INT NOT NULL;
ALTER TABLE carrito MODIFY COLUMN id INT AUTO_INCREMENT PRIMARY KEY;

-- Corregir tabla detalle_orden (ID_Producto debe ser INT)
ALTER TABLE detalle_orden MODIFY COLUMN ID_Producto INT NOT NULL;
ALTER TABLE detalle_orden MODIFY COLUMN id INT AUTO_INCREMENT PRIMARY KEY;

-- Corregir tabla mensajes_directos si existe
-- ALTER TABLE mensajes_directos MODIFY COLUMN ID_Usuario INT NOT NULL;
-- ALTER TABLE mensajes_directos MODIFY COLUMN id INT AUTO_INCREMENT PRIMARY KEY;

-- Corregir tabla favorito (ID_Usuario debe ser INT)
ALTER TABLE favorito MODIFY COLUMN usuario_id INT NOT NULL;
ALTER TABLE favorito MODIFY COLUMN id INT AUTO_INCREMENT PRIMARY KEY;

-- Corregir tabla user_payment_methods (user_id debe ser INT)
ALTER TABLE user_payment_methods MODIFY COLUMN user_id INT NOT NULL;
ALTER TABLE user_payment_methods MODIFY COLUMN id INT AUTO_INCREMENT PRIMARY KEY;

-- Corregir tabla mensaje_empleado (empleado_id debe ser INT)
ALTER TABLE mensaje_empleado MODIFY COLUMN empleado_id INT NOT NULL;
ALTER TABLE mensaje_empleado MODIFY COLUMN id INT AUTO_INCREMENT PRIMARY KEY;

-- Reactivar la verificación de claves foráneas
SET FOREIGN_KEY_CHECKS = 1;

-- Eliminar restricciones problemáticas si existen
DROP FOREIGN KEY IF EXISTS FK9u5ke88ysfr99vjcxok1136i6;
DROP FOREIGN KEY IF EXISTS FKgy7qks0mv9fqa0lvs4036npg;

-- Recrear las restricciones con tipos correctos
-- Estas se crearán automáticamente cuando Hibernate inicie

-- Mostrar el estado final de las tablas
SELECT 
    TABLE_NAME,
    COLUMN_NAME,
    DATA_TYPE,
    IS_NULLABLE,
    COLUMN_KEY
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'technova_java1' 
    AND COLUMN_NAME IN ('id', 'ID_Producto', 'ID_Usuario', 'user_id', 'usuario_id', 'empleado_id')
ORDER BY TABLE_NAME, COLUMN_NAME;

SELECT 'Database schema fixes completed successfully!' as status;
