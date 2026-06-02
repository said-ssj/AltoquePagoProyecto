package com.;

import com.modelo.ItemCarrito;
import com.modelo.Producto;
import com.servicio.CarritoServicio;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TestCarrito {

    @Test
    public void testCalcularTotalCarrito() {
        CarritoServicio carrito = new CarritoServicio();
        Producto galleta = new Producto("P003", "Galleta Oreo", "Snacks", 2.00, 100);
        Producto gaseosa = new Producto("P004", "Inca Kola", "Bebidas", 3.50, 50);

        carrito.agregarItem(new ItemCarrito(galleta, 2)); // 2 * 2.00 = 4.00
        carrito.agregarItem(new ItemCarrito(gaseosa, 1)); // 1 * 3.50 = 3.50

        double totalEsperado = 7.50;
        assertEquals(totalEsperado, carrito.calcularTotal(), "El total del carrito debe ser la suma exacta de los items");
    }
}