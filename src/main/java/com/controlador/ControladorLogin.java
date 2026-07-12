package com.controlador;

import com.dao.UsuarioPersonalDAO;
import com.modelo.UsuarioPersonal;
import com.servicio.Seguridad;
import com.servicio.SesionActual;

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
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Controlador del formulario de Login.
 *
 * SEGURIDAD [SEC-05]: Implementa protección contra fuerza bruta:
 *   - Máximo 5 intentos fallidos por email.
 *   - Bloqueo temporal de 5 minutos tras superar el límite.
 *   - Los contadores se guardan en memoria (se reinician al cerrar la app).
 *
 * SEGURIDAD [SEC-03]: Guarda el usuario autenticado en SesionActual para
 *   que los demás controladores puedan verificar el rol.
 */
public class ControladorLogin {

    // Límites de seguridad
    private static final int  MAX_INTENTOS      = 5;
    private static final long BLOQUEO_SEGUNDOS  = 300; // 5 minutos

    // Mapa en memoria: email → número de intentos fallidos
    private static final Map<String, Integer>  intentosFallidos = new HashMap<>();
    // Mapa en memoria: email → timestamp del primer bloqueo
    private static final Map<String, Instant>  tiempoBloqueo    = new HashMap<>();

    @FXML private TextField     txtUsuario;
    @FXML private PasswordField txtPassword;
    @FXML private Button        btnEntrarGestion;
    @FXML private Button        btnKiosko;

    private final UsuarioPersonalDAO usuarioDAO = new UsuarioPersonalDAO();

    @FXML
    public void ingresarGestion(ActionEvent event) {
        UsuarioPersonal usuario = autenticar();
        if (usuario != null) {
            SesionActual.getInstancia().iniciarSesion(usuario);
            cambiarEscena(event, "/com/vista/menu-view.fxml", false);
        }
    }

    @FXML
    public void iniciarKiosko(ActionEvent event) {
            cambiarEscena(event, "/com/vista/AutoservicioCheckoutDividida-view.fxml", true);
    }

    /**
     * Verifica credenciales con protección contra fuerza bruta.
     * Retorna el usuario autenticado o null si falla.
     *
     * SEGURIDAD [SEC-05]: Antes de consultar la BD, comprueba si la cuenta
     * está bloqueada. Incrementa el contador por cada intento fallido.
     */
    private UsuarioPersonal autenticar() {
        String email    = txtUsuario.getText().trim();
        String password = txtPassword.getText();

        // Validar campos vacíos
        if (email.isEmpty() || password.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campos Vacíos",
                    "Por favor, ingresa tu email y contraseña para continuar.");
            return null;
        }

        // SEGURIDAD [SEC-05]: Verificar bloqueo temporal
        if (estaBloqueado(email)) {
            long segundosRestantes = segundosRestantesBloqueo(email);
            mostrarAlerta(Alert.AlertType.ERROR, "Cuenta Bloqueada Temporalmente",
                    "Demasiados intentos fallidos. Espera " + segundosRestantes +
                            " segundo(s) antes de intentar de nuevo.");
            return null;
        }

        // Consultar base de datos
        UsuarioPersonal usuario = usuarioDAO.autenticarUsuario(email, password);

        if (usuario != null) {
            // Éxito: limpiar contadores
            intentosFallidos.remove(email);
            tiempoBloqueo.remove(email);
            return usuario;
        } else {
            // Fallo: incrementar contador
            int intentos = intentosFallidos.getOrDefault(email, 0) + 1;
            intentosFallidos.put(email, intentos);

            if (intentos >= MAX_INTENTOS) {
                tiempoBloqueo.put(email, Instant.now());
                mostrarAlerta(Alert.AlertType.ERROR, "Cuenta Bloqueada Temporalmente",
                        "Has superado el límite de " + MAX_INTENTOS + " intentos. " +
                                "Espera " + (BLOQUEO_SEGUNDOS / 60) + " minuto(s) antes de intentar de nuevo.");
            } else {
                int restantes = MAX_INTENTOS - intentos;
                mostrarAlerta(Alert.AlertType.ERROR, "Acceso Denegado",
                        "El email o la contraseña son incorrectos. " +
                                "Te quedan " + restantes + " intento(s) antes del bloqueo temporal.");
            }
            return null;
        }
    }

    private boolean estaBloqueado(String email) {
        Instant bloqueadoDesde = tiempoBloqueo.get(email);
        if (bloqueadoDesde == null) return false;
        long segundosPasados = Instant.now().getEpochSecond() - bloqueadoDesde.getEpochSecond();
        if (segundosPasados >= BLOQUEO_SEGUNDOS) {
            // Expiró el bloqueo: reiniciar contadores
            intentosFallidos.remove(email);
            tiempoBloqueo.remove(email);
            return false;
        }
        return true;
    }

    private long segundosRestantesBloqueo(String email) {
        Instant bloqueadoDesde = tiempoBloqueo.get(email);
        if (bloqueadoDesde == null) return 0;
        long pasados = Instant.now().getEpochSecond() - bloqueadoDesde.getEpochSecond();
        return Math.max(0, BLOQUEO_SEGUNDOS - pasados);
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
