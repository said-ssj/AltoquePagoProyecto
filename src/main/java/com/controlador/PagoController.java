package com.controlador;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
public class PagoController {
    @FXML
    private Label lblTotal;

    @FXML
    private void continuarPago(){

        System.out.println(
                "Ir a pantalla pago"
        );

    }

}

