package com;

import com.modelo.Producto;
import com.servicio.CarritoServicio;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TestCarrito {

    @Test
    public void testCalcularTotalCarrito() {

        CarritoServicio carrito = new CarritoServicio();

        Producto galleta = new Producto(1, "7590011251100", "Oreo clásica (paquete comercial de 36 g)", 2.00);
        Producto gaseosa = new Producto(2, "7750236173896", "Inca Kola de 500 ml", 3.50);

        carrito.agregarProducto(galleta);
        carrito.agregarProducto(galleta);
        carrito.agregarProducto(gaseosa);

        double totalEsperado = 7.50;

        assertEquals(totalEsperado, carrito.obtenerTotal(), "El total del carrito debe ser la suma exacta de los items");
    }
}