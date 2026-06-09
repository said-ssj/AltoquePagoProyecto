package com.dao;
import com.DB.ConexionDB;
import com.modelo.Cliente;
import java.sql.*;
public class ClienteDAO {

    public int guardarCliente(Cliente c) {

        int idGenerado = -1;

        String sql = "INSERT INTO cliente(nombre, telefono, email) VALUES (?, ?, ?)";

        try (Connection con = ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, c.getNombre());
            ps.setString(2, c.getTelefono());
            ps.setString(3, c.getEmail());

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