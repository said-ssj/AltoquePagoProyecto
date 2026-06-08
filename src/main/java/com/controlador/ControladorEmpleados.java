package com.controlador;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class ControladorEmpleados implements Initializable {

    @FXML private Button btnNuevoEmpleado;
    @FXML private TextField txtBuscarEmpleado;
    @FXML private Button btnFiltros;

    // Declaración del ComboBox
    @FXML
    private ComboBox<String> cbFiltrosEmpleados;

    @FXML
    public void abrirNuevoEmpleado(javafx.event.ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("nuevoempleado-view.fxml"));
            javafx.scene.Parent vistaNuevoEmpleado = loader.load();

            javafx.scene.Node boton = (javafx.scene.Node) event.getSource();
            javafx.scene.layout.BorderPane panelPrincipal = (javafx.scene.layout.BorderPane) boton.getScene().getRoot();

            panelPrincipal.setCenter(vistaNuevoEmpleado);
        } catch (java.io.IOException e) {
            System.err.println("Error al cargar la vista de nuevo empleado");
            e.printStackTrace();
        }
    }

    public void initialize(URL url, ResourceBundle resourceBundle) {

        // CONFIGURACION DEL COMBOBOX
        cbFiltrosEmpleados.getItems().addAll("A - Z ⬆", "Z - A ⬇", "Administrador - Rol", "Empleado - Rol");
        cbFiltrosEmpleados.setOnAction(e -> {
            String seleccion = cbFiltrosEmpleados.getValue();
            System.out.println("Filtrando por: " + seleccion);
        });

    }
}
