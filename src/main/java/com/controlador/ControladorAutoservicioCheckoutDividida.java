package com.controlador;

import com.dao.ClienteDAO;
import com.modelo.Cliente;
import com.modelo.Producto;
import com.modelo.Venta;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import java.io.IOException;

public class ControladorAutoservicioCheckoutDividida {

    @FXML private BorderPane panelKioskoPrincipal;
    @FXML private Label lblTotalKiosko;
    @FXML private Button btnProcederPago;
    @FXML private Button btnVolver;
    @FXML private Button btnCancelarCompra;

    // 1. Enlazamos el Label del FXML
    @FXML private Label lblContadorProductos;

    private int pasoActual = 1;
    private double totalAcumulado = 0.0;
    private int cantidadProductos = 0;

    private String dniCliente = "00000000";
    private String nombreCliente = "CLIENTE VARIOS";
    private String correoCliente = "";
    private String direccionCliente = "";

    @FXML
    public void initialize() {
        btnProcederPago.setDisable(true);
        lblTotalKiosko.setText("S/ 0.00");

        // Reiniciar el texto del contador visual
        if (lblContadorProductos != null) {
            lblContadorProductos.setText("0 productos");
        }

        // ELIMINACIÓN DE SEGURIDAD: Desaparecer botón cancelar
        if (btnCancelarCompra != null) {
            btnCancelarCompra.setVisible(false);
            btnCancelarCompra.setManaged(false);
        }

        cargarPaso(1);
    }

    // 2. Ahora solo maneja la suma del dinero
    public void agregarProductoAlTotal(double precio) {
        this.totalAcumulado += precio;
        lblTotalKiosko.setText(String.format("S/ %.2f", this.totalAcumulado));

        if (this.cantidadProductos > 0) {
            btnProcederPago.setDisable(false);
        }
    }

    // 3. Maneja el conteo de unidades y actualiza la pantalla
    public void actualizarUnidadesContador(int cantidad) {
        this.cantidadProductos += cantidad;

        if (lblContadorProductos != null) {
            if (this.cantidadProductos == 1) {
                lblContadorProductos.setText("1 producto");
            } else {
                lblContadorProductos.setText(this.cantidadProductos + " productos");
            }
        }

        // Habilitar el botón de pago si ya hay productos
        if (this.cantidadProductos > 0) {
            btnProcederPago.setDisable(false);
        }
    }

    public void cargarPaso(int paso) {
        this.pasoActual = paso;
        String fxml = "";

        switch (paso) {
            case 1:
                fxml = "AutoservicioEscaner-view.fxml";
                btnProcederPago.setText("Proceder al Pago");
                btnProcederPago.setVisible(true);
                btnProcederPago.setManaged(true);
                btnVolver.setVisible(false);
                btnProcederPago.setDisable(cantidadProductos == 0);
                break;

            case 2:
                fxml = "AutoservicioDatos-view.fxml";
                actualizarBotonPaso2();
                btnVolver.setVisible(true);
                break;

            case 3:
                fxml = "AutoservicioMetodo-view.fxml";
                btnProcederPago.setText("Finalizar Compra");
                btnProcederPago.setVisible(true);
                btnProcederPago.setManaged(true);
                btnVolver.setVisible(true);
                btnProcederPago.setDisable(false);
                break;

            case 4:
                fxml = "AutoservicioPagoExitoso-view.fxml";
                btnProcederPago.setVisible(false);
                btnProcederPago.setManaged(false);
                btnVolver.setVisible(false);

                // Reiniciar todo para el siguiente cliente
                totalAcumulado = 0.0;
                cantidadProductos = 0;
                dniCliente = "";
                nombreCliente = "CLIENTE VARIOS";
                correoCliente = "";
                direccionCliente = "";
                lblTotalKiosko.setText("S/ 0.00");
                if (lblContadorProductos != null) {
                    lblContadorProductos.setText("0 productos");
                }
                break;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vista/" + fxml));
            Parent vistaCentro = loader.load();

            Object controladorHijo = loader.getController();
            if (controladorHijo instanceof ControladorAutoservicioEscaner) {
                ((ControladorAutoservicioEscaner) controladorHijo).setContenedorPadre(this);
            } else if (controladorHijo instanceof ControladorAutoservicioDatos) {
                ((ControladorAutoservicioDatos) controladorHijo).setContenedorPadre(this);
            }

            panelKioskoPrincipal.setCenter(vistaCentro);
        } catch (IOException e) {
            System.err.println("Error al cargar la vista del Kiosko: " + fxml);
            e.printStackTrace();
        }
    }

