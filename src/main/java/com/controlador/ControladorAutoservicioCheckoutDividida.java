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

    private void cargarVistaCentral(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vista/" + fxml));
            Parent vistaCentro = loader.load();
            panelKioskoPrincipal.setCenter(vistaCentro);
        } catch (IOException e) {
            System.err.println("Error al cargar la vista central del kiosko: " + fxml);
            e.printStackTrace();
        }
    }

    @FXML
    public void initialize() {
        cargarVistaCentral("AutoservicioEscaner-view.fxml");
    }
    @FXML
    public void procederAlPago(ActionEvent event) {
        cargarVistaCentral("AutoservicioDatos-view.fxml");
        btnProcederPago.setVisible(false);
    }

    @FXML
    public void cancelarCompra(ActionEvent event) {
        System.out.println("Compra cancelada. Limpiando carrito y regresando al inicio...");
        // Volvemos al escáner
        cargarVistaCentral("AutoservicioEscaner-view.fxml");
        // Restauramos el botón verde y el total
        btnProcederPago.setVisible(true);
        lblTotalKiosko.setText("S/ 0.00");
    }
}