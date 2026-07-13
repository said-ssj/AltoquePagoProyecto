/*
 * Controlador de la pantalla final de éxito.
 * Implementa un temporizador automático para reiniciar la máquina de estados del Kiosko
 * y devolver la interfaz al escáner de productos para el siguiente cliente.
 */
package com.controlador;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Window;
import javafx.util.Duration;

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
            BorderPane panelPrincipal = obtenerPanelPrincipalActivo();

            if (panelPrincipal != null) {
                // Extraemos el controlador orquestador mediante el UserData
                Object controlador = panelPrincipal.getUserData();

                if (controlador instanceof ControladorAutoservicioCheckoutDividida checkout) {

                    // CORRECCIÓN DEFINITIVA: Usar el método cíclico para volver al Paso 1
                    System.out.println("-> Kiosko: Reiniciando máquina de estados al Paso 1 (Escáner).");
                    checkout.cargarPaso(1);

                } else {
                    ejecutarReinicioDeRespaldo(panelPrincipal);
                }
            } else {
                System.err.println("-> Kiosko Error: No se encontró el BorderPane principal activo.");
            }
        });
    }

    // ============================================================
    //  MÉTODOS UTILITARIOS
    // ============================================================

    /**
     * Busca de forma segura la ventana principal visible que contenga un BorderPane como raíz.
     */
    private BorderPane obtenerPanelPrincipalActivo() {
        Parent rootActivo = Window.getWindows().stream()
                .filter(Window::isShowing) // Solo ventanas visibles
                .filter(window -> window.getScene() != null && window.getScene().getRoot() instanceof BorderPane)
                .map(window -> window.getScene().getRoot())
                .findFirst()
                .orElse(null);

        return (BorderPane) rootActivo;
    }

    /**
     * Carga la vista por defecto si la máquina de estados principal no fue encontrada.
     */
    private void ejecutarReinicioDeRespaldo(BorderPane panelPrincipal) {
        System.out.println("-> No se pudo enlazar con la máquina de estados. Intentando reinicio de respaldo...");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vista/AutoservicioEscaner-view.fxml"));
            Parent vistaEscaner = loader.load();
            panelPrincipal.setCenter(vistaEscaner);
        } catch (Exception e) {
            System.err.println("Error crítico al cargar vista alternativa de Escáner.");
            e.printStackTrace(); // Vital para depurar errores de FXML
        }
    }
}