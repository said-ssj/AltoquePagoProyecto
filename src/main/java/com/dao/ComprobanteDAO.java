package com.dao;

import com.DB.ConexionDB;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class ComprobanteDAO implements IComprobanteDAO {

    @Override
    public boolean generarComprobante(int idVenta, String tipoDoc, String numDoc) {
        String sql = "INSERT INTO comprobante (id_venta, tipo_documento, numero_documento, fecha_emision) VALUES (?, ?, ?, NOW())";

        try (Connection cn = ConexionDB.conectar();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, idVenta);
            ps.setString(2, tipoDoc);
            ps.setString(3, numDoc);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}