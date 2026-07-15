-- ==============================================================================
--  LIMPIAR_CARRITOS_ABANDONADOS.SQL
--  Tarea de mantenimiento: elimina "registros temporales" del sistema.
--
--  En AltoquePago, el registro temporal por excelencia es el carrito de
--  compra: se crea con estado 'ACTIVO' (ver tabla `carrito`, columna
--  `estado`, default 'ACTIVO' en database/schema.sql) y normalmente pasa a
--  concretarse en una venta. Los carritos que quedan en 'ACTIVO' por mucho
--  tiempo corresponden a compras abandonadas (kiosko cerrado a mitad de
--  compra, cliente que se retira, etc.) y solo ocupan espacio.
--
--  Este script elimina los carritos en estado 'ACTIVO' con mas de 15 dias
--  de antiguedad, junto con sus lineas de detalle (detalle_carrito).
--  NO toca carritos que ya se convirtieron en venta (esos quedan asociados
--  al historial de ventas/reportes y no deben borrarse).
--
--  Recomendacion de uso: ejecutar semanalmente (o mensual, segun el
--  volumen del negocio), idealmente despues de un backup.
--
--  Ejecucion:
--    mysql -u root -p Autoservicio < limpiar_carritos_abandonados.sql
-- ==============================================================================

USE Autoservicio;

START TRANSACTION;

-- 1) Reporte previo: cuantos carritos se van a eliminar (para el log/consola)
SELECT COUNT(*) AS carritos_abandonados_a_eliminar
FROM carrito
WHERE estado = 'ACTIVO'
  AND fecha_creacion < DATE_SUB(NOW(), INTERVAL 15 DAY);

-- 2) Elimina primero el detalle (tabla hija) para respetar las FOREIGN KEY
DELETE dc FROM detalle_carrito dc
INNER JOIN carrito c ON c.id_carrito = dc.id_carrito
WHERE c.estado = 'ACTIVO'
  AND c.fecha_creacion < DATE_SUB(NOW(), INTERVAL 15 DAY);

-- 3) Elimina los carritos abandonados (tabla padre)
DELETE FROM carrito
WHERE estado = 'ACTIVO'
  AND fecha_creacion < DATE_SUB(NOW(), INTERVAL 15 DAY);

COMMIT;

-- Nota: si en el futuro se agregan otras tablas "temporales" (por ejemplo,
-- sesiones de kiosko, tokens de pago pendientes, etc.), agreguen aqui un
-- bloque DELETE similar, siempre respetando el orden hijo -> padre por las
-- llaves foraneas.
