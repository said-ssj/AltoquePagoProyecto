package com.controlador;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("login-view.fxml"));

        // Tamaño pequeño definido para el Login
        Scene scene = new Scene(fxmlLoader.load(), 900, 600);

        stage.setTitle("ALToque Pago");
        stage.setScene(scene);

        // Aseguramos que NO inicie maximizado y bloqueamos el redimensionamiento
        stage.setMaximized(false);
        stage.setResizable(false);

        stage.show();

        // Centramos la ventana exactamente en el medio del monitor
        stage.centerOnScreen();
    }

    public static void main(String[] args) {
        launch();
    }

}