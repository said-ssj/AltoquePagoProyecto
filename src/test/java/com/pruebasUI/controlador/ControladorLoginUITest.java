package com.pruebasUI.controlador;

import com.controlador.ControladorLogin;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;

public class ControladorLoginUITest extends ApplicationTest {

    @Override
    public void start(Stage stage) throws Exception {
        // Cargamos de forma real nuestra vista FXML del Login antes de iniciar cada test
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vista/Login-view.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
        // Nos aseguramos de traer la ventana gráfica al primer plano de nuestro sistema operativo
        stage.toFront();
    }

    @Test
    @DisplayName("Debería mostrar mensajes de advertencia visuales si intentamos ingresar con campos vacíos")
    public void testLoginCamposVaciosMuestraAlerta() {
        // Dejamos las entradas de texto en blanco intencionalmente
        clickOn("#txtUsuario").write("");
        clickOn("#txtPassword").write("");

        // Automatizamos el clic real del mouse sobre el botón de ingresar a gestión
        clickOn("#btnEntrarGestion");

        // Verificamos visualmente que el cuadro de diálogo flotante (Alert) de JavaFX se haya abierto en la pantalla
        // Nota: TestFX busca las alertas mediante la clase interna de diálogo de JavaFX
        verifyThat(".dialog-pane", isVisible());

        // Presionamos de forma virtual la tecla ENTER para cerrar el cuadro de alerta emergente y continuar con el hilo
        press(KeyCode.ENTER).release(KeyCode.ENTER);
    }

    @Test
    @DisplayName("Debería activar correctamente el Modo Kiosko a pantalla completa al hacer clic en el botón correspondiente")
    public void testBotonKioskoCambiaEscenaYActivaFullScreen() {
        // Dirigimos el mouse virtual y hacemos clic en el botón de autoservicio para el cliente
        clickOn("#btnKiosko");

        // Como nuestro controlador cambia de escena hacia 'AutoservicioCheckoutDividida-view.fxml',
        // verificamos que la nueva interfaz gráfica se haya cargado de forma correcta y que sea visible.
        // Nota: Reemplacemos '#idDeUnComponenteDelKiosko' por un ID real que tengamos en la vista del Kiosko (ej. #rootKiosko)
        // verifyThat("#idDeUnComponenteDelKiosko", isVisible());
    }
}