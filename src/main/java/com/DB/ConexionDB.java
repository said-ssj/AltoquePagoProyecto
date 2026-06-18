package com.DB;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.util.Properties;

public class ConexionDB {
    public static Connection conectar() {
        Connection conexion = null;
        try (InputStream input = ConexionDB.class.getClassLoader().getResourceAsStream("db.properties")) {
            Properties prop = new Properties();
            if (input == null) {
                System.out.println("No se encontro db.properties");
                return null;
            }
            prop.load(input);
            String url = prop.getProperty("db.url");
            String usuario = prop.getProperty("db.user");
            String clave = prop.getProperty("db.password");
            Class.forName("com.mysql.cj.jdbc.Driver");

            conexion = DriverManager.getConnection(url, usuario, clave);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return conexion;
    }
}