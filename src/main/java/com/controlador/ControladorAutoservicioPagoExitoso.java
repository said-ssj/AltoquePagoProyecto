package com.controlador;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import javafx.util.Duration;
import javafx.application.Platform;

public class ControladorAutoservicioPagoExitoso {

    @FXML
    public void initialize() {
        System.out.println("-> Kiosko: Venta finalizada con éxito. Iniciando temporizador de 2.5s...");

        Timeline temporizadorReinicio = new Timeline(
                new KeyFrame(Duration.seconds(2.5), event -> reiniciarKioscoCoordinado())
        );
        temporizadorReinicio.setCycleCount(1);
        temporizadorReinicio.play();
    }

    private void reiniciarKioscoCoordinado() {
        System.out.println("-> Kiosko: Tiempo cumplido. Regresando a la pantalla de Escaneo...");

        Platform.runLater(() -> {
            // 1. Encontrar la ventana activa de la aplicación
            Parent rootActivo = javafx.stage.Stage.getWindows().stream()
                    .filter(window -> window.getScene() != null)
                    .map(window -> window.getScene().getRoot())
                    .findFirst().orElse(null);

            if (rootActivo instanceof BorderPane) {
                BorderPane panelPrincipal = (BorderPane) rootActivo;

                // 2. Extraer el controlador Checkout mediante el UserData que inyectamos en HelloApplication
                Object controlador = panelPrincipal.getUserData();

                if (controlador instanceof ControladorAutoservicioCheckoutDividida) {
                    ControladorAutoservicioCheckoutDividida checkout = (ControladorAutoservicioCheckoutDividida) controlador;

                    // 3. CORRECCIÓN DEFINITIVA: Usar el propio método cíclico del contenedor para volver al paso 1 (Escáner)
                    System.out.println("-> Kiosko: Reiniciando máquina de estados al Paso 1 (Escáner).");
                    checkout.cargarPaso(1);
                } else {
                    System.out.println("-> No se pudo enlazar con la máquina de estados. Intentando reinicio por defecto de vista intermedia.");
                    try {
                        javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/vista/AutoservicioEscaner-view.fxml"));
                        Parent vistaEscaner = loader.load();
                        panelPrincipal.setCenter(vistaEscaner);
                    } catch (Exception e) {
                        System.err.println("Error alternativo de carga: " + e.getMessage());
                    }
                }
            }
        });
    }
}