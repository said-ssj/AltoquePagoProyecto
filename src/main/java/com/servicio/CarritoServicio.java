/*
 * Implementamos la lógica de control y validación de artículos agregados al carrito de compras.
 * Hemos aplicado el Principio de Inversión de Dependencias (DIP) abstrayendo el comportamiento
 * en una interfaz e integrando la estructura de datos interna de forma aislada para asegurar
 * que el estado de la compra sea consistente y fácil de testear.
 */
package com.servicio;

import com.modelo.Carrito;
import com.modelo.ItemCarrito;
import com.modelo.Producto;
import java.util.List;

public class CarritoServicio implements ICarritoServicio {

    private final Carrito carrito;

    public CarritoServicio() {
        this.carrito = new Carrito();
    }

    @Override
    public void agregarProducto(Producto producto) {
        carrito.agregarProducto(producto);
    }

    @Override
    public double obtenerTotal() {
        return carrito.calcularTotal();
    }

    @Override
    public Carrito getCarrito() {
        return carrito;
    }

    @Override
    public List<ItemCarrito> obtenerItems() {
        return carrito.getItems();
    }

    @Override
    public void modificarCantidad(Producto producto, int nuevaCantidad) {
        if (nuevaCantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }
        for (ItemCarrito item : carrito.getItems()) {
            if (item.getProducto().getId_producto() == producto.getId_producto()) {
                return;
            }
        }
    }
}