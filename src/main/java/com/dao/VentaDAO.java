/*
 * En este archivo implementamos el acceso a datos para las transacciones y listados de ventas.
 * Hemos unificado el archivo colocando las anotaciones @Override correspondientes para cumplir
 * estrictamente con el contrato IVentaDAO. Se han mantenido intactos todos los comentarios de
 * seguridad y optimizaciones de recursos mediante bloques try-with-resources.
 */
package com.dao;

import com.DB.ConexionDB;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.modelo.Venta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DAO para operaciones sobre la tabla venta.
 *
 * SEGURIDAD [SEC-06]: Se corrigieron fugas de recursos de base de datos.
 * Los métodos guardarVenta() y listarVentas() originales no usaban
 * try-with-resources, dejando Connections, PreparedStatements y ResultSets
 * sin cerrar ante cualquier excepción. Ahora todos usan try-with-resources.
 */
public class VentaDAO implements IVentaDAO {
    private static final Logger logger = LoggerFactory.getLogger(VentaDAO.class);

    @Override
    public int guardarVenta(int idCliente, double total) {
        int idVentaGenerado = 0;
        String sql = "INSERT INTO venta(id_cliente, total) VALUES(?, ?)";

        // SEGURIDAD [SEC-06]: try-with-resources garantiza cierre aunque haya excepción
        try (Connection cn = ConexionDB.conectar()) {
            if (cn == null) return 0;
            try (PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, idCliente);
                ps.setDouble(2, total);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) idVentaGenerado = rs.getInt(1);
                }
            }
        } catch (Exception e) {
            logger.error("Error al guardar la venta para idCliente={}", idCliente, e);
        }
        return idVentaGenerado;
    }

    @Override
    public List<Venta> listarVentas() {
        List<Venta> lista = new ArrayList<>();
        String sql =
                "SELECT v.id_venta, v.fecha, v.total, " +
                        "  c.id_cliente, CONCAT(c.nombre, ' ', IFNULL(c.apellido,'')) AS cliente, " +
                        "  COUNT(dv.id_producto) AS productos, " +
                        "  p.estado AS estado, " +
                        "  IFNULL(p.metodo, 'N/A') AS metodo_pago " +
                        "FROM venta v " +
                        "INNER JOIN cliente c ON v.id_cliente = c.id_cliente " +
                        "LEFT JOIN detalle_venta dv ON v.id_venta = dv.id_venta " +
                        "LEFT JOIN pago p ON v.id_venta = p.id_venta " +
                        "GROUP BY v.id_venta, v.fecha, v.total, c.id_cliente, c.nombre, c.apellido, p.estado, p.metodo " +
                        "ORDER BY v.id_venta DESC";

        // SEGURIDAD [SEC-06]: try-with-resources en todos los niveles
        try (Connection cn = ConexionDB.conectar()) {
            if (cn == null) return lista;
            try (PreparedStatement ps = cn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String estado = rs.getString("estado");
                    if (estado == null) estado = "PENDIENTE";
                    lista.add(new Venta(
                            String.valueOf(rs.getInt("id_venta")),
                            rs.getString("fecha"),
                            rs.getString("cliente"),
                            rs.getInt("id_cliente"),
                            rs.getInt("productos"),
                            rs.getDouble("total"),
                            estado,
                            rs.getString("metodo_pago")
                    ));
                }
            }
        } catch (Exception e) {
            logger.error("Error al listar las ventas", e);
        }
        return lista;
    }

    @Override
    public List<String[]> listarClientes() {
        List<String[]> lista = new ArrayList<>();
        String sql = "SELECT id_cliente, CONCAT(nombre,' ',IFNULL(apellido,'')) FROM cliente ORDER BY nombre";

        try (Connection cn = ConexionDB.conectar()) {
            if (cn == null) return lista;
            try (PreparedStatement ps = cn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(new String[]{rs.getString(1), rs.getString(2)});
            }
        } catch (Exception e) {
            logger.error("Error al listar clientes", e);
        }
        return lista;
    }
}