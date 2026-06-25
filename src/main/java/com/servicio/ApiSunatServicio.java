package com.servicio;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class ApiSunatServicio {

    // TOKEN GENERADO EN JSON.PE
    private static final String TOKEN = "5db222f8fa1e10b3a8f89ff40fd49524d9e6b583ea7237d38b655e037832";

    public static JsonObject consultarDocumento(String numero) {
        String url = "";
        String jsonBody = "";

        // Determinamos si es DNI o RUC y armamos el body que exige json.pe
        if (numero.length() == 8) {
            url = "https://api.json.pe/api/dni";
            jsonBody = "{\"dni\": \"" + numero + "\"}";
        } else if (numero.length() == 11) {
            url = "https://api.json.pe/api/ruc";
            jsonBody = "{\"ruc\": \"" + numero + "\"}";
        } else {
            return null; // Documento inválido
        }

        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + TOKEN)
                    .header("Content-Type", "application/json") // Exigido por json.pe
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody)) // Cambiado a POST
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();

                // La API de json.pe responde con { "success": true, "data": { ... } }
                // Validamos que haya tenido éxito
                if (jsonResponse.has("success") && jsonResponse.get("success").getAsBoolean()) {
                    // Devolvemos directamente el bloque "data", que es el que contiene los nombres
                    return jsonResponse.getAsJsonObject("data");
                } else {
                    System.out.println("Error desde API: " + jsonResponse.get("message").getAsString());
                }
            } else {
                System.out.println("Error de servidor API: Código " + response.statusCode());
                System.out.println("Detalle: " + response.body());
            }
        } catch (Exception e) {
            System.err.println("Error de conexión con la API: " + e.getMessage());
        }
        return null;
    }
}