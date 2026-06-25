package com.controlador;

import com.dao.UsuarioPersonalDAO;
import com.modelo.UsuarioPersonal;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
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

    private final UsuarioPersonalDAO usuarioDAO = new UsuarioPersonalDAO();

    // Variable para guardar quién inició sesión
    private UsuarioPersonal usuarioActual;

    @FXML
    public void ingresarGestion(ActionEvent event) {
        if (validarCredenciales()) {
            System.out.println("Iniciando sesión en Gestión...");
            cambiarEscena(event, "/com/vista/menu-view.fxml", false);
        }
    }

    @FXML
    public void iniciarKiosko(ActionEvent event) {
        // Ingreso directo al modo Kiosko sin requerir autenticación
        System.out.println("Iniciando modo Kiosko de Autoservicio (Acceso Libre)...");
        cambiarEscena(event, "/com/vista/AutoservicioCheckoutDividida-view.fxml", true);
    }

    private boolean validarCredenciales() {
        String email = txtUsuario.getText().trim();
        String password = txtPassword.getText();

        if (email.isEmpty() || password.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campos Vacíos", "Por favor, ingresa tu email y contraseña para continuar.");
            return false;
        }

        // Guardamos el usuario en nuestra variable global de la clase
        usuarioActual = usuarioDAO.autenticarUsuario(email, password);

        if (usuarioActual != null) {
            return true; // Login exitoso
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, "Acceso Denegado", "El email o la contraseña son incorrectos. Intenta nuevamente.");
            return false;
        }
    }

    private void cambiarEscena(ActionEvent event, String fxml, boolean modoKiosko) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();

            // Antes de mostrar la escena, le pasamos el ROL al Menú Principal
            if (fxml.equals("/com/vista/menu-view.fxml")) {
                ControladorPrincipal controllerMenu = loader.getController();
                controllerMenu.configurarAccesos(usuarioActual.getIdRol());
            }

            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setResizable(true);

            if (modoKiosko) {
                stage.setFullScreen(true);
                stage.setFullScreenExitHint("");
            } else {
                stage.setMaximized(true);
            }

            stage.show();
        } catch (IOException e) {
            System.err.println("Error al cargar la vista: " + fxml);
            e.printStackTrace();
        }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}