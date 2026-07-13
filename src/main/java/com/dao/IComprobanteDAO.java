package com.dao;

/**
 * Contrato para la persistencia de documentos legales (Boletas/Facturas).
 * Define las operaciones necesarias para sellar la venta y generar
 * el documento correspondiente en el sistema.
 */
public interface IComprobanteDAO {

    /**
     * Registra un nuevo comprobante vinculado a una venta específica.
     * @param idVenta El ID de la transacción realizada.
     * @param tipoDoc El tipo de documento (Ej: "BOLETA", "FACTURA").
     * @param numDoc El número de DNI o RUC del cliente.
     * @return true si el registro fue exitoso.
     */
    boolean generarComprobante(int idVenta, String tipoDoc, String numDoc);
}