-- ==============================================================================
--  BLOQUE DE REINICIO Y CREACIÓN DE ESTRUCTURA (BASE DE DATOS: AUTOSERVICIO)
-- ==============================================================================

DROP DATABASE IF EXISTS Autoservicio;
CREATE DATABASE Autoservicio;
USE Autoservicio;

-- 1. Tabla Cliente (Estructura alineada a SUNAT)
CREATE TABLE cliente (
    id_cliente INT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(100),
    apellido VARCHAR(100),
    razon_social VARCHAR(200),
    correo VARCHAR(100),
    numero_documento VARCHAR(8),
    numero_ruc VARCHAR(11),
    telefono VARCHAR(15),             
    direccion VARCHAR(255),
    ubigeo CHAR(6),
    tipo_documento CHAR(1) NOT NULL,  
    observacion TEXT
);

-- 2. Tabla Rol
CREATE TABLE rol (
    id_rol INT PRIMARY KEY AUTO_INCREMENT,
    nombre_rol VARCHAR(50) NOT NULL
);

-- 3. Tabla Empleados (usuario_personal)
CREATE TABLE usuario_personal (
    id_usuario INT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    contraseña VARCHAR(255) NOT NULL,
    id_rol INT NOT NULL,
    fecha_nacimiento DATE NOT NULL DEFAULT '2000-01-01',
    tipo_documento VARCHAR(20) NOT NULL DEFAULT 'DNI',
    numero_documento VARCHAR(20) NOT NULL DEFAULT '00000000',
    nacionalidad VARCHAR(50) NOT NULL DEFAULT 'Peruano',
    direccion VARCHAR(255) NULL,
    telefono VARCHAR(15) NOT NULL DEFAULT '000000000',
    telefono_emergencia VARCHAR(15) NULL,
    area VARCHAR(50) NOT NULL DEFAULT 'Ventas',
    tipo_contrato VARCHAR(50) NOT NULL DEFAULT 'Tiempo Completo',
    fecha_inicio DATE NOT NULL DEFAULT '2026-01-01',
    salario_base DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    metodo_pago VARCHAR(50) NOT NULL DEFAULT 'Efectivo',
    datos_bancarios VARCHAR(255) NOT NULL DEFAULT 'Pendiente',
    antecedentes VARCHAR(50) NULL DEFAULT 'Pendiente',
    
    CONSTRAINT FK_usuario_idrol FOREIGN KEY (id_rol) REFERENCES rol(id_rol)
);

-- 4. Tabla Reporte
CREATE TABLE reporte (
    id_reporte INT PRIMARY KEY AUTO_INCREMENT,
    id_usuario INT NOT NULL,
    tipo_reporte VARCHAR(60) NOT NULL,
    fecha DATETIME DEFAULT NOW(),
    
    CONSTRAINT FK_Reporte_Usuario FOREIGN KEY (id_usuario) REFERENCES usuario_personal(id_usuario)
);

-- 5. Tabla Producto
CREATE TABLE producto (
    id_producto INT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    precio DECIMAL(10,2) NOT NULL,
    stock INT NOT NULL CHECK(stock >= 0),
    codigo_barras VARCHAR(100) UNIQUE
);

-- 6. Tabla Carrito
CREATE TABLE carrito (
    id_carrito INT PRIMARY KEY AUTO_INCREMENT,
    id_cliente INT NOT NULL,
    total DECIMAL(10,2) DEFAULT 0,
    fecha_creacion DATETIME DEFAULT NOW(),
    estado VARCHAR(20) DEFAULT 'ACTIVO',
    
    CONSTRAINT FK_Carrito_Cliente FOREIGN KEY (id_cliente) REFERENCES cliente(id_cliente)
);

-- 7. Tabla Detalle_Carrito
CREATE TABLE detalle_carrito (
    id_carrito INT NOT NULL,
    id_producto INT NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    
    PRIMARY KEY (id_carrito, id_producto),
    CONSTRAINT FK_DetalleC_Carrito FOREIGN KEY (id_carrito) REFERENCES carrito(id_carrito),
    CONSTRAINT FK_DetalleC_Producto FOREIGN KEY (id_producto) REFERENCES producto(id_producto)
);

-- 8. Tabla Venta
CREATE TABLE venta (
    id_venta INT PRIMARY KEY AUTO_INCREMENT,
    id_cliente INT NOT NULL,
    fecha DATETIME DEFAULT NOW(),
    total DECIMAL(10,2) NOT NULL,
    
    CONSTRAINT FK_Venta_Cliente FOREIGN KEY (id_cliente) REFERENCES cliente(id_cliente)
);

