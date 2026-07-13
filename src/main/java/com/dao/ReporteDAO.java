package com.dao;

import com.DB.ConexionDB;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReporteDAO implements IReporteDAO {

    // Helper para mapear ResultSets automáticamente conservando el orden de las columnas (LinkedHashMap)
    private List<Map<String, Object>> mapearResultSet(ResultSet rs) throws SQLException {
        List<Map<String, Object>> lista = new ArrayList<>();
        ResultSetMetaData meta = rs.getMetaData();
        int cols = meta.getColumnCount();

        while (rs.next()) {
            Map<String, Object> fila = new LinkedHashMap<>();
            for (int i = 1; i <= cols; i++) {
                fila.put(meta.getColumnLabel(i), rs.getObject(i));
            }
            lista.add(fila);
        }
        return lista;
    }

    @Override
    public List<Map<String, Object>> obtenerReporteVentas(LocalDate inicio, LocalDate fin) {
        String sql = "SELECT v.id_venta AS 'ID', DATE(v.fecha) AS 'Fecha', " +
                "CONCAT(c.nombre,' ',IFNULL(c.apellido,'')) AS 'Cliente', " +
                "v.total AS 'Total (S/)', IFNULL(p.estado,'PENDIENTE') AS 'Estado' " +
                "FROM venta v " +
                "INNER JOIN cliente c ON v.id_cliente = c.id_cliente " +
                "LEFT JOIN pago p ON v.id_venta = p.id_venta " +
                "WHERE DATE(v.fecha) BETWEEN ? AND ? " +
                "ORDER BY v.fecha DESC";

        try (Connection cn = ConexionDB.conectar();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(inicio));
            ps.setDate(2, Date.valueOf(fin));
            try (ResultSet rs = ps.executeQuery()) {
                return mapearResultSet(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    @Override
    public List<Map<String, Object>> obtenerReporteInventario(LocalDate inicio, LocalDate fin) {
        String sql = "SELECT p.id_producto AS 'ID', p.codigo_barras AS 'Código', p.nombre AS 'Producto', " +
                "p.precio AS 'Precio (S/)', p.stock AS 'Stock', " +
                "CASE WHEN p.stock = 0 THEN 'SIN STOCK' " +
                "     WHEN p.stock < 5 THEN 'BAJO STOCK' " +
                "     ELSE 'OK' END AS 'Estado' " +
                "FROM producto p " +
                "LEFT JOIN movimiento_inventario mi ON p.id_producto = mi.id_producto " +
                "AND DATE(mi.fecha) BETWEEN ? AND ? " +
                "GROUP BY p.id_producto, p.codigo_barras, p.nombre, p.precio, p.stock " +
                "ORDER BY p.stock ASC";

        try (Connection cn = ConexionDB.conectar();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(inicio));
            ps.setDate(2, Date.valueOf(fin));
            try (ResultSet rs = ps.executeQuery()) {
                return mapearResultSet(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    @Override
    public List<Map<String, Object>> obtenerReporteEmpleados() {
        String sql = "SELECT u.id_usuario AS 'ID', u.nombre AS 'Nombre', u.email AS 'Email', " +
                "r.nombre_rol AS 'Rol', u.area AS 'Área', u.tipo_contrato AS 'Contrato', " +
                "u.telefono AS 'Teléfono' " +
                "FROM usuario_personal u " +
                "LEFT JOIN rol r ON u.id_rol = r.id_rol " +
                "ORDER BY u.area, u.nombre";

        try (Connection cn = ConexionDB.conectar();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return mapearResultSet(rs);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    @Override
    public void guardarRegistroReporte(int idUsuario, String tipoReporte) {
        String sqlInsert = "INSERT INTO reporte (id_usuario, tipo_reporte, fecha) VALUES (?, ?, NOW())";
        try (Connection cn = ConexionDB.conectar();
             PreparedStatement ps = cn.prepareStatement(sqlInsert)) {
            ps.setInt(1, idUsuario);
            ps.setString(2, tipoReporte);
            ps.executeUpdate();
        } catch (Exception e) {
            System.err.println("Aviso: no se pudo registrar el reporte en BD: " + e.getMessage());
        }
    }

    @Override
    public List<Map<String, Object>> obtenerHistorialReportes(int limite) {
        List<Map<String, Object>> lista = new ArrayList<>();
        String sql = "SELECT r.id_reporte, r.tipo_reporte, r.fecha " +
                "FROM reporte r ORDER BY r.fecha DESC LIMIT ?";

        try (Connection cn = ConexionDB.conectar();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, limite);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> reg = new java.util.HashMap<>();
                    reg.put("id_reporte", rs.getInt("id_reporte"));
                    reg.put("tipo_reporte", rs.getString("tipo_reporte"));
                    reg.put("fecha", rs.getTimestamp("fecha").toLocalDateTime());
                    lista.add(reg);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }
}