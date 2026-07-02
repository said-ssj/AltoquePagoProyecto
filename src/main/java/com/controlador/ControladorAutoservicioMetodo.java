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
        // 1. Configuramos el tamaño del QR por código de forma estricta
        imgCodigoQR.setFitWidth(230);
        imgCodigoQR.setFitHeight(230);

        // 2. ESTADO INICIAL: Forzamos que el QR empiece totalmente oculto y colapsado
        imgCodigoQR.setVisible(false);
        imgCodigoQR.setManaged(false);

        // 3. Dejamos visible el ícono de Ikonli
        iconoQrBoton.setVisible(true);
        iconoQrBoton.setManaged(true);

        // Precargar la imagen desde los recursos
        try {
            Image imagen = new Image(getClass().getResourceAsStream("/com/imagenes/qr-plin.png"));
            imgCodigoQR.setImage(imagen);
        } catch (Exception e) {
            System.err.println("Error al precargar la imagen de Plin: " + e.getMessage());
        }
    }

    @FXML
    void seleccionarQr(MouseEvent event) {
        System.out.println("-> Kiosko: Generando QR de pago y centralizando vista...");

        // 1. Desaparecer por completo la opción de tarjeta física del layout
        btnOpcionTarjeta.setVisible(false);
        btnOpcionTarjeta.setManaged(false);

        // 2. CONTROL NATIVO: Apagamos el ícono de Ikonli y encendemos el QR real
        iconoQrBoton.setVisible(false);
        iconoQrBoton.setManaged(false);

        imgCodigoQR.setVisible(true);
        imgCodigoQR.setManaged(true);

        // 3. Aplicamos la clase de diseño para el borde azul centrado
        btnOpcionQr.getStyleClass().add("metodo-activo-centrado");

        // 4. Actualizar la instrucción en pantalla
        lblInstruccionQr.setText("Escanee el código QR desde su aplicación móvil para finalizar.");
    }

    @FXML
    void seleccionarTarjeta(MouseEvent event) {
        System.out.println("-> Kiosko: Tarjeta POS seleccionada. Centralizando vista...");

        // 1. Desaparecer por completo la opción de QR del layout
        btnOpcionQr.setVisible(false);
        btnOpcionQr.setManaged(false);

        // 2. Centralizar la tarjeta visualmente
        btnOpcionTarjeta.getStyleClass().add("metodo-activo-centrado");

        // 3. Mostrar el mensaje de alerta para el POS
        lblInstruccionTarjeta.setText("POR FAVOR, ACERQUE O INSERTE SU TARJETA AL TERMINAL POS");
        lblInstruccionTarjeta.getStyleClass().add("texto-alerta-pos");
    }
}