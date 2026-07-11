package com.dao;

import com.DB.ConexionDB;
import com.modelo.Oferta;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class OfertaDAO {

    // ============================================================
    // MODO AUTOSERVICIO: Listar todas (para la tabla y kiosko)
    // ============================================================
    public List<Oferta> listarTodas() {
        List<Oferta> lista = new ArrayList<>();
        String sql =
                "SELECT o.id_oferta, o.id_producto, p.nombre AS nombre_producto, " +
                        "  o.descripcion, o.descuento, o.fecha_inicio, o.fecha_fin, o.estado " +
                        "FROM oferta o " +
                        "INNER JOIN producto p ON o.id_producto = p.id_producto " +
                        "ORDER BY o.id_oferta DESC";
        try (Connection cn = ConexionDB.conectar();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return lista;
    }

    // ── Buscar por nombre de producto o descripción ───────────────
    public List<Oferta> buscar(String texto) {
        List<Oferta> lista = new ArrayList<>();
        String sql =
                "SELECT o.id_oferta, o.id_producto, p.nombre AS nombre_producto, " +
                        "  o.descripcion, o.descuento, o.fecha_inicio, o.fecha_fin, o.estado " +
                        "FROM oferta o " +
                        "INNER JOIN producto p ON o.id_producto = p.id_producto " +
                        "WHERE p.nombre LIKE ? OR o.descripcion LIKE ? " +
                        "ORDER BY o.id_oferta DESC";
        try (Connection cn = ConexionDB.conectar();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, "%" + texto + "%");
            ps.setString(2, "%" + texto + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) lista.add(mapear(rs));
        } catch (Exception e) { e.printStackTrace(); }
        return lista;
    }

    // ── Buscar activa por producto (para el punto de venta / kiosko) ───
    public Oferta buscarOferta(int idProducto) {
        String sql = "SELECT o.id_oferta, o.id_producto, p.nombre AS nombre_producto, " +
                "  o.descripcion, o.descuento, o.fecha_inicio, o.fecha_fin, o.estado " +
                "FROM oferta o INNER JOIN producto p ON o.id_producto = p.id_producto " +
                "WHERE o.id_producto=? AND o.estado=1";
        try (Connection cn = ConexionDB.conectar();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapear(rs);
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    // ── Insertar ─────────────────────────────────────────────────
    public boolean insertar(Oferta o) {
        String sql = "INSERT INTO oferta (id_producto, descripcion, descuento, fecha_inicio, fecha_fin, estado) VALUES (?,?,?,?,?,?)";
        try (Connection cn = ConexionDB.conectar();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt   (1, o.getId_producto());
            ps.setString(2, o.getDescripcion());
            ps.setDouble(3, o.getDescuento());
            ps.setDate  (4, o.getFechaInicio() != null ? Date.valueOf(o.getFechaInicio()) : null);
            ps.setDate  (5, o.getFechaFin()    != null ? Date.valueOf(o.getFechaFin())    : null);
            ps.setBoolean(6, o.isEstado());
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    // ── Actualizar ───────────────────────────────────────────────
    public boolean actualizar(Oferta o) {
        String sql = "UPDATE oferta SET id_producto=?, descripcion=?, descuento=?, fecha_inicio=?, fecha_fin=?, estado=? WHERE id_oferta=?";
        try (Connection cn = ConexionDB.conectar();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt   (1, o.getId_producto());
            ps.setString(2, o.getDescripcion());
            ps.setDouble(3, o.getDescuento());
            ps.setDate  (4, o.getFechaInicio() != null ? Date.valueOf(o.getFechaInicio()) : null);
            ps.setDate  (5, o.getFechaFin()    != null ? Date.valueOf(o.getFechaFin())    : null);
            ps.setBoolean(6, o.isEstado());
            ps.setInt   (7, o.getId_oferta());
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    // ── Eliminar ─────────────────────────────────────────────────
    public boolean eliminar(int idOferta) {
        String sql = "DELETE FROM oferta WHERE id_oferta=?";
        try (Connection cn = ConexionDB.conectar();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idOferta);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    // ── Método de Mapeo Interno Real ───────────────────────────────────
    private Oferta mapear(ResultSet rs) throws SQLException {
        LocalDate inicio = rs.getDate("fecha_inicio") != null ? rs.getDate("fecha_inicio").toLocalDate() : null;
        LocalDate fin    = rs.getDate("fecha_fin")    != null ? rs.getDate("fecha_fin").toLocalDate()    : null;
        return new Oferta(
                rs.getInt("id_oferta"),
                rs.getInt("id_producto"),
                rs.getString("nombre_producto"),
                rs.getString("descripcion"),
                rs.getDouble("descuento"),
                inicio, fin,
                rs.getBoolean("estado")
        );
    }
}