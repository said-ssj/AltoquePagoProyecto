package com.dao;

import com.DB.ConexionDB;
import com.modelo.Oferta;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OfertaDAO {

    // 1. Listar todas las ofertas (Para llenar el TableView al iniciar)
    public List<Oferta> listarTodas() {
        List<Oferta> lista = new ArrayList<>();
        String sql = "SELECT id_oferta, id_producto, descripcion, descuento, fecha_inicio, fecha_fin, estado FROM oferta";

        // Usamos tu método ConexionDB.conectar()
        try (Connection con = ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Oferta oferta = new Oferta();
                oferta.setIdOferta(rs.getInt("id_oferta"));
                oferta.setIdProducto(rs.getInt("id_producto"));
                oferta.setDescripcion(rs.getString("descripcion"));
                oferta.setDescuento(rs.getDouble("descuento"));

                // Conversión segura de java.sql.Date a java.time.LocalDate
                if (rs.getDate("fecha_inicio") != null) {
                    oferta.setFechaInicio(rs.getDate("fecha_inicio").toLocalDate());
                }
                if (rs.getDate("fecha_fin") != null) {
                    oferta.setFechaFin(rs.getDate("fecha_fin").toLocalDate());
                }

                oferta.setEstado(rs.getBoolean("estado"));

                lista.add(oferta);
            }
        } catch (SQLException e) {
            System.err.println("Error al listar ofertas: " + e.getMessage());
        }
        return lista;
    }

    // 2. Registrar una nueva oferta
    public boolean registrarOferta(Oferta oferta) {
        String sql = "INSERT INTO oferta (id_producto, descripcion, descuento, fecha_inicio, fecha_fin, estado) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, oferta.getIdProducto());
            ps.setString(2, oferta.getDescripcion());
            ps.setDouble(3, oferta.getDescuento());

            // Convertir LocalDate a java.sql.Date para enviarlo a MySQL
            ps.setDate(4, Date.valueOf(oferta.getFechaInicio()));
            ps.setDate(5, Date.valueOf(oferta.getFechaFin()));
            ps.setBoolean(6, oferta.isEstado());

            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            System.err.println("Error al registrar oferta: " + e.getMessage());
            return false;
        }
    }

    // Actualizar una oferta existente (por si el admin quiere cambiar fechas o desactivarla)
    public boolean actualizarOferta(Oferta oferta) {
        String sql = "UPDATE oferta SET id_producto=?, descripcion=?, descuento=?, fecha_inicio=?, fecha_fin=?, estado=? WHERE id_oferta=?";

        try (Connection con = ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, oferta.getIdProducto());
            ps.setString(2, oferta.getDescripcion());
            ps.setDouble(3, oferta.getDescuento());
            ps.setDate(4, Date.valueOf(oferta.getFechaInicio()));
            ps.setDate(5, Date.valueOf(oferta.getFechaFin()));
            ps.setBoolean(6, oferta.isEstado());
            ps.setInt(7, oferta.getIdOferta());

            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar oferta: " + e.getMessage());
            return false;
        }
    }

    // Buscar oferta por descripción o nombre de producto (Para la barra de búsqueda)
    public List<Oferta> buscarOferta(String textoBusqueda) {
        List<Oferta> lista = new ArrayList<>();
        // Hacemos un JOIN rápido con la tabla producto para poder buscar por el nombre del producto también
        String sql = "SELECT o.* FROM oferta o INNER JOIN producto p ON o.id_producto = p.id_producto " +
                "WHERE o.descripcion LIKE ? OR p.nombre LIKE ?";

        try (Connection con = ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            String comodin = "%" + textoBusqueda + "%";
            ps.setString(1, comodin);
            ps.setString(2, comodin);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Oferta oferta = new Oferta();
                    oferta.setIdOferta(rs.getInt("id_oferta"));
                    oferta.setIdProducto(rs.getInt("id_producto"));
                    oferta.setDescripcion(rs.getString("descripcion"));
                    oferta.setDescuento(rs.getDouble("descuento"));

                    if (rs.getDate("fecha_inicio") != null) {
                        oferta.setFechaInicio(rs.getDate("fecha_inicio").toLocalDate());
                    }
                    if (rs.getDate("fecha_fin") != null) {
                        oferta.setFechaFin(rs.getDate("fecha_fin").toLocalDate());
                    }

                    oferta.setEstado(rs.getBoolean("estado"));

                    lista.add(oferta);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar ofertas: " + e.getMessage());
        }
        return lista;
    }
}