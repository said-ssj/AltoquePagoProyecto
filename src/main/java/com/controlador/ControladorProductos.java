package com.controlador;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;

import java.net.URL;
import java.util.ResourceBundle;

public class ControladorProductos implements Initializable {

    @FXML
    private ComboBox<String> cbFiltrosProductos;

    @FXML
    public void abrirNuevoProducto(javafx.event.ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("nuevoproducto-view.fxml"));
            javafx.scene.Parent vistaNuevoProducto = loader.load();

            javafx.scene.Node boton = (javafx.scene.Node) event.getSource();
            javafx.scene.layout.BorderPane panelPrincipal = (javafx.scene.layout.BorderPane) boton.getScene().getRoot();

            panelPrincipal.setCenter(vistaNuevoProducto);
        } catch (java.io.IOException e) {
            System.err.println("Error al cargar la vista de nuevo producto");
            e.printStackTrace();
        }
    }

    public void initialize(URL url, ResourceBundle resourceBundle) {

        // CONFIGURACION DEL COMBOBOX
        cbFiltrosProductos.getItems().addAll("A - Z ⬆", "Z - A ⬇", "Categoria -  ", "Categoria - ","Categoria - ","IDs");
        cbFiltrosProductos.setOnAction(e -> {
            String seleccion = cbFiltrosProductos.getValue();
            System.out.println("Filtrando por: " + seleccion);
        });

    }

}