-- 9. Tabla Detalle_Venta
CREATE TABLE detalle_venta (
    id_detalle INT PRIMARY KEY AUTO_INCREMENT,
    id_venta INT NOT NULL,
    id_producto INT NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    
    CONSTRAINT FK_DetalleV_Venta FOREIGN KEY (id_venta) REFERENCES venta(id_venta),
    CONSTRAINT FK_DetalleV_Producto FOREIGN KEY (id_producto) REFERENCES producto(id_producto)
);

-- 10. Tabla Pago
CREATE TABLE pago (
    id_pago INT PRIMARY KEY AUTO_INCREMENT,
    id_venta INT NOT NULL,
    metodo ENUM('YAPE','PLIN','TARJETA') NOT NULL,
    monto DECIMAL(10,2) NOT NULL,
    estado ENUM('PENDIENTE','PAGADO','RECHAZADO') NOT NULL,
    
    CONSTRAINT FK_Pago_Venta FOREIGN KEY (id_venta) REFERENCES venta(id_venta)
);

-- 11. Tabla Comprobante
CREATE TABLE comprobante (
    id_comprobante INT PRIMARY KEY AUTO_INCREMENT,
    id_venta INT NOT NULL,
    tipo_documento VARCHAR(20) NOT NULL,
    numero_documento VARCHAR(20) NOT NULL,
    fecha_emision DATETIME DEFAULT NOW(),
    
    CONSTRAINT FK_Comprobante_Venta FOREIGN KEY (id_venta) REFERENCES venta(id_venta)
);

-- 12. Tabla Movimiento de Inventario
CREATE TABLE movimiento_inventario (
    id_movimiento INT PRIMARY KEY AUTO_INCREMENT,
    id_producto INT NOT NULL,
    tipo_movimiento VARCHAR(50) NOT NULL,
    cantidad INT NOT NULL,
    fecha DATETIME DEFAULT NOW(),
    descripcion VARCHAR(255),
    
    CONSTRAINT FK_Movimiento_Producto FOREIGN KEY (id_producto) REFERENCES producto(id_producto)
);

-- 13. Tabla Arqueo de Caja
CREATE TABLE arqueo_caja (
    id_arqueo INT PRIMARY KEY AUTO_INCREMENT,
    id_usuario INT NULL,
    fecha_apertura DATETIME DEFAULT NOW(),
    fecha_cierre DATETIME NULL,
    monto_inicial DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    efectivo_sistema DECIMAL(10,2) NULL,
    efectivo_contado DECIMAL(10,2) NULL,
    diferencia DECIMAL(10,2) NULL,
    total_yape DECIMAL(10,2) NULL,
    total_plin DECIMAL(10,2) NULL,
    total_tarjeta DECIMAL(10,2) NULL,
    total_ventas DECIMAL(10,2) NULL,
    estado VARCHAR(10) NOT NULL DEFAULT 'ABIERTA',

    CONSTRAINT FK_Arqueo_Usuario FOREIGN KEY (id_usuario) REFERENCES usuario_personal(id_usuario)
);

-- 14. Tabla Oferta
CREATE TABLE oferta (
    id_oferta INT PRIMARY KEY AUTO_INCREMENT,
    id_producto INT NOT NULL,
    descripcion VARCHAR(255),
    descuento DECIMAL(5,2),
    fecha_inicio DATE,
    fecha_fin DATE,
    estado BOOLEAN DEFAULT TRUE,
    
    CONSTRAINT FK_Oferta_Producto FOREIGN KEY (id_producto) REFERENCES producto(id_producto)
);


-- ==============================================================================
--  BLOQUE DE INSERCIÓN DE DATOS SEMILLA / PRUEBA
-- ==============================================================================

-- Roles de usuario
INSERT INTO rol (nombre_rol) VALUES 
('Administrador'), ('Vendedor'), ('Almacen');

