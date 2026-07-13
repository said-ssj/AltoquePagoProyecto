/*
 * Coordinamos las consultas de documentos de identidad contra la API externa de json.pe.
 * Hemos aplicado el Principio Abierto/Cerrado (OCP) delegando la resolución de URLs y cuerpos
 * JSON en un arreglo de estrategias abstractas, lo que nos permite dar soporte a nuevos
 * documentos de forma totalmente extensible y sin alterar esta clase.
 */
package com.servicio;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Properties;

/**
 * Servicio para consultar DNI y RUC contra la API de json.pe.
 *
 * SEGURIDAD [SEC-02]: El token de API ya no está hardcodeado en el código.
 * Se lee desde el archivo externo "api.properties" (excluido de Git).
 * Si el archivo no existe, el servicio falla de forma segura retornando null.
 */

public class ApiSunatServicio {

    private static final String TOKEN = cargarToken();

    // Declaramos las estrategias de consulta soportadas por el sistema
    private static final IEstrategiaConsulta[] ESTRATEGIAS = {
            new ConsultaDniEstrategia(),
            new ConsultaRucEstrategia()
    };

    private static String cargarToken() {
        try (InputStream input = ApiSunatServicio.class.getClassLoader()
                .getResourceAsStream("api.properties")) {
            if (input == null) {
                System.err.println("[ApiSunatServicio] AVISO: api.properties no encontrado. " +
                        "La consulta SUNAT/RENIEC no estará disponible.");
                return null;
            }
            Properties props = new Properties();
            props.load(input);
            return props.getProperty("sunat.token");
        } catch (Exception e) {
            System.err.println("[ApiSunatServicio] Error al leer el token de API: " + e.getMessage());
            return null;
        }
    }

    public static JsonObject consultarDocumento(String numero) {
        if (TOKEN == null || TOKEN.isBlank()) {
            System.err.println("[ApiSunatServicio] Token no configurado. Consulta cancelada.");
            return null;
        }

        // Buscamos de forma dinámica la estrategia que se adecúe al formato del documento
        IEstrategiaConsulta estrategiaSeleccionada = null;
        for (IEstrategiaConsulta estrategia : ESTRATEGIAS) {
            if (estrategia.soporta(numero)) {
                estrategiaSeleccionada = estrategia;
                break;
            }
        }

        // Si ninguna estrategia soporta el formato, rechazamos la petición inmediatamente
        if (estrategiaSeleccionada == null) {
            return null;
        }

        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(estrategiaSeleccionada.obtenerUrl()))
                    .header("Authorization", "Bearer " + TOKEN)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(estrategiaSeleccionada.construirJsonBody(numero)))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();
                if (jsonResponse.has("success") && jsonResponse.get("success").getAsBoolean()) {
                    return jsonResponse.getAsJsonObject("data");
                } else {
                    System.out.println("Error desde API: " + jsonResponse.get("message").getAsString());
                }
            } else {
                System.out.println("Error de servidor API: Código " + response.statusCode());
            }
        } catch (Exception e) {
            System.err.println("Error de conexión con la API: " + e.getMessage());
        }
        return null;
    }
}
