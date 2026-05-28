package com.DB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionDB {
    private static final String URL = "jdbc:mysql://localhost:3306/Autoservicio";

    private static final String USER = "root";

    private static final String PASSWORD = "root1234";

    public static Connection conectar() {
        try {Connection cn = DriverManager.getConnection(
                            URL,
                            USER,
                            PASSWORD);
            System.out.println("Conexión exitosa");

            return cn;

        } catch (SQLException e) {
            System.out.println("Error conexión: " + e.getMessage()
            );
            return null;
        }
    }
}