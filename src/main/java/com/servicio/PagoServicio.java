/*
 * Implementamos la lógica de verificación y registro real de flujos de pago del sistema.
 * Hemos aplicado el Principio de Inversión de Dependencias (DIP) inyectando la abstracción
 * IPagoDAO mediante el constructor, eliminando de forma definitiva el uso de instanciaciones
 * rígidas y permitiendo un registro financiero seguro, limpio y fácil de testear de forma aislada.
 */
package com.servicio;

import com.dao.IPagoDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PagoServicio implements IPagoServicio {

    private static final Logger log = LoggerFactory.getLogger(PagoServicio.class);
    private final IPagoDAO pagoDAO;

    public PagoServicio(IPagoDAO pagoDAO) {
        this.pagoDAO = pagoDAO;
    }

    /**
     * Verifica que el total sea válido.
     */

    @Override
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

    @Override
    public boolean procesarPagoReal(int idVenta, String metodo, double monto) {
        if (monto <= 0) {
            log.warn("Intento de procesamiento de pago rechazado por monto inválido: S/ {} para la venta N° {}", monto, idVenta);
            return false;
        }
        try {
            pagoDAO.guardarPago(idVenta, metodo, monto, "PAGADO");
            log.info("Pago registrado exitosamente. Venta N° {}, Método: {}, Monto: S/ {}", idVenta, metodo, monto);
            return true;
        } catch (Exception e) {
            log.error("Error crítico en la base de datos al registrar el pago para la venta N° {}: {}", idVenta, e.getMessage(), e);
            return false;
        }
    }
}