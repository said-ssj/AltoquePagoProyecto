package com.dao;

import com.DB.ConexionDB;
import java.sql.*;

public class DetalleVentaDAO {

    public void guardarDetalle(
            int idVenta,
            int idProducto,
            int cantidad,
            double precioUnitario,
            double subtotal
    ) {
        try {
            Connection cn = ConexionDB.conectar();
            String sql = "INSERT INTO detalle_venta(" +
                    "id_venta,id_producto,cantidad," +
                    "precio_unitario,subtotal) " +
                    "VALUES(?,?,?,?,?)";
            PreparedStatement ps = cn.prepareStatement(sql);
            ps.setInt(1, idVenta);
            ps.setInt(2, idProducto);
            ps.setInt(3, cantidad);
            ps.setDouble(4, precioUnitario);
            ps.setDouble(5, subtotal);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}