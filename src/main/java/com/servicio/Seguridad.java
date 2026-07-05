package com.servicio;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Servicio de seguridad para hash de contraseñas.
 *
 * SEGURIDAD [SEC-04]: Se reemplazó SHA-256 (sin sal, rápido, vulnerable a tablas
 * rainbow) por PBKDF2WithHmacSHA256 con sal aleatoria de 16 bytes y 310.000
 * iteraciones (estándar NIST SP 800-132 para 2023).
 *
 * Formato del hash almacenado: "ITERACIONES:SAL_BASE64:HASH_BASE64"
 * Ejemplo:  "310000:aBcDeFgH...:xYzWqR..."
 *
 * COMPATIBILIDAD CON CONTRASEÑAS ANTIGUAS (SHA-256):
 * El método {@link #verificarPassword(String, String)} detecta el formato antiguo
 * (cadena hexadecimal de 64 chars) y lo acepta una última vez, para que los
 * empleados existentes puedan seguir iniciando sesión. Al próximo login, la
 * contraseña debería ser re-hasheada con PBKDF2 (esto se implementaría en
 * UsuarioPersonalDAO.autenticarUsuario al detectar el formato legacy).
 */
public class Seguridad {

    private static final String ALGORITMO = "PBKDF2WithHmacSHA256";
    private static final int ITERACIONES  = 310_000;
    private static final int LONGITUD_KEY = 256; // bits
    private static final int LONGITUD_SAL = 16;  // bytes

    /**
     * Genera un hash seguro con sal aleatoria para la contraseña dada.
     * Formato de salida: "ITERACIONES:SAL_B64:HASH_B64"
     */
    public static String encriptarPassword(String password) {
        try {
            SecureRandom random = new SecureRandom();
            byte[] sal = new byte[LONGITUD_SAL];
            random.nextBytes(sal);

            byte[] hash = calcularHash(password, sal, ITERACIONES, LONGITUD_KEY);

            String salB64  = Base64.getEncoder().encodeToString(sal);
            String hashB64 = Base64.getEncoder().encodeToString(hash);

            return ITERACIONES + ":" + salB64 + ":" + hashB64;

        } catch (Exception e) {
            throw new RuntimeException("Error fatal al encriptar contraseña", e);
        }
    }

    /**
     * Verifica si una contraseña en texto plano coincide con el hash almacenado.
     * Soporta tanto el formato nuevo (PBKDF2) como el formato legacy (SHA-256 hex).
     *
     * @param passwordTextoPlano  Contraseña ingresada por el usuario
     * @param hashAlmacenado      Valor guardado en la base de datos
     * @return true si coinciden, false en caso contrario
     */
    public static boolean verificarPassword(String passwordTextoPlano, String hashAlmacenado) {
        if (hashAlmacenado == null || hashAlmacenado.isEmpty()) return false;

        // Detectar formato legacy SHA-256 (cadena hexadecimal de exactamente 64 caracteres)
        if (hashAlmacenado.matches("[0-9a-f]{64}")) {
            return hashLegacySHA256(passwordTextoPlano).equals(hashAlmacenado);
        }

        // Formato nuevo PBKDF2: "iteraciones:sal:hash"
        try {
            String[] partes = hashAlmacenado.split(":");
            if (partes.length != 3) return false;

            int iteraciones   = Integer.parseInt(partes[0]);
            byte[] sal        = Base64.getDecoder().decode(partes[1]);
            byte[] hashGuardado = Base64.getDecoder().decode(partes[2]);

            byte[] hashIntento = calcularHash(passwordTextoPlano, sal, iteraciones, LONGITUD_KEY);

            return slowEquals(hashGuardado, hashIntento);

        } catch (Exception e) {
            return false;
        }
    }

    // ---------------------------------------------------------------
    // Métodos privados de soporte
    // ---------------------------------------------------------------

    private static byte[] calcularHash(String password, byte[] sal, int iteraciones, int longitudBits)
            throws Exception {
        PBEKeySpec spec = new PBEKeySpec(
                password.toCharArray(), sal, iteraciones, longitudBits);
        SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITMO);
        byte[] hash = skf.generateSecret(spec).getEncoded();
        spec.clearPassword();
        return hash;
    }

    /** Comparación en tiempo constante para prevenir timing attacks. */
    private static boolean slowEquals(byte[] a, byte[] b) {
        if (a.length != b.length) return false;
        int diff = 0;
        for (int i = 0; i < a.length; i++) {
            diff |= a[i] ^ b[i];
        }
        return diff == 0;
    }

    /** Hash SHA-256 sin sal — SOLO para compatibilidad con cuentas antiguas. */
    private static String hashLegacySHA256(String password) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(2 * hash.length);
            for (byte b : hash) {
                String h = Integer.toHexString(0xff & b);
                if (h.length() == 1) hex.append('0');
                hex.append(h);
            }
            return hex.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
