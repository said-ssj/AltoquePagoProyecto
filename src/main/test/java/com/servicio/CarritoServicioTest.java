package com.;

import com.modelo.ItemCarrito;
import com.modelo.Producto;
import com.servicio.CarritoServicio;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@Test
void agregarProductoIncrementaTotal() {
    CarritoServicio s = new CarritoServicio();
    Producto p = new Producto(1,"123","Leche",4.50);
    s.agregarProducto(p);
    assertEquals(4.50, s.obtenerTotal());
}