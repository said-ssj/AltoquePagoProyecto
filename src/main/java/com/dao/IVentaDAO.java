/*
 * Definimos el contrato formal para todas las operaciones de persistencia de ventas del sistema.
 * Al aislar estas firmas en una interfaz, aseguramos que la capa de control administrativo pueda
 * listar y registrar transacciones sin depender de la infraestructura de MySQL, facilitando
 * el cumplimiento del Principio de Inversión de Dependencias (DIP) y la creación de pruebas unitarias.
 */
package com.dao;

import com.modelo.Venta;
import java.util.List;

public interface IVentaDAO {
    int guardarVenta(int idCliente, double total);
    List<Venta> listarVentas();
    List<String[]> listarClientes();
}