package com.DB;

import org.flywaydb.core.Flyway;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;
import java.util.HashMap;
import java.util.Map;
import java.io.InputStream;
import java.util.Properties;

public class ConexionDB {
    private ConexionDB() {
        throw new IllegalStateException("Clase de utilidad - No debe ser instanciada");
    }

    public static Connection conectar() {
        Connection conexion = null;
        try (InputStream input = ConexionDB.class.getClassLoader().getResourceAsStream("db.properties")) {
            Properties prop = new Properties();
            if (input == null) {
                System.out.println("No se encontró db.properties en el classpath");
                return null;
            }

            prop.load(input);

            String url = prop.getProperty("db.url");
            String usuario = prop.getProperty("db.user");
            String clave = prop.getProperty("db.password");

            // Validación de seguridad para que no intente conectar si las propiedades están vacías
            if (url == null || usuario == null) {
                System.err.println("Error: Las credenciales en db.properties son inválidas o nulas.");
                return null;
            }

            Class.forName("com.mysql.cj.jdbc.Driver");
            conexion = DriverManager.getConnection(url, usuario, clave);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return conexion;
    }

    public static void ejecutarMigraciones() {
        try (InputStream input = ConexionDB.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (input == null) {
                System.err.println("No se encontró db.properties");
                return;
            }

            Properties prop = new Properties();
            prop.load(input);

            String url = prop.getProperty("db.url");
            String usuario = prop.getProperty("db.user");
            String clave = prop.getProperty("db.password");

            if (url == null || url.trim().isEmpty()) return;

            Flyway flyway = Flyway.configure()
                    .dataSource(url, usuario, clave)
                    .locations("filesystem:src/main/resources/db/migration")
                    .baselineOnMigrate(true)
                    .load();

            flyway.migrate();
            System.out.println("¡Migración de base de datos completada con éxito!");

        } catch (Exception e) {
            System.err.println("Error al migrar: " + e.getMessage());
        }
    }
}