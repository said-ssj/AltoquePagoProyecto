/*
 * Establecemos el contrato para la gestión de datos relacionados con el Carrito de compras.
 * Con esta abstracción nos aseguramos de que el ciclo de vida del carrito en la base de datos
 * pueda ser accedido de forma desacoplada por los controladores del sistema.
 */
package com.dao;

import com.modelo.DetalleVenta;
import java.util.List;

public interface ICarritoDAO {
    int obtenerOCrearCarritoActivo(int idCliente);
    void agregarProductoAlBD(int idCarrito, int idProducto, double precioUnitario);
    List<DetalleVenta> obtenerDetallesDelCarrito(int idCarrito);
    void marcarCarritoComoPagado(int idCarrito);
}