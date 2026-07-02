package com.controlador;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.application.Platform;

public class ControladorAutoservicioDatos {

    @FXML private TextField txtDni;
    @FXML private TextField txtNombres;
    @FXML private TextField txtDireccion;
    @FXML private TextField txtCorreo;

    private ControladorAutoservicioCheckoutDividida contenedorPadre;

    @FXML
    public void initialize() {
        // Se mantiene vacío para evitar NullPointerException tempranos
    }

    public void setContenedorPadre(ControladorAutoservicioCheckoutDividida padre) {
        this.contenedorPadre = padre;

        // Esperamos a que los nodos gráficos estén completamente listos y renderizados
        Platform.runLater(() -> {
            if (txtDni == null) {
                System.err.println("-> Error crítico: El objeto txtDni sigue siendo null. Revisa que el fx:id en tu FXML sea exactamente 'txtDni'.");
                return;
            }

            // 1. Configurar valores iniciales por defecto para Boleta Simple
            if (txtDni.getText() == null || txtDni.getText().trim().isEmpty() || txtDni.getText().equals("00000000")) {
                txtDni.setText("00000000");
                txtNombres.setText("CLIENTE VARIOS");
                txtDireccion.setText("");
                txtCorreo.setText("");

                if (contenedorPadre != null) {
                    contenedorPadre.modificarBotonDatos(false);
                    contenedorPadre.setDatosCliente("00000000", "CLIENTE VARIOS");
                }
            }

            // 2. Listener reactivo en tiempo real al escribir en la caja del DNI
            txtDni.textProperty().addListener((observable, oldValue, newValue) -> {
                if (contenedorPadre == null) return;

                String dniLimpio = (newValue == null) ? "" : newValue.trim();

                // Si el campo vuelve a quedar vacío, se borra o es el por defecto
                if (dniLimpio.isEmpty() || dniLimpio.equals("00000000")) {
                    txtNombres.setText("CLIENTE VARIOS");
                    txtDireccion.setText("");
                    txtCorreo.setText("");
                    contenedorPadre.modificarBotonDatos(false);
                    contenedorPadre.setDatosCliente("00000000", "CLIENTE VARIOS");
                } else {
                    // Si el usuario ingresó un DNI (por ejemplo para buscar en RENIEC)
                    contenedorPadre.modificarBotonDatos(true);
                    contenedorPadre.setDatosCliente(dniLimpio, txtNombres.getText());
                }
            });
        });
    }
}