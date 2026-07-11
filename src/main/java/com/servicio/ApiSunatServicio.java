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

    // Token leído una sola vez al cargar la clase
    private static final String TOKEN = cargarToken();

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

        String url;
        String jsonBody;

        if (numero.length() == 8) {
            url = "https://api.json.pe/api/dni";
            jsonBody = "{\"dni\": \"" + numero + "\"}";
        } else if (numero.length() == 11) {
            url = "https://api.json.pe/api/ruc";
            jsonBody = "{\"ruc\": \"" + numero + "\"}";
        } else {
            return null;
        }

        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + TOKEN)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
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
