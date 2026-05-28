package com.servicio;

import com.modelo.Carrito;
import com.modelo.Producto;

public class CarritoServicio {
    private Carrito carrito = new Carrito();

    public void agregarProducto(
            Producto producto
    ){
        carrito.agregarProducto(
                producto
        );
    }

    public double obtenerTotal(){
        return carrito.calcularTotal();
    }

    public Carrito getCarrito(){
        return carrito;
    }
}