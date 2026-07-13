/*
 * Implementamos de forma exclusiva la configuración y estructura requerida para consultar
 * documentos de tipo DNI de 8 dígitos ante la API externa, aislando sus parámetros según SRP.
 */
package com.servicio;

import com.google.gson.JsonObject;

public class ConsultaDniEstrategia implements IEstrategiaConsulta {

    @Override
    public boolean soporta(String numero) {
        return numero != null && numero.length() == 8;
    }

    @Override
    public String obtenerUrl() {
        return "https://api.json.pe/api/dni";
    }

    @Override
    public String construirJsonBody(String numero) {
        return "{\"dni\": \"" + numero + "\"}";
    }
}