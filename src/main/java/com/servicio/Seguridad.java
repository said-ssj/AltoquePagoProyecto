package com.servicio;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class Seguridad {

    // Método que convierte texto plano en un Hash SHA-256
    public static String encriptarPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));

            // Convertimos los bytes a formato Hexadecimal (que es lo que usa MySQL)
            StringBuilder hexString = new StringBuilder(2 * hash.length);
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();

        } catch (Exception e) {
            throw new RuntimeException("Error fatal al encriptar contraseña", e);
        }
    }
}