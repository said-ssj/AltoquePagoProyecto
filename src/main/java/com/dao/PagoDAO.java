package com.dao;
import com.DB.ConexionDB;
import java.sql.*;

public class PagoDAO implements IPagoDAO {

    // Obtener el total por un método específico (Ej: "YAPE", "TARJETA")
    public double obtenerTotalPorMetodoHoy(String metodo) {
        double total = 0.0;
        // OJO: la PK de venta se llama id_venta (antes se usaba "v.id", que no existe y rompía la consulta)
        String sql = "SELECT COALESCE(SUM(p.monto), 0) FROM pago p " +
                "INNER JOIN venta v ON p.id_venta = v.id_venta " +
                "WHERE p.metodo = ? AND DATE(v.fecha) = CURDATE() AND p.estado = 'PAGADO'";

        try (Connection con = ConexionDB.conectar()) {
            if (con == null) return 0.0;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, metodo);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        total = rs.getDouble(1);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return total;
    }

    // Obtener el total específico de Billeteras Digitales (Yape/Plin)
    public double obtenerTotalBilleterasDigitalesHoy() {
        double total = 0.0;
        String sql = "SELECT COALESCE(SUM(p.monto), 0) FROM pago p " +
                "INNER JOIN venta v ON p.id_venta = v.id_venta " +
                "WHERE p.metodo IN ('YAPE', 'PLIN') AND DATE(v.fecha) = CURDATE() AND p.estado = 'PAGADO'";

        try (Connection con = ConexionDB.conectar()) {
            if (con == null) return 0.0;
            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    total = rs.getDouble(1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return total;
    }

    // Obtener el ingreso total del día sumando todos los métodos registrados en pago (Yape/Plin/Tarjeta)
    public double obtenerTotalIngresosHoy() {
        double total = 0.0;
        String sql = "SELECT COALESCE(SUM(p.monto), 0) FROM pago p " +
                "INNER JOIN venta v ON p.id_venta = v.id_venta " +
                "WHERE DATE(v.fecha) = CURDATE() AND p.estado = 'PAGADO'";

        try (Connection con = ConexionDB.conectar()) {
            if (con == null) return 0.0;
            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    total = rs.getDouble(1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return total;
    }

    @Override
    public boolean guardarPago(int idVenta, String metodo, double monto, String estado) {
        String sql = "INSERT INTO pago(id_venta, metodo, monto, estado) VALUES (?, ?, ?, ?)";
        try (Connection con = ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idVenta);
            ps.setString(2, metodo);
            ps.setDouble(3, monto); // Usa setDouble en lugar de String.valueOf(monto) para mayor precisión
            ps.setString(4, estado);

            // Retorna true si se insertó correctamente
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false; // Retorna false si hubo una excepción
        }
    }
}