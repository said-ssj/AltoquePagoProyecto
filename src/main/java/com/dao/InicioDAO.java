package com.dao;

import com.DB.ConexionDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InicioDAO implements IInicioDAO {

    @Override
    public double obtenerVentasDelMes() {
        String sql = """
            SELECT IFNULL(SUM(total), 0) AS total_mes
            FROM venta
            WHERE MONTH(fecha) = MONTH(CURDATE())
              AND YEAR(fecha)  = YEAR(CURDATE())
        """;
        try (Connection cn = ConexionDB.conectar();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getDouble("total_mes");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    @Override
    public int obtenerTotalProductos() {
        String sql = "SELECT COUNT(*) AS total FROM producto";
        try (Connection cn = ConexionDB.conectar();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt("total");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public double obtenerCrecimientoVentas() {
        String sql = """
            SELECT
              (SELECT IFNULL(SUM(total),0) FROM venta
               WHERE MONTH(fecha)=MONTH(CURDATE()) AND YEAR(fecha)=YEAR(CURDATE())) AS actual,
              (SELECT IFNULL(SUM(total),0) FROM venta
               WHERE MONTH(fecha)=MONTH(CURDATE() - INTERVAL 1 MONTH)
                 AND YEAR(fecha)=YEAR(CURDATE() - INTERVAL 1 MONTH)) AS anterior
        """;
        try (Connection cn = ConexionDB.conectar();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                double actual = rs.getDouble("actual");
                double anterior = rs.getDouble("anterior");
                if (anterior > 0) {
                    return ((actual - anterior) / anterior) * 100;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    @Override
    public List<Map<String, Object>> obtenerVentasRecientes(int limite) {
        List<Map<String, Object>> lista = new ArrayList<>();
        String sql = """
            SELECT v.id_venta,
                   CONCAT(c.nombre, ' ', IFNULL(c.apellido,'')) AS nombre_cliente,
                   v.total,
                   DATE_FORMAT(v.fecha, '%d/%m/%Y') AS fecha_fmt
            FROM venta v
            INNER JOIN cliente c ON v.id_cliente = c.id_cliente
            ORDER BY v.fecha DESC, v.id_venta DESC
            LIMIT ?
        """;
        try (Connection cn = ConexionDB.conectar();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, limite);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> venta = new HashMap<>();
                    venta.put("id", rs.getInt("id_venta"));
                    venta.put("nombre", rs.getString("nombre_cliente"));
                    venta.put("monto", rs.getDouble("total"));
                    venta.put("fecha", rs.getString("fecha_fmt"));
                    lista.add(venta);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }

    @Override
    public List<Map<String, Object>> obtenerProductosMasVendidos(int limite) {
        List<Map<String, Object>> lista = new ArrayList<>();
        String sql = """
            SELECT p.nombre,
                   SUM(dv.cantidad) AS vendidos
            FROM detalle_venta dv
            INNER JOIN producto p ON dv.id_producto = p.id_producto
            GROUP BY p.id_producto, p.nombre
            ORDER BY vendidos DESC
            LIMIT ?
        """;
        try (Connection cn = ConexionDB.conectar();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, limite);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> prod = new HashMap<>();
                    prod.put("nombre", rs.getString("nombre"));
                    prod.put("vendidos", rs.getInt("vendidos"));
                    lista.add(prod);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }
}