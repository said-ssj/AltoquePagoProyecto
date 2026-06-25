package com.servicio;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ApiSunatServicioTest {

    @Test
    @DisplayName("Prueba de Integración: Consultar un DNI válido a la API externa")
    public void testConsultarDniValido() {
        // Usamos un DNI de prueba
        String dniPrueba = "61087816";
        // Ejecutar
        JsonObject resultado = ApiSunatServicio.consultarDocumento(dniPrueba);
        // Verificamos que la API pueda conectar
        assertNotNull(resultado, "La respuesta de la API no debe ser nula.");
        // Verificamos que el JSON de respuesta contenga el campo de nombre que espera nuestro sistema
        assertTrue(resultado.has("nombre_completo"), "El JSON debe contener la propiedad 'nombre_completo'");
        // Imprimimos en consola para la demostración
        System.out.println("Integración DNI exitosa. Cliente encontrado: " + resultado.get("nombre_completo").getAsString());
    }

    @Test
    @DisplayName("Prueba de Integración: Consultar un RUC válido a la API externa")
    public void testConsultarRucValido() {
        // Usamos un RUC público (Ejem. 20100047218 del BCP)
        String rucPrueba = "20100047218";
        // Ejecutar
        JsonObject resultado = ApiSunatServicio.consultarDocumento(rucPrueba);
        // Comprobaamos
        assertNotNull(resultado, "La conexión a la API de RUC falló.");
        assertTrue(resultado.has("nombre_o_razon_social"), "El JSON debe contener 'nombre_o_razon_social'");
        assertTrue(resultado.has("estado"), "El JSON debe contener el 'estado' del RUC");
        //Mensaje de en cosola, prueba exitosa
        System.out.println("Integración RUC exitosa. Empresa: " + resultado.get("nombre_o_razon_social").getAsString());
    }

    @Test
    @DisplayName("Prueba de Integración: Comportamiento ante un documento de longitud inválida")
    public void testConsultarDocumentoInvalido() {
        // Introducimos un dato con 5 digitos ya que no es ni DNI ni RUC
        String documentoInvalido = "12345";
        // Ejecutar
        JsonObject resultado = ApiSunatServicio.consultarDocumento(documentoInvalido);
        // Nuestro método debe bloquearlo y devolver null antes de hacer la petición HTTP
        assertNull(resultado, "Este Mensaje no se Vera, El Sistema debe rechazar documentos que no tengan 8 u 11 dígitos devolviendo null.");
    }
}