package com.controlador;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ControladorPrincipal {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }
}