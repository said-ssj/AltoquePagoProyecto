package com.dao;
import com.DB.ConexionDB;
import com.modelo.Cliente;
import java.sql.*;
public class ClienteDAO {

    public int guardarCliente(Cliente c) {
        int idGenerado = -1;

        // Ajustado para coincidir exactamente con tu nueva tabla MySQL
        String sql = "INSERT INTO cliente(nombre, apellido, razon_social, correo, numero_documento, " +
                "numero_ruc, telefono, direccion, ubigeo, tipo_documento, observacion) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, c.getNombre());
            ps.setString(2, c.getApellido());
            ps.setString(3, c.getRazonSocial());
            ps.setString(4, c.getCorreo());
            ps.setString(5, c.getNumeroDocumento());
            ps.setString(6, c.getNumeroRuc());
            ps.setString(7, c.getTelefono());
            ps.setString(8, c.getDireccion());
            ps.setString(9, c.getUbigeo());
            ps.setString(10, c.getTipoDocumento());
            ps.setString(11, c.getObservacion());

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                idGenerado = rs.getInt(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return idGenerado;
    }
}