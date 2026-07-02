package com.pruebasUnitarias.servicio;

import com.servicio.Seguridad;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SeguridadTest {

    @Test
    @DisplayName("Debería encriptar correctamente una contraseña en formato SHA-256")
    public void testEncriptarPasswordExitoso() {
        // 1. Arreglar (Arrange)
        String passwordPlano = "admin123";
        // El hash SHA-256 conocido en hexadecimal para "admin123" es el siguiente:
        String hashEsperado = "240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9";
        // 2. Actuar (Act)
        String hashObtenido = Seguridad.encriptarPassword(passwordPlano);

        // 3. Afirmar (Assert)
        assertNotNull(hashObtenido, "El hash retornado no debería ser nulo");
        assertEquals(hashEsperado, hashObtenido, "El hash generado no coincide con el valor SHA-256 real");
    }

    @Test
    @DisplayName("Debería generar hashes idénticos para la misma entrada (Determinismo)")
    public void testEncriptarPasswordConsistencia() {
        String password = "MiClaveSegura2026";

        String primerHash = Seguridad.encriptarPassword(password);
        String segundoHash = Seguridad.encriptarPassword(password);

        assertEquals(primerHash, segundoHash, "El método de encriptación debe ser determinista (mismo texto, mismo hash)");
    }

    // ============================================================
    //  PRUEBAS ROBUSTAS / NEGATIVAS (Manejo de Errores)
    // ============================================================

    @Test
    @DisplayName("Debería lanzar RuntimeException cuando el parámetro es nulo (null)")
    public void testEncriptarPasswordConNull() {
        // Evaluamos que al pasar null, la lógica falle de manera controlada lanzando la excepción esperada
        RuntimeException excepcion = assertThrows(RuntimeException.class, () -> {
            Seguridad.encriptarPassword(null);
        }, "Se esperaba un RuntimeException debido a un parámetro nulo");

        // Verificamos que el mensaje de error personalizado de tu catch se mantenga
        assertTrue(excepcion.getMessage().contains("Error fatal al encriptar contraseña"),
                "El mensaje de la excepción no es el esperado");
    }

    @Test
    @DisplayName("Debería encriptar correctamente una cadena vacía sin romperse")
    public void testEncriptarPasswordCadenaVacia() {
        String passwordVacio = "";

        // El hash SHA-256 para un texto vacío "" es conocido universalmente:
        String hashVacioEsperado = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";

        String hashObtenido = Seguridad.encriptarPassword(passwordVacio);

        assertNotNull(hashObtenido);
        assertEquals(hashVacioEsperado, hashObtenido, "No se calculó correctamente el hash para una cadena vacía");
    }
}