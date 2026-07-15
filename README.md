# ALToque Pago - Sistema de Autoservicio y Gestión

[![Java](https://img.shields.io/badge/Java-17%2B-ED8B00?style=for-the-badge&logo=java&logoColor=white)](https://img.shields.io/badge/Java-17%2B-ED8B00?style=for-the-badge&logo=java&logoColor=white)
[![JavaFX](https://img.shields.io/badge/JavaFX-UI-4796CE?style=for-the-badge&logo=java&logoColor=white)](https://img.shields.io/badge/JavaFX-UI-4796CE?style=for-the-badge&logo=java&logoColor=white)
[![MySQL](https://img.shields.io/badge/MySQL-Database-4479A1?style=for-the-badge&logo=mysql&logoColor=white)](https://img.shields.io/badge/MySQL-Database-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
[![Maven](https://img.shields.io/badge/Maven-Build-C71A22?style=for-the-badge&logo=apachemaven&logoColor=white)](https://img.shields.io/badge/Maven-Build-C71A22?style=for-the-badge&logo=apachemaven&logoColor=white)

ALToque Pago es una aplicación de escritorio desarrollada en **JavaFX** orientada a la gestión integral y facturación rápida para negocios tipo minimarket o ferretería. El sistema está dividido en dos grandes módulos: un **Back-Office** para la administración interna y un **Front-Office** diseñado como kiosko de autoservicio para los clientes.

## Características Principales

### Módulo de Gestión (Back-Office)

- **Login y Perfiles:** Autenticación de usuarios con control de acceso basado en roles (Administrador, Vendedor, Almacén).
- **Menú Principal:** Panel de navegación central hacia todos los módulos del sistema.
- **Gestión de Productos:** Registro y actualización de productos, con pantalla dedicada para altas de nuevos productos.
- **Control de Inventario:** Seguimiento de stock y registro de movimientos de inventario (ingresos, ajustes, mermas).
- **Control de Empleados:** Alta y administración del personal (`empleados` / `nuevoempleado`).
- **Sistema de Ventas:** Punto de venta tradicional para cajeros, con flujo de nueva venta y carrito de compra.
- **Gestión de Caja:** Apertura, control y arqueo de caja.
- **Ofertas y Promociones:** Módulo para la configuración de ofertas sobre productos.
- **Consultas:** Pantalla de consultas generales sobre el sistema (por ejemplo, búsqueda de clientes vía DNI/RUC).
- **Reportes:** Generación de reportes de ventas e inventario en formato PDF (vía OpenPDF) y exportación a Excel (vía Apache POI).
- **Configuración:** Pantalla para editar los datos de la empresa (razón social, RUC, dirección, teléfono) y las preferencias del kiosko (impresión automática, modo oscuro, sonido del escáner), persistidos en `configuracion.properties`.

### Kiosko de Autoservicio (Front-Office)

- **Modo Pantalla Completa (Kiosk Mode):** Interfaz inmersiva y segura para el cliente.
- **Escaneo Rápido:** Pantalla de escáner para integración con lector de código de barras y carga de productos al carrito.
- **Captura de Datos del Cliente:** Formulario de datos con consulta automática de DNI/RUC contra la API de SUNAT.
- **Selección de Método de Pago:** Pantalla de elección entre los medios de pago disponibles.
- **Pago con Cuenta Dividida:** Flujo de checkout con posibilidad de dividir el pago entre varios métodos/personas.
- **Pasarela de Cobro QR (Yape / Plin):** Generación dinámica de códigos QR para el cobro (vía ZXing).
- **Confirmación de Pago:** Pantalla de éxito de pago con emisión del comprobante (boleta) en PDF.

## Tecnologías y Arquitectura

El proyecto sigue el patrón de diseño **Modelo-Vista-Controlador (MVC)**, separando la lógica de negocio de la interfaz gráfica, aplica el patrón **DAO (Data Access Object)** —con sus respectivas interfaces— para las interacciones con la base de datos, y una capa de **Servicios** que encapsula la lógica de negocio (carrito, pagos, productos, generación de comprobantes, consultas a SUNAT, seguridad, sesión).

- **Lenguaje:** Java 21
- **Interfaz Gráfica:** JavaFX 17 (Archivos FXML estilizados con CSS nativo).
- **Base de Datos:** MySQL (Diseño relacional en 3FN, con 14 tablas: `cliente`, `rol`, `usuario_personal`, `reporte`, `producto`, `carrito`, `detalle_carrito`, `venta`, `detalle_venta`, `pago`, `comprobante`, `movimiento_inventario`, `arqueo_caja`, `oferta`).
- **Gestor de Dependencias:** Maven.
- **Testing:** JUnit 5, Mockito y TestFX para pruebas unitarias, de integración y de interfaz (UI), más TestNG para pruebas de flujo/UAT.
- **Librerías Adicionales:**
  * `OpenPDF`: Para la emisión de comprobantes (boletas) de venta y generación de reportes en PDF.
  * `Apache POI`: Para la exportación de historiales y cuadres de caja a formatos de Microsoft Excel (.xlsx).
  * `Gson (Google)`: Para parsear e integrar la respuesta JSON de la API pública de SUNAT (búsqueda automática de DNI/RUC).
  * `ZXing (Zebra Crossing)`: Para la generación dinámica de códigos QR bidimensionales en la pasarela de cobro (Yape / Plin).
  * `SLF4J & Logback`: Para el monitoreo y registro (logging) de errores transaccionales en un archivo local (`altoquepago-errores.log`).
  * `Ikonli (FontAwesome)` y `ControlsFX` / `BootstrapFX`: Para la iconografía vectorial y componentes adicionales de la interfaz gráfica.

## Estructura del Proyecto

```
src/main/java/com/
├── DB/            # Conexión a base de datos (ConexionDB)
├── controlador/   # Controladores JavaFX (Back-Office y Front-Office/Autoservicio)
├── dao/           # Acceso a datos (interfaces I*DAO + implementaciones)
├── modelo/        # Entidades del dominio (Producto, Venta, Cliente, etc.)
└── servicio/      # Lógica de negocio (carrito, pagos, PDF, SUNAT, seguridad)

src/main/resources/com/
├── vista/         # Vistas FXML (login, menú, ventas, autoservicio, etc.)
├── stylesCSS/      # Hojas de estilo
└── imagenes/       # Recursos gráficos

src/test/java/com/
├── pruebasUnitarias/    # Pruebas unitarias (controlador, dao, servicio)
├── pruebasIntegracion/  # Pruebas de integración contra la base de datos
└── pruebasUI/           # Pruebas de interfaz con TestFX

database/          # Script SQL de creación de la base de datos (schema.sql)
Tickets/           # Ejemplos de boletas/comprobantes generados en PDF
```

## Estructura de la Base de Datos

El sistema se apoya en una base de datos relacional (`Autoservicio`) compuesta por 14 tablas principales que gestionan la trazabilidad completa, desde el ingreso del producto por el proveedor hasta la emisión del comprobante de pago al cliente. El script completo de creación se encuentra en [`database/schema.sql`](database/schema.sql).

## Instalación y Configuración

1. **Clonar el repositorio:**

   ```bash
   git clone https://github.com/said-ssj/AltoquePagoProyecto.git
   cd AltoquePagoProyecto
   ```

2. **Crear la base de datos:**

   Ejecuta el script `database/schema.sql` en tu servidor MySQL. Esto elimina (si existe) y crea la base de datos `Autoservicio` con sus 14 tablas.

   ```bash
   mysql -u root -p < database/schema.sql
   ```

3. **Configurar la conexión a la base de datos:**

   Edita el archivo `src/main/resources/db.properties` con tus credenciales locales:

   ```properties
   db.url=jdbc:mysql://localhost:3306/Autoservicio
   db.user=root
   db.password=tu_contraseña
   ```

4. **Compilar y ejecutar con Maven:**

   ```bash
   ./mvnw clean javafx:run
   ```

   La clase principal de la aplicación es `com.controlador.MainApp`.

5. **(Opcional) Ejecutar las pruebas:**

   ```bash
   ./mvnw test
   ```

## Configuración de la Empresa

Los datos del negocio (razón social, RUC, dirección, teléfono) y las preferencias del kiosko (impresión automática, modo oscuro, sonido del escáner, tamaño de papel de impresión) se gestionan desde el módulo **Configuración** del Back-Office y se persisten en el archivo `configuracion.properties` en la raíz del proyecto.
