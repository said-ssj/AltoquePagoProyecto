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
import org.flywaydb.core.Flyway;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

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
        try {
            Properties prop = new Properties();
            prop.load(new FileInputStream("configuracion.properties"));

            String url = prop.getProperty("db.url");
            String user = prop.getProperty("db.user");
            String pass = prop.getProperty("db.password");

            Flyway flyway = Flyway.configure()
                    .dataSource(url, user, pass)
                    .load();

            flyway.migrate();

        } catch (Exception e) {
            System.err.println("Error al migrar la base de datos: " + e.getMessage());
        }

        launch(args);
    }
}