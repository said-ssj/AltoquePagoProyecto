package com.servicio;

import com.dao.PagoDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PagoServicio {

    // Inicializamos el Logger usando la fachada SLF4J (que por debajo usará tu configuración de Logback)
    private static final Logger log = LoggerFactory.getLogger(PagoServicio.class);
    private final PagoDAO pagoDAO = new PagoDAO();

    /**
     * Verifica que el total sea válido.
     */
    public boolean procesarPago(double total) {
        return total > 0;
    }

    /**
     * Procesa e inserta de forma real el pago usando la estructura exacta de tu PagoDAO.
     * @param idVenta ID de la venta generada
     * @param metodo Método seleccionado ('YAPE', 'PLIN', 'TARJETA')
     * @param monto Monto total cobrado
     * @return true si el monto es válido y se ejecuta el guardado
     */
    public boolean procesarPagoReal(int idVenta, String metodo, double monto) {
        if (monto <= 0) {
            log.warn("Intento de procesamiento de pago rechazado por monto inválido: S/ {} para la venta N° {}", monto, idVenta);
            return false;
        }

        try {
            // Invocamos a tu método real con sus 4 parámetros exactos del PagoDAO
            pagoDAO.guardarPago(idVenta, metodo, monto, "PAGADO");

            log.info("Pago registrado exitosamente. Venta N° {}, Método: {}, Monto: S/ {}", idVenta, metodo, monto);
            return true;
        } catch (Exception e) {
            // Logback interceptará el objeto de la excepción 'e' al final y guardará el StackTrace completo en el archivo de log
            log.error("Error crítico en la base de datos al registrar el pago para la venta N° {}: {}", idVenta, e.getMessage(), e);
            return false;
        }
    }

}