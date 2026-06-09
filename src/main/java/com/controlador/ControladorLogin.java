package com.controlador;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class ControladorLogin {

    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtPassword;
    @FXML private Button btnEntrarGestion;
    @FXML private Button btnKiosko;

    @FXML
    public void ingresarGestion(ActionEvent event) {
        // Aquí luego agregarás la validación de usuario y contraseña con la BD
        System.out.println("Iniciando sesión en Gestión...");
        cambiarEscena(event, "menu.fxml", false);
    }

    @FXML
    public void iniciarKiosko(ActionEvent event) {
        System.out.println("Iniciando modo Kiosko de Autoservicio...");
        // ¡Carga la pantalla maestra!
        cambiarEscena(event, "AutoservicioCheckoutDividida.fxml", true);
    }

    private void cambiarEscena(ActionEvent event, String fxml, boolean modoKiosko) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();

            // Obtener la ventana (Stage) actual
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();

            Scene scene = new Scene(root);
            stage.setScene(scene);

            // 1. Desbloqueamos el tamaño de la ventana para que pueda crecer
            stage.setResizable(true);

            if (modoKiosko) {
                // MODO KIOSKO: Oculta la barra de tareas de Windows (Modo FullScreen inmersivo)
                stage.setFullScreen(true);
                stage.setFullScreenExitHint(""); // Quita el mensaje molesto de "Presione ESC para salir"
            } else {
                // MODO GESTIÓN: Se maximiza como un programa normal (con barra superior y de Windows)
                stage.setMaximized(true);
            }

            stage.show();
        } catch (IOException e) {
            System.err.println("Error al cargar la vista: " + fxml);
            e.printStackTrace();
        }
    }
}