-- Usuarios del Personal 
INSERT INTO usuario_personal (nombre, email, contraseña, id_rol) VALUES 
('Carlos Admin', 'admin1@minimarket.com', 'S3gur@_M1n1m4rk3t!#2026', 1),
('Ana Lopez', 'ana.lopez@minimarket.com', 'ana1995', 2),
('Juan Perez', 'juan.perez@minimarket.com', '123456789', 2),
('Maria Gomez', 'maria.gomez@minimarket.com', 'mariagomez', 2),
('Luis Ramirez', 'luis.ramirez@minimarket.com', 'V3nd3d0r_L0c4l$$', 2),
('Marta Rojas', 'marta.rojas@minimarket.com', 'marta2010', 2),
('Pedro Almacen', 'pedro.almacen@minimarket.com', '111111', 3),
('Lucia Torres', 'lucia.torres@minimarket.com', 'luciatorres', 3),
('Jorge Silva', 'jorge.silva@minimarket.com', 'J0rg3_@lm4c3n*99', 3),
('Gerente General', 'gerente@minimarket.com', 'G3r3nt3#2026%', 1),
('ADMIN', 'admin@minimarket.com', 'admin', 1);

-- Encriptación inmediata de contraseñas mediante Hash SHA-256
SET SQL_SAFE_UPDATES = 0;
UPDATE usuario_personal SET contraseña = SHA2(contraseña, 256);
SET SQL_SAFE_UPDATES = 1;

-- Catálogo de Productos
INSERT INTO producto (nombre, precio, stock, codigo_barras) VALUES 
('Gaseosa Inca Kola 1.5L', 6.50, 100, '7750001001'),
('Gaseosa Coca Cola 1.5L', 6.50, 120, '7750001002'),
('Agua San Mateo 2.5L', 3.50, 80, '7750001003'),
('Jugo Frugos Durazno 1L', 4.80, 50, '7750001004'),
('Cerveza Pilsen Callao 620ml', 5.50, 200, '7750001005'),
('Cerveza Cusqueña Trigo 620ml', 6.00, 150, '7750001006'),
('Leche Evaporada Gloria Azul', 3.80, 300, '7750001007'),
('Leche Evaporada Ideal', 3.50, 120, '7750001008'),
('Yogurt Gloria Fresa 1L', 6.20, 60, '7750001009'),
('Mantequilla Gloria 200g', 5.50, 40, '7750001010'),
('Queso Edam Laive 250g', 9.50, 30, '7750001011'),
('Aceite Primor Premium 1L', 9.50, 100, '7750001012'),
('Aceite Cocinero 1L', 7.50, 90, '7750001013'),
('Arroz Costeño Extra 1Kg', 4.20, 200, '7750001014'),
('Arroz Paisana 1Kg', 4.00, 150, '7750001015'),
('Azúcar Rubia Cartavio 1Kg', 3.80, 250, '7750001016'),
('Azúcar Blanca Paramonga 1Kg', 4.20, 100, '7750001017'),
('Fideos Don Vittorio Spaghetti 500g', 2.80, 180, '7750001018'),
('Fideos Lavaggi Tallarin 500g', 2.50, 120, '7750001019'),
('Atún Florida Grated', 4.50, 100, '7750001020'),
('Atún Campomar Filete', 5.50, 80, '7750001021'),
('Avena Quaker 500g', 3.50, 70, '7750001022'),
('Mayonesa Alacena 500cc', 7.50, 90, '7750001023'),
('Ketchup Alacena 250g', 3.50, 60, '7750001024'),
('Sal de Maras Emsal 1Kg', 2.00, 150, '7750001025'),
('Galleta Soda Field (Pack x6)', 3.50, 100, '7750001026'),
('Galleta Morochas', 0.80, 300, '7750001027'),
('Galleta Casino Menta', 0.80, 250, '7750001028'),
('Chocolate Sublime', 1.50, 400, '7750001029'),
('Doña Pepa', 1.20, 200, '7750001030'),
('Papitas Lays Clásica 150g', 4.50, 80, '7750001031'),
('Doritos Queso 150g', 5.00, 75, '7750001032'),
('Piqueo Snax 200g', 6.50, 60, '7750001033'),
('Detergente Ariel 1Kg', 12.50, 50, '7750001034'),
('Detergente Bolivar 1Kg', 11.00, 60, '7750001035'),
('Suavizante Downy 800ml', 8.50, 40, '7750001036'),
('Jabón Bolívar Lavar (Pack 2)', 4.50, 100, '7750001037'),
('Jabón de Tocador Nivea', 2.50, 120, '7750001038'),
('Champú Head & Shoulders 400ml', 18.50, 45, '7750001039'),
('Pasta Dental Colgate 90g', 3.50, 150, '7750001040'),
('Papel Higiénico Suave (Aroma)', 2.00, 300, '7750001041'),
('Papel Higiénico Paracas (Pack x4)', 5.50, 200, '7750001042'),
('Servilletas Elite 100un', 2.50, 80, '7750001043'),
('Lejía Clorox 1L', 3.50, 100, '7750001044'),
('Limpiador Sapolio Lavanda 900ml', 4.00, 90, '7750001045'),
('Esponja Ayudin', 1.50, 150, '7750001046'),
('Lavalozas Ayudin Pasta', 3.00, 100, '7750001047'),
('Pilas Energizer AA (Pack x2)', 6.50, 50, '7750001048'),
('Gillette Prestobarba 3', 4.50, 80, '7750001049'),
('Desodorante Old Spice', 12.00, 40, '7750001050'),
('Desodorante Rexona Mujer', 11.50, 45, '7750001051'),
('Toallas Higiénicas Nosotras', 4.50, 60, '7750001052'),
('Pan de Molde Bimbo', 7.50, 30, '7750001053'),
('Café Altomayo 200g', 12.50, 40, '7750001054'),
('Café Nescafe Tradición 200g', 14.00, 35, '7750001055'),
('Infusión McColin Manzanilla', 2.50, 80, '7750001056'),
('Mermelada Gloria Fresa 320g', 4.50, 60, '7750001057'),
('Milo Lata 400g', 15.50, 30, '7750001058'),
('Huevos Pardos La Calera (Plancha x30)', 18.00, 25, '7750001059'),
('Agua Cielo 2.5L', 3.00, 100, '7750001060');

