package com.pruebasUnitarias.servicio;

import com.modelo.Producto;
import com.servicio.CarritoServicio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CarritoServicioTest {

    private CarritoServicio carritoServicio;

    @BeforeEach
    public void setUp() {
        carritoServicio = new CarritoServicio();
    }

    @Test
    @DisplayName("Agregar un producto incrementa el total correctamente")
    public void agregarProductoIncrementaTotal() {
        Producto p = new Producto(1, "123", "Leche", 4.50, 10);
        carritoServicio.agregarProducto(p);
        assertEquals(4.50, carritoServicio.obtenerTotal(), "El total debe ser igual al precio del único producto agregado");
    }

    @Test
    @DisplayName("Agregar un producto repetido debe incrementar la cantidad en vez de duplicar la fila")
    public void testAgregarProductoRepetidoIncrementaCantidad() {
        Producto p1 = new Producto(1, "123", "Leche", 4.50, 10);
        Producto p2 = new Producto(1, "123", "Leche", 4.50, 10);

        carritoServicio.agregarProducto(p1);
        carritoServicio.agregarProducto(p2);

        assertEquals(1, carritoServicio.obtenerItems().size(), "El carrito solo debe contener un ítem único");
    }

    @Test
    @DisplayName("Reducir la cantidad a cero o negativo debe lanzar una excepción")
    public void testReducirCantidadNegativaLanzaExcepcion() {
        Producto p = new Producto(1, "123", "Leche", 4.50, 10);
        carritoServicio.agregarProducto(p);

        assertThrows(IllegalArgumentException.class, () -> {
            carritoServicio.modificarCantidad(p, -1);
        }, "Debería lanzar IllegalArgumentException si la cantidad es menor o igual a cero");
    }
}