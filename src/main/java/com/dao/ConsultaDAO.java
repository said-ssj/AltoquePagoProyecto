package com.dao;

import com.DB.ConexionDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ConsultaDAO implements IConsultaDAO {

    private List<Map<String, Object>> mapearResultSet(ResultSet rs) throws Exception {
        List<Map<String, Object>> resultados = new ArrayList<>();
        ResultSetMetaData meta = rs.getMetaData();
        int columnas = meta.getColumnCount();

        while (rs.next()) {
            Map<String, Object> fila = new LinkedHashMap<>();
            for (int i = 1; i <= columnas; i++) {
                fila.put(meta.getColumnLabel(i), rs.getObject(i));
            }
            resultados.add(fila);
        }
        return resultados;
    }

    @Override
    public List<String> obtenerDepartamentos() {
        List<String> departamentos = new ArrayList<>();
        String sql = "SELECT DISTINCT area FROM usuario_personal ORDER BY area";
        try (Connection cn = ConexionDB.conectar();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String area = rs.getString("area");
                if (area != null && !area.isBlank()) departamentos.add(area);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return departamentos;
    }

    @Override
    public List<Map<String, Object>> consultarVentas(String fechaInicio, String fechaFin) {
        StringBuilder sql = new StringBuilder(
                "SELECT v.id_venta AS id_venta, DATE(v.fecha) AS fecha, " +
                        "CONCAT(c.nombre, ' ', IFNULL(c.apellido,'')) AS cliente, " +
                        "COUNT(dv.id_producto) AS productos, v.total AS total, " +
                        "IFNULL(p.estado, 'PENDIENTE') AS estado_pago " +
                        "FROM venta v " +
                        "INNER JOIN cliente c ON v.id_cliente = c.id_cliente " +
                        "LEFT JOIN detalle_venta dv ON v.id_venta = dv.id_venta " +
                        "LEFT JOIN pago p ON v.id_venta = p.id_venta "
        );

        List<String> condiciones = new ArrayList<>();
        if (fechaInicio != null) condiciones.add("DATE(v.fecha) >= ?");
        if (fechaFin != null) condiciones.add("DATE(v.fecha) <= ?");

        if (!condiciones.isEmpty()) {
            sql.append("WHERE ").append(String.join(" AND ", condiciones)).append(" ");
        }
        sql.append("GROUP BY v.id_venta, v.fecha, c.nombre, c.apellido, v.total, p.estado ORDER BY v.fecha DESC");

        try (Connection cn = ConexionDB.conectar(); PreparedStatement ps = cn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (fechaInicio != null) ps.setString(idx++, fechaInicio);
            if (fechaFin != null) ps.setString(idx, fechaFin);

            try (ResultSet rs = ps.executeQuery()) {
                return mapearResultSet(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public List<Map<String, Object>> consultarProductos(int stockMinimo, String categoria) {
        String sql = "SELECT id_producto AS id, codigo_barras AS codigo, nombre, precio, stock " +
                "FROM producto WHERE stock >= ? ";
        if (!"Todas las categorías".equals(categoria)) {
            sql += "AND categoria = ? ";
        }
        sql += "ORDER BY nombre ASC";

        try (Connection cn = ConexionDB.conectar(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, stockMinimo);
            if (!"Todas las categorías".equals(categoria)) {
                ps.setString(2, categoria);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return mapearResultSet(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public List<Map<String, Object>> consultarEmpleados(String departamento, String estado) {
        StringBuilder sql = new StringBuilder(
                "SELECT u.id_usuario AS id, u.nombre, u.email, r.nombre_rol AS rol, " +
                        "u.area, u.tipo_contrato, u.telefono, u.fecha_inicio " +
                        "FROM usuario_personal u LEFT JOIN rol r ON u.id_rol = r.id_rol "
        );

        if (!"Todos los departamentos".equals(departamento)) {
            sql.append("WHERE u.area = ? ");
        }
        sql.append("ORDER BY u.nombre ASC");

        try (Connection cn = ConexionDB.conectar(); PreparedStatement ps = cn.prepareStatement(sql.toString())) {
            if (!"Todos los departamentos".equals(departamento)) {
                ps.setString(1, departamento);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return mapearResultSet(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public List<Map<String, Object>> consultarGeneral(String tipo, String parametro) {
        String sql = "";
        try (Connection cn = ConexionDB.conectar()) {
            PreparedStatement ps = null;

            switch (tipo) {
                case "Ventas por cliente" -> {
                    sql = "SELECT CONCAT(c.nombre, ' ', IFNULL(c.apellido,'')) AS cliente, " +
                            "COUNT(v.id_venta) AS total_ventas, SUM(v.total) AS monto_total " +
                            "FROM venta v INNER JOIN cliente c ON v.id_cliente = c.id_cliente " +
                            (parametro.isBlank() ? "" : "WHERE c.nombre LIKE ? OR c.apellido LIKE ? ") +
                            "GROUP BY c.id_cliente, c.nombre, c.apellido ORDER BY monto_total DESC";
                    ps = cn.prepareStatement(sql);
                    if (!parametro.isBlank()) {
                        ps.setString(1, "%" + parametro + "%");
                        ps.setString(2, "%" + parametro + "%");
                    }
                }
                case "Ventas por producto" -> {
                    sql = "SELECT p.nombre AS producto, SUM(dv.cantidad) AS unidades_vendidas, " +
                            "SUM(dv.subtotal) AS ingreso_total FROM detalle_venta dv " +
                            "INNER JOIN producto p ON dv.id_producto = p.id_producto " +
                            (parametro.isBlank() ? "" : "WHERE p.nombre LIKE ? ") +
                            "GROUP BY p.id_producto, p.nombre ORDER BY ingreso_total DESC";
                    ps = cn.prepareStatement(sql);
                    if (!parametro.isBlank()) ps.setString(1, "%" + parametro + "%");
                }
                case "Ventas por empleado" -> {
                    sql = "SELECT u.nombre AS empleado, u.area, COUNT(r.id_reporte) AS reportes_generados " +
                            "FROM usuario_personal u LEFT JOIN reporte r ON u.id_usuario = r.id_usuario " +
                            (parametro.isBlank() ? "" : "WHERE u.nombre LIKE ? ") +
                            "GROUP BY u.id_usuario, u.nombre, u.area ORDER BY reportes_generados DESC";
                    ps = cn.prepareStatement(sql);
                    if (!parametro.isBlank()) ps.setString(1, "%" + parametro + "%");
                }
            }

            if (ps != null) {
                try (ResultSet rs = ps.executeQuery()) {
                    List<Map<String, Object>> resultado = mapearResultSet(rs);
                    ps.close();
                    return resultado;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
}