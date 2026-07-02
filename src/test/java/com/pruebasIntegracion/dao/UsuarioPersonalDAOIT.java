package com.pruebasIntegracion.dao;

import com.DB.ConexionDB;
import com.dao.UsuarioPersonalDAO;
import com.modelo.UsuarioPersonal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

public class UsuarioPersonalDAOIT {

    private UsuarioPersonalDAO usuarioDAO;
    private Connection conexionReal;

    @BeforeEach
    public void setUp() throws SQLException {
        usuarioDAO = new UsuarioPersonalDAO();

        // 1. Abrimos una conexión física real a tu base de datos configurada en db.properties
        conexionReal = ConexionDB.conectar();
        assertNotNull(conexionReal, "La conexión a la base de datos es nula. Verifica que MySQL esté corriendo y db.properties sea correcto.");

        // 2. DESACTIVAMOS el autocommit. Todo lo que haga el DAO a partir de ahora será temporal.
        conexionReal.setAutoCommit(false);
    }

    @AfterEach
    public void tearDown() throws SQLException {
        if (conexionReal != null && !conexionReal.isClosed()) {
            // 3. ¡ESTRATEGIA CRÍTICA!: Forzamos la marcha atrás (Rollback).
            // Cualquier fila insertada por el DAO se esfuma de MySQL en este preciso instante.
            conexionReal.rollback();
            conexionReal.close();
        }
    }

    @Test
    @DisplayName("guardarUsuario debería insertar físicamente un registro en MySQL y retornar true")
    public void testGuardarUsuarioEnBaseDatosReal() throws SQLException {
        // 1. Arrange: Creamos un correo único en cada ejecución (ej. test_1712493021@altoquepago.com)
        String correoDinamicoUnico = "test_" + System.currentTimeMillis() + "@altoquepago.com";

        UsuarioPersonal u = new UsuarioPersonal();
        u.setNombre("Empleado Integracion");
        u.setEmail(correoDinamicoUnico); // Asignamos el correo que jamás se repetirá
        u.setContraseña("hash_simulado_2026");
        u.setIdRol(1); // Asegúrate de tener al menos el id_rol 1 insertado en tu tabla 'roles'
        u.setFechaNacimiento("2000-01-01");
        u.setTipoDocumento("DNI");
        u.setNumeroDocumento("99999999");
        u.setNacionalidad("Peruana");
        u.setDireccion("Ica, Ica");
        u.setTelefono("987654321");
        u.setTelefonoEmergencia("912345678");
        u.setArea("Sistemas");
        u.setTipoContrato("Completo");
        u.setFechaInicio("2026-01-01");
        u.setSalarioBase(1200.0);
        u.setMetodoPago("Yape");
        u.setDatosBancarios("No aplica");
        u.setAntecedentes("Ninguno");

        // 2. Act: Ejecutamos el DAO normal
        boolean resultado = usuarioDAO.guardarUsuario(u);

        // 3. Assert: Verificamos que se insertó
        assertTrue(resultado, "El DAO debería retornar true si la sintaxis SQL y los tipos de datos son aceptados por MySQL");

        // 4. Verificación cruzada: Buscamos el correo específico que acabamos de inventar
        String sqlVerificar = "SELECT COUNT(*) FROM usuario_personal WHERE email = ?";
        try (PreparedStatement ps = conexionReal.prepareStatement(sqlVerificar)) {
            ps.setString(1, correoDinamicoUnico);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int cantidad = rs.getInt(1);
                    assertEquals(1, cantidad, "Debería existir exactamente 1 registro con el correo dinámico en la base de datos.");
                }
            }
        }
    }

    @Test
    @DisplayName("autenticarUsuario debería retornar null si el email buscado no existe físicamente en MySQL")
    public void testAutenticarUsuarioInexistenteEnBaseDatosReal() {
        // Act
        // Buscamos un correo que sabemos que no existe en tu minimarket
        UsuarioPersonal resultado = usuarioDAO.autenticarUsuario("correo_fantasma_999@altoquepago.com", "clave");

        // Assert
        assertNull(resultado, "Debería retornar null de manera limpia si las credenciales no existen en las tablas");
    }
}