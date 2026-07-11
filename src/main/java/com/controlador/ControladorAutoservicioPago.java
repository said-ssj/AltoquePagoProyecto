package com.controlador;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import com.modelo.Pago;
import com.servicio.ComprobanteImpresionServicio;

public class ControladorAutoservicioPago {

    @FXML
    private ImageView imgCodigoQR;

    @FXML
    private Label lblEstadoPago;

    @FXML
    private VBox contenedorQrPago;

    /**
     * Método que se invoca al entrar a la pantalla de pago recibiendo la opción elegida.
     * Muestra el QR correspondiente de forma limpia sin inyectar CSS en el código.
     */
    public void inicializarFlujoPago(String metodoSeleccionado, double montoTotal) {
        lblEstadoPago.setText("Esperando escaneo de pago: S/ " + String.format("%.2f", montoTotal));

        switch (metodoSeleccionado.toUpperCase()) {
            /*
            case "YAPE":
                cargarImagenQR("/com/imagenes/qr-yape.png");
                contenedorQrPago.getStyleClass().removeAll("pago-oculto", "pago-tarjeta");
                contenedorQrPago.getStyleClass().add("pago-qr-activo");
                break;
            */
            case "PLIN":
                cargarImagenQR("/com/imagenes/qr-plin.png");
                contenedorQrPago.getStyleClass().removeAll("pago-oculto", "pago-tarjeta");
                contenedorQrPago.getStyleClass().add("pago-qr-activo");
                break;

            case "TARJETA":
                // Para tarjetas se oculta el contenedor del QR o se muestra una animación diferente
                imgCodigoQR.setImage(null);
                contenedorQrPago.getStyleClass().removeAll("pago-qr-activo");
                contenedorQrPago.getStyleClass().add("pago-tarjeta");
                break;

            default:
                contenedorQrPago.getStyleClass().add("pago-oculto");
                break;
        }
    }

    private void cargarImagenQR(String rutaRecurso) {
        try {
            Image imagen = new Image(getClass().getResourceAsStream(rutaRecurso));
            imgCodigoQR.setImage(imagen);
        } catch (Exception e) {
            System.err.println("Error al extraer el archivo de imagen QR: " + rutaRecurso);
            e.printStackTrace();
        }
    }



}