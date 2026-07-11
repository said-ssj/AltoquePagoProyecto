package com.pruebasIntegracion.dao;

import com.DB.ConexionDB;
import com.dao.ProductoDAO;
import com.modelo.Producto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

public class ProductoDAOIT {

    private ProductoDAO productoDAO;
    private Connection conexionReal;

    @BeforeEach
    public void setUp() throws SQLException {
        productoDAO = new ProductoDAO();

        // Abrimos la conexión física directa hacia nuestra base de datos local
        conexionReal = ConexionDB.conectar();
        assertNotNull(conexionReal, "No pudimos establecer la conexión. Revisemos MySQL y el db.properties.");

        // Desactivamos el autocommit para controlar la transacción de forma temporal
        conexionReal.setAutoCommit(false);
    }

    @AfterEach
    public void tearDown() throws SQLException {
        if (conexionReal != null && !conexionReal.isClosed()) {
            // Ejecutamos el rollback para asegurar que no ensuciamos las tablas de productos reales
            conexionReal.rollback();
            conexionReal.close();
        }
    }

    @Test
    @DisplayName("guardarProducto debería registrar un artículo de forma real en MySQL y retornar true")
    public void testGuardarProductoExitoso() throws SQLException {
        // 1. Generamos un código de barras dinámico de 13 dígitos usando el tiempo actual
        String codigoDinamico = String.valueOf(System.currentTimeMillis());

        // 2. Inicializamos un producto de prueba
        Producto p = new Producto();
        p.setCodigo_barras(codigoDinamico);
        p.setNombre("Gaseosa Inka Kola 3L (Prueba)");
        p.setPrecio(12.50);
        p.setStock(24);

        // 3. Ejecutamos la inserción mediante el DAO
        boolean resultado = productoDAO.guardarProducto(p);

        // 4. Validamos que el método devuelva verdadero confirmando la operación
        assertTrue(resultado, "El DAO nos debería retornar true si la sintaxis del INSERT es correcta.");

        // 5. Realizamos una consulta de verificación directa en la base de datos usando el código dinámico
        String sqlVerificar = "SELECT COUNT(*), precio FROM producto WHERE codigo_barras = ?";
        try (PreparedStatement ps = conexionReal.prepareStatement(sqlVerificar)) {
            ps.setString(1, codigoDinamico);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int cantidad = rs.getInt(1);
                    double precioGuardado = rs.getDouble("precio");

                    // Comprobamos que el registro exista temporalmente y que el precio no haya perdido decimales
                    assertEquals(1, cantidad, "Deberíamos encontrar exactamente 1 producto insertado.");
                    assertEquals(12.50, precioGuardado, 0.001, "El precio guardado no coincide con el asignado.");
                }
            }
        }
    }

    @Test
    @DisplayName("buscarPorCodigo debería retornar null si consultamos un código de barras que no existe")
    public void testBuscarProductoInexistente() {
        // Solicitamos al DAO un código ficticio que sabemos que no está en las góndolas
        Producto resultado = productoDAO.buscarPorCodigo("0000000000000");

        // Evaluamos que el sistema maneje la ausencia del producto devolviendo un valor nulo
        assertNull(resultado, "Esperamos recibir null al buscar un artículo que no existe en el catálogo.");
    }
}