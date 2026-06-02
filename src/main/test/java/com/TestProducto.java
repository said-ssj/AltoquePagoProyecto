package com.;

import com.modelo.Producto;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TestProducto {

    @Test
    public void testCreacionProducto() {
        Producto pan = new Producto("P001", "Pan de Molde", "Abarrotes", 8.50, 20);
        assertEquals("P001", pan.getId(), "El ID debe ser P001");
        assertEquals("Pan de Molde", pan.getNombre(), "El nombre debe coincidir");
        assertEquals(8.50, pan.getPrecio(), "El precio debe ser 8.50");
    }

    @Test
    public void testReduccionStock() {
        Producto leche = new Producto("P002", "Leche Gloria", "Lácteos", 4.20, 50);
        int nuevoStock = leche.getStock() - 1;
        leche.setStock(nuevoStock);

        assertEquals(49, leche.getStock(), "El stock debería reducirse a 49");
    }
}