    public void modificarBotonDatos(boolean tieneDni) {
        if (!tieneDni) {
            btnProcederPago.setText("Continuar con Boleta Simple");
            btnProcederPago.setPrefWidth(260);
        } else {
            btnProcederPago.setText("Continuar");
            btnProcederPago.setPrefWidth(180);
        }
        btnProcederPago.setDisable(false);
    }

    private void actualizarBotonPaso2() {
        modificarBotonDatos(!dniCliente.trim().isEmpty() && !dniCliente.equals("00000000"));
    }

    @FXML
    public void avanzarPaso(ActionEvent event) {
        if (pasoActual == 1) {
            cargarPaso(2);
        } else if (pasoActual == 2) {
            cargarPaso(3);
        } else if (pasoActual == 3) {

            // 1. Recuperar el ID real del carrito activo desde la Base de Datos
            com.dao.CarritoDAO cDao = new com.dao.CarritoDAO();
            int idCarritoActivo = cDao.obtenerOCrearCarritoActivo(1); // Cliente Kiosko ID: 1

            // NEGOCIO [CLIENTES]: si el cliente se identificó con un documento real
            // (no el walk-in "00000000"), se guarda o actualiza en la tabla `cliente`
            // (nombre, correo, dirección). Así, la próxima vez que ingrese el mismo
            // DNI/RUC —aquí o en Ventas normales— sus datos aparecen automáticamente.
            if (dniCliente != null && !dniCliente.trim().isEmpty() && !dniCliente.equals("00000000")) {
                boolean esRuc = dniCliente.length() == 11;
                Cliente cliente = new Cliente(
                        nombreCliente, "", nombreCliente, correoCliente,
                        esRuc ? null : dniCliente,
                        esRuc ? dniCliente : null,
                        "", direccionCliente, "",
                        esRuc ? "6" : "1", ""
                );
                new ClienteDAO().guardarOActualizarCliente(cliente);
            }

            // 2. Instanciamos la Venta usando el ID del carrito como correlativo oficial
            Venta ventaGenerada = new Venta();
            ventaGenerada.setId(idCarritoActivo);
            ventaGenerada.setTotal(this.totalAcumulado);
            ventaGenerada.setProductos(this.cantidadProductos);
            ventaGenerada.setCliente(this.nombreCliente);
            ventaGenerada.setMetodoPago("Tarjeta / Billetera Digital");

            // Esta venta se generó sola, sin cajero: la marcamos como AUTOSERVICIO
            // para que el comprobante muestre "AUTOSERVICIO (Kiosko)" en vez de un nombre de usuario.
            ventaGenerada.setCanalVenta("AUTOSERVICIO");

            // Evaluamos el tipo de documento real según el documento ingresado
            String tipoDocumento = (this.dniCliente != null && this.dniCliente.length() == 11) ? "FACTURA" : "BOLETA";
            ventaGenerada.setEstado(tipoDocumento);

            // Formateamos la fecha actual
            java.time.LocalDateTime ahora = java.time.LocalDateTime.now();
            java.time.format.DateTimeFormatter formato = java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
            ventaGenerada.setFecha(ahora.format(formato));

            // 3. Recuperar la lista real de artículos comprados de la base de datos
            java.util.List<com.modelo.DetalleVenta> listaItems = cDao.obtenerDetallesDelCarrito(idCarritoActivo);
            ventaGenerada.setDetalles(listaItems);

            // 4. Emitir el ticket físico e impreso con el número correlativo real
            com.servicio.ComprobanteImpresionServicio.emitirEImprimirTicketKiosko(ventaGenerada);

            // 5. Cerramos el carrito actual (pasa a PAGADO) para limpiar el Kiosko para el siguiente cliente
            cDao.marcarCarritoComoPagado(idCarritoActivo);

            // 6. Pasar a la pantalla final de éxito
            cargarPaso(4);
        }
    }

    @FXML
    public void volverPasoAnterior(ActionEvent event) {
        if (pasoActual > 1 && pasoActual < 4) {
            cargarPaso(pasoActual - 1);
        }
    }

    @FXML
    public void cancelarCompra(ActionEvent event) {
        System.out.println("-> Botón Cancelar deshabilitado por seguridad.");
    }

    public void setDatosCliente(String dni, String nombre) {
        this.dniCliente = dni;
        this.nombreCliente = nombre;
    }

    /**
     * NEGOCIO [CLIENTES]: variante que además guarda el correo y la dirección
     * capturados en el paso 2 (AutoservicioDatos), para poder registrarlos en
     * la BD al finalizar la compra.
     */
    public void setDatosCliente(String dni, String nombre, String correo, String direccion) {
        this.dniCliente = dni;
        this.nombreCliente = nombre;
        this.correoCliente = correo != null ? correo : "";
        this.direccionCliente = direccion != null ? direccion : "";
    }
}