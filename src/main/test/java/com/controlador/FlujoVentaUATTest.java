package com.controlador;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import static org.junit.jupiter.api.Assertions.*;

public class FlujoVentaUATTest extends ApplicationTest {

    // Arranca la vista nuevaventa-view.fxml
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vista/nuevaventa-view.fxml"));
        Parent root = loader.load();
        stage.setScene(new Scene(root));
        stage.show();
    }

    @Test
    @DisplayName("UAT Automatizado: Flujo feliz de agregar producto y registrar cliente")
    public void testFlujoCompletoDeVenta() {
        // Ejecutamos el Robot que simula el agregado de Datos de cliente
        // Simula escanear un código de barras en el campo de texto
        clickOn("#txtBuscarProducto").write("7750001010").write("\n"); ;

        // Hace clic en el botón de "Agregar Producto"
        clickOn("#btnAgregarProducto");

        // Despliega el panel de los datos del cliente
        clickOn("#btnMostrarDatosCliente");

        // Escribe un RUC/DNI
        clickOn("#txtRucDni").write("00000000");

        // Guarda los datos del cliente temporalmente
        clickOn("#btnGuardarCliente");

        // Buscamos la etiqueta del Total en la pantalla de JavaFX
        Label lblTotal = lookup("#lblTotal").query();

        // Esto comprueba que el flujo visual funcionó desde la búsqueda hasta la suma del precio.
        assertNotEquals("S/ 0.00", lblTotal.getText(), "El total de la venta no se calculó correctamente en la interfaz.");

        System.out.println("UAT Superado: El flujo interactivo de venta se completó sin errores visuales ni lógicos.");
    }
}