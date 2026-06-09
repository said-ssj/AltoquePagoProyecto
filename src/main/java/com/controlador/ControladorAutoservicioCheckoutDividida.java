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

    @FXML
    public void initialize() {
        // Apenas cargue, inyectamos el escáner en el centro
        cargarVistaCentral("AutoservicioEscaner.fxml");
    }

    private void cargarVistaCentral(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent vistaCentro = loader.load();
            panelKioskoPrincipal.setCenter(vistaCentro);
        } catch (IOException e) {
            System.err.println("Error al cargar la vista central del kiosko: " + fxml);
            e.printStackTrace();
        }
    }

    @FXML
    public void procederAlPago(ActionEvent event) {
        System.out.println("Cambiando a pantalla de Datos del Cliente...");
        // 1. Cargamos el formulario de datos
        cargarVistaCentral("AutoservicioDatos.fxml");
        // 2. Ocultamos el botón verde de abajo para que usen el botón de la tarjeta
        btnProcederPago.setVisible(false);
    }

    @FXML
    public void cancelarCompra(ActionEvent event) {
        System.out.println("Compra cancelada. Limpiando carrito y regresando al inicio...");
        // 1. Volvemos al escáner
        cargarVistaCentral("AutoservicioEscaner.fxml");
        // 2. Restauramos el botón verde y el total
        btnProcederPago.setVisible(true);
        lblTotalKiosko.setText("S/ 0.00");
    }
}