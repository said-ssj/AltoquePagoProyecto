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

    // Instanciamos el DAO para consultar la base de datos
    private final UsuarioPersonalDAO usuarioDAO = new UsuarioPersonalDAO();

    @FXML
    public void ingresarGestion(ActionEvent event) {
        // Solo cambia de escena si las credenciales son válidas
        if (validarCredenciales()) {
            System.out.println("Iniciando sesión en Gestión...");
            cambiarEscena(event, "/com/vista/menu-view.fxml", false);
        }
    }

    @FXML
    public void iniciarKiosko(ActionEvent event) {
        System.out.println("Iniciando modo Kiosko de Autoservicio...");
        cambiarEscena(event, "/com/vista/AutoservicioCheckoutDividida-view.fxml", true);

    }

    /**
     * Extrae los datos de los campos de texto, verifica que no estén vacíos
     * y consulta la base de datos para autenticar al usuario.
     */
    private boolean validarCredenciales() {
        String email = txtUsuario.getText().trim();
        String password = txtPassword.getText();

        // Validar campos vacíos
        if (email.isEmpty() || password.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campos Vacíos", "Por favor, ingresa tu email y contraseña para continuar.");
            return false;
        }

        // Validar contra la base de datos (Este método ya encripta la contraseña internamente)
        UsuarioPersonal usuarioLogueado = usuarioDAO.autenticarUsuario(email, password);

        if (usuarioLogueado != null) {
            // Login exitoso
            return true;
        } else {
            // Login fallido
            mostrarAlerta(Alert.AlertType.ERROR, "Acceso Denegado", "El email o la contraseña son incorrectos. Intenta nuevamente.");
            return false;
        }
    }

    private void cambiarEscena(ActionEvent event, String fxml, boolean modoKiosko) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();

            if (modoKiosko) {
                Object controladorKiosko = loader.getController();
                root.setUserData(controladorKiosko);
            }

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

    /**
     * Método auxiliar para mostrar ventanas emergentes de error o advertencia.
     */
    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}