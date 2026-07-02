package com.controlador;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // CORRECCIÓN: Iniciar siempre desde la vista de Login
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vista/Login-view.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("ALToque Pago - Iniciar Sesión");
        stage.show();
        stage.centerOnScreen();
    }

    public static void main(String[] args) {
        launch();
    }
}