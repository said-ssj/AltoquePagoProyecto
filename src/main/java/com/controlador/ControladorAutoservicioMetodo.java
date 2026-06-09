package com.controlador;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

public class ControladorAutoservicioMetodo {

    @FXML private Button btnFinalizarExito;

    @FXML
    public void initialize() {

    }

    @FXML
    public void finalizarCompra(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Pago Exitoso");
        alert.setHeaderText("¡Gracias por su compra en ALToque Pago!");
        alert.setContentText("Su comprobante ha sido emitido. Por favor, retire sus productos.");
        alert.showAndWait();

        System.out.println("Compra finalizada. Reiniciando kiosko...");

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("AutoservicioEscaner.fxml"));
            Parent vistaEscaner = loader.load();

            Node botonPresionado = (Node) event.getSource();
            BorderPane panelKioskoPrincipal = (BorderPane) botonPresionado.getScene().getRoot();

            panelKioskoPrincipal.setCenter(vistaEscaner);

            Button btnPagar = (Button) panelKioskoPrincipal.lookup("#btnProcederPago");
            if(btnPagar != null) btnPagar.setVisible(true);

            Label lblTotal = (Label) panelKioskoPrincipal.lookup("#lblTotalKiosko");
            if(lblTotal != null) lblTotal.setText("S/ 0.00");

        } catch (IOException e) {
            System.err.println("Error al reiniciar el Kiosko.");
            e.printStackTrace();
        }
    }
}