-- Ofertas Activas/Inactivas
INSERT INTO oferta (id_producto, descripcion, descuento, fecha_inicio, fecha_fin, estado) VALUES 
(1, 'Oferta Fin de Semana - Inca Kola', 1.00, '2026-06-01', '2026-06-30', TRUE),
(7, 'Descuento en Leche Gloria', 0.50, '2026-06-15', '2026-07-15', TRUE),
(14, 'Oferta Arroz Costeño', 0.70, '2026-06-01', '2026-06-20', FALSE), 
(29, 'Sublime 2x1 (Descuento del 50%)', 0.75, '2026-06-17', '2026-06-25', TRUE),
(34, 'Detergente Ariel Promo', 2.50, '2026-06-10', '2026-06-20', TRUE),
(42, 'Papel Paracas Familiar', 1.00, '2026-06-05', '2026-06-25', TRUE),
(12, 'Aceite Primor Rebajado', 1.50, '2026-06-17', '2026-06-30', TRUE),
(54, 'Café Altomayo Despierta', 2.00, '2026-06-01', '2026-06-30', TRUE),
(20, 'Atún Florida x Semana Santa', 0.50, '2026-03-01', '2026-04-01', FALSE),
(53, 'Pan Bimbo Desayuno', 1.50, '2026-06-15', '2026-06-22', TRUE);

