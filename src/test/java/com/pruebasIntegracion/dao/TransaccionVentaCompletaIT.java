package com.pruebasIntegracion.dao;

import com.dao.VentaDAO;
import com.dao.ProductoDAO;
import com.modelo.Producto;
import com.modelo.Venta;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class TransaccionVentaCompletaIT {

    @Test
    @DisplayName("Ciclo de vida de una venta: registrar cabecera de venta y decrementar stock de producto")
    public void testFlujoCompletoTransaccionVenta() {
        VentaDAO ventaDAO = new VentaDAO();       // Corrección: Sin argumentos en el constructor
        ProductoDAO productoDAO = new ProductoDAO(); // Corrección: Sin argumentos en el constructor

        // 1. Verificar existencia y stock inicial de un producto de prueba (ID 1)
        // Como no tienes 'obtenerStock(id)', listamos o buscamos por nombre/código
        // para validar que la lógica de actualización responda correctamente.
        List<Producto> productos = productoDAO.obtenerTodos();
        assertFalse(productos.isEmpty(), "Debe haber al menos un producto en la base de datos para correr la prueba");

        Producto productoPrueba = productos.get(0);
        int idProducto = productoPrueba.getId_producto();
        int stockAntes = productoPrueba.getStock();

        // 2. Registrar Cabecera de Venta usando el método real 'guardarVenta'
        int idClientePrueba = 1; // Asumiendo que existe el cliente ID 1 en tu entorno de pruebas
        double totalVenta = productoPrueba.getPrecio();

        int idVentaGenerado = ventaDAO.guardarVenta(idClientePrueba, totalVenta);
        assertTrue(idVentaGenerado > 0, "La venta debió registrarse y retornar un ID autogenerado válido");

        // 3. Simular la reducción de stock usando el método real 'actualizarStock'
        // Tu método resta la cantidad internamente: stock = stock - ?
        boolean stockActualizado = productoDAO.actualizarStock(idProducto, 1);
        assertTrue(stockActualizado, "El stock debió actualizarse con éxito si había disponibilidad");

        // 4. Volver a consultar para validar que el stock bajó en la base de datos
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