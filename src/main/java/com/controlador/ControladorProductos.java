package com.controlador;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;

import java.net.URL;
import java.util.ResourceBundle;

public class ControladorProductos implements Initializable {

    @FXML
    private ComboBox<String> cbFiltrosProductos;

    public void initialize(URL url, ResourceBundle resourceBundle) {

        // CONFIGURACION DEL COMBOBOX
        cbFiltrosProductos.getItems().addAll("A - Z ⬆", "Z - A ⬇", "Categoria -  ", "Categoria - ","Categoria - ","IDs");
        cbFiltrosProductos.setOnAction(e -> {
            String seleccion = cbFiltrosProductos.getValue();
            System.out.println("Filtrando por: " + seleccion);
        });

    }

}
