package com.dao;

import com.DB.ConexionDB;
import com.modelo.ArqueoCaja;

import java.sql.*;

public class ArqueoCajaDAO {

    /** Devuelve el arqueo actualmente ABIERTO (si existe), o null si no hay ninguno abierto. */
    public ArqueoCaja obtenerArqueoAbierto() {
        String sql = "SELECT id_arqueo, fecha_apertura, monto_inicial, estado " +
                "FROM arqueo_caja WHERE estado = 'ABIERTA' ORDER BY id_arqueo DESC LIMIT 1";

        try (Connection con = ConexionDB.conectar()) {
            if (con == null) return null;
            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new ArqueoCaja(
                            rs.getInt("id_arqueo"),
                            rs.getTimestamp("fecha_apertura").toLocalDateTime(),
                            rs.getDouble("monto_inicial"),
                            rs.getString("estado")
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /** Abre un nuevo turno de caja con el monto base indicado. Devuelve el id generado, o -1 si falló. */
    public int abrirArqueo(double montoInicial, Integer idUsuario) {
        String sql = "INSERT INTO arqueo_caja (fecha_apertura, monto_inicial, estado, id_usuario) " +
                "VALUES (NOW(), ?, 'ABIERTA', ?)";

        try (Connection con = ConexionDB.conectar()) {
            if (con == null) return -1;
            try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setDouble(1, montoInicial);
                if (idUsuario != null) {
                    ps.setInt(2, idUsuario);
                } else {
                    ps.setNull(2, Types.INTEGER);
                }
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) return rs.getInt(1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    /** Cierra el turno de caja indicado, guardando los totales del arqueo. */
    public boolean cerrarArqueo(int idArqueo, double efectivoSistema, double efectivoContado, double diferencia,
                                double totalYape, double totalPlin, double totalTarjeta, double totalVentas) {
        String sql = "UPDATE arqueo_caja SET fecha_cierre = NOW(), efectivo_sistema = ?, efectivo_contado = ?, " +
                "diferencia = ?, total_yape = ?, total_plin = ?, total_tarjeta = ?, total_ventas = ?, estado = 'CERRADA' " +
                "WHERE id_arqueo = ?";

        try (Connection con = ConexionDB.conectar()) {
            if (con == null) return false;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setDouble(1, efectivoSistema);
                ps.setDouble(2, efectivoContado);
                ps.setDouble(3, diferencia);
                ps.setDouble(4, totalYape);
                ps.setDouble(5, totalPlin);
                ps.setDouble(6, totalTarjeta);
                ps.setDouble(7, totalVentas);
                ps.setInt(8, idArqueo);
                return ps.executeUpdate() > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
