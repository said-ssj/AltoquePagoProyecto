/*
 * Definimos el contrato base para las estrategias de consulta de documentos de identidad.
 * Al aislar el comportamiento en este contrato, permitimos la incorporación de nuevos
 * tipos de documentos en el futuro sin alterar la estructura del servicio principal (OCP).
 */
package com.servicio;

import com.google.gson.JsonObject;

public interface IEstrategiaConsulta {
    boolean soporta(String numero);
    String obtenerUrl();
    String construirJsonBody(String numero);
}