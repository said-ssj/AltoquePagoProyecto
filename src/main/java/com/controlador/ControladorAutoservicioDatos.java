package com.controlador;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

public class ControladorAutoservicioDatos {

    @FXML private ToggleButton btnBoleta;
    @FXML private ToggleButton btnFactura;

    @FXML private TextField txtDocumento;
    @FXML private TextField txtNombreCliente;
    @FXML private TextField txtCorreoCliente;
    @FXML private TextField txtDireccionCliente;

    @FXML private Button btnBuscarDoc;
    @FXML private Button btnContinuar;

    @FXML
    public void initialize() {
        // Lógica inicial para cambiar los textos de ayuda según se elija Boleta o Factura
        btnFactura.setOnAction(e -> {
            txtDocumento.setPromptText("Número de RUC (11 dígitos)");
            txtNombreCliente.setPromptText("Razón Social");
        });

        btnBoleta.setOnAction(e -> {
            txtDocumento.setPromptText("Número de DNI (8 dígitos)");
            txtNombreCliente.setPromptText("Nombre completo");
        });
    }

    @FXML
    public void continuarAPago(ActionEvent event) {
        // 1. Validar que los campos obligatorios no estén vacíos
        if(txtDocumento.getText().trim().isEmpty() || txtNombreCliente.getText().trim().isEmpty()) {
            System.out.println("Alerta: Debe completar los campos obligatorios.");
            // Opcional: Aquí podrías poner el txtDocumento.requestFocus() o cambiarle el borde a rojo
            return;
        }

        System.out.println("Datos confirmados. Pasando a selección de Método de Pago...");

        // 2. Hacer el salto de pantalla en el centro del Kiosko
        try {
            // Cargamos la siguiente pantalla (asegúrate de que este archivo exista)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("AutoservicioMetodo.fxml"));
            Parent vistaMetodoPago = loader.load();

            // Atrapamos la ventana principal desde el botón que acabamos de hacer clic
            Node botonPresionado = (Node) event.getSource();
            BorderPane panelKioskoPrincipal = (BorderPane) botonPresionado.getScene().getRoot();

            // Inyectamos la nueva vista en el centro, reemplazando el formulario de datos
            panelKioskoPrincipal.setCenter(vistaMetodoPago);

        } catch (IOException e) {
            System.err.println("Error al cargar la pantalla de Método de Pago.");
            e.printStackTrace();
        }
    }
}