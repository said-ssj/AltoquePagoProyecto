package com.dao;

import com.DB.ConexionDB;
import com.modelo.MovimientoInventario;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MovimientoInventarioDAO implements IMovimientoInventarioDAO{

    private static final Logger logger = LoggerFactory.getLogger(MovimientoInventarioDAO.class);

    // ============================================================
    // REGISTRAR UN NUEVO MOVIMIENTO (Entrada, Salida, Merma, Ajuste)
    // ============================================================
    public boolean registrarMovimiento(MovimientoInventario mov) {
        // La columna 'fecha' toma el DEFAULT NOW() automáticamente de MySQL
        String sql = "INSERT INTO movimiento_inventario (id_producto, tipo_movimiento, cantidad, descripcion) VALUES (?, ?, ?, ?)";

        try (Connection cn = ConexionDB.conectar()) {
            if (cn == null) {
                logger.error("No se pudo conectar a la base de datos para registrar el movimiento.");
                return false;
            }

            try (PreparedStatement ps = cn.prepareStatement(sql)) {
                ps.setInt(1, mov.getIdProducto());
                ps.setString(2, mov.getTipoMovimiento());
                ps.setInt(3, mov.getCantidad());
                ps.setString(4, mov.getDescripcion());

                int filasAfectadas = ps.executeUpdate();
                return filasAfectadas > 0;
            }
        } catch (Exception e) {
            logger.error("Error al registrar movimiento para el producto ID: {}", mov.getIdProducto(), e);
            return false;
        }
    }

    // ============================================================
    // LISTAR EL KARDEX DE UN PRODUCTO ESPECÍFICO (Tabla Inferior)
    // ============================================================
    public List<MovimientoInventario> listarPorProducto(int idProducto) {
        List<MovimientoInventario> lista = new ArrayList<>();
        // Ordenamos por fecha de forma descendente para ver los movimientos más recientes primero
        String sql = "SELECT id_movimiento, id_producto, tipo_movimiento, cantidad, fecha, descripcion " +
                "FROM movimiento_inventario WHERE id_producto = ? ORDER BY fecha DESC";

        try (Connection cn = ConexionDB.conectar()) {
            if (cn == null) return lista;

            try (PreparedStatement ps = cn.prepareStatement(sql)) {
                ps.setInt(1, idProducto);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        MovimientoInventario mov = new MovimientoInventario();
                        mov.setIdMovimiento(rs.getInt("id_movimiento"));
                        mov.setIdProducto(rs.getInt("id_producto"));
                        mov.setTipoMovimiento(rs.getString("tipo_movimiento"));
                        mov.setCantidad(rs.getInt("cantidad"));

                        // Convertir Timestamp de MySQL a LocalDateTime de Java
                        if (rs.getTimestamp("fecha") != null) {
                            mov.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
                        }

                        mov.setDescripcion(rs.getString("descripcion"));

                        lista.add(mov);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error al listar el Kardex para el producto ID: {}", idProducto, e);
        }
        return lista;
    }
}