package com.DB;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class ConexionDB {

        private static String url;
        private static String user;
        private static String password;

        static {
            try {
                Properties prop = new Properties();
                InputStream input = ConexionDB.class.getClassLoader().getResourceAsStream("db.properties");
                prop.load(input);
                url = prop.getProperty("db.url");
                user = prop.getProperty("db.user");
                password = prop.getProperty("db.password");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public static Connection conectar() {
            try {
                return DriverManager.getConnection(
                        url,
                        user,
                        password
                );

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }