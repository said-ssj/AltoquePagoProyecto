package com.dao;

import com.DB.ConexionDB;
import java.sql.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PagoDAO {

    private static final Logger logger = LoggerFactory.getLogger(PagoDAO.class);

    // ============================================================
    // OBTENER LA SUMA TOTAL DE INGRESOS DEL DÍA POR MÉTODO DE PAGO
    // ============================================================
    public double obtenerTotalPorMetodoHoy(String metodoPago) {
        double total = 0.0;
        // Hacemos JOIN con 'venta' para filtrar solo los pagos de las ventas realizadas HOY
        String sql = "SELECT SUM(p.monto) AS total_metodo " +
                "FROM pago p " +
                "INNER JOIN venta v ON p.id_venta = v.id_venta " +
                "WHERE p.metodo = ? AND p.estado = 'PAGADO' AND DATE(v.fecha) = CURDATE()";

        try (Connection cn = ConexionDB.conectar()) {
            if (cn == null) return 0.0;

            try (PreparedStatement ps = cn.prepareStatement(sql)) {
                ps.setString(1, metodoPago);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        total = rs.getDouble("total_metodo");
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error al obtener total por método de pago: {}", metodoPago, e);
        }
        return total;
    }

    // ============================================================
    // OBTENER LA SUMA DE BILLETERAS DIGITALES (YAPE + PLIN) DE HOY
    // ============================================================
    public double obtenerTotalBilleterasDigitalesHoy() {
        double total = 0.0;
        String sql = "SELECT SUM(p.monto) AS total_digital " +
                "FROM pago p " +
                "INNER JOIN venta v ON p.id_venta = v.id_venta " +
                "WHERE p.metodo IN ('YAPE', 'PLIN') AND p.estado = 'PAGADO' AND DATE(v.fecha) = CURDATE()";

        try (Connection cn = ConexionDB.conectar()) {
            if (cn == null) return 0.0;

            try (PreparedStatement ps = cn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    total = rs.getDouble("total_digital");
                }
            }
        } catch (Exception e) {
            logger.error("Error al obtener total de billeteras digitales", e);
        }
        return total;
    }

    // ============================================================
    // OBTENER EL TOTAL GENERAL DE INGRESOS DE HOY
    // ============================================================
    public double obtenerTotalIngresosHoy() {
        double total = 0.0;
        String sql = "SELECT SUM(p.monto) AS total_dia " +
                "FROM pago p " +
                "INNER JOIN venta v ON p.id_venta = v.id_venta " +
                "WHERE p.estado = 'PAGADO' AND DATE(v.fecha) = CURDATE()";

        try (Connection cn = ConexionDB.conectar()) {
            if (cn == null) return 0.0;

            try (PreparedStatement ps = cn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    total = rs.getDouble("total_dia");
                }
            }
        } catch (Exception e) {
            logger.error("Error al obtener el total de ingresos del día", e);
        }
        return total;
    }

    // ============================================================
    // REGISTRAR UN NUEVO PAGO (Usado al concretar una venta)
    // ============================================================
    public boolean registrarPago(int idVenta, String metodo, double monto, String estado) {
        String sql = "INSERT INTO pago (id_venta, metodo, monto, estado) VALUES (?, ?, ?, ?)";

        try (Connection cn = ConexionDB.conectar()) {
            if (cn == null) return false;

            try (PreparedStatement ps = cn.prepareStatement(sql)) {
                ps.setInt(1, idVenta);
                ps.setString(2, metodo);
                ps.setDouble(3, monto);
                ps.setString(4, estado);

                return ps.executeUpdate() > 0;
            }
        } catch (Exception e) {
            logger.error("Error al registrar pago para la venta ID: {}", idVenta, e);
            return false;
        }
    }
}