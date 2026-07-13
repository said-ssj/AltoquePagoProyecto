/*
 * Controlador visual para la selección del método de pago en el Kiosko.
 * Se optimizó la manipulación del DOM de JavaFX extrayendo la lógica repetitiva
 * de visibilidad (Visible/Managed) a métodos utilitarios privados, manteniendo
 * las acciones de los botones limpias y directas.
 */
package com.controlador;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

public class ControladorAutoservicioMetodo {

    @FXML private VBox btnOpcionQr;
    @FXML private VBox btnOpcionTarjeta;
    @FXML private HBox contenedorMetodos;

    @FXML private FontIcon iconoQrBoton;
    @FXML private ImageView imgCodigoQR;

    @FXML private Label lblInstruccionQr;
    @FXML private Label lblInstruccionTarjeta;

    @FXML
    public void initialize() {
        // 1. Configuramos el tamaño del QR de forma estricta
        imgCodigoQR.setFitWidth(230);
        imgCodigoQR.setFitHeight(230);

        // 2. Estado inicial: QR oculto, Ícono visible
        alternarVisibilidadQr(false);

        // 3. Precargar la imagen desde los recursos
        cargarImagenQR("/com/imagenes/qr-plin.png");
    }

    @FXML
    void seleccionarQr(MouseEvent event) {
        System.out.println("-> Kiosko: Generando QR de pago y centralizando vista...");

        // 1. Desaparecer por completo la opción de tarjeta física
        ocultarElemento(btnOpcionTarjeta);

        // 2. Intercambiar el ícono vectorizado por la imagen real del QR
        alternarVisibilidadQr(true);

        // 3. Aplicar estilo de selección y actualizar texto
        btnOpcionQr.getStyleClass().add("metodo-activo-centrado");
        lblInstruccionQr.setText("Escanee el código QR desde su aplicación móvil para finalizar.");

        // [!] Aquí podrías lanzar un Temporizador o notificar al Controlador principal
        // para avanzar automáticamente a la siguiente pantalla si así lo requiere tu flujo.
    }

    @FXML
    void seleccionarTarjeta(MouseEvent event) {
        System.out.println("-> Kiosko: Tarjeta POS seleccionada. Centralizando vista...");

        // 1. Desaparecer por completo la opción de QR
        ocultarElemento(btnOpcionQr);

        // 2. Centralizar la tarjeta visualmente y aplicar estilos de alerta
        btnOpcionTarjeta.getStyleClass().add("metodo-activo-centrado");
        lblInstruccionTarjeta.setText("POR FAVOR, ACERQUE O INSERTE SU TARJETA AL TERMINAL POS");

        // Evitamos duplicar la clase si el usuario da múltiples clics
        if (!lblInstruccionTarjeta.getStyleClass().contains("texto-alerta-pos")) {
            lblInstruccionTarjeta.getStyleClass().add("texto-alerta-pos");
        }
    }

    // ============================================================
    //  MÉTODOS UTILITARIOS (DRY)
    // ============================================================

    private void cargarImagenQR(String ruta) {
        try {
            Image imagen = new Image(getClass().getResourceAsStream(ruta));
            imgCodigoQR.setImage(imagen);
        } catch (Exception e) {
            System.err.println("Error al precargar la imagen QR en ruta: " + ruta);
        }
    }

    private void alternarVisibilidadQr(boolean mostrarQr) {
        // Enciende/Apaga el QR
        imgCodigoQR.setVisible(mostrarQr);
        imgCodigoQR.setManaged(mostrarQr);

        // Hace lo inverso con el ícono vectorial
        iconoQrBoton.setVisible(!mostrarQr);
        iconoQrBoton.setManaged(!mostrarQr);
    }

    private void ocultarElemento(VBox elemento) {
        elemento.setVisible(false);
        elemento.setManaged(false);
    }
}