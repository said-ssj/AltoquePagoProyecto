/*
 * Definimos el contrato para las operaciones de lógica de negocio del carrito de compras.
 * Al establecer esta interfaz, permitimos que las vistas de usuario interactúen con la lógica
 * del carrito sin acoplarse rígidamente a una implementación concreta de servicio.
 */
package com.servicio;

import com.modelo.Carrito;
import com.modelo.ItemCarrito;
import com.modelo.Producto;
import java.util.List;

public interface ICarritoServicio {
    void agregarProducto(Producto producto);
    double obtenerTotal();
    Carrito getCarrito();
    List<ItemCarrito> obtenerItems();
    void modificarCantidad(Producto producto, int nuevaCantidad);
}