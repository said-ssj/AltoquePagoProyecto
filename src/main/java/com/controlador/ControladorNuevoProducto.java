package com.controlador;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;

import java.io.IOException;

public class ControladorNuevoProducto {

    @FXML private Button btnVolver;
    @FXML private TextField txtCodigoBarras;
    @FXML private TextField txtNombre;
    @FXML private ComboBox<String> cbCategoria;
    @FXML private TextField txtMarca;
    @FXML private TextField txtPrecioCompra;
    @FXML private TextField txtPrecioVenta;
    @FXML private TextField txtStockInicial;
    @FXML private TextField txtStockMinimo;
    @FXML private ComboBox<String> cbProveedor;

    // El nuevo botón interruptor
    @FXML private ToggleButton btnModoBusqueda;

    @FXML private ComboBox<String> cbUnidadMedida;
    @FXML private ComboBox<String> cbEstado;
    @FXML private javafx.scene.control.TextArea txtDescripcion;

    @FXML
    public void initialize() {

        // 1. Arranca por defecto en modo código de barras
        configurarModoCodigoBarras();

        // 2. Evento al hacer clic en el interruptor
        btnModoBusqueda.setOnAction(e -> {
            if (btnModoBusqueda.isSelected()) {
                configurarModoBusqueda(); // Búsqueda manual
            } else {
                configurarModoCodigoBarras(); // Lector láser
            }
        });

        // 3. Restricción: Si está en modo escáner, SOLO acepta números
        txtCodigoBarras.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!btnModoBusqueda.isSelected()) {
                if (!newValue.matches("\\d*")) {
                    txtCodigoBarras.setText(newValue.replaceAll("[^\\d]", ""));
                }
            }
        });
    }

    // --- Métodos de ayuda visuales ---
    private void configurarModoCodigoBarras() {
        btnModoBusqueda.setText("🔍 Modo Búsqueda");
        txtCodigoBarras.setPromptText("||||| Escanear código de barras...");

        // Coloca el cursor automáticamente para estar listo para disparar el lector
        Platform.runLater(() -> txtCodigoBarras.requestFocus());
    }

    private void configurarModoBusqueda() {
        btnModoBusqueda.setText("||||| Modo Código de Barras");
        txtCodigoBarras.setPromptText("🔍 Escribe el nombre o código...");
    }

    @FXML
    public void abrirProductos(javafx.event.ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("productos-view.fxml"));
            javafx.scene.Parent vistaProductos = loader.load();

            javafx.scene.Node boton = (javafx.scene.Node) event.getSource();
            javafx.scene.layout.BorderPane panelPrincipal = (javafx.scene.layout.BorderPane) boton.getScene().getRoot();

            panelPrincipal.setCenter(vistaProductos);
        } catch (IOException e) {
            System.err.println("Error al volver a la vista de productos");
            e.printStackTrace();
        }
    }
}