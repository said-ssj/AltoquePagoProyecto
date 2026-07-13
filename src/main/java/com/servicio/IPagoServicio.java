/*
 * Establecemos el contrato para la lógica de negocio y control de transacciones de pago.
 * Esta abstracción permite que las capas de presentación invoquen los flujos financieros
 * del sistema de manera totalmente desacoplada.
 */
package com.servicio;

public interface IPagoServicio {
    boolean procesarPago(double total);
    boolean procesarPagoReal(int idVenta, String metodo, double monto);
}