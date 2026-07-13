/*
 * Definimos el contrato para las operaciones de persistencia de la entidad Cliente.
 * Al aislar estas firmas en una interfaz, permitimos que las capas superiores utilicen
 * los métodos de negocio sin quedar amarradas a una implementación directa de base de datos.
 */
package com.dao;

import com.modelo.Cliente;

public interface IClienteDAO {
    int guardarCliente(Cliente c);
    Cliente buscarPorDocumento(String documento);
    int guardarOActualizarCliente(Cliente c);
}