-- Directorio de Clientes (Naturales y Jurídicos)
INSERT INTO cliente (nombre, razon_social, numero_documento, numero_ruc, tipo_documento, direccion, ubigeo) VALUES 
('CLIENTES VARIOS', 'CLIENTES VARIOS', '00000000', NULL, '0', '', ''),
('Andrea Gutierrez', 'Andrea Gutierrez', '10000002', NULL, '1', '', ''),
('Manuel Flores', 'Manuel Flores', '10000003', NULL, '1', '', ''),
('Rosa Castro', 'Rosa Castro', '10000004', NULL, '1', '', ''),
('Luis Vasquez', 'Luis Vasquez', '10000005', NULL, '1', '', ''),
('Carmen Ruiz', 'Carmen Ruiz', '10000006', NULL, '1', '', ''),
('Javier Mendez', 'Javier Mendez', '10000007', NULL, '1', '', ''),
('Patricia Vargas', 'Patricia Vargas', '10000008', NULL, '1', '', ''),
('Raul Fernandez', 'Raul Fernandez', '10000009', NULL, '1', '', ''),
('Monica Salazar', 'Monica Salazar', '10000010', NULL, '1', '', ''),
('Jorge Chavez', 'Jorge Chavez', '10000011', NULL, '1', '', ''),
('Silvia Herrera', 'Silvia Herrera', '10000012', NULL, '1', '', ''),
('Ricardo Aguilar', 'Ricardo Aguilar', '10000013', NULL, '1', '', ''),
('Teresa Cabrera', 'Teresa Cabrera', '10000014', NULL, '1', '', ''),
('Fernando Rios', 'Fernando Rios', '10000015', NULL, '1', '', ''),
('Elena Rojas', 'Elena Rojas', '10000016', NULL, '1', '', ''),
('Victor Morales', 'Victor Morales', '10000017', NULL, '1', '', ''),
('Gladys Medina', 'Gladys Medina', '10000018', NULL, '1', '', ''),
('Roberto Ortiz', 'Roberto Ortiz', '10000019', NULL, '1', '', ''),
('Isabel Cruz', 'Isabel Cruz', '10000020', NULL, '1', '', ''),
('Hugo Espinoza', 'Hugo Espinoza', '10000021', NULL, '1', '', ''),
('Daniela Paredes', 'Daniela Paredes', '10000022', NULL, '1', '', ''),
('Oscar Campos', 'Oscar Campos', '10000023', NULL, '1', '', ''),
('Sonia Vega', 'Sonia Vega', '10000024', NULL, '1', '', ''),
('Eduardo Dominguez', 'Eduardo Dominguez', '10000025', NULL, '1', '', ''),
('Lucero Leon', 'Lucero Leon', '10000026', NULL, '1', '', ''),
('Martin Navarro', 'Martin Navarro', '10000027', NULL, '1', '', ''),
('Alicia Villanueva', 'Alicia Villanueva', '10000028', NULL, '1', '', ''),
('Julio Miranda', 'Julio Miranda', '10000029', NULL, '1', '', ''),
('Rosaura Palacios', 'Rosaura Palacios', '10000030', NULL, '1', '', ''),
('Andres Soto', 'Andres Soto', '10000031', NULL, '1', '', ''),
('Cecilia Ramos', 'Cecilia Ramos', '10000032', NULL, '1', '', ''),
('Enrique Lozano', 'Enrique Lozano', '10000033', NULL, '1', '', ''),
('Beatriz Silva', 'Beatriz Silva', '10000034', NULL, '1', '', ''),
('Alfonso Aguilar', 'Alfonso Aguilar', '10000035', NULL, '1', '', ''),
('Maritza Reyes', 'Maritza Reyes', '10000036', NULL, '1', '', ''),
('Gustavo Delgado', 'Gustavo Delgado', '10000037', NULL, '1', '', ''),
('Rocio Peña', 'Rocio Peña', '10000038', NULL, '1', '', ''),
('Ernesto Cardenas', 'Ernesto Cardenas', '10000039', NULL, '1', '', ''),
('Viviana Ocampo', 'Viviana Ocampo', '10000040', NULL, '1', '', ''),
('Felix Suarez', 'Felix Suarez', '10000041', NULL, '1', '', ''),
('Lorena Pineda', 'Lorena Pineda', '10000042', NULL, '1', '', ''),
('Cesar Castillo', 'Cesar Castillo', '10000043', NULL, '1', '', ''),
('Juana Mendoza', 'Juana Mendoza', '10000044', NULL, '1', '', ''),
('Arturo Guerrero', 'Arturo Guerrero', '10000045', NULL, '1', '', ''),
('AGROLIGHT PERU S.A.C.', 'AGROLIGHT PERU S.A.C.', NULL, '20552103816', '6', 'PJ. JORGE BASADRE NRO. 158', '150137'),
('CONSTRUCTORA LOS ANDES EIRL', 'CONSTRUCTORA LOS ANDES EIRL', NULL, '20123456789', '6', 'AV. LOS INCAS 450', '110101'),
('MINIMARKET EL SOL', 'MINIMARKET EL SOL', NULL, '20987654321', '6', 'CA. LIMA 100', '110101'),
('TRANSPORTES GOMEZ', 'TRANSPORTES GOMEZ', NULL, '20555555555', '6', 'AV. PANAMERICANA SUR KM 300', '110101'),
('SERVICIOS GENERALES SAC', 'SERVICIOS GENERALES SAC', NULL, '20444444444', '6', 'MZ. A LT. 5 URB. SANTA MARIA', '110101');

-- Carritos Activos
INSERT INTO carrito (id_cliente, total, estado) VALUES 
(1, 15.50, 'ACTIVO'), (2, 45.00, 'ACTIVO'), (3, 8.50, 'ACTIVO'), 
(4, 25.00, 'ACTIVO'), (5, 110.00, 'ACTIVO'), (6, 12.00, 'ACTIVO'), 
(7, 30.50, 'ACTIVO'), (8, 6.50, 'ACTIVO'), (9, 14.50, 'ACTIVO'), 
(10, 55.00, 'ACTIVO');

