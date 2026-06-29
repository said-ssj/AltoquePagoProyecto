package com;

import com.modelo.Producto;
import org.junit.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TestProducto {

    @Test
    public void testCreacionProducto() {
        Producto pan = new Producto(1, "7751234567890", "Pan de Molde Bimbo", 8.50,1);

        assertEquals(1, pan.getId_producto(), "El ID_producto debe ser 1");
        assertEquals("7751234567890", pan.getCodigo_barras(), "El código de barras debe coincidir");
        assertEquals("Pan de Molde Bimbo", pan.getNombre(), "El nombre debe coincidir");
        assertEquals(8.50, pan.getPrecio(), "El precio debe ser 8.50");
    }

    @Test
    public void testModificacionDatosProducto() {
        Producto leche = new Producto(2, "7750236173896", "Leche Gloria", 4.20,1);

        leche.setPrecio(4.50);

        assertEquals(4.50, leche.getPrecio(), "El precio debería haberse actualizado a 4.50");
    }
}