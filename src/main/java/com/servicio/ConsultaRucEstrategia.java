/*
 * Implementamos de forma exclusiva la configuración y estructura requerida para consultar
 * documentos de tipo RUC de 11 dígitos ante la API externa, aislando sus parámetros según SRP.
 */
package com.servicio;

import com.google.gson.JsonObject;

public class ConsultaRucEstrategia implements IEstrategiaConsulta {

    @Override
    public boolean soporta(String numero) {
        return numero != null && numero.length() == 11;
    }

    @Override
    public String obtenerUrl() {
        return "https://api.json.pe/api/ruc";
    }

    @Override
    public String construirJsonBody(String numero) {
        return "{\"ruc\": \"" + numero + "\"}";
    }
}