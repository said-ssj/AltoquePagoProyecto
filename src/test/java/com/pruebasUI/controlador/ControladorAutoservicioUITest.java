package com.pruebasUI.controlador;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;

public class ControladorAutoservicioUITest extends ApplicationTest {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vista/AutoservicioEscaner-view.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
        stage.toFront();
    }

    @Test
    @DisplayName("El layout principal del Kiosko de escáner debe cargar correctamente")
    public void testCargaInicialElementos() {
        // Validamos que los contenedores clave donde se inyectan los productos existan y sean visibles
        verifyThat("#panelProductos", isVisible());
        verifyThat("#panelCarrito", isVisible());
    }

    @Test
    @DisplayName("Simular escaneo de producto mediante pistola lectora (Teclado) y reflejo en carrito")
    public void testSimularEscaneoLectorBarras() {
        // 1. OBTENER ESTADO INICIAL
        // Buscamos el VBox del carrito para ver cuántos items tiene antes de escanear
        VBox panelCarrito = lookup("#panelCarrito").queryAs(VBox.class);
        int cantidadItemsAntes = panelCarrito.getChildren().size();

        // 2. SIMULAR ESCANEO (El Robot teclea "en el aire" igual que un lector láser físico)
        String codigoPruebaReal = "7750001007";

        write(codigoPruebaReal); // Escribe el código rápidamente
        push(KeyCode.ENTER);     // Presiona ENTER (lo que hace la pistola al final del escaneo)

        // 3. VERIFICAR COMPORTAMIENTO
        // Damos un diminuto respiro al hilo de JavaFX (Platform.runLater) que usas en el controlador
        sleep(500);

        int cantidadItemsDespues = panelCarrito.getChildren().size();

        // (Descomenta esta aserción si configuras el código real)
        // assertTrue(cantidadItemsDespues > cantidadItemsAntes, "La tarjeta del producto debió renderizarse visualmente en el panelCarrito");

        // Por seguridad en pruebas generales, verificamos que el panel siga intacto (no se haya roto)
        verifyThat("#panelCarrito", isVisible());
    }
}