-- Detalles de Carrito
INSERT INTO detalle_carrito (id_carrito, id_producto, cantidad, precio_unitario) VALUES 
(1, 1, 1, 6.50), (1, 14, 1, 4.20), (1, 4, 1, 4.80),
(2, 12, 2, 9.50), (2, 34, 1, 12.50), (2, 7, 3, 3.80),
(3, 29, 2, 1.50), (3, 5, 1, 5.50),
(4, 53, 1, 7.50), (4, 10, 1, 5.50), (4, 54, 1, 12.00);

-- Historial de Ventas
INSERT INTO venta (id_cliente, fecha, total) VALUES 
(11, '2026-06-10 10:30:00', 45.50), (12, '2026-06-10 11:15:00', 12.00), (13, '2026-06-10 12:00:00', 85.00),
(14, '2026-06-11 08:45:00', 23.50), (15, '2026-06-11 09:30:00', 5.50),  (16, '2026-06-11 14:20:00', 110.00),
(17, '2026-06-11 16:10:00', 32.00), (18, '2026-06-12 07:30:00', 15.50), (19, '2026-06-12 10:10:00', 65.40),
(20, '2026-06-12 11:55:00', 40.00), (21, '2026-06-12 13:40:00', 9.50),  (22, '2026-06-12 15:20:00', 78.00),
(23, '2026-06-13 09:05:00', 21.00), (24, '2026-06-13 10:50:00', 55.50), (25, '2026-06-13 12:35:00', 14.50),
(26, '2026-06-13 16:00:00', 88.00), (27, '2026-06-14 08:20:00', 3.50),  (28, '2026-06-14 09:15:00', 120.50),
(29, '2026-06-14 11:30:00', 44.00), (30, '2026-06-14 13:10:00', 19.50), (31, '2026-06-14 15:45:00', 66.00),
(32, '2026-06-15 07:50:00', 25.00), (33, '2026-06-15 09:25:00', 8.50),  (34, '2026-06-15 11:40:00', 95.00),
(35, '2026-06-15 14:15:00', 38.50), (36, '2026-06-15 16:30:00', 17.50), (37, '2026-06-16 08:10:00', 52.00),
(38, '2026-06-16 10:45:00', 29.00), (39, '2026-06-16 12:20:00', 11.50), (40, '2026-06-16 14:55:00', 74.50),
(41, '2026-06-16 17:05:00', 41.50), (42, '2026-06-16 18:30:00', 22.00), (43, '2026-06-17 07:40:00', 105.00),
(44, '2026-06-17 09:10:00', 16.00), (45, '2026-06-17 11:25:00', 82.50), (46, '2026-06-17 13:00:00', 35.00),
(47, '2026-06-17 14:35:00', 27.50), (48, '2026-06-17 15:50:00', 60.00), (49, '2026-06-17 17:15:00', 18.50),
(50, '2026-06-17 18:40:00', 92.00), (1,  '2026-06-17 19:00:00', 14.00), (2,  '2026-06-17 19:20:00', 55.00),
(3,  '2026-06-17 19:45:00', 33.50), (4,  '2026-06-17 20:10:00', 7.50),  (5,  '2026-06-17 20:30:00', 48.00),
(6,  '2026-06-17 20:55:00', 26.50), (7,  '2026-06-17 21:15:00', 19.00), (8,  '2026-06-17 21:35:00', 68.50),
(9,  '2026-06-17 21:50:00', 42.00), (10, '2026-06-17 22:10:00', 115.00);

-- Detalle de Ventas Realizadas
INSERT INTO detalle_venta (id_venta, id_producto, cantidad, precio_unitario, subtotal) VALUES 
(1, 1, 2, 6.50, 13.00), (1, 14, 2, 4.20, 8.40), (1, 34, 1, 12.50, 12.50), (1, 44, 1, 3.50, 3.50), 
(2, 5, 2, 5.50, 11.00), (2, 27, 1, 0.80, 0.80), 
(3, 39, 2, 18.50, 37.00), (3, 40, 2, 3.50, 7.00), (3, 12, 2, 9.50, 19.00), (3, 42, 2, 5.50, 11.00); 

