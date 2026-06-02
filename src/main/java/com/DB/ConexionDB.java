package com.DB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionDB {
    private static final String URL = "jdbc:mysql://localhost:3306/Autoservicio";

    // PONER TU USUARIO DE MySQL
    private static final String USER = "root";
    // PONER TU CONTRASEÑA DE MySQL
    private static final String PASSWORD = "123456789";


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

    public static void main(String[] args) {
        conectar();
    }

}