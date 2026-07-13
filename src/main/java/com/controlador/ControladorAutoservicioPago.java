/*
 * Controlador de la pasarela de pago del Autoservicio.
 * Gestiona la interfaz de espera, visualización del método de pago (QR)
 * y el cierre definitivo de la transacción integrando el registro de pago
 * y la emisión del comprobante mediante inyección de dependencias.
 */
package com.controlador;

import com.dao.IPagoDAO;
import com.dao.PagoDAO;
import com.dao.IComprobanteDAO;
import com.dao.ComprobanteDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class ControladorAutoservicioPago {

    @FXML private ImageView imgCodigoQR;
    @FXML private Label lblEstadoPago;
    @FXML private VBox contenedorQrPago;

    // Abstracciones inyectadas para cumplir con SOLID
    private final IPagoDAO pagoDAO;
    private final IComprobanteDAO comprobanteDAO;

    public ControladorAutoservicioPago() {
        this.pagoDAO = new PagoDAO();
        this.comprobanteDAO = new ComprobanteDAO();
    }

    /**
     * Prepara la interfaz según el método de pago seleccionado por el cliente.
     */
    public void initializeFlujoPago(String metodoSeleccionado, double montoTotal) {
        lblEstadoPago.setText("Esperando escaneo de pago: S/ " + String.format("%.2f", montoTotal));

        switch (metodoSeleccionado.toUpperCase()) {
            case "PLIN":
                cargarImagenQR("/com/imagenes/qr-plin.png");
                contenedorQrPago.getStyleClass().removeAll("pago-oculto", "pago-tarjeta");
                contenedorQrPago.getStyleClass().add("pago-qr-activo");
                break;

            case "TARJETA":
                imgCodigoQR.setImage(null);
                contenedorQrPago.getStyleClass().removeAll("pago-qr-activo");
                contenedorQrPago.getStyleClass().add("pago-tarjeta");
                break;

            default:
                contenedorQrPago.getStyleClass().add("pago-oculto");
                break;
        }
    }

    /**
     * Punto final del flujo de autoservicio. Registra el pago y genera el comprobante.
     * @param idVenta ID de la venta procesada.
     * @param metodo Método de pago (Yape/Plin/Tarjeta).
     * @param monto Monto total de la operación.
     * @param tipoDoc Tipo de documento (BOLETA/FACTURA).
     * @param numDoc DNI o RUC del cliente.
     */
    public boolean finalizarTransaccion(int idVenta, String metodo, double monto, String tipoDoc, String numDoc) {
        // 1. Registro del pago en base de datos
        boolean pagoRegistrado = pagoDAO.guardarPago(idVenta, metodo, monto, "PAGADO");

        if (pagoRegistrado) {
            // 2. Generación del comprobante legal (Sustituye al antiguo servicio de impresión)
            boolean comprobanteGenerado = comprobanteDAO.generarComprobante(idVenta, tipoDoc, numDoc);

            if (comprobanteGenerado) {
                System.out.println("-> Transacción y documento legal registrados correctamente.");
                return true;
            }
        }

        System.err.println("-> Error al finalizar la transacción en el Autoservicio.");
        return false;
    }

    private void cargarImagenQR(String rutaRecurso) {
        try {
            Image imagen = new Image(getClass().getResourceAsStream(rutaRecurso));
            imgCodigoQR.setImage(imagen);
        } catch (Exception e) {
            System.err.println("Error al cargar recurso QR: " + rutaRecurso);
            e.printStackTrace();
        }
    }
}