-- Registro de Pagos Realizados
INSERT INTO pago (id_venta, metodo, monto, estado) VALUES 
(1, 'YAPE', 45.50, 'PAGADO'),       (2, 'PLIN', 12.00, 'PAGADO'),       (3, 'TARJETA', 85.00, 'PAGADO'),
(4, 'YAPE', 23.50, 'PAGADO'),       (5, 'YAPE', 5.50, 'PAGADO'),        (6, 'TARJETA', 110.00, 'PAGADO'),
(7, 'PLIN', 32.00, 'PAGADO'),       (8, 'YAPE', 15.50, 'PAGADO'),       (9, 'TARJETA', 65.40, 'PAGADO'),
(10, 'YAPE', 40.00, 'RECHAZADO'),   (11, 'YAPE', 9.50, 'PAGADO'),       (12, 'TARJETA', 78.00, 'PAGADO'),
(13, 'PLIN', 21.00, 'PAGADO'),      (14, 'YAPE', 55.50, 'PAGADO'),      (15, 'YAPE', 14.50, 'PAGADO'),
(16, 'TARJETA', 88.00, 'PAGADO'),   (17, 'PLIN', 3.50, 'PAGADO'),       (18, 'TARJETA', 120.50, 'PAGADO'),
(19, 'YAPE', 44.00, 'PAGADO'),      (20, 'YAPE', 19.50, 'PENDIENTE'),   (21, 'TARJETA', 66.00, 'PAGADO'),
(22, 'PLIN', 25.00, 'PAGADO'),      (23, 'YAPE', 8.50, 'PAGADO'),       (24, 'TARJETA', 95.00, 'PAGADO'),
(25, 'YAPE', 38.50, 'PAGADO'),      (26, 'PLIN', 17.50, 'PAGADO'),      (27, 'TARJETA', 52.00, 'PAGADO'),
(28, 'YAPE', 29.00, 'PAGADO'),      (29, 'YAPE', 11.50, 'PAGADO'),      (30, 'TARJETA', 74.50, 'PAGADO'),
(31, 'PLIN', 41.50, 'PAGADO'),      (32, 'YAPE', 22.00, 'PAGADO'),      (33, 'TARJETA', 105.00, 'RECHAZADO'),
(34, 'YAPE', 16.00, 'PAGADO'),      (35, 'YAPE', 82.50, 'PAGADO'),      (36, 'TARJETA', 35.00, 'PAGADO'),
(37, 'PLIN', 27.50, 'PAGADO'),      (38, 'YAPE', 60.00, 'PAGADO'),      (39, 'TARJETA', 18.50, 'PAGADO'),
(40, 'YAPE', 92.00, 'PAGADO'),      (41, 'PLIN', 14.00, 'PAGADO'),      (42, 'TARJETA', 55.00, 'PAGADO'),
(43, 'YAPE', 33.50, 'PAGADO'),      (44, 'YAPE', 7.50, 'PAGADO'),       (45, 'TARJETA', 48.00, 'PAGADO'),
(46, 'PLIN', 26.50, 'PAGADO'),      (47, 'YAPE', 19.00, 'PENDIENTE'),   (48, 'TARJETA', 68.50, 'PAGADO'),
(49, 'YAPE', 42.00, 'PAGADO'),      (50, 'TARJETA', 115.00, 'PAGADO');

-- Comprobantes Emitidos
INSERT INTO comprobante (id_venta, tipo, fecha) VALUES 
(1, 'BOLETA', '2026-06-10 10:30:15'),  (2, 'BOLETA', '2026-06-10 11:15:20'),  (3, 'FACTURA', '2026-06-10 12:00:30'),
(4, 'BOLETA', '2026-06-11 08:45:10'),  (5, 'BOLETA', '2026-06-11 09:30:05'),  (6, 'FACTURA', '2026-06-11 14:20:45'),
(7, 'BOLETA', '2026-06-11 16:10:15'),  (8, 'BOLETA', '2026-06-12 07:30:25'),  (9, 'FACTURA', '2026-06-12 10:10:10'),
(11, 'BOLETA', '2026-06-12 13:40:15'), (12, 'FACTURA', '2026-06-12 15:20:30'), (13, 'BOLETA', '2026-06-13 09:05:05'),
(14, 'BOLETA', '2026-06-13 10:50:20'), (15, 'BOLETA', '2026-06-13 12:35:10'), (16, 'FACTURA', '2026-06-13 16:00:40'),
(17, 'BOLETA', '2026-06-14 08:20:15'), (18, 'FACTURA', '2026-06-14 09:15:50'), (19, 'BOLETA', '2026-06-14 11:30:20'),
(21, 'FACTURA', '2026-06-14 15:45:30'), (22, 'BOLETA', '2026-06-15 07:50:10'), (23, 'BOLETA', '2026-06-15 09:25:05'),
(24, 'FACTURA', '2026-06-15 11:40:40'), (25, 'BOLETA', '2026-06-15 14:15:20'), (26, 'BOLETA', '2026-06-15 16:30:15'),
(27, 'FACTURA', '2026-06-16 08:10:35'), (28, 'BOLETA', '2026-06-16 10:45:10'), (29, 'BOLETA', '2026-06-16 12:20:05'),
(30, 'FACTURA', '2026-06-16 14:55:40'), (31, 'BOLETA', '2026-06-16 17:05:25'), (32, 'BOLETA', '2026-06-16 18:30:15'),
(34, 'BOLETA', '2026-06-17 09:10:05'), (35, 'FACTURA', '2026-06-17 11:25:35'), (36, 'BOLETA', '2026-06-17 13:00:10'),
(37, 'BOLETA', '2026-06-17 14:35:15'), (38, 'FACTURA', '2026-06-17 15:50:45'), (39, 'BOLETA', '2026-06-17 17:15:20'),
(40, 'FACTURA', '2026-06-17 18:40:30'), (41, 'BOLETA', '2026-06-17 19:00:10'), (42, 'FACTURA', '2026-06-17 19:20:40'),
(43, 'BOLETA', '2026-06-17 19:45:15'), (44, 'BOLETA', '2026-06-17 20:10:05'), (45, 'FACTURA', '2026-06-17 20:30:35'),
(46, 'BOLETA', '2026-06-17 20:55:10'), (48, 'FACTURA', '2026-06-17 21:35:45'), (49, 'BOLETA', '2026-06-17 21:50:20'),
(50, 'FACTURA', '2026-06-17 22:10:50');

