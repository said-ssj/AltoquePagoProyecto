package com.controlador;

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
    @FXML private Button btnCancelarCompra;
    @FXML private Button btnProcederPago;
    @FXML private Button btnVolver;

    // Rastrear en qué pantalla está el cliente
    private int pasoActual = 1;

    @FXML
    public void initialize() {
        // Arrancamos siempre en el Escáner (Paso 1)
        cargarPaso(1);
    }

    private void cargarPaso(int paso) {
        this.pasoActual = paso;
        String fxml = "";

        // LA MÁQUINA DE ESTADOS: Configuramos visualmente según la pantalla
        switch (paso) {
            case 1: // ESCÁNER
                fxml = "AutoservicioEscaner-view.fxml";
                btnProcederPago.setText("Proceder al Pago");
                btnProcederPago.setStyle("-fx-background-color: #16a34a; -fx-text-fill: #ffffff; -fx-font-size: 22px; -fx-font-weight: bold; -fx-background-radius: 12px; -fx-cursor: hand;");
                btnVolver.setVisible(false); // Ocultar atrás
                btnCancelarCompra.setVisible(true);
                break;
            case 2: // DATOS CLIENTE
                fxml = "AutoservicioDatos-view.fxml";
                btnProcederPago.setText("Continuar");
                btnProcederPago.setStyle("-fx-background-color: #16a34a; -fx-text-fill: #ffffff; -fx-font-size: 22px; -fx-font-weight: bold; -fx-background-radius: 12px; -fx-cursor: hand;");
                btnVolver.setVisible(true); // Mostrar atrás
                btnCancelarCompra.setVisible(true);
                break;
            case 3: // MÉTODO PAGO
                fxml = "AutoservicioMetodo-view.fxml";
                btnProcederPago.setText("Finalizar Pago");
                btnProcederPago.setStyle("-fx-background-color: #16a34a; -fx-text-fill: #ffffff; -fx-font-size: 22px; -fx-font-weight: bold; -fx-background-radius: 12px; -fx-cursor: hand;");
                btnVolver.setVisible(true);
                btnCancelarCompra.setVisible(true);
                break;
            case 4: // PAGO EXITOSO
                fxml = "AutoservicioPagoExitoso-view.fxml";
                btnProcederPago.setText("Nueva Compra");
                btnProcederPago.setStyle("-fx-background-color: #1d4ed8; -fx-text-fill: #ffffff; -fx-font-size: 22px; -fx-font-weight: bold; -fx-background-radius: 12px; -fx-cursor: hand;");
                btnVolver.setVisible(false); // Ocultar atrás
                btnCancelarCompra.setVisible(false); // Ocultar cancelar (ya pagó)
                lblTotalKiosko.setText("S/ 0.00"); // Resetear el monto
                break;
        }

        // Inyectamos la vista al centro
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vista/" + fxml));
            Parent vistaCentro = loader.load();
            panelKioskoPrincipal.setCenter(vistaCentro);
        } catch (IOException e) {
            System.err.println("Error al cargar la vista del Kiosko: " + fxml);
            e.printStackTrace();
        }
    }

    @FXML
    public void avanzarPaso(ActionEvent event) {
        if (pasoActual == 1) {
            cargarPaso(2);
        } else if (pasoActual == 2) {
            cargarPaso(3);
        } else if (pasoActual == 3) {
            cargarPaso(4);
        } else if (pasoActual == 4) {
            cargarPaso(1);
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
        System.out.println("Compra cancelada. Limpiando datos...");
        lblTotalKiosko.setText("S/ 0.00");
        cargarPaso(1);
    }
}