/*
 * En esta clase definimos el punto de entrada nativo de nuestra aplicación JavaFX (Versión V2).
 * Nos aseguramos de que el sistema arranque siempre desde la pantalla de Login,
 * centralizando la configuración inicial de la ventana (Stage) para mantener el
 * flujo de inicio seguro y ordenado. Automatiza las migraciones de la base de datos
 * con Flyway leyendo las credenciales de configuracion.properties antes de iniciar la interfaz.
 */
package com.controlador;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vista/Login-view.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("ALToque Pago - Iniciar Sesión");
        stage.centerOnScreen();
        stage.show();
    }

    public static void main(String[] args) {
        // Delegamos el inicio de la interfaz gráfica a JavaFX
        launch(args);
    }
}