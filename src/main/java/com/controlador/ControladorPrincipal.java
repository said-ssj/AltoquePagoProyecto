package com.controlador;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ControladorPrincipal implements Initializable {    // Captura el BorderPane principal que definiste en menu.fxml

    @FXML private BorderPane panelPrincipal;
    @FXML private ToggleButton btnInicio;
    // Metodo para Inicializar
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Enciende el botón visualmente de color azul
        btnInicio.setSelected(true);
        // Carga la pantalla en el centro
        abrirInicio();
    }

    //  Métodos conectados a los botones

    @FXML
    public void abrirInicio() { cargarVista("inicio-view.fxml"); }

    @FXML
    public void abrirVentas() { cargarVista("ventas-view.fxml"); }

    @FXML
    public void abrirProductos() {
        cargarVista("productos-view.fxml");
    }

    @FXML
    public void abrirEmpleados() {
        cargarVista("empleados-view.fxml");
    }

    @FXML
    public void abrirConsultas() {
        cargarVista("consultas-view.fxml");
    }

    @FXML
    public void abrirReportes() {
        cargarVista("reportes-view.fxml");
    }

    //  Lógica central para intercambiar las vistas
    private void cargarVista(String nombreFxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(nombreFxml));
            VBox vista = loader.load();
            // Coloca la vista seleccionada en el centro del BorderPane
            panelPrincipal.setCenter(vista);
        } catch (IOException e) {
            System.err.println("Error al cargar la vista: " + nombreFxml);
            e.printStackTrace();
        }
    }
}
