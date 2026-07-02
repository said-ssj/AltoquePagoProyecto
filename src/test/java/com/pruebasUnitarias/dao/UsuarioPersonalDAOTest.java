package com.pruebasUnitarias.dao;

import com.DB.ConexionDB;
import com.dao.UsuarioPersonalDAO;
import com.modelo.UsuarioPersonal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class UsuarioPersonalDAOTest {

    private UsuarioPersonalDAO usuarioDAO;

    // Mocks de las interfaces JDBC
    private Connection mockConnection;
    private PreparedStatement mockPreparedStatement;
    private ResultSet mockResultSet;

    // Almacenamos el mock estático para poder cerrarlo después de cada prueba
    private MockedStatic<ConexionDB> conexionDBMockedStatic;

    @BeforeEach
    public void setUp() {
        usuarioDAO = new UsuarioPersonalDAO();

        // Inicializamos los mocks individuales tradicionales
        mockConnection = mock(Connection.class);
        mockPreparedStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);

        // Mockeamos el método estático ConexionDB.conectar()
        conexionDBMockedStatic = mockStatic(ConexionDB.class);
    }

    @AfterEach
    public void tearDown() {
        // Es obligatorio cerrar los mocks estáticos para no saturar la memoria de JUnit
        conexionDBMockedStatic.close();
    }

    @Test
    @DisplayName("guardarUsuario debería retornar true cuando el insert en MySQL es exitoso")
    public void testGuardarUsuarioExitoso() throws Exception {
        // 1. Arreglar (Arrange)
        UsuarioPersonal u = new UsuarioPersonal();
        u.setNombre("Juan Perez");
        u.setEmail("juan@altoquepago.com");
        u.setContraseña("clavehash123");
        u.setIdRol(1);
        u.setSalarioBase(1500.0);

        // Simulamos la cadena de llamadas JDBC: ConexionDB.conectar() -> connection.prepareStatement() -> ps.executeUpdate()
        conexionDBMockedStatic.when(ConexionDB::conectar).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1); // 1 fila afectada = Éxito

        // 2. Actuar (Act)
        boolean resultado = usuarioDAO.guardarUsuario(u);

        // 3. Afirmar (Assert)
        assertTrue(resultado, "Debería retornar true si la inserción fue exitosa");
        verify(mockPreparedStatement, times(1)).executeUpdate();
    }

    @Test
    @DisplayName("autenticarUsuario debería retornar el objeto Usuario si el email y contraseña coinciden en BD")
    public void testAutenticarUsuarioExitoso() throws Exception {
        // 1. Arreglar (Arrange)
        String email = "admin@altoquepago.com";
        String passwordPlano = "admin123";

        conexionDBMockedStatic.when(ConexionDB::conectar).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        // Simulamos el comportamiento del ResultSet para un registro encontrado
        when(mockResultSet.next()).thenReturn(true); // Se encontró el usuario
        when(mockResultSet.getInt("id_usuario")).thenReturn(10);
        when(mockResultSet.getString("nombre")).thenReturn("Administrador");
        when(mockResultSet.getString("email")).thenReturn(email);
        when(mockResultSet.getString("contraseña")).thenReturn("8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918");
        when(mockResultSet.getByte("id_rol")).thenReturn((byte) 1);

        // 2. Actuar (Act)
        UsuarioPersonal usuarioResult = usuarioDAO.autenticarUsuario(email, passwordPlano);

        // 3. Afirmar (Assert)
        assertNotNull(usuarioResult, "El usuario logueado no debería ser nulo");
        assertEquals(10, usuarioResult.getIdUsuario());
        assertEquals("Administrador", usuarioResult.getNombre());
        assertEquals(email, usuarioResult.getEmail());
    }

    // ============================================================
    //  PRUEBAS ROBUSTAS / NEGATIVAS (Manejo de Fallas de BD)
    // ============================================================

    @Test
    @DisplayName("guardarUsuario debería retornar false si ConexionDB devuelve un objeto null (BD caída)")
    public void testGuardarUsuarioBaseDatosCaida() {
        // 1. Arreglar (Arrange)
        UsuarioPersonal u = new UsuarioPersonal();

        // Simulamos que la conexión retorna null de forma inesperada
        conexionDBMockedStatic.when(ConexionDB::conectar).thenReturn(null);

        // 2. Actuar (Act)
        boolean resultado = usuarioDAO.guardarUsuario(u);

        // 3. Afirmar (Assert)
        assertFalse(resultado, "Debería retornar false elegantemente si la conexión a la base de datos es nula");
    }

    @Test
    @DisplayName("autenticarUsuario debería retornar null si el email o clave no existen en el ResultSet")
    public void testAutenticarUsuarioCredencialesIncorrectas() throws Exception {
        // 1. Arreglar (Arrange)
        conexionDBMockedStatic.when(ConexionDB::conectar).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        // El ResultSet retorna false indicando que no hay coincidencias con ese email/hash
        when(mockResultSet.next()).thenReturn(false);

        // 2. Actuar (Act)
        UsuarioPersonal usuarioResult = usuarioDAO.autenticarUsuario("incorrecto@correo.com", "clavefalsa");

        // 3. Afirmar (Assert)
        assertNull(usuarioResult, "Debería retornar null indicando que las credenciales no existen");
    }
}