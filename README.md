# ALToque Pago - Sistema de Autoservicio y Gestión

![Java](https://img.shields.io/badge/Java-17%2B-ED8B00?style=for-the-badge&logo=java&logoColor=white) ![JavaFX](https://img.shields.io/badge/JavaFX-UI-4796CE?style=for-the-badge&logo=java&logoColor=white) ![MySQL](https://img.shields.io/badge/MySQL-Database-4479A1?style=for-the-badge&logo=mysql&logoColor=white) ![Maven](https://img.shields.io/badge/Maven-Build-C71A22?style=for-the-badge&logo=apachemaven&logoColor=white)

ALToque Pago es una aplicación de escritorio desarrollada en **JavaFX** orientada a la gestión integral y facturación rápida para negocios tipo minimarket o ferretería. El sistema está dividido en dos grandes módulos: un **Back-Office** para la administración interna y un **Front-Office** diseñado como kiosko de autoservicio para los clientes.

## Características Principales

###Módulo de Gestión (Back-Office)
* **Gestión de Productos:** Registro, actualización y control de inventario con alertas de stock mínimo.
* **Control de Empleados:** Administración del personal y tipos de contrato.
* **Sistema de Ventas:** Punto de venta tradicional para cajeros.
* **Perfiles y Accesos:** Control de seguridad basado en roles (Administrador, Vendedor, Almacén).
* **Reportes:** Generación automática de reportes de ventas e inventario en formato PDF.

### Kiosko de Autoservicio (Front-Office)
* **Modo Pantalla Completa (Kiosk Mode):** Interfaz inmersiva y segura para el cliente.
* **Escaneo Rápido:** Integración con lector de código de barras para agregar productos al carrito.
* **Checkout Intuitivo:** Pantalla de resumen de compra y selección de métodos de pago.

## Tecnologías y Arquitectura

El proyecto sigue el patrón de diseño **Modelo-Vista-Controlador (MVC)**, separando la lógica de negocio de la interfaz gráfica, y utiliza el patrón **DAO (Data Access Object)** para las interacciones con la base de datos.

* **Lenguaje:** Java 21
* **Interfaz Gráfica:** JavaFX (Archivos FXML estilizados con CSS nativo).
* **Base de Datos:** MySQL (Diseño relacional en 3FN).
* **Gestor de Dependencias:** Maven.
* **Librerías Adicionales:** * `OpenPDF`: Para la emisión de tickets de venta y generación de reportes en PDF.
  * `Apache POI`: Para la exportación de historiales y cuadres de caja a formatos de Microsoft Excel (.xlsx).
  * `Gson (Google)`: Para parsear e integrar la respuesta JSON de la API pública de SUNAT (búsqueda automática de DNI).
  * `ZXing (Zebra Crossing)`: Para la generación dinámica de códigos QR bidimensionales en la pasarela de cobro (Yape / Plin).
  * `SLF4J & Logback`: Para el monitoreo y registro (logging) de errores transaccionales en un archivo local.
  * `Ikonli (FontAwesome)`: Para la iconografía vectorial y moderna de la interfaz gráfica.
  
##Estructura de la Base de Datos
El sistema se apoya en una base de datos relacional compuesta por 11 tablas principales que gestionan la trazabilidad completa, desde el ingreso del producto por el proveedor hasta la emisión del comprobante de pago al cliente.

##Instalación y Configuración

1. **Clonar el repositorio:**
   ```bash
   git clone [https://github.com/alvaro2016az-ai/AltoquePagoProyecto.git](https://github.com/tu-usuario/AltoquePagoProyecto.git)
