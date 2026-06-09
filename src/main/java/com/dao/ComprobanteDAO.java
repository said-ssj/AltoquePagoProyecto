package com.dao;

import com.DB.ConexionDB;
import java.sql.*;

public class ComprobanteDAO {
    public void guardarComprobante(int idVenta, String tipo) {

        String sql = "INSERT INTO comprobante(id_venta, tipo) VALUES (?, ?)";

        try (Connection cn = ConexionDB.conectar();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, idVenta);
            ps.setString(2, tipo);

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}