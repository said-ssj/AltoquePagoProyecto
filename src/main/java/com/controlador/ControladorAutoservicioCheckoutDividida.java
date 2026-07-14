/*
 * Controlamos el flujo del proceso de pago dividido en el kiosko de autoservicio.
 * Hemos aplicado el Principio de Inversión de Dependencias (DIP) inyectando las abstracciones
 * ICarritoDAO e IClienteDAO a través del constructor, lo que nos permite eliminar por completo
 * el acoplamiento rígido de instanciaciones directas dentro de la lógica de negocio del controlador.
 */
package com.controlador;

import com.dao.ClienteDAO;
import com.dao.IClienteDAO;
import com.dao.CarritoDAO;
import com.dao.ICarritoDAO;
import com.modelo.Cliente;
import com.modelo.Producto;
import com.modelo.Venta;
import com.servicio.ComprobantePdfGenerador;
import com.servicio.IComprobanteGenerador;
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
    @FXML private Label lblContadorProductos;

    private int pasoActual = 1;
    private double totalAcumulado = 0.0;
    private int cantidadProductos = 0;
    private String dniCliente = "00000000";
    private String nombreCliente = "CLIENTE VARIOS";
    private String correoCliente = "";
    private String direccionCliente = "";

    private final ICarritoDAO carritoDAO;
    private final IClienteDAO clienteDAO;

    public ControladorAutoservicioCheckoutDividida() {
        this.carritoDAO = new CarritoDAO();
        this.clienteDAO = new ClienteDAO();
    }

    @FXML
    public void initialize() {
        btnProcederPago.setDisable(true);
        lblTotalKiosko.setText("S/ 0.00");
        if (lblContadorProductos != null) {
            lblContadorProductos.setText("0 productos");
        }
        if (btnCancelarCompra != null) {
            btnCancelarCompra.setVisible(false);
            btnCancelarCompra.setManaged(false);
        }
        cargarPaso(1);
    }

    public void agregarProductoAlTotal(double precio) {
        this.totalAcumulado += precio;
        lblTotalKiosko.setText(String.format("S/ %.2f", this.totalAcumulado));
        if (this.cantidadProductos > 0) {
            btnProcederPago.setDisable(false);
        }
    }

    public void actualizarUnidadesContador(int cantidad) {
        this.cantidadProductos += cantidad;
        if (lblContadorProductos != null) {
            if (this.cantidadProductos == 1) {
                lblContadorProductos.setText("1 producto");
            } else {
                lblContadorProductos.setText(this.cantidadProductos + " productos");
            }
        }
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
            if (!correoCliente.trim().isEmpty() && !com.servicio.ValidacionFormatos.validarCorreo(correoCliente)) {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
                alert.setTitle("Correo Inválido");
                alert.setHeaderText(null);
                alert.setContentText("El correo debe tener un formato válido (debe contener \"@\"). Ej: cliente@correo.com");
                alert.showAndWait();
                return;
            }
            cargarPaso(3);
        } else if (pasoActual == 3) {
            int idCarritoActivo = carritoDAO.obtenerOCrearCarritoActivo(1);
            if (dniCliente != null && !dniCliente.trim().isEmpty() && !dniCliente.equals("00000000")) {
                boolean esRuc = dniCliente.length() == 11;
                Cliente cliente = new Cliente(
                        nombreCliente, "", nombreCliente, correoCliente,
                        esRuc ? null : dniCliente,
                        esRuc ? dniCliente : null,
                        "", direccionCliente, "",
                        esRuc ? "6" : "1", ""
                );
                clienteDAO.guardarOActualizarCliente(cliente);
            }
            Venta ventaGenerada = new Venta();
            ventaGenerada.setId(idCarritoActivo);
            ventaGenerada.setTotal(this.totalAcumulado);
            ventaGenerada.setProductos(this.cantidadProductos);
            ventaGenerada.setCliente(this.nombreCliente);
            ventaGenerada.setMetodoPago("Tarjeta / Billetera Digital");
            ventaGenerada.setCanalVenta("AUTOSERVICIO");
            String tipoDocumento = (this.dniCliente != null && this.dniCliente.length() == 11) ? "FACTURA" : "BOLETA";
            ventaGenerada.setEstado(tipoDocumento);
            java.time.LocalDateTime ahora = java.time.LocalDateTime.now();
            java.time.format.DateTimeFormatter formato = java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
            ventaGenerada.setFecha(ahora.format(formato));
            java.util.List<com.modelo.DetalleVenta> listaItems = carritoDAO.obtenerDetallesDelCarrito(idCarritoActivo);
            ventaGenerada.setDetalles(listaItems);
            IComprobanteGenerador comprobanteServicio = new ComprobantePdfGenerador();
            comprobanteServicio.emitirTicketKiosko(ventaGenerada);
            carritoDAO.marcarCarritoComoPagado(idCarritoActivo);
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
        System.out.println("-> Bot n Cancelar deshabilitado por seguridad.");
    }

    public void setDatosCliente(String dni, String nombre) {
        this.dniCliente = dni;
        this.nombreCliente = nombre;
    }

    public void setDatosCliente(String dni, String nombre, String correo, String direccion) {
        this.dniCliente = dni;
        this.nombreCliente = nombre;
        this.correoCliente = correo != null ? correo : "";
        this.direccionCliente = direccion != null ? direccion : "";
    }
}