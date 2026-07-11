package com.pruebasIntegracion.dao;

import com.dao.VentaDAO;
import com.dao.ProductoDAO;
import com.modelo.Producto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class TransaccionVentaCompletaIT {

    @Test
    @DisplayName("Ciclo de vida de una venta: registrar cabecera de venta y decrementar stock de producto")
    public void testFlujoCompletoTransaccionVenta() {
        VentaDAO ventaDAO = new VentaDAO();
        ProductoDAO productoDAO = new ProductoDAO();

        // 1. Obtener todos los productos
        List<Producto> productos = productoDAO.obtenerTodos();
        assertFalse(productos.isEmpty(), "Debe haber al menos un producto en la BD para correr la prueba.");

        // 2. BUSCAMOS UN PRODUCTO QUE TENGA STOCK MAYOR A 0
        Producto productoPrueba = null;
        for (Producto p : productos) {
            if (p.getStock() > 0) {
                productoPrueba = p;
                break; // Encontramos uno con stock, salimos del bucle
            }
        }

        // Si recorrió todos y ninguno tenía stock, abortamos la prueba con un mensaje claro
        assertNotNull(productoPrueba, "Para simular una venta, necesitas tener al menos un producto en MySQL con stock mayor a cero.");

        int idProducto = productoPrueba.getId_producto();
        int stockAntes = productoPrueba.getStock();

        // 3. Registrar Cabecera de Venta
        int idClientePrueba = 1; // Asegúrate de tener al cliente ID 1
        double totalVenta = productoPrueba.getPrecio();

        int idVentaGenerado = ventaDAO.guardarVenta(idClientePrueba, totalVenta);
        assertTrue(idVentaGenerado > 0, "La venta debió registrarse y retornar un ID autogenerado válido");

        // 4. Simular la reducción de stock
        boolean stockActualizado = productoDAO.actualizarStock(idProducto, 1);
        assertTrue(stockActualizado, "El stock debió actualizarse con éxito porque verificamos que había disponibilidad.");

        // 5. Volver a consultar para validar que el stock bajó en la base de datos
        List<Producto> productosPostVenta = productoDAO.obtenerTodos();
        int stockDespues = 0;
        for (Producto p : productosPostVenta) {
            if (p.getId_producto() == idProducto) {
                stockDespues = p.getStock();
                break;
            }
        }

        assertEquals(stockAntes - 1, stockDespues, "El stock en la base de datos debió disminuir exactamente en 1");
    }
}