/*
 * Implementación del DAO para el detalle de la venta.
 * Se ha refactorizado para implementar la abstracción IDetalleVentaDAO
 * y se ha protegido el acceso a datos mediante bloques try-with-resources
 * para prevenir el agotamiento del pool de conexiones MySQL.
 */
package com.dao;

import com.DB.ConexionDB;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class DetalleVentaDAO implements IDetalleVentaDAO {

    @Override
    public boolean guardarDetalle(int idVenta,
                                  int idProducto,
                                  int cantidad,
                                  double precioUnitario,
                                  double subtotal)
    {
        String sql = "INSERT INTO detalle_venta(" +
                "id_venta, " +
                "id_producto, " +
                "cantidad, " +
                "precio_unitario, " +
                "subtotal) " +
                "VALUES(?, ?, ?, ?, ?)";

        // El try-with-resources garantiza que Connection y PreparedStatement se cierren siempre
        try (Connection cn = ConexionDB.conectar();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, idVenta);
            ps.setInt(2, idProducto);
            ps.setInt(3, cantidad);
            ps.setDouble(4, precioUnitario);
            ps.setDouble(5, subtotal);

            // Retorna true si se insertó al menos una fila
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false; // Retorna false para que el orquestador pueda hacer un Rollback si falla
        }
    }
}