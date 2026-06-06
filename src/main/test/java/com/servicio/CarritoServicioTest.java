package com.servicio;

import com.modelo.Producto;
import com.servicio.CarritoServicio;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CarritoServicioTest {

    @Test
    public void agregarProductoIncrementaTotal() {
        CarritoServicio s = new CarritoServicio();

        Producto p = new Producto(1, "123", "Leche", 4.50);

        s.agregarProducto(p);
        assertEquals(4.50, s.obtenerTotal(), "El total debe ser igual al precio del único producto agregado");
    }
}