package com.pruebasUI.controlador;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;

public class ControladorMenuUITest extends ApplicationTest {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vista/menu-view.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
        stage.toFront();
    }

    @Test
    @DisplayName("Debería cargar dinámicamente el panel de Reportes al hacer clic en su respectivo menú")
    public void testIntercambioPanelAReportes() {
        // Hacemos clic en el botón del menú
        clickOn("#btnReportes");

        // Verificamos que el botón de generar ventas de la vista de reportes sea visible
        verifyThat("#btnGenerarVentas", isVisible());
    }
}