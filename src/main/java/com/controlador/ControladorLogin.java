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
        System.out.println("Iniciando sesión en Gestión...");
        cambiarEscena(event, "/com/vista/menu-view.fxml", false);
    }

    @FXML
    public void iniciarKiosko(ActionEvent event) {
        System.out.println("Iniciando modo Kiosko de Autoservicio...");
        cambiarEscena(event, "/com/vista/AutoservicioCheckoutDividida-view.fxml", true);
    }

    private void cambiarEscena(ActionEvent event, String fxml, boolean modoKiosko) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();

            Scene scene = new Scene(root);
            stage.setScene(scene);
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