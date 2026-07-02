package com.pruebasUnitarias.servicio;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

// Se simula un validador estático o de servicio. Reemplazar por tu clase real de utilitarios si existe.
public class ValidacionFormatosTest {

    public boolean validarDNI(String dni) {
        return dni != null && dni.matches("\\d{8}");
    }

    public boolean validarCorreo(String correo) {
        return correo != null && correo.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    public boolean validarPrecio(String precioStr) {
        try {
            double precio = Double.parseDouble(precioStr);
            return precio >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Test
    @DisplayName("El DNI debe tener exactamente 8 dígitos numéricos")
    public void testValidarDNI() {
        assertTrue(validarDNI("74589632"));
        assertFalse(validarDNI("12345"));
        assertFalse(validarDNI("7458963A"));
    }

    @Test
    @DisplayName("El formato del correo electrónico debe ser válido")
    public void testValidarCorreo() {
        assertTrue(validarCorreo("cliente@gmail.com"));
        assertFalse(validarCorreo("cliente.com"));
    }

    @Test
    @DisplayName("El campo precio no debe aceptar letras ni números negativos")
    public void testValidarPrecio() {
        assertTrue(validarPrecio("15.50"));
        assertFalse(validarPrecio("15.50a"));
        assertFalse(validarPrecio("-4.50"));
    }
}