package com.controlador;

import com.modelo.Producto;
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
    @FXML private Button btnCancelarCompra; // Lo mantenemos mapeado para que no de error el FXML

    private int pasoActual = 1;
    private double totalAcumulado = 0.0;
    private int cantidadProductos = 0;

    private String dniCliente = "00000000";
    private String nombreCliente = "CLIENTE VARIOS";

    @FXML
    public void initialize() {
        btnProcederPago.setDisable(true);
        lblTotalKiosko.setText("S/ 0.00");

        // ELIMINACIÓN DE SEGURIDAD: Desaparecer por completo el botón de cancelar del diseño
        if (btnCancelarCompra != null) {
            btnCancelarCompra.setVisible(false);
            btnCancelarCompra.setManaged(false);
        }

        cargarPaso(1);
    }

    public void agregarProductoAlTotal(double precio) {
        this.totalAcumulado += precio;
        this.cantidadProductos++;
        lblTotalKiosko.setText(String.format("S/ %.2f", this.totalAcumulado));
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

                // Reiniciar para la siguiente venta
                totalAcumulado = 0.0;
                cantidadProductos = 0;
                dniCliente = "";
                nombreCliente = "CLIENTE VARIOS";
                lblTotalKiosko.setText("S/ 0.00");
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
        // Dejamos el método vacío por si el FXML aún lo referencia, evitando crasheos
        System.out.println("-> Botón Cancelar deshabilitado por seguridad.");
    }

    // Agrega este método dentro de ControladorAutoservicioCheckoutDividida.java
    public void setDatosCliente(String dni, String nombre) {
        this.dniCliente = dni;
        this.nombreCliente = nombre;
    }
}