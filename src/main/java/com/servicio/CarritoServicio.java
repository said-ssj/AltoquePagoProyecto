package com.servicio;

import com.modelo.Carrito;
import com.modelo.ItemCarrito;
import com.modelo.Producto;
import java.util.List;

public class CarritoServicio { // <-- Clase normal de servicio, NO de test
    private Carrito carrito = new Carrito();

    public void agregarProducto(Producto producto){
        carrito.agregarProducto(producto);
    }

    public double obtenerTotal(){
        return carrito.calcularTotal();
    }

    public Carrito getCarrito(){
        return carrito;
    }

    // 1. Método para obtener los ítems desde el carrito
    public List<ItemCarrito> obtenerItems() {
        return carrito.getItems();
    }

    // 2. Método para modificar la cantidad
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