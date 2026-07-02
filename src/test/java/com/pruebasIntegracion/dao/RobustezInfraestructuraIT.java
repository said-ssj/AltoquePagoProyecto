package com.pruebasIntegracion.dao;

import com.DB.ConexionDB;
import com.dao.ProductoDAO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.SQLException;
import static org.junit.jupiter.api.Assertions.*;

public class RobustezInfraestructuraIT {

    @Test
    @DisplayName("El sistema debe manejar una simulación de conexión cerrada controladamente")
    public void testFalloDeConexionNoProvocaCrash() throws SQLException {
        Connection conexionSimulada = ConexionDB.conectar(); // Corrección: Usar conectar()
        assertNotNull(conexionSimulada, "La conexión inicial no debería ser nula si db.properties es correcto");

        conexionSimulada.close();

        // Verificamos que intentar operar sobre una conexión cerrada dispare la excepción esperada
        assertThrows(SQLException.class, () -> {
            conexionSimulada.prepareStatement("SELECT * FROM producto").executeQuery();
        }, "Operar en una conexión cerrada de MySQL debe lanzar SQLException");
    }
}