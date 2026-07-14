/*
 * Utilidades de validación de formato reutilizables en toda la aplicación
 * (Back-Office y Front-Office). Centralizamos aquí las reglas para no repetir
 * la misma expresión regular en cada controlador.
 */
package com.servicio;

import java.util.regex.Pattern;

public class ValidacionFormatos {

    // Exige: algo antes de la "@", la "@", y algo después (con al menos un punto,
    // p. ej. dominio.com). Esto garantiza que la "@" esté presente y bien ubicada.
    private static final Pattern PATRON_CORREO =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private ValidacionFormatos() {
        // Clase utilitaria: no se instancia.
    }

    /**
     * Valida que el correo tenga al menos el formato mínimo "algo@algo.algo".
     * Rechaza null, vacíos, y cualquier texto sin "@" bien ubicada.
     */
    public static boolean validarCorreo(String correo) {
        if (correo == null) return false;
        String valor = correo.trim();
        if (valor.isEmpty()) return false;
        return PATRON_CORREO.matcher(valor).matches();
    }
}