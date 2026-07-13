package com.dao;

public interface IDetalleVentaDAO {
    boolean guardarDetalle(int idVenta, int idProducto, int cantidad, double precioUnitario, double subtotal);
}