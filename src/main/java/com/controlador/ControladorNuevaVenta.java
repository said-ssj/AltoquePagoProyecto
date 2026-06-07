package com.controlador;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ControladorNuevaVenta {

    @FXML private Button btnMostrarDatosCliente;
    @FXML private Button btnCerrarDatosCliente;
    @FXML private VBox panelDatosCliente;

    // Nuevos elementos inyectados del FXML
    @FXML private TextField txtFechaVenta;
    @FXML private ToggleButton btnModoBusqueda;
    @FXML private TextField txtBuscarProducto;

    @FXML
    public void initialize() {

        // 1. Ocultar panel de cliente al inicio (Asegurarnos de que arranque cerrado)
        panelDatosCliente.setVisible(false);
        panelDatosCliente.setManaged(false);

        // 2. Lógica del botón Desplegable del Cliente
        btnMostrarDatosCliente.setOnAction(e -> {
            panelDatosCliente.setVisible(true);
            panelDatosCliente.setManaged(true);
            btnMostrarDatosCliente.setVisible(false);
            btnMostrarDatosCliente.setManaged(false);
        });

        btnCerrarDatosCliente.setOnAction(e -> {
            panelDatosCliente.setVisible(false);
            panelDatosCliente.setManaged(false);
            btnMostrarDatosCliente.setVisible(true);
            btnMostrarDatosCliente.setManaged(true);
        });

        // 3. Poner la fecha actual automáticamente
        txtFechaVenta.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        // 4. Lógica interactiva del Buscador vs Código de Barras
        configurarModoCodigoBarras(); // Arranca por defecto en modo código de barras

        btnModoBusqueda.setOnAction(e -> {
            if (btnModoBusqueda.isSelected()) {
                // Cambió a Modo Búsqueda por Texto
                configurarModoBusqueda();
            } else {
                // Regresó a Modo Código de Barras
                configurarModoCodigoBarras();
            }
        });

        // 5. Restricción para que solo acepte números cuando está en modo escáner
        txtBuscarProducto.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!btnModoBusqueda.isSelected()) { // Si NO está presionado (Modo Escáner)
                if (!newValue.matches("\\d*")) { // Si detecta una letra o símbolo
                    txtBuscarProducto.setText(newValue.replaceAll("[^\\d]", "")); // Lo borra automáticamente
                }
            }
        });
    }

    // Funciones de ayuda para cambiar el aspecto visual según el modo
    private void configurarModoCodigoBarras() {
        btnModoBusqueda.setText("🔍 Modo Búsqueda");
        txtBuscarProducto.setPromptText("||||| Escanear código de barras...");

        Platform.runLater(() -> txtBuscarProducto.requestFocus());
    }

    private void configurarModoBusqueda() {
        btnModoBusqueda.setText("||||| Modo Código de Barras");
        txtBuscarProducto.setPromptText("🔍 Escribe el nombre del producto...");
    }

    @FXML
    public void abrirVentas(javafx.event.ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ventas-view.fxml"));
            javafx.scene.Parent vistaVentas = loader.load();

            javafx.scene.Node boton = (javafx.scene.Node) event.getSource();
            javafx.scene.layout.BorderPane panelPrincipal = (javafx.scene.layout.BorderPane) boton.getScene().getRoot();

            panelPrincipal.setCenter(vistaVentas);

        } catch (IOException e) {
            System.err.println("Error al volver a la vista de ventas");
            e.printStackTrace();
        }
    }
}