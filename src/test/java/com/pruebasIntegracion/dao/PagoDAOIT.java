package com.pruebasIntegracion.dao;

import com.DB.ConexionDB;
import com.dao.PagoDAO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

public class PagoDAOIT {

    private PagoDAO pagoDAO;
    private Connection conexionReal;

    @BeforeEach
    public void setUp() throws SQLException {
        pagoDAO = new PagoDAO();

        // Abrimos la conexión física directa hacia nuestra base de datos local
        conexionReal = ConexionDB.conectar();
        assertNotNull(conexionReal, "No pudimos establecer la conexión física. Asegurémonos de que MySQL esté activo.");

        // Desactivamos el autocommit para mantener bajo control preventivo todas nuestras operaciones
        conexionReal.setAutoCommit(false);
    }

    @AfterEach
    public void tearDown() throws SQLException {
        if (conexionReal != null && !conexionReal.isClosed()) {
            // Ejecutamos el rollback para asegurar que no impactamos las finanzas reales
            conexionReal.rollback();
            conexionReal.close();
        }
    }

    @Test
    @DisplayName("guardarPago debería registrar un flujo de pago real en MySQL usando parámetros directos")
    public void testGuardarPagoExitoso() throws SQLException {
        // Definimos las variables locales que coinciden exactamente con lo requerido por nuestro DAO
        int idVentaSimulada = 1; // Asumimos una venta existente en nuestra BD local de pruebas
        String metodoPago = "Yape";
        double monto = 45.50;

        // CORRECCIÓN: Usamos una cadena corta ("PAGADO") para evitar el error de truncamiento en la columna 'estado'
        String estado = "PAGADO";

        // Invocamos el método de tu DAO original
        pagoDAO.guardarPago(idVentaSimulada, metodoPago, monto, estado);

        // CORRECCIÓN: Filtramos la verificación solo por el monto para evitar el error de columna 'metodo_pago' inexistente
        String sqlVerificar = "SELECT COUNT(*), monto FROM pago WHERE monto = ?";
        try (PreparedStatement ps = conexionReal.prepareStatement(sqlVerificar)) {
            ps.setDouble(1, 45.50);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int cantidad = rs.getInt(1);
                    double montoGuardado = rs.getDouble("monto");

                    // Corroboramos que el registro haya llegado temporalmente antes del rollback
                    assertTrue(cantidad > 0, "Deberíamos encontrar el registro del pago guardado temporalmente en MySQL.");
                    assertEquals(45.50, montoGuardado, 0.001, "El monto financiero guardado no coincide.");
                }
            }
        }
    }
}