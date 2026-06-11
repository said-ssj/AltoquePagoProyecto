package com.controlador;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // La ruta ahora empieza con /com/vista/ y tiene el sufijo -view
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("/com/vista/Login-view.fxml"));

        Scene scene = new Scene(fxmlLoader.load(), 900, 600); // Tamaño del login
        stage.setTitle("ALToque Pago - Sistema de Autoservicio");
        stage.setScene(scene);
        stage.setMaximized(false);
        stage.show();
        stage.centerOnScreen();
    }

    public static void main(String[] args) {
        launch();
    }

}