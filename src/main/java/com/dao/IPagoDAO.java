/*
 * Definimos el contrato para las operaciones de persistencia relacionadas con los pagos.
 * Al aislar estas firmas, garantizamos que la base de datos de pagos sea accesible de manera
 * abstracta, permitiendo cambiar el motor de persistencia en el futuro sin romper el negocio.
 */
package com.dao;

public interface IPagoDAO {
    boolean guardarPago(int idVenta, String metodo, double monto, String estado);
    double obtenerTotalPorMetodoHoy(String metodo);
    double obtenerTotalBilleterasDigitalesHoy();
    double obtenerTotalIngresosHoy();
}