/*
 * Definimos el contrato formal para el registro e historial de movimientos del Kardex en el almacén.
 * Con esta abstracción impedimos que la visualización física del inventario dependa de un motor de base
 * de datos acoplado, garantizando la flexibilidad requerida para pruebas unitarias.
 */
package com.dao;

import com.modelo.MovimientoInventario;
import java.util.List;

public interface IMovimientoInventarioDAO {
    boolean registrarMovimiento(MovimientoInventario mov);
    List<MovimientoInventario> listarPorProducto(int idProducto);
}