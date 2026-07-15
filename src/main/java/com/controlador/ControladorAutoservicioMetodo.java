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

    /** Método elegido: null = ninguno, "QR", "TARJETA" */
    private String metodoPagoElegido = null;

    /** Referencia al padre para comunicar selección */
    private ControladorAutoservicioCheckoutDividida contenedorPadre;

    public void setContenedorPadre(ControladorAutoservicioCheckoutDividida padre) {
        this.contenedorPadre = padre;
    }

    public String getMetodoPagoElegido() {
        return metodoPagoElegido;
    }

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
        System.out.println("-> Kiosko: QR seleccionado.");
        metodoPagoElegido = "QR";

        ocultarElemento(btnOpcionTarjeta);
        alternarVisibilidadQr(true);
        btnOpcionQr.getStyleClass().remove("metodo-activo-centrado");
        if (!btnOpcionQr.getStyleClass().contains("metodo-activo-centrado"))
            btnOpcionQr.getStyleClass().add("metodo-activo-centrado");
        lblInstruccionQr.setText("Escanee el código QR desde su aplicación móvil para finalizar.");

        // Habilitar botón Finalizar en el padre
        if (contenedorPadre != null) contenedorPadre.habilitarFinalizarCompra();
    }

    @FXML
    void seleccionarTarjeta(MouseEvent event) {
        System.out.println("-> Kiosko: Tarjeta POS seleccionada.");
        metodoPagoElegido = "TARJETA";

        ocultarElemento(btnOpcionQr);
        btnOpcionTarjeta.getStyleClass().remove("metodo-activo-centrado");
        if (!btnOpcionTarjeta.getStyleClass().contains("metodo-activo-centrado"))
            btnOpcionTarjeta.getStyleClass().add("metodo-activo-centrado");
        lblInstruccionTarjeta.setText("POR FAVOR, ACERQUE O INSERTE SU TARJETA AL TERMINAL POS");
        if (!lblInstruccionTarjeta.getStyleClass().contains("texto-alerta-pos"))
            lblInstruccionTarjeta.getStyleClass().add("texto-alerta-pos");

        // Habilitar botón Finalizar en el padre
        if (contenedorPadre != null) contenedorPadre.habilitarFinalizarCompra();
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