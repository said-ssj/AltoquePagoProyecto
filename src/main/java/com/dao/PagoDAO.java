package com.dao;
import com.DB.ConexionDB;
import java.sql.*;

public class PagoDAO {

    public void guardarPago(int idVenta, String metodo, double monto, String estado) {
        String sql = "INSERT INTO pago(id_venta, metodo, monto, estado) VALUES (?, ?, ?, ?)";

        try (Connection con = ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idVenta);
            ps.setString(2, metodo);
            ps.setString(3, String.valueOf(monto)); // DECIMAL ok
            ps.setString(4, estado);

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Obtener el total por un método específico (Ej: "Efectivo", "Tarjeta")
    public double obtenerTotalPorMetodoHoy(String metodo) {
        double total = 0.0;
        // Se hace un JOIN con la tabla venta para validar la fecha de hoy
        String sql = "SELECT COALESCE(SUM(p.monto), 0) FROM pago p " +
                "INNER JOIN venta v ON p.id_venta = v.id " +
                "WHERE p.metodo = ? AND DATE(v.fecha) = CURDATE() AND p.estado = 'Pagado'";

        try (Connection con = ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, metodo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    total = rs.getDouble(1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return total;
    }

    // Obtener el total específico de Billeteras Digitales
    public double obtenerTotalBilleterasDigitalesHoy() {
        double total = 0.0;
        String sql = "SELECT COALESCE(SUM(p.monto), 0) FROM pago p " +
                     "INNER JOIN venta v ON p.id_venta = v.id " +
                     "WHERE p.metodo IN ('Yape', 'Plin') AND DATE(v.fecha) = CURDATE() AND p.estado = 'Pagado'";

        try (Connection con = ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                total = rs.getDouble(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return total;
    }

    // 3. Obtener el ingreso total del día sumando todos los métodos
    public double obtenerTotalIngresosHoy() {
        double total = 0.0;
        String sql = "SELECT COALESCE(SUM(p.monto), 0) FROM pago p " +
                     "INNER JOIN venta v ON p.id_venta = v.id " +
                     "WHERE DATE(v.fecha) = CURDATE() AND p.estado = 'Pagado'";

        try (Connection con = ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                total = rs.getDouble(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return total;
    }
}