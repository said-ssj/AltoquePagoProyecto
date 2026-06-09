package com.dao;
import com.DB.ConexionDB;

import java.sql.*;

public class PagoDAO {

    public void guardarPago(int idVenta, String metodo, double monto, String estado) {

        String sql = "INSERT INTO pago(id_venta, metodo, monto, estado) VALUES (?, ?, ?, ?)";

        try (Connection con = ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idVenta);
            ps.setString(2, metodo);
            ps.setString(3, String.valueOf(monto)); // DECIMAL ok
            ps.setString(4, estado);

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}