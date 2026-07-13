/*
 * Definimos el contrato genérico para cualquier motor de generación de comprobantes.
 * Al abstraer la emisión, permitimos que el sistema se extienda fácilmente con nuevos
 * formatos de salida (como XML, HTML o formatos de impresión alternativos) sin alterar
 * la lógica de negocio de los controladores, cumpliendo con el Principio Abierto/Cerrado (OCP).
 */
package com.servicio;

import com.modelo.Venta;

public interface IComprobanteGenerador {
    void emitirTicketKiosko(Venta venta);
    void emitirFormatoA4(Venta venta);
}