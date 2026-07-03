package com.pruebasIntegracion.dao;

import com.DB.ConexionDB;
import com.dao.ClienteDAO;
import com.modelo.Cliente;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

public class ClienteDAOIT {

    private ClienteDAO clienteDAO;
    private Connection conexionReal;

    @BeforeEach
    public void setUp() throws SQLException {
        clienteDAO = new ClienteDAO();
        conexionReal = ConexionDB.conectar();
        assertNotNull(conexionReal, "No pudimos establecer la conexión real. Revisemos MySQL y el archivo db.properties.");
        conexionReal.setAutoCommit(false);
    }

    @AfterEach
    public void tearDown() throws SQLException {
        if (conexionReal != null && !conexionReal.isClosed()) {
            conexionReal.rollback();
            conexionReal.close();
        }
    }

    @Test
    @DisplayName("guardarCliente debería insertar un cliente real en MySQL y retornar el ID auto-generado")
    public void testGuardarClienteExitoso() throws SQLException {
        // 1. Generamos un DNI simulado único de 8 dígitos basado en el tiempo actual
        String dniDinamico = String.valueOf(System.currentTimeMillis()).substring(5);

        // 2. Inicializamos un objeto cliente con datos ficticios válidos
        Cliente c = new Cliente();
        c.setNombre("Cliente Prueba Integracion");

        c.setTipoDocumento("D");

        c.setNumeroDocumento(dniDinamico);

        // 3. Capturamos el int que nos devuelve el método original (ID autogenerado)
        int idGenerado = clienteDAO.guardarCliente(c);

        // 4. Comprobamos que nos retorne un ID válido (mayor que 0)
        assertTrue(idGenerado > 0, "El DAO nos debería retornar un ID autogenerado mayor a 0 si el INSERT fue correcto.");

        // 5. Realizamos nuestra consulta de verificación cruzada
        String sqlVerificar = "SELECT COUNT(*) FROM cliente WHERE numero_documento = ?";
        try (PreparedStatement ps = conexionReal.prepareStatement(sqlVerificar)) {
            ps.setString(1, dniDinamico);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int cantidad = rs.getInt(1);
                    assertEquals(1, cantidad, "Deberíamos encontrar exactamente 1 cliente con el DNI dinámico.");
                }
            }
        }
    }
}