-- Auditoría de Kárdex / Inventario
INSERT INTO movimiento_inventario (id_producto, tipo_movimiento, cantidad, descripcion) VALUES 
(1, 'ENTRADA', 100, 'Compra a proveedor distribuidor Coca Cola'),
(7, 'ENTRADA', 300, 'Lote mensual de Leche Gloria'),
(14, 'ENTRADA', 200, 'Ingreso de sacos de arroz'),
(1, 'SALIDA', 5, 'Venta en caja'),
(14, 'SALIDA', 2, 'Venta en caja'),
(34, 'ENTRADA', 50, 'Reposición de stock de detergente'),
(5, 'ENTRADA', 200, 'Stock fin de semana cervezas'),
(5, 'SALIDA', 24, 'Venta al por mayor a cliente VIP'),
(40, 'ENTRADA', 150, 'Reposición pasta dental'),
(53, 'ENTRADA', 30, 'Lote fresco de pan Bimbo'),
(53, 'SALIDA', 5, 'Venta en mostrador'),
(12, 'ENTRADA', 100, 'Compra Aceite Primor'),
(29, 'ENTRADA', 400, 'Compra caja de chocolates Sublime para oferta'),
(29, 'SALIDA', 20, 'Ventas por promo 2x1'),
(60, 'ENTRADA', 100, 'Reposición de Agua Cielo');


-- ==============================================================================
--  CONSULTAS DE ANALÍTICA E INDICADORES (DASHBOARD)
-- ==============================================================================

-- 1. Total de ventas del mes actual
SELECT SUM(total) AS total_mes 
FROM venta 
WHERE MONTH(fecha) = MONTH(CURRENT_DATE()) 
  AND YEAR(fecha) = YEAR(CURRENT_DATE());

-- 2. Total de ventas del mes anterior (Métrica comparativa de crecimiento)
SELECT SUM(total) AS total_mes_anterior 
FROM venta 
WHERE MONTH(fecha) = MONTH(CURRENT_DATE() - INTERVAL 1 MONTH) 
  AND YEAR(fecha) = YEAR(CURRENT_DATE() - INTERVAL 1 MONTH);

-- 3. Volumen total de productos en catálogo
SELECT COUNT(id_producto) AS total_productos 
FROM producto;

-- 4. Monitoreo de actividad: Últimas 4 ventas procesadas
SELECT c.nombre, c.apellido, v.id_venta, v.total, v.fecha 
FROM venta v 
JOIN cliente c ON v.id_cliente = c.id_cliente 
ORDER BY v.fecha DESC 
LIMIT 4;

-- 5. Top de demanda: Los 4 productos más vendidos
SELECT p.nombre, SUM(dv.cantidad) AS cantidad_vendida 
FROM detalle_venta dv 
JOIN producto p ON dv.id_producto = p.id_producto 
GROUP BY p.id_producto 
ORDER BY cantidad_vendida DESC 
LIMIT 4;
