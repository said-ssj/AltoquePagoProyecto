-- ==============================================================================
--  PLANTILLA_ACTUALIZAR_TABLAS.SQL
--
--  Plantilla para futuras migraciones/actualizaciones de estructura de la
--  base de datos "Autoservicio", SIN perder los datos existentes (a
--  diferencia de database/schema.sql, que borra y recrea todo desde cero).
--
--  Como usarla:
--    1. Copie este archivo con un nombre descriptivo y con fecha, por
--       ejemplo: 2026-07-15_agregar_campo_descuento_producto.sql
--    2. Escriba las sentencias ALTER TABLE / CREATE TABLE / etc. necesarias.
--    3. SIEMPRE respalde la base antes de aplicarla (01_backups\backup_bd.bat).
--    4. Pruebe primero en un entorno de desarrollo/pruebas.
--    5. Registre el cambio tambien en database/schema.sql para que el
--       script de creacion desde cero quede sincronizado con la realidad.
--
--  Ejemplo (comentado, solo como referencia):
-- ==============================================================================

USE Autoservicio;

START TRANSACTION;

-- Ejemplo: agregar una columna nueva sin romper datos existentes
-- ALTER TABLE producto
--     ADD COLUMN descuento_maximo DECIMAL(5,2) NOT NULL DEFAULT 0.00
--     AFTER precio_venta;

-- Ejemplo: crear un indice para mejorar el rendimiento de una consulta frecuente
-- CREATE INDEX idx_venta_fecha ON venta (fecha);

